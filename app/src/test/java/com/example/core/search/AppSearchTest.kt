package com.example.core.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** The one search behaviour every list in the app shares (see [AppSearch]). */
class AppSearchTest {
    private data class Surah(val number: Int, val name: String, val arabic: String, val meaning: String)

    private val surahs = listOf(
        Surah(1, "Al-Fatihah", "الفاتحة", "The Opener"),
        Surah(2, "Al-Baqarah", "البقرة", "The Cow"),
        Surah(18, "Al-Kahf", "الكهف", "The Cave"),
        Surah(20, "Ta-Ha", "طه", "Ta-Ha"),
        Surah(22, "Al-Hajj", "الحج", "The Pilgrimage"),
        Surah(36, "Ya-Sin", "يس", "Ya Sin"),
        Surah(55, "Ar-Rahman", "الرحمن", "The Beneficent"),
        Surah(67, "Al-Mulk", "الملك", "The Sovereignty"),
        Surah(112, "Al-Ikhlas", "الإخلاص", "The Sincerity")
    )

    private fun fields(s: Surah) = AppSearch.Fields(title = s.name, nativeTitle = s.arabic, other = listOf(s.meaning), number = s.number)
    private val ranker = AppSearch.Ranker(surahs, ::fields)
    private fun names(query: String) = ranker.search(query).map { it.name }

    // region Normal

    @Test fun `a blank query keeps every item in its own order`() {
        assertEquals(surahs, ranker.search(""))
        assertEquals(surahs, ranker.search("   "))
        assertEquals(surahs, ranker.search("-- , ."))
    }

    @Test fun `an exact name comes first`() {
        assertEquals("Al-Kahf", names("al kahf").first())
        assertEquals("Ya-Sin", names("yasin").first())
        assertEquals("Ar-Rahman", names("rahman").first())
    }

    @Test fun `spelling, case, joining and script variants find the same item`() {
        assertEquals("Al-Baqarah", names("BAQARA").first())
        assertEquals("Al-Baqarah", names("baqra").first())
        assertEquals("Al-Mulk", names("الملك").first())
        assertEquals("Al-Ikhlas", names("ikhlaas").first())
    }

    @Test fun `a meaning or other name finds the item`() {
        assertEquals("Al-Baqarah", names("cow").first())
        assertEquals("Al-Kahf", names("the cave").first())
    }

    @Test fun `a half typed last word already matches`() {
        assertTrue("Al-Mulk" in names("mul"))
        assertEquals("Al-Ikhlas", names("ikhl").first())
    }

    @Test fun `a small typo still matches`() {
        assertEquals("Al-Fatihah", names("fatiha").first())
        assertEquals("Al-Fatihah", names("fatihha").first())
    }

    @Test fun `typo look-alikes are not listed when the word itself matches`() {
        val girls = listOf("Aisha", "Aida", "Asiyah", "Raisah", "Inaya", "Inayat", "Inara", "Bushra")
        fun found(query: String) = AppSearch.rank(query, girls) { AppSearch.Fields(it) }
        assertEquals(listOf("Aisha"), found("ayesha"))
        assertEquals(listOf("Inaya", "Inayat"), found("inaaya"))
    }

    @Test fun `typos are tried when nothing matches without them`() {
        val girls = listOf("Aisha", "Fatima", "Bushra")
        assertEquals(listOf("Fatima"), AppSearch.rank("fatmia", girls) { AppSearch.Fields(it) })
    }

    @Test fun `every word of the query has to match`() {
        assertEquals(emptyList<String>(), names("kahf zzzqqq"))
        assertEquals(emptyList<String>(), names("zzzqqq"))
    }

    // endregion

    // region Numbers

    @Test fun `a number finds that item first, not the ones starting with it`() {
        assertEquals("Al-Baqarah", names("2").first())
        assertEquals("Ta-Ha", names("20").first())
        assertEquals("Al-Ikhlas", names("112").first())
    }

    @Test fun `a number still lists the items whose number starts with it, after the exact one`() {
        val found = names("2")
        assertEquals("Al-Baqarah", found[0])
        assertTrue("Ta-Ha" in found)
        assertTrue("Al-Hajj" in found)
        assertFalse("Al-Kahf" in found)
    }

    @Test fun `a number written in another script finds the same item`() {
        assertEquals("Ya-Sin", names("٣٦").first())
        assertEquals("Ya-Sin", names("३६").first())
    }

    @Test fun `a number that no item has finds nothing`() {
        assertEquals(emptyList<String>(), names("999"))
    }

    // endregion

    // region Long text

    @Test fun `a word deep inside a long text is found`() {
        val longBody = (1..400).joinToString(" ") { "word$it" } + " mercy"
        val items = listOf("a" to longBody, "b" to "nothing to see here")
        val found = AppSearch.rank("mercy", items) { AppSearch.Fields(title = it.first, other = listOf(it.second)) }
        assertEquals(listOf("a" to longBody), found)
    }

    @Test fun `long text is cut into pieces the engine reads whole, losing no word`() {
        val text = (1..500).joinToString(" ") { "w$it" } + " " + "x".repeat(5_000)
        val pieces = AppSearch.chunks(text)
        assertTrue(pieces.all { it.split(' ').size <= 40 && it.length <= 800 })
        assertEquals((1..500).map { "w$it" }, pieces.flatMap { it.split(' ') }.filter { it.startsWith("w") })
        assertEquals(listOf("short text"), AppSearch.chunks("short text"))
        assertEquals(emptyList<String>(), AppSearch.chunks("   "))
    }

    @Test fun `only whole numbers in any digits count as a number`() {
        assertEquals(36, AppSearch.asNumber(" 36 "))
        assertEquals(36, AppSearch.asNumber("٣٦"))
        assertEquals(null, AppSearch.asNumber("36a"))
        assertEquals(null, AppSearch.asNumber("-3"))
        assertEquals(null, AppSearch.asNumber("12345678901"))
        assertEquals(null, AppSearch.asNumber(""))
    }

    @Test fun `title matches beat matches only in the body`() {
        data class Dua(val title: String, val body: String)
        val inBody = Dua("Before sleeping", "O Allah, in Your name I die and I live; protect me on the journey")
        val inTitle = Dua("Dua for journey", "Glory to Him who has subjected this to us")
        val found = AppSearch.rank("journey", listOf(inBody, inTitle)) { AppSearch.Fields(title = it.title, other = listOf(it.body)) }
        assertEquals(listOf(inTitle, inBody), found)
    }

    // endregion

    // region Bad input

    @Test fun `a pasted page or odd characters do not break the search`() {
        val huge = "kahf ".repeat(10_000)
        assertEquals("Al-Kahf", names(huge).first())
        // Nothing searchable was typed, so the list stays as it is.
        assertEquals(surahs, ranker.search("\u0000\uD83D"))
        assertEquals(surahs, ranker.search("💥💥"))
    }

    @Test fun `an empty list answers an empty list`() {
        assertEquals(emptyList<Surah>(), AppSearch.Ranker(emptyList<Surah>(), ::fields).search("kahf"))
    }

    @Test fun `a ranker built once returns the same list for the same query`() {
        val first = ranker.search("al")
        assertSame(first, ranker.search("al"))
        assertEquals(first, ranker.search(" al "))
    }

    // endregion
}
