package com.example.hadith

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BookTitlesAndIndexTest {
    @Test fun `upload names become readable titles without changing their words`() {
        assertEquals("Anware Shariat Kanada", BookTitles.clean("ANWARE SHARIAT KANADA"))
        assertEquals("Bahar e Shariat", BookTitles.clean("bahar-e-shariat_202001"))
        assertEquals("Fatawa Razaviyya Vol 2", BookTitles.clean("Fatawa_Razaviyya_Vol_2.pdf"))
        assertEquals("Anwar e Shariat", BookTitles.clean("AnwarEShariat"))
        assertEquals("Kanzul Iman aur Tafseer", BookTitles.clean("  kanzul   iman aur tafseer "))
        // Written titles, years and other scripts are left exactly as they are.
        assertEquals("Bahar-e-Shariat (Urdu) 2014", BookTitles.clean("Bahar-e-Shariat (Urdu) 2014"))
        assertEquals("بہارِ شریعت", BookTitles.clean("بہارِ شریعت"))
    }

    @Test fun `titles in two scripts get one separator and tidy punctuation`() {
        assertEquals("Jaa Al Haq - جاء الحق", BookTitles.clean("Jaa Al Haq . جاء الحق"))
        assertEquals("Sarkar ki Amad Marhaba - سرکارﷺ کی آمد مرحبا", BookTitles.clean("Sarkar Ki Amad Marhaba,سرکارﷺ کی آمد مرحبا"))
        assertEquals("Rafiq ul Mutamirin - رفیق المعتمرین", BookTitles.clean("rafiq-ul-mutamirin PDF رفیق المعتمرین"))
        assertEquals("Bayanaat Seerat e Mustafaﷺ", BookTitles.clean("Bayanaat Seerat E Mustafaﷺ"))
        assertEquals("قانون شریعت (Qanoon e Shariat)", BookTitles.clean("قانون شریعت ( Qanoon E Shariat )"))
        assertEquals("Sunni Aqaid (हिन्दी)", BookTitles.clean("Sunni Aqaid (हिन्दी)"))
        // Already well written: nothing to change.
        assertEquals("Khulasat Tibyan al-Wudu - خلاصة تبیان الوضوء", BookTitles.clean("Khulasat Tibyan al-Wudu - خلاصة تبیان الوضوء"))
    }

    @Test fun `upload leftovers and shouting are removed`() {
        assertEquals("Quran Kanzul Iman (Urdu translation) with Tafsir", BookTitles.clean("[PDF] Quran Kanzul Iman (Urdu translation) with Tafsir"))
        assertEquals("Fatawa e Razvia 30 Volumes", BookTitles.clean("Fatawa E Razvia 30 Volumes By I-Sunni.blogspot.com"))
        assertEquals("Fazail-e-Shaban (Shab-e-Barat)", BookTitles.clean("FAZAIL-E-SHABAN (SHAB-E-BARAT)"))
        assertEquals("Shariat wa Tariqat (हिन्दी)", BookTitles.clean("SHARIAT Wa TARIQAT (हिन्दी)"))
        assertEquals("Shirk Kya Hai?", BookTitles.clean("SHIRK KYA HAI ?"))
        assertEquals("Madani Qaida", BookTitles.clean("Madani qaida"))
        assertEquals("Juraboon par Masah - جرابوں پر مسح", BookTitles.clean(", Juraboon Par Masah,جرابوں پر مسح"))
    }

    @Test fun `natural order follows numbers of any length and script`() {
        val sorted = listOf("Vol 10", "vol 2", "Vol 01", "Vol ٣").sortedWith(BookTitles.naturalOrder)
        assertEquals(listOf("Vol 01", "vol 2", "Vol ٣", "Vol 10"), sorted)
    }

    private val chapters = listOf(
        HadithChapter("Book 2: Belief", 13),
        HadithChapter("  Chapter on intention", 13),
        HadithChapter("Book 1: Revelation", 7),
        HadithChapter("Book 10: Times", 130),
        HadithChapter("Book 2: Belief", 13),
        HadithChapter("Beyond the last page", 999),
        HadithChapter("   ", 5)
    )

    @Test fun `index is in page order, nested, and free of broken entries`() {
        val entries = BookIndex.entries(chapters, pageCount = 200)
        assertEquals(
            listOf(
                IndexEntry("Book 1: Revelation", 7), IndexEntry("Book 2: Belief", 13),
                IndexEntry("Chapter on intention", 13, depth = 1), IndexEntry("Book 10: Times", 130)
            ),
            entries
        )
        assertEquals(entries, BookIndex.visible(entries, "", IndexSort.PAGE_ASC))
        assertEquals(listOf(130, 13, 13, 7), BookIndex.visible(entries, "", IndexSort.PAGE_DESC).map { it.page })
    }

    @Test fun `title sort counts numbers and search flattens the list`() {
        val entries = BookIndex.entries(chapters, pageCount = 200)
        assertEquals(
            listOf("Book 1: Revelation", "Book 2: Belief", "Book 10: Times", "Chapter on intention"),
            BookIndex.visible(entries, "", IndexSort.TITLE_ASC).map { it.title }
        )
        assertEquals(
            listOf(IndexEntry("Chapter on intention", 13)),
            BookIndex.visible(entries, " INTENTION ", IndexSort.PAGE_ASC)
        )
    }

    @Test fun `current entry is the last heading at or before the page`() {
        val entries = BookIndex.entries(chapters, pageCount = 200)
        assertNull(BookIndex.current(entries, 3))
        assertEquals("Chapter on intention", BookIndex.current(entries, 50)?.title)
        assertEquals("Book 10: Times", BookIndex.current(entries, 200)?.title)
    }
}

