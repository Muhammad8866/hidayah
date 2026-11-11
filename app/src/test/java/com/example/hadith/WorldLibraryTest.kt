package com.example.hadith

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
class WorldLibraryTest {
    @get:Rule val folder = TemporaryFolder()

    // The Dawat-e-Islami list: saved copy, shipped copy and download are all replaced here.

    private val week = DawateIslamiIndex.MAX_AGE_MS
    private var now = 10 * week
    private lateinit var cacheFile: File

    private val fixture: DiIndex by lazy {
        checkNotNull(javaClass.classLoader?.getResourceAsStream("di_search_fixture.json"))
            .use { DawateIslamiIndex.parseResponse(it, now = 0L) }
    }

    private fun list(updatedAt: Long, books: List<DiBook> = fixture.books) = DiIndex(books, fixture.categories, updatedAt)

    private fun shipped(index: DiIndex): () -> InputStream? {
        val file = File(folder.root, "shipped-${index.updatedAt}.json").also { DawateIslamiIndex.writeCache(it, index) }
        return { file.inputStream() }
    }

    private fun <T : Any> waitFor(value: () -> T?): T = runBlocking {
        withTimeout(5_000) {
            var found = value()
            while (found == null) { delay(10); found = value() }
            checkNotNull(found)
        }
    }

    private fun state() = WorldLibrary.diIndexState.value

    @Before fun freshList() {
        WorldLibrary.resetDiForTest()
        cacheFile = File(folder.root, "di-index.json")
        WorldLibrary.diCachePath = { cacheFile }
        WorldLibrary.diSeed = { null }
        WorldLibrary.diClock = { now }
        WorldLibrary.diFetcher = { throw IOException("No network in tests") }
    }

    @After fun restoreList() = WorldLibrary.resetDiForTest()

    @Test fun `a week-old saved list is shown at once while a new one downloads behind it`() {
        val old = now - week - 1
        DawateIslamiIndex.writeCache(cacheFile, list(updatedAt = old))
        val started = CompletableDeferred<Unit>()
        val gate = CompletableDeferred<DiIndex>()
        WorldLibrary.diFetcher = { started.complete(Unit); gate.await() }

        assertEquals(5, runBlocking { withTimeout(5_000) { WorldLibrary.refreshDiIndex() } }.getOrThrow())
        runBlocking { withTimeout(5_000) { started.await() } }
        assertEquals(DiIndexState.Ready(5, old, refreshing = true), waitFor { state().takeIf { it is DiIndexState.Ready && it.refreshing } })
        // Searching meanwhile uses the old list and does not wait for the download.
        val page = runBlocking {
            withTimeout(5_000) { WorldLibrary.search("", HadithCatalog.ALL_LANGUAGES, 1) }
        }
        assertEquals(5, page.getOrThrow().total)

        gate.complete(list(updatedAt = now, books = fixture.books.drop(1)))
        waitFor { state().takeIf { it == DiIndexState.Ready(4, now) } }
        assertEquals(now, DawateIslamiIndex.readCache(cacheFile)!!.updatedAt)
    }

    @Test fun `with no saved list the copy shipped with the app is shown and a new one is fetched`() {
        WorldLibrary.diSeed = shipped(list(updatedAt = now - 2 * week))
        val started = CompletableDeferred<Unit>()
        val gate = CompletableDeferred<DiIndex>()
        WorldLibrary.diFetcher = { started.complete(Unit); gate.await() }

        assertEquals(5, runBlocking { withTimeout(5_000) { WorldLibrary.refreshDiIndex() } }.getOrThrow())
        runBlocking { withTimeout(5_000) { started.await() } }
        // The shipped copy is never saved as the phone's list; only a downloaded one is.
        assertFalse(cacheFile.exists())
        gate.complete(list(updatedAt = now))
        waitFor { state().takeIf { it == DiIndexState.Ready(5, now) } }
        assertTrue(cacheFile.exists())
    }

    @Test fun `with no list anywhere the first search waits for the download`() {
        val gate = CompletableDeferred<DiIndex>()
        WorldLibrary.diFetcher = { gate.await() }
        runBlocking {
            val pending = async(Dispatchers.IO) { WorldLibrary.refreshDiIndex() }
            withTimeout(5_000) { WorldLibrary.diIndexState.first { it == DiIndexState.Loading } }
            delay(100)
            assertFalse(pending.isCompleted)
            gate.complete(list(updatedAt = now))
            assertEquals(5, withTimeout(5_000) { pending.await() }.getOrThrow())
        }
        assertEquals(DiIndexState.Ready(5, now), state())
    }

    @Test fun `after a failed refresh the slow server is left alone for a while unless a refresh is asked for`() {
        DawateIslamiIndex.writeCache(cacheFile, list(updatedAt = now - week - 1))
        val calls = AtomicInteger()
        WorldLibrary.diFetcher = { calls.incrementAndGet(); throw IOException("Server down") }
        fun settled() = (state() as? DiIndexState.Ready)?.refreshing == false

        assertTrue(runBlocking { WorldLibrary.refreshDiIndex() }.isSuccess)
        waitFor { (calls.get() == 1 && settled()).takeIf { it } }
        now += 60_000
        // The old list is still served, and no second download starts within fifteen minutes.
        assertEquals(5, runBlocking { WorldLibrary.refreshDiIndex() }.getOrThrow())
        Thread.sleep(200)
        assertEquals(1, calls.get())
        // A pull to refresh always asks, and reports that it failed.
        assertTrue(runBlocking { WorldLibrary.refreshDiIndex(force = true) }.isFailure)
        assertEquals(2, calls.get())
        assertEquals(DiIndexState.Ready(5, now - 60_000 - week - 1), state())
        now += 15 * 60_000
        runBlocking { WorldLibrary.refreshDiIndex() }
        waitFor { (calls.get() == 3 && settled()).takeIf { it } }
    }

    @Test fun `a pull to refresh waits for the new list`() {
        DawateIslamiIndex.writeCache(cacheFile, list(updatedAt = now))
        val gate = CompletableDeferred<DiIndex>()
        WorldLibrary.diFetcher = { gate.await() }
        runBlocking {
            // A list younger than a week is used as it is, with no download.
            assertEquals(5, WorldLibrary.refreshDiIndex().getOrThrow())
            assertEquals(DiIndexState.Ready(5, now), state())
            val pending = async(Dispatchers.IO) { WorldLibrary.refreshDiIndex(force = true) }
            withTimeout(5_000) { WorldLibrary.diIndexState.first { it is DiIndexState.Ready && it.refreshing } }
            assertFalse(pending.isCompleted)
            gate.complete(list(updatedAt = now + 1))
            assertEquals(5, withTimeout(5_000) { pending.await() }.getOrThrow())
        }
        assertEquals(DiIndexState.Ready(5, now + 1), state())
    }

    @Test fun `only a downloaded list removes saved books the library no longer has`() {
        WorldLibrary.init(ApplicationProvider.getApplicationContext<Context>())
        val gone = WorldLibrary.diBook(diItem.copy(identifier = "di:999999"))
        WorldLibrary.setSavedForTest(listOf(gone))
        WorldLibrary.diSeed = shipped(list(updatedAt = now - 2 * week))
        val gate = CompletableDeferred<DiIndex>()
        WorldLibrary.diFetcher = { gate.await() }

        runBlocking { WorldLibrary.refreshDiIndex() }.getOrThrow()
        assertEquals(listOf(gone), WorldLibrary.saved.value)
        gate.complete(list(updatedAt = now))
        waitFor { WorldLibrary.saved.value.takeIf { it.isEmpty() } }
    }

    @Test fun `recent searches put the newest first without duplicates and keep only a few`() {
        val recent = listOf("Fatawa Razvia", "Kanzul Iman")
        assertEquals(listOf("kanzul iman", "Fatawa Razvia"), WorldLibrary.withRecent(recent, "  kanzul   iman "))
        assertEquals(recent, WorldLibrary.withRecent(recent, "   "))
        val many = (1..10).fold(emptyList<String>()) { list, n -> WorldLibrary.withRecent(list, "book $n") }
        assertEquals(6, many.size)
        assertEquals("book 10", many.first())
    }

    private val diItem = WorldItem(
        identifier = "di:7734", title = "Bukhari Sharif Ki Pehli Aur Aakhri Hadees", creator = "Ameer-e-Ahl-e-Sunnat",
        languageCode = "sd", nativeTitle = "بخاري شريف جي پهرين ۽ آخري حديث",
        pageCount = 29, remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/si/pdf/2025/3495-1.pdf",
        sourceUrl = "https://www.dawateislami.net/bookslibrary/si/bukhari-sharif-ki-pehli-aur-aakhri-hadees",
        categories = listOf(4, 45)
    )

    @Test fun `a Dawat-e-Islami book is one PDF read straight from the library`() {
        val book = runBlocking { WorldLibrary.volumes(diItem) }.getOrThrow()!!.single()
        assertEquals("di-7734", book.id)
        assertEquals("wl-di:7734", book.collectionId)
        assertEquals("di:7734", book.slug)
        assertEquals(diItem.title, book.romanTitle)
        assertEquals(diItem.nativeTitle, book.nativeTitle)
        assertEquals("sd", book.languageCode)
        assertEquals("سنڌي", book.languageName)
        assertEquals(29, book.pageCount)
        assertEquals(listOf(diItem.remoteUrl), book.downloadUrls)
        assertEquals(diItem.sourceUrl, book.sourcePageUrl)
        assertEquals(1 to 1, book.volume to book.volumeCount)
    }

    @Test fun `saved library books survive a restart, a shelf book included`() {
        val di = runBlocking { WorldLibrary.volumes(diItem) }.getOrThrow()!!.single()
        assertEquals(di, WorldLibrary.savedBook(WorldLibrary.savedEntry(di)))
        assertEquals("dawateislami", WorldLibrary.savedEntry(di).getString("source"))

        // A shelf book is saved and read back as the same library book, with its shelf titles.
        val shelf = IslamicBooksCatalog.book("di-1885")!!
        val read = WorldLibrary.savedBook(WorldLibrary.savedEntry(shelf))
        assertEquals(shelf.copy(publicationYear = read.publicationYear), read)
    }

    @Test fun `saved books of other sites, as earlier versions wrote them, are not read`() {
        val archive = """{"id":"wl-0123456789abcdef","identifier":"AnwarEShariat","collection":"Anwar e Shariat","title":"Anwar e Shariat",
            "language":"ur","volume":1,"volumeCount":1,"url":"https://archive.org/download/AnwarEShariat/a.pdf","volumeTitle":""}"""
        val library = WorldLibrary.savedEntry(runBlocking { WorldLibrary.volumes(diItem) }.getOrThrow()!!.single())
        val books = WorldLibrary.parseSaved(JSONArray().put(JSONObject(archive)).put(library).toString())
        assertEquals(listOf("di-7734"), books.map { it.id })
        assertTrue(books.none { book -> book.downloadUrls.any { "archive.org" in it } })
    }

    @Test fun `the library offers its own languages and every code has a name`() {
        val di = WorldLibrary.languages.map { it.code }
        assertTrue(di.containsAll(listOf("ur", "ar", "en", "hi", "sd", "rd", "si", "ms", "ha")))
        assertEquals(di.size, di.toSet().size)
        assertEquals(DawateIslamiIndex.languages.size, di.size)
        assertEquals("Roman Urdu", WorldLibrary.languageName("rd"))
        assertEquals("Sinhala", WorldLibrary.languageName("si"))
        assertEquals("اردو", WorldLibrary.languageName("ur"))
        assertEquals("سنڌي", WorldLibrary.languageName("sd"))
        assertNull(WorldLibrary.languageName("qq"))
    }

    @Test fun `the library remembers the category`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("world_library", android.content.Context.MODE_PRIVATE).edit().clear().commit()
        WorldLibrary.init(context)
        assertNull(WorldLibrary.preferredCategory())
        WorldLibrary.setPreferredCategory(4)
        assertEquals(4, WorldLibrary.preferredCategory())
        WorldLibrary.setPreferredCategory(null)
        assertNull(WorldLibrary.preferredCategory())
    }

    @Test fun `a much shorter library list never deletes saved books`() {
        assertTrue(WorldLibrary.shouldDropMissingDi(null, 7355))
        assertTrue(WorldLibrary.shouldDropMissingDi(7355, 7355))
        assertTrue(WorldLibrary.shouldDropMissingDi(7355, 6620))
        assertFalse(WorldLibrary.shouldDropMissingDi(7355, 6619))
        assertFalse(WorldLibrary.shouldDropMissingDi(7355, 12))
    }

    @Test fun `a saved entry whose id or language could name another file is skipped`() {
        fun entry(id: String, language: String = "ur") =
            """{"id":"$id","identifier":"x","collection":"c","title":"t","language":"$language","volume":1,"volumeCount":1,"url":"https://data2.dawateislami.net/Data/Books/Download/ur/pdf/2014/1-1.pdf","volumeTitle":"","source":"dawateislami"}"""
        val books = WorldLibrary.parseSaved(
            "[" + listOf(
                entry("di-1885"), entry("di-1551", "ar"), entry("../../shared_prefs/x"), entry("wl-0123456789abcdef"),
                entry("di-1885", "../x"), entry("di-12a"), """{"broken":true}""", """{"source":"dawateislami"}"""
            ).joinToString(",") + "]"
        )
        assertEquals(listOf("di-1885", "di-1551"), books.map { it.id })
    }

    @Test fun `saved books stay searchable offline in any spelling, best match first`() {
        val bahar = IslamicBooksCatalog.book("di-1885")!!
        val salah = IslamicBooksCatalog.book("di-574")!!
        val zevar = IslamicBooksCatalog.book("di-361")!!
        val saved = listOf(salah, zevar, bahar)
        // A query with no searchable word keeps the shelf as it is.
        assertEquals(saved, WorldLibrary.matchSaved(saved, ""))
        assertEquals(saved, WorldLibrary.matchSaved(saved, "  ·  "))
        assertEquals(listOf(bahar), WorldLibrary.matchSaved(saved, "SHARIAT bahar"))
        assertEquals(listOf(bahar), WorldLibrary.matchSaved(saved, "bahare shariyat"))
        assertEquals(listOf(bahar), WorldLibrary.matchSaved(saved, "بہار شریعت"))
        assertEquals(listOf(bahar), WorldLibrary.matchSaved(saved, "jild 1 part a"))
        // नमाज़ in the Hindi title is the same word as namaz.
        assertEquals(listOf(salah), WorldLibrary.matchSaved(saved, "namaz"))
        assertEquals(zevar, WorldLibrary.matchSaved(saved, "zevar").first())
        assertTrue(WorldLibrary.matchSaved(saved, "bahar fatawa").isEmpty())
        assertTrue(WorldLibrary.matchSaved(emptyList(), "bahar").isEmpty())
    }

    @Test fun `a book counts as listed until a list without it has loaded`() {
        assertTrue(WorldLibrary.isListed(1885))
        assertTrue(WorldLibrary.isListed(-1))
        DawateIslamiIndex.writeCache(cacheFile, list(updatedAt = now))
        runBlocking { WorldLibrary.refreshDiIndex() }.getOrThrow()
        assertTrue(WorldLibrary.isListed(7734))
        assertFalse(WorldLibrary.isListed(1885))
        assertEquals(emptyList<String>(), IslamicBooksCatalog.availableWorks().map { it.id })
    }
}
