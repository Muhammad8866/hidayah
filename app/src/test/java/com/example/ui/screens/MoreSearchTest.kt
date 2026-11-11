package com.example.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The More tab's search box finds a feature the way a search engine would. */
class MoreSearchTest {
    private val qibla = MoreSearchEntry("more_qibla", "Qibla Direction", "Direction of the Kaaba", "क़िबला दिशा", "काबा की दिशा")
    private val calendar = MoreSearchEntry("more_calendar", "Islamic Calendar", "Hijri dates & key days", "Islamic Calendar", null)
    private val hadith = MoreSearchEntry("more_hadith", "Hadith", "Sayings of the Prophet ﷺ", "Hadith", "Sayings of the Prophet ﷺ")
    private val allahNames = MoreSearchEntry("more_allah_names", "99 Names of Allah", "Asma ul Husna with meanings", "99 Names of Allah", null)
    private val books = MoreSearchEntry("more_books", "Islamic Books", "Books in every language", "Islamic Books", null)
    private val babyNames = MoreSearchEntry("more_baby_names", "Islamic Baby Names", "Baby names with meanings", "Islamic Baby Names", null)
    private val zakat = MoreSearchEntry("more_zakat", "Zakat calculator", "Estimate your 2.5% Zakat", "ज़कात कैलकुलेटर", null)
    private val search = moreSearch(listOf(qibla, calendar, hadith, allahNames, books, babyNames, zakat))
    private fun tags(query: String) = search.search(query).map { it.tag }

    @Test fun `a feature's name finds it first, in any case`() {
        assertEquals("more_qibla", tags("qibla").first())
        assertEquals("more_qibla", tags("QIBLA").first())
        assertEquals("more_books", tags("books").first())
        assertEquals("more_hadith", tags("hadith").first())
    }

    @Test fun `a word of the subtitle finds the feature`() {
        assertEquals(listOf("more_calendar"), tags("hijri"))
        assertEquals("more_qibla", tags("kaaba").first())
    }

    @Test fun `another spelling finds the same feature`() {
        assertEquals("more_qibla", tags("kibla").first())
        assertEquals("more_hadith", tags("hadis").first())
        assertEquals("more_zakat", tags("zakaat").first())
    }

    @Test fun `the feature's translated name finds it, and so does its roman spelling`() {
        assertEquals("more_qibla", tags("किबला").first())
        assertEquals("more_zakat", tags("ज़कात").first())
    }

    @Test fun `every word must match, so a narrower query lists fewer features`() {
        val islamic = tags("islamic")
        assertTrue(islamic.containsAll(listOf("more_calendar", "more_books", "more_baby_names")))
        assertFalse("more_hadith" in islamic)
        assertEquals("more_baby_names", tags("islamic names").first())
        assertFalse("more_allah_names" in tags("baby names"))
    }

    @Test fun `a half typed word already matches`() {
        assertEquals("more_calendar", tags("calen").first())
    }

    @Test fun `nothing related finds nothing`() {
        assertEquals(emptyList<String>(), tags("zzzz qqqq"))
    }
}
