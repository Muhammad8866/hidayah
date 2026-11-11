package com.example.ui.screens

import com.example.core.search.AppSearch
import com.example.domain.model.DuaDatasetRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Search on the Manzil / Ruqyah / Wazaif screen: Quran passages and duas. */
class RecitationSearchTest {
    private val manzil = AppSearch.Ranker(RecitationCollection.manzil) { recitationPassageFields(it, emptyList()) }
    private fun ids(query: String) = manzil.search(query).map { it.id }
    private val baqarah = listOf("quran_2_1_5", "quran_2_163_163", "quran_2_255_257", "quran_2_284_286")

    @Test fun `a surah name, meaning, number or reference finds its passages`() {
        assertEquals(baqarah, ids("baqarah"))
        assertEquals(baqarah, ids("al baqara"))
        assertEquals("quran_2_1_5", ids("the cow").first())
        assertEquals(baqarah, ids("2").take(4))
        assertEquals(listOf("quran_2_255_257"), ids("2:255"))
        assertEquals("quran_112_1_4", ids("ikhlas").first())
        assertEquals("quran_112_1_4", ids("112").first())
        assertEquals("quran_112_1_4", ids("١١٢").first())
        assertEquals("quran_112_1_4", ids("الإخلاص").first())
    }

    @Test fun `the arabic of the verses is searched without harakat`() {
        val verses = mapOf(
            2 to listOf("اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ"),
            112 to listOf("قُلْ هُوَ اللَّهُ أَحَدٌ")
        )
        val ranker = AppSearch.Ranker(listOf(RecitationPassage(2, 255, 255), RecitationPassage(112, 1, 4))) {
            recitationPassageFields(it, verses.getValue(it.surah))
        }
        assertEquals(listOf("quran_2_255_255"), ranker.search("القيوم").map { it.id })
        assertEquals(listOf("quran_112_1_4"), ranker.search("قل هو الله احد").map { it.id })
    }

    @Test fun `the surah name in the reader's language is searched`() {
        val ranker = AppSearch.Ranker(RecitationCollection.ruqyah) {
            recitationPassageFields(it, emptyList(), localizedTitle = if (it.surah == 113) "सूरह फ़लक़" else "")
        }
        assertEquals("quran_113_1_5", ranker.search("फ़लक़").first().id)
    }

    @Test fun `wazaif duas are found by their transliteration, joined or split`() {
        val wazaif = AppSearch.Ranker(RecitationCollection.wazaif) { DuaDatasetRepository.searchFields(it) }
        val joined = wazaif.search("subhanallah").map { it.id }
        val split = wazaif.search("subhan allah").map { it.id }
        assertTrue(joined.isNotEmpty() && split.isNotEmpty())
        assertTrue(joined.all { id -> RecitationCollection.wazaif.any { it.id == id } })
        assertEquals("dua_sayyidul_istighfar", wazaif.search("sayyidul istighfar").first().id)
    }

    @Test fun `bad input keeps or empties the list without breaking`() {
        assertEquals(RecitationCollection.manzil.map { it.id }, ids(""))
        assertEquals(RecitationCollection.manzil.map { it.id }, ids("💥"))
        assertEquals(emptyList<String>(), ids("x".repeat(5_000)))
        assertEquals(emptyList<String>(), ids("kursi falaq zzzq"))
    }
}
