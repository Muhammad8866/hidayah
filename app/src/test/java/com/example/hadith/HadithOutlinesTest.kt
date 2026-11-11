package com.example.hadith

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HadithOutlinesTest {
    // The bundled bookmarks cover the Arabic sets; the scanned Urdu commentaries have none.
    private val books = HadithCatalog.arabicBooks

    /** Pages in each mirrored PDF, as counted by tools/gen_hadith_outlines.py. */
    private val pdfPages = mapOf(
        "00-bukhari-1" to 967, "00-bukhari-2" to 921, "01-muslim-1" to 694, "01-muslim-2" to 731,
        "02-abu_dawud-1" to 406, "02-abu_dawud-2" to 648, "03-tirmidhi-1" to 88, "03-tirmidhi-2" to 129,
        "03-tirmidhi-3" to 253, "03-tirmidhi-4" to 366, "03-tirmidhi-5" to 400, "04-nasai-1" to 516,
        "04-nasai-2" to 425, "04-nasai-3" to 394, "04-nasai-4" to 423, "04-nasai-5" to 432,
        "04-nasai-6" to 479, "05-ibn_majah-1" to 357, "05-ibn_majah-2" to 394
    )

    @Test fun `every volume ships its bookmarks so the chapter list needs no download`() {
        assertEquals(pdfPages.keys, books.map { it.id }.toSet())
        books.forEach { book ->
            assertTrue(book.id, book.chapters.isNotEmpty())
            assertEquals(book.id, HadithOutlines.chapters(book.id), book.chapters)
        }
        assertEquals(2288, books.sumOf { it.chapters.size })
        assertEquals(710, books.single { it.id == "01-muslim-2" }.chapters.size)
    }

    @Test fun `entries follow the reader's own outline rules`() {
        books.forEach { book ->
            val pages = pdfPages.getValue(book.id)
            book.chapters.forEach { chapter ->
                assertTrue("${book.id} ${chapter.page}", chapter.page in 1..pages)
                val title = chapter.title.trimStart(' ')
                assertTrue(book.id, title.isNotBlank() && title == title.trim())
                assertEquals(book.id, 0, (chapter.title.length - title.length) % 2)
            }
            assertEquals(book.id, book.chapters.size, book.chapters.distinctBy { it.title to it.page }.size)
            // The index screen keeps every entry: all are inside the book and none is a duplicate.
            assertEquals(book.id, book.chapters.size, BookIndex.entries(book.chapters, pages).size)
        }
    }

    @Test fun `spot checks match the PDF bookmarks exactly`() {
        fun first(id: String) = books.single { it.id == id }.chapters.first()
        assertEquals(HadithChapter("۱ – كتاب بدء الوحي.", 6), first("00-bukhari-1"))
        assertEquals(HadithChapter("كتاب الجهاد", 7), first("04-nasai-3"))
        assertEquals(HadithChapter("التشديد في ترك الجهاد", 10), books.single { it.id == "04-nasai-3" }.chapters[1])
        assertTrue(books.single { it.id == "01-muslim-2" }.chapters.any { it.title.startsWith("  ") })
    }

    @Test fun `unknown ids and other editions get no bundled bookmarks`() {
        assertTrue(HadithOutlines.chapters("").isEmpty())
        assertTrue(HadithOutlines.chapters("00-bukhari-3").isEmpty())
        assertTrue(HadithOutlines.chapters("h00-bukhari-1").isEmpty())
    }
}
