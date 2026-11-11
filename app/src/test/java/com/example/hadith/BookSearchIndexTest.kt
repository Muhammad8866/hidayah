package com.example.hadith

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.Collections
import java.util.zip.DeflaterOutputStream

/**
 * Searches book_search_fixture.db, a tiny index written by tools/book_search/make_test_fixture.py
 * with the real dictionary: an English Bukhari and Muslim with a few pages and an Arabic Bukhari
 * with two chapters. The English books exist only in this test; the library lists the Arabic one.
 * Page previews come from book_search_fixture_bodies.bin through a fake network.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BookSearchIndexTest {
    private val books = listOf(fixtureBook("e-bukhari", "bukhari"), fixtureBook("e-muslim", "muslim")) +
        HadithCatalog.books.filter { it.id == "00-bukhari-1" }

    private lateinit var context: Context
    private val bodies: ByteArray by lazy { resource("book_search_fixture_bodies.bin").use { it.readBytes() } }
    private val fetches: MutableList<LongRange> = Collections.synchronizedList(mutableListOf())
    private var installs = 0
    private var version = "fixture-1"

    private fun fixtureBook(id: String, collectionId: String) = HadithBook(
        id = id, collectionId = collectionId, collectionName = collectionId, romanTitle = id,
        languageCode = "en", languageName = "English", pageCount = 0, fileId = 0, slug = id,
        volume = 1, volumeCount = 1, mirrored = false
    )

    private fun resource(name: String) = checkNotNull(javaClass.classLoader?.getResourceAsStream(name)) { name }

    private val folder get() = File(context.filesDir, "book_search")

    private val prefs get() = context.getSharedPreferences("book_search", Context.MODE_PRIVATE)

    @Before fun installIndex() {
        context = ApplicationProvider.getApplicationContext()
        // Closes the previous test's database first, so its file can be removed.
        BookSearchIndex.resetForTest(context)
        folder.deleteRecursively()
        prefs.edit().clear().commit()
        BookSearchIndex.bundled = {
            BookSearchIndex.Seed(version) {
                installs++
                resource("book_search_fixture.db")
            }
        }
        BookSearchIndex.fetchRange = { _, from, to ->
            fetches += from..to
            bodies.copyOfRange(from.toInt(), to.toInt() + 1)
        }
        BookSearchIndex.fetchTimeoutMs = 8_000L
        BookSearchIndex.resetForTest(context)
        BookSearchIndex.init(context)
    }

    private fun search(query: String, within: List<HadithBook> = books) =
        runBlocking { BookSearchIndex.search(query, within) }

    private fun excerpts(query: String, outcome: BookSearchIndex.Outcome, within: List<HadithBook> = books) =
        runBlocking { BookSearchIndex.withExcerpts(query, within, outcome) }

    private fun BookSearchIndex.Hit.marked() = highlights.map { snippet.substring(it.first, it.last + 1) }

    private fun deflate(data: ByteArray) =
        ByteArrayOutputStream().also { out -> DeflaterOutputStream(out).use { it.write(data) } }.toByteArray()

    @Test fun `a Roman Urdu question opens the chapter that answers both of its parts`() {
        val outcome = search("safar me namaz kaise padhe")
        assertEquals(listOf("Safar", "Namaz"), outcome.understood)
        val first = outcome.hits.first()
        assertEquals("e-muslim", first.book.id)
        assertEquals(404, first.page)
        assertTrue(first.heading!!.contains("journey"))
        assertTrue(first.highlights.any { first.snippet.substring(it.first, it.last + 1) == "journey" })
    }

    @Test fun `Hindi Gujarati and Bengali spellings reach the same English chapter`() {
        listOf("वुज़ू", "वुजू का तरीका", "વુઝૂ", "wuzu", "wudhu", "ablution").forEach { query ->
            val first = search(query).hits.first()
            assertEquals(query, "e-bukhari", first.book.id)
            assertEquals(query, 47, first.page)
        }
        listOf("रोज़ा", "રોઝા", "রোজা", "roza").forEach { query ->
            assertEquals(query, 431, search(query).hits.first { it.book.languageCode == "en" }.page)
        }
    }

    @Test fun `an Arabic question prefers the Arabic edition and others the English one`() {
        assertEquals("ar", search("كتاب الوضوء").hits.first().book.languageCode)
        assertEquals("ar", search("وضو کا طریقہ").hits.first().book.languageCode)
        assertEquals("en", search("wuzu").hits.first().book.languageCode)
        assertEquals(listOf(44), search("الوضوء", books.filter { it.languageCode == "ar" }).hits.map { it.page })
    }

    @Test fun `a misspelt dictionary word is corrected but a real word of the books is not`() {
        assertEquals(listOf("Namaz"), search("namz").understood)
        // "Raza" is one letter from "roza" (fasting) and must still find the pages that name him.
        val raza = search("raza")
        assertEquals(listOf("raza"), raza.understood)
        assertTrue(raza.hits.isNotEmpty() && raza.hits.all { it.book.id == "e-muslim" })
    }

    @Test fun `searching works without any network and leaves page previews empty`() {
        val hit = search("abu hurairah honey").hits.first()
        assertEquals(300, hit.page)
        assertNull(hit.heading)
        assertEquals("", hit.snippet)
        assertTrue(hit.highlights.isEmpty())
        assertTrue(fetches.isEmpty())
        assertEquals(1, installs)
    }

    @Test fun `a page result shows the matching words in context once its preview arrives`() {
        val found = search("abu hurairah honey")
        val hit = excerpts("abu hurairah honey", found).hits.first()
        assertEquals(300, hit.page)
        assertNull(hit.heading)
        assertTrue(hit.marked().toString(), hit.marked().containsAll(listOf("Abu Hurairah", "honey")))
        // The filled answer is what a reader returning to the list sees.
        assertEquals(hit, BookSearchIndex.remembered("abu hurairah honey", books)?.hits?.first())
    }

    @Test fun `nearby pages arrive in one request and are not fetched again`() {
        val found = search("raza")
        assertTrue(found.hits.size >= 2)
        val filled = excerpts("raza", found)
        assertEquals(1, fetches.size)
        assertTrue(filled.hits.all { hit -> hit.marked().any { it.equals("raza", ignoreCase = true) } })

        excerpts("raza", found)
        assertEquals(1, fetches.size)
        // Another search reaching the same pages shows their previews straight away, offline too.
        BookSearchIndex.fetchRange = { _, _, _ -> throw IOException("offline") }
        val again = search("raza", books.filter { it.id == "e-muslim" })
        assertTrue(again.hits.all { it.snippet.isNotEmpty() })
    }

    @Test fun `offline the hits stay without previews and nothing fails`() {
        BookSearchIndex.fetchRange = { _, _, _ -> throw IOException("offline") }
        val found = search("raza")
        val after = excerpts("raza", found)
        assertEquals(found.hits.map { it.book.id to it.page }, after.hits.map { it.book.id to it.page })
        assertTrue(after.hits.all { it.snippet.isEmpty() && it.highlights.isEmpty() })
    }

    @Test fun `a damaged preview file is skipped`() {
        BookSearchIndex.fetchRange = { _, from, to -> ByteArray((to - from + 1).toInt()) { 0x55 } }
        assertTrue(excerpts("raza", search("raza")).hits.all { it.snippet.isEmpty() })
        BookSearchIndex.fetchRange = { _, _, _ -> ByteArray(3) }
        assertTrue(excerpts("raza", search("raza")).hits.all { it.snippet.isEmpty() })
    }

    @Test fun `a slow network gives up and keeps the hits`() {
        BookSearchIndex.fetchTimeoutMs = 200L
        BookSearchIndex.fetchRange = { _, _, _ -> kotlinx.coroutines.delay(30_000); ByteArray(0) }
        val started = System.nanoTime()
        val after = excerpts("raza", search("raza"))
        assertTrue((System.nanoTime() - started) / 1_000_000 < 5_000)
        assertTrue(after.hits.isNotEmpty() && after.hits.all { it.snippet.isEmpty() })
    }

    @Test fun `an outcome without missing previews is returned as it is`() {
        val chapters = search("الوضوء", books.filter { it.languageCode == "ar" })
        assertTrue(chapters.hits.all { it.heading != null })
        assertSame(chapters, excerpts("الوضوء", chapters, books.filter { it.languageCode == "ar" }))
        assertTrue(fetches.isEmpty())
    }

    @Test fun `a page is inflated only when whole and within the size cap`() {
        val text = "Narrated Abu Hurairah that honey was mentioned as a cure."
        val packed = deflate(text.encodeToByteArray())
        assertEquals(text, BookSearchIndex.inflatePage(packed, 0, packed.size))
        val padded = ByteArray(5) + packed + ByteArray(7)
        assertEquals(text, BookSearchIndex.inflatePage(padded, 5, packed.size))
        assertNull(BookSearchIndex.inflatePage(packed, 0, packed.size - 3))
        assertNull(BookSearchIndex.inflatePage(packed, 4, packed.size))
        assertNull(BookSearchIndex.inflatePage(ByteArray(64) { 0x55 }, 0, 64))
        val bomb = deflate(ByteArray(BookSearchIndex.MAX_PAGE_BYTES + 1024) { 'a'.code.toByte() })
        assertTrue(bomb.size < 4096)
        assertNull(BookSearchIndex.inflatePage(bomb, 0, bomb.size))
    }

    @Test fun `ranges close together share a request and distant ones do not`() {
        val gap = BookSearchIndex.RANGE_GAP
        val spans = listOf(
            BookSearchIndex.Span(1, 0, 100),
            BookSearchIndex.Span(2, 100, 50),
            BookSearchIndex.Span(3, 150 + gap, 10),
            BookSearchIndex.Span(4, 150 + gap + 10 + gap + 1, 20)
        )
        val groups = BookSearchIndex.coalesce(spans.shuffled())
        assertEquals(listOf(listOf(1, 2, 3), listOf(4)), groups.map { group -> group.spans.map { it.doc } })
        assertEquals(0L, groups[0].from)
        assertEquals(150 + gap + 10 - 1, groups[0].to)
        assertEquals(groups[1].spans[0].at, groups[1].from)
        assertEquals(groups[1].spans[0].at + 19, groups[1].to)

        // Back-to-back pages still split once a request would pass the group size.
        val page = 200 * 1024
        val run = (0 until 12).map { BookSearchIndex.Span(it, it.toLong() * page, page) }
        val split = BookSearchIndex.coalesce(run)
        assertTrue(split.size > 1)
        assertTrue(split.all { it.to - it.from + 1 <= BookSearchIndex.RANGE_GROUP })
        assertEquals(run.map { it.doc }, split.flatMap { group -> group.spans.map { it.doc } })
        assertTrue(BookSearchIndex.coalesce(emptyList()).isEmpty())
    }

    @Test fun `the old downloaded index is removed`() {
        folder.mkdirs()
        val old = listOf("index.db", "index.db.gz", "index.db.part").map { File(folder, it).apply { writeText("old") } }
        prefs.edit().putString("version", "1700000000").putLong("checked", 5L).commit()
        assertEquals(47, search("wuzu").hits.first().page)
        assertTrue(old.none { it.exists() })
        assertFalse(prefs.contains("version"))
        assertFalse(prefs.contains("checked"))
        assertTrue(File(folder, "core.db").isFile)
    }

    @Test fun `a new bundled version replaces the copy and the same version is reused`() {
        search("wuzu")
        assertEquals(1, installs)
        BookSearchIndex.resetForTest(context)
        assertEquals(47, search("wuzu").hits.first().page)
        assertEquals(1, installs)
        version = "fixture-2"
        BookSearchIndex.resetForTest(context)
        assertEquals(47, search("wuzu").hits.first().page)
        assertEquals(2, installs)
    }

    @Test fun `a damaged copy is replaced by the bundled index`() {
        search("wuzu")
        BookSearchIndex.resetForTest(context)
        File(folder, "core.db").writeText("not a database")
        assertEquals(47, search("wuzu").hits.first().page)
        assertEquals(2, installs)
    }

    @Test fun `an index that cannot be stored is not copied again on every keystroke`() {
        BookSearchIndex.bundled = {
            BookSearchIndex.Seed(version) {
                installs++
                throw IOException("No space left on device")
            }
        }
        BookSearchIndex.resetForTest(context)
        assertTrue(search("wuzu").hits.isEmpty())
        assertEquals(2, installs)
        assertTrue(search("namaz").hits.isEmpty())
        assertEquals(2, installs)
        assertFalse(File(folder, "core.db").exists())
    }

    @Test fun `filler words alone and books outside the index find nothing`() {
        assertTrue(search("kya hai").hits.isEmpty())
        assertTrue(search("namaz", books.filter { it.languageCode == "hi" }).hits.isEmpty())
    }
}
