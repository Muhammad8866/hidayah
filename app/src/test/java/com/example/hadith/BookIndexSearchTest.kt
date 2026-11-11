package com.example.hadith

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Searching a book's chapter index ("Search chapters" in the PDF reader) with the shared engine. */
class BookIndexSearchTest {
    private val entries = BookIndex.entries(
        listOf(
            HadithChapter("Kitab al-Wudu (Book of Ablution)", 10),
            HadithChapter("  Bab: Wudu with one handful of water", 12),
            HadithChapter("Kitab as-Salah (Book of Prayer)", 40),
            HadithChapter("  Bab: The excellence of Fajr prayer", 45),
            HadithChapter("Kitab az-Zakat", 90),
            HadithChapter("  Bab: Zakat on gold and silver", 95),
            HadithChapter("Book 10: Times", 130),
            HadithChapter("كتاب الصوم", 150)
        ),
        pageCount = 200
    )

    private fun titles(query: String, sort: IndexSort = IndexSort.PAGE_ASC) =
        BookIndex.visible(entries, query, sort).map { it.title }

    @Test fun `the best match comes first in the default order`() {
        assertEquals("Kitab al-Wudu (Book of Ablution)", titles("kitab wudu").first())
        assertEquals("Kitab az-Zakat", titles("zakat").first())
    }

    @Test fun `chapters are found in other spellings, with a typo or half typed`() {
        assertTrue("Kitab al-Wudu (Book of Ablution)" in titles("wuzu"))
        assertTrue("Kitab az-Zakat" in titles("zakaat"))
        assertTrue("Kitab as-Salah (Book of Prayer)" in titles("salat"))
        assertTrue("Bab: The excellence of Fajr prayer" in titles("excelence"))
        assertTrue("Bab: The excellence of Fajr prayer" in titles("excel"))
    }

    @Test fun `every word must match and words may come in any order`() {
        assertEquals(listOf("Bab: Zakat on gold and silver"), titles("silver zakat"))
        assertEquals(emptyList<String>(), titles("zakat zzqqxx"))
    }

    @Test fun `Arabic titles are found with or without the article and vowel marks`() {
        assertEquals(listOf("كتاب الصوم"), titles("الصَّوْم"))
        assertEquals(listOf("كتاب الصوم"), titles("صوم"))
    }

    @Test fun `numbers in a title are found as words`() {
        assertEquals("Book 10: Times", titles("10").first())
        assertEquals("Book 10: Times", titles("book 10").first())
    }

    @Test fun `a chosen sort orders the matches and search shows a flat list`() {
        assertEquals(
            listOf("Bab: Zakat on gold and silver", "Kitab az-Zakat"),
            titles("zakat", IndexSort.PAGE_DESC)
        )
        assertEquals(
            listOf("Bab: Zakat on gold and silver", "Kitab az-Zakat"),
            titles("zakat", IndexSort.TITLE_ASC)
        )
        assertTrue(BookIndex.visible(entries, "zakat", IndexSort.PAGE_ASC).all { it.depth == 0 })
    }

    @Test fun `a blank or unsearchable query shows the whole nested index`() {
        assertEquals(entries, BookIndex.visible(entries, "", IndexSort.PAGE_ASC))
        assertEquals(entries, BookIndex.visible(entries, "   ", IndexSort.PAGE_ASC))
        assertEquals(entries, BookIndex.visible(entries, "🙂 --", IndexSort.PAGE_ASC))
    }

    @Test fun `a pasted page does not break the search`() {
        assertEquals("Kitab az-Zakat", titles("zakat ".repeat(5_000)).first())
        assertEquals(emptyList<String>(), titles("x".repeat(100_000)))
    }

    @Test fun `an empty index stays empty`() {
        assertEquals(emptyList<IndexEntry>(), BookIndex.visible(emptyList(), "wudu", IndexSort.PAGE_ASC))
    }
}
