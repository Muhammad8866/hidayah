package com.example.hadith

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * The curated shelves (Bahar-e-Shariat and Ladies / Muslimah) hold only books of the library list
 * shipped with the app, each with the very identity the full library gives that book, so a book
 * downloaded, read or bookmarked from a shelf is the same book in the full list.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class IslamicBooksCatalogTest {
    private val seed: DiIndex by lazy {
        val file = generateSequence(File("").absoluteFile) { it.parentFile }
            .map { File(it, "app/src/main/assets/world_library/di-index.json") }.first { it.isFile }
        checkNotNull(DawateIslamiIndex.readCache(file))
    }

    private val books = IslamicBooksCatalog.books

    @Test fun `every curated book is a row of the shipped library list with that row's addresses`() {
        books.forEach { book ->
            val number = book.id.removePrefix("di-").toLong()
            assertNotNull(book.id, seed.byId[number])
            val row = seed.byId.getValue(number)
            assertEquals(book.id, "di-${row.id}", book.id)
            assertEquals(book.id, "wl-di:${row.id}", book.collectionId)
            assertEquals(book.id, "di:${row.id}", book.slug)
            assertEquals(book.id, DawateIslamiIndex.appLanguage(row.languageCode), book.languageCode)
            assertEquals(book.id, row.fileId.toInt(), book.fileId)
            assertEquals(book.id, row.pages, book.pageCount)
            assertEquals(book.id, DawateIslamiIndex.downloadUrl(row), book.remoteUrl)
            assertEquals(book.id, DawateIslamiIndex.pageUrl(row), book.sourcePageUrl)
        }
    }

    @Test fun `a curated book has the identity the full library gives the same book`() {
        books.forEach { book ->
            val full = WorldLibrary.diBook(DawateIslamiIndex.toItem(seed.byId.getValue(book.id.removePrefix("di-").toLong())))
            assertEquals(full.id, book.id)
            assertEquals(full.collectionId, book.collectionId)
            assertEquals(full.slug, book.slug)
            assertEquals(full.languageCode, book.languageCode)
            assertEquals(full.languageName, book.languageName)
            assertEquals(full.downloadUrls, book.downloadUrls)
            assertEquals(full.sourcePageUrl, book.sourcePageUrl)
            assertEquals(full.pageCount, book.pageCount)
        }
    }

    @Test fun `every book is read only from the library itself`() {
        assertEquals(books.size, books.map { it.id }.toSet().size)
        assertEquals(books.size, books.map { it.downloadUrl }.toSet().size)
        books.forEach { book ->
            assertFalse(book.id, book.mirrored)
            assertEquals(book.id, 1, book.downloadUrls.size)
            assertTrue(book.downloadUrl, book.downloadUrl.startsWith("https://data2.dawateislami.net/Data/Books/Download/"))
            assertTrue(book.downloadUrl, book.downloadUrl.endsWith("-1.pdf") && ' ' !in book.downloadUrl)
            assertTrue(book.sourcePageUrl, book.sourcePageUrl.startsWith("https://www.dawateislami.net/bookslibrary/"))
            listOf("archive.org", "github.com", "drive.google").forEach { host ->
                assertFalse(book.id, host in book.downloadUrl || host in book.sourcePageUrl)
            }
        }
    }

    @Test fun `books with no copy in the library are gone`() {
        val ids = IslamicBooksCatalog.works.map { it.id }.toSet()
        listOf("anwar_e_shariat", "shawahid_un_nubuwwat", "maarij_un_nubuwwat").forEach { assertFalse(it, it in ids) }
        assertTrue(books.none { it.id.startsWith("bk-") })
        val bahar = checkNotNull(IslamicBooksCatalog.work(IslamicBooksCatalog.BAHAR))
        assertEquals(listOf("ur", "hi"), bahar.languages)
        assertEquals((1885L..1890L).map { "di-$it" }, bahar.editions("ur").map { it.id })
        assertEquals(listOf("di-8807", "di-8796"), bahar.editions("hi").map { it.id })
        assertEquals((1..6).toList(), bahar.editions("ur").map { it.volume })
        val salah = checkNotNull(IslamicBooksCatalog.work("muslimah_islami_behno_ki_namaz"))
        assertEquals(setOf("ur", "hi", "sd", "en", "bn", "gu", "te", "pt"), salah.languages.toSet())
        assertEquals(8, salah.books.size)
        assertTrue(salah.editions("sd").single().downloadUrl.contains("/si/pdf/"))
    }

    @Test fun `works keep their shelves, authors and order`() {
        val works = IslamicBooksCatalog.works
        assertEquals(IslamicBooksCatalog.BAHAR, works.first().id)
        assertEquals(IslamicBooksCatalog.Shelf.CLASSICAL, works.first().shelf)
        assertTrue(works.drop(1).all { it.shelf == IslamicBooksCatalog.Shelf.MUSLIMAH })
        assertEquals(11, works.size)
        assertEquals("Sadr ash-Shariah Mufti Amjad Ali Azmi · d. 1367 AH", works.first().author)
        works.forEach { work ->
            assertTrue(work.id, work.books.isNotEmpty())
            assertTrue(work.id, work.books.all { it.collectionName == work.name })
            assertEquals(work.id, IslamicBooksCatalog.work(work.id)?.id)
        }
        books.forEach { assertEquals(it, IslamicBooksCatalog.book(it.id)) }
        assertNull(IslamicBooksCatalog.book("bk-bahar-ur-1"))
        assertNull(IslamicBooksCatalog.work(null))
    }

    @Test fun `a book the library no longer lists leaves its shelf, and an empty work goes too`() {
        val gone = setOf(1885L, 8066L)
        val shown = IslamicBooksCatalog.availableWorks { it !in gone }
        val bahar = shown.single { it.id == IslamicBooksCatalog.BAHAR }
        assertFalse(bahar.books.any { it.id == "di-1885" })
        assertEquals(7, bahar.books.size)
        assertFalse(shown.any { it.id == "muslimah_jannati_khawateen" })
        assertEquals(IslamicBooksCatalog.works.size - 1, shown.size)
        // With no list loaded yet every curated book is offered.
        assertEquals(IslamicBooksCatalog.works.map { it.id }, IslamicBooksCatalog.availableWorks { true }.map { it.id })
    }

    @Test fun `books already in the page search index keep their indexed names`() {
        assertEquals("ur-bahar-e-shariat-v1", IslamicBooksCatalog.legacyAsset("di-1885"))
        assertEquals("ur-bahar-e-shariat-v6", IslamicBooksCatalog.legacyAsset("di-1890"))
        assertEquals("sd-muslimah-islami-behno-ki-namaz-v1", IslamicBooksCatalog.legacyAsset("di-1020"))
        // A different printing than the one indexed before is not mapped to it.
        assertNull(IslamicBooksCatalog.legacyAsset("di-8807"))
        assertNull(IslamicBooksCatalog.legacyAsset("di-999999"))
    }

    @Test fun `old shelf ids move to the library's own ids`() {
        val moves = IslamicBooksCatalog.legacyMoves
        assertEquals("di-1885", moves.getValue("bk-bahar-ur-1").id)
        assertEquals("di-1890", moves.getValue("bk-bahar-ur-6").id)
        assertEquals("di-1020", moves.getValue("bk-muslimah-islami-behno-ki-namaz-sd").id)
        assertEquals("di-549", moves.getValue("bk-muslimah-saas-bahu-main-sulha-ka-raaz-sd").id)
        assertEquals(26, moves.size)
        assertEquals(moves.size, moves.values.map { it.id }.toSet().size)
        // Every removed edition is listed with the language its file was saved under.
        val removed = IslamicBooksCatalog.removedEditions.toMap()
        assertEquals("kn", removed["bk-anwar-kn"])
        assertEquals("hi", removed["bk-muslimah-islami-behno-ki-namaz-hi-2011"])
        assertEquals("en", removed["bk-bahar-en-16"])
        assertTrue(removed.keys.none { it in moves })
        assertEquals(18, removed.size)
    }
}
