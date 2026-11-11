package com.example.hadith

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OutlineFilterTest {
    /** As in the scanned Sharh Tirmidhi volume 1: every page bookmarked with its image name. */
    private val pageImages = listOf(HadithChapter("شرح ترمذی از مفتی ہاشم صاحب (2)_Page_0001", 1)) +
        (2..1043).map { HadithChapter("شرح ترمذی از مفتی ہاشم صاحب (2)_Page_%04d".format(it), it + 1) }

    /** As in the scanned Sharh Sahih Muslim volume 4: a title entry, then one per page. */
    private val scannerPages = listOf(HadithChapter("title", 1)) +
        (1..910).map { HadithChapter("SSM4_%04dNafseislam".format(it), it + 1) }

    @Test fun `an outline that only names each scanned page is dropped`() {
        assertTrue(OutlineFilter.isPageLabels(pageImages))
        assertTrue(OutlineFilter.isPageLabels(scannerPages))
        assertEquals(emptyList<HadithChapter>(), OutlineFilter.chapterIndex(pageImages))
        assertEquals(emptyList<HadithChapter>(), OutlineFilter.chapterIndex(scannerPages))
    }

    @Test fun `page labels in any digits are recognised`() {
        val eastern = (1..60).map { HadithChapter("صفحہ " + it.toString().map { d -> '۰' + (d - '0') }.joinToString(""), it) }
        assertTrue(OutlineFilter.isPageLabels(eastern))
    }

    @Test fun `every bundled Kutub al-Sittah outline is kept as it is`() {
        HadithCatalog.arabicBooks.forEach { book ->
            assertFalse(book.id, OutlineFilter.isPageLabels(book.chapters))
            assertEquals(book.id, book.chapters, OutlineFilter.chapterIndex(book.chapters))
        }
    }

    @Test fun `real chapter lists are kept even when long or numbered`() {
        // One chapter per page, each with its own name.
        val named = (1..300).map { HadithChapter("باب ${"ابجدهوزحطی"[it % 10]}${it * 7919 % 1000} في ${"صلاة زكاة صوم حج".split(' ')[it % 4]}", it) }
        assertFalse(OutlineFilter.isPageLabels(named))
        // Numbered hadith bookmarks, several to a page, are a real index.
        val numbered = (1..400).map { HadithChapter("حدیث $it", 1 + it / 3) }
        assertFalse(OutlineFilter.isPageLabels(numbered))
        // A short outline is never treated as page labels.
        val short = (1..19).map { HadithChapter("Page $it", it) }
        assertFalse(OutlineFilter.isPageLabels(short))
        assertEquals(short, OutlineFilter.chapterIndex(short))
    }

    @Test fun `empty and odd input is safe`() {
        assertFalse(OutlineFilter.isPageLabels(emptyList()))
        assertEquals(emptyList<HadithChapter>(), OutlineFilter.chapterIndex(emptyList()))
        val blankTitles = (1..40).map { HadithChapter("", it) }
        assertTrue(OutlineFilter.isPageLabels(blankTitles))
    }
}
