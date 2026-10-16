package com.example.hadith

import org.junit.Assert.*
import org.junit.Test

class MuslimahBooksCatalogTest {
    @Test fun publisherEditionsKeepDistinctIdsAndOriginalSources() {
        val books = MuslimahBooksCatalog.books
        assertEquals(books.size, books.map { it.id }.toSet().size)
        assertEquals(books.size, books.map { it.downloadUrl }.toSet().size)
        books.forEach { book ->
            assertTrue(book.sourceUrl!!.startsWith("https://www.dawateislami.net/bookslibrary/"))
            assertTrue(book.downloadUrl.startsWith("https://data2.dawateislami.net/Data/Books/Download/"))
            assertTrue(book.downloadUrl.endsWith(".pdf"))
            assertFalse("No mirror has been published for this edition", book.mirrored)
            assertTrue(IslamicBooksCatalog.books.contains(book))
        }
    }

    @Test fun salahIncludesEveryPublisherLanguageWithSindhiNormalizedForAppFilters() {
        val salah = MuslimahBooksCatalog.books.filter { it.collectionId == "muslimah_islami_behno_ki_namaz" }
        assertEquals(setOf("ur", "hi", "sd", "en", "bn", "gu", "te", "pt"), salah.map { it.languageCode }.toSet())
        val sindhi = salah.single { it.languageCode == "sd" }
        assertEquals("Sindhi", sindhi.languageName)
        assertTrue(sindhi.downloadUrl.contains("/si/pdf/"))
        assertEquals(8, salah.size)
    }
}
