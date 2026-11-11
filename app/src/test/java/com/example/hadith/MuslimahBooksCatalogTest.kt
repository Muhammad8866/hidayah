package com.example.hadith

import org.junit.Assert.*
import org.junit.Test

class MuslimahBooksCatalogTest {
    @Test fun publisherEditionsKeepDistinctIdsAndLibrarySources() {
        val books = MuslimahBooksCatalog.books
        assertEquals(books.size, books.map { it.id }.toSet().size)
        assertEquals(books.size, books.map { it.downloadUrl }.toSet().size)
        books.forEach { book ->
            assertTrue(book.id, book.id.matches(Regex("di-[0-9]+")))
            assertEquals("wl-" + book.id.replace('-', ':'), book.collectionId)
            assertTrue(book.sourceUrl!!.startsWith("https://www.dawateislami.net/bookslibrary/"))
            assertTrue(book.downloadUrl.startsWith("https://data2.dawateislami.net/Data/Books/Download/"))
            assertTrue(book.downloadUrl.endsWith(".pdf"))
            assertEquals(listOf(book.downloadUrl), book.downloadUrls)
            assertFalse("Read only from the library itself", book.mirrored)
            assertTrue(IslamicBooksCatalog.books.contains(book))
        }
    }

    @Test fun salahIncludesEveryLibraryLanguageWithSindhiNormalizedForAppFilters() {
        val salah = checkNotNull(IslamicBooksCatalog.work("muslimah_islami_behno_ki_namaz")).books
        assertEquals(setOf("ur", "hi", "sd", "en", "bn", "gu", "te", "pt"), salah.map { it.languageCode }.toSet())
        val sindhi = salah.single { it.languageCode == "sd" }
        assertEquals("di-1020", sindhi.id)
        assertTrue(sindhi.downloadUrl.contains("/si/pdf/"))
        // The 2011 Hindi print that was served from another copy is gone; the library's Hindi edition stays.
        assertEquals(8, salah.size)
        assertEquals(listOf("di-574"), salah.filter { it.languageCode == "hi" }.map { it.id })
        assertTrue(MuslimahBooksCatalog.books.none { "github" in it.downloadUrl })
    }
}
