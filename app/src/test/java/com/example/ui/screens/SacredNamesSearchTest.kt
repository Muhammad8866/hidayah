package com.example.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Search on the 99 Names of Allah, the 99 Names of the Prophet ﷺ and the 40 Rabbana duas. */
class SacredNamesSearchTest {
    private val allah = allahNamesSearch("en")
    private val prophet = prophetNamesSearch("en")
    private val rabbana = rabbanaDuasSearch("en")

    private fun allahNumbers(query: String) = allah.search(query).map { it.number }
    private fun prophetNumbers(query: String) = prophet.search(query).map { it.number }
    private fun rabbanaNumbers(query: String) = rabbana.search(query).map { it.number }

    // region Normal

    @Test fun `a name is found by its transliteration in any spelling`() {
        assertEquals(listOf(17), allahNumbers("Ar-Razzaq"))
        assertEquals(listOf(17), allahNumbers("razzaq"))
        assertEquals(listOf(17), allahNumbers("al razzaq"))
        assertEquals(listOf(2), allahNumbers("raheem"))
        assertEquals(listOf(2), allahNumbers("rahim"))
        assertEquals(listOf(4), allahNumbers("Al Quddoos"))
        assertEquals(listOf(47), allahNumbers("wadood"))
    }

    @Test fun `a name is found by its meaning in English, Urdu, Hindi and Roman Urdu`() {
        assertEquals(listOf(17), allahNumbers("provider"))
        assertEquals(listOf(2), allahNumbers("the most merciful"))
        assertEquals(listOf(1), allahNumbers("meharbaan"))
        assertEquals(listOf(1), allahNumbers("मेहरबान"))
        assertEquals(listOf(63), prophetNumbers("seal of the prophets"))
    }

    @Test fun `a name is found by its Arabic with or without harakat`() {
        assertEquals(listOf(2), allahNumbers("الرحيم"))
        assertEquals(listOf(2), allahNumbers("الرَّحِيمُ"))
    }

    @Test fun `a typed number opens that name first`() {
        assertEquals(listOf(17), allahNumbers("17"))
        assertEquals(listOf(99), allahNumbers("99"))
        assertEquals(listOf(63), prophetNumbers("63"))
        assertEquals(9, allahNumbers("9").first())
        assertTrue(allahNumbers("9").containsAll((90..99).toList()))
    }

    @Test fun `a number in Arabic or Hindi digits finds the same name`() {
        assertEquals(listOf(17), allahNumbers("١٧"))
        assertEquals(listOf(17), allahNumbers("१७"))
    }

    @Test fun `prophet names are found by their best-known spellings`() {
        assertEquals(listOf(1), prophetNumbers("muhammad"))
        assertEquals(listOf(2), prophetNumbers("ahmad"))
        assertEquals(listOf(63), prophetNumbers("khatam"))
    }

    @Test fun `a rabbana dua is found by its words, its meaning, its surah or its reference`() {
        assertEquals(listOf(32), rabbanaNumbers("hab lana min azwajina"))
        assertEquals(32, rabbanaNumbers("spouses").first())
        assertEquals(listOf(32), rabbanaNumbers("25:74"))
        assertEquals(listOf(32), rabbanaNumbers("32"))
        assertEquals(listOf(3), rabbanaNumbers("atina fid dunya"))
        assertTrue(rabbanaNumbers("baqarah").isNotEmpty())
        assertTrue(rabbana.search("baqarah").all { it.reference.startsWith("Al-Baqarah") })
    }

    // endregion

    // region Edge

    @Test fun `a small typo finds the name when nothing is spelt that way`() {
        assertEquals(listOf(17), allahNumbers("razaq"))
        assertEquals(listOf(14), allahNumbers("gaffar"))
    }

    @Test fun `case and spaces around the query do not matter`() {
        assertEquals(listOf(17), allahNumbers("  RAZZAQ  "))
    }

    @Test fun `every word must match`() {
        assertEquals(listOf(17), allahNumbers("ar razzaq provider"))
        assertEquals(emptyList<Int>(), allahNumbers("razzaq zzzzqqq"))
    }

    // endregion

    // region Bad input

    @Test fun `a blank query, punctuation or an emoji keeps the whole list in order`() {
        assertEquals((1..99).toList(), allahNumbers(""))
        assertEquals((1..99).toList(), allahNumbers("   "))
        assertEquals((1..99).toList(), allahNumbers("💥"))
        assertEquals((1..99).toList(), prophetNumbers("-- ,"))
        assertEquals((1..40).toList(), rabbanaNumbers(""))
    }

    @Test fun `a pasted page finds nothing instead of everything`() {
        assertEquals(emptyList<Int>(), allahNumbers("x".repeat(5_000)))
        assertEquals(emptyList<Int>(), rabbanaNumbers("zzzz ".repeat(2_000)))
    }

    @Test fun `every language builds a working search`() {
        listOf("en", "ur", "hi", "hinglish", "ar", "fr", "bn").forEach { language ->
            assertEquals(language, listOf(17), allahNamesSearch(language).search("razzaq").map { it.number })
            assertEquals(language, listOf(63), prophetNamesSearch(language).search("63").map { it.number })
            assertEquals(language, listOf(32), rabbanaDuasSearch(language).search("25:74").map { it.number })
        }
    }

    // endregion
}
