package com.example.quran

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranPdfCatalogTest {

    @Test
    fun `every quran has an ordered surah and para index inside its pages`() {
        assertEquals(13, QuranPdfCatalog.all.size)
        assertEquals(QuranPdfCatalog.all.size, QuranPdfCatalog.all.map { it.id }.toSet().size)
        assertEquals(emptyList<String>(), QuranPdfCatalog.all.filter { it.bundled }.map { it.id })
        assertEquals("quran_hindi_kanzul_iman.pdf", QuranPdfCatalog.byId("kanzul_iman")?.fileName)
        QuranPdfCatalog.all.forEach { book ->
            assertEquals(book.id, 114, book.surahPages.size)
            assertEquals(book.id, 30, book.paraPages.size)
            assertEquals(book.id, book.surahPages.sorted(), book.surahPages.toList())
            assertEquals(book.id, book.paraPages.sorted(), book.paraPages.toList())
            assertTrue(book.id, (book.surahPages + book.paraPages).all { it in 1..book.pageCount })
            // Para 1 opens with Al-Fatiha.
            assertEquals(book.id, book.surahPages[0], book.paraPages[0])
            assertEquals(book.id, 64, book.sha256.length)
        }
    }
}
