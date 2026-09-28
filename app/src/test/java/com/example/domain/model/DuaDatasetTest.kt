package com.example.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DuaDatasetTest {

    private val duas = DuaDatasetRepository.DUAS

    @Test
    fun `every dua has a unique id and a listed category`() {
        val duplicates = duas.groupBy { it.id }.filterValues { it.size > 1 }.keys
        assertTrue("Duplicate ids: $duplicates", duplicates.isEmpty())

        val categories = DuaDatasetRepository.CATEGORIES - DuaDatasetRepository.ALL_CATEGORY
        val unknown = duas.filter { it.category !in categories }.map { it.id }
        assertTrue("Unknown categories: $unknown", unknown.isEmpty())
        categories.forEach { category ->
            assertTrue("$category is empty", duas.any { it.category == category })
        }
    }

    @Test
    fun `every dua has real English copy and no English entry is orphaned`() {
        val missing = duas.filter { DuaEnglish.forId(it.id) == null }.map { it.id }
        assertTrue("Duas without English copy: $missing", missing.isEmpty())
        val ids = duas.map { it.id }.toSet()
        val orphaned = (DuaEnglish1.MAP.keys + DuaEnglish2.MAP.keys + DuaEnglish3.MAP.keys) - ids
        assertTrue("English copy for unknown duas: $orphaned", orphaned.isEmpty())
        duas.forEach { dua ->
            listOf(dua.englishMeaning, dua.englishContext, dua.englishBenefit)
                .forEach { assertTrue("${dua.id} has blank English copy", it.isNotBlank()) }
        }
    }

    @Test
    fun `every dua carries its text, meaning and source reference`() {
        duas.forEach { dua ->
            listOf(
                dua.title, dua.context, dua.arabicText, dua.transliteration, dua.englishTranslation,
                dua.urduTranslation, dua.benefit, dua.sourceCollection, dua.sourceReference, dua.authenticityGrading
            ).forEach { field -> assertTrue("${dua.id} has a blank field", field.isNotBlank()) }
            assertTrue("${dua.id} recommendedCount", dua.recommendedCount >= 1)
            assertTrue("${dua.id} Arabic text must not contain Latin letters", dua.arabicText.none { it in 'A'..'z' })
            // A bare end-of-ayah sign renders as an empty ring; verse ends carry their number instead.
            assertTrue("${dua.id} has a bare ۝", '۝' !in dua.arabicText)
        }
    }

    @Test
    fun `grade labels use the badge vocabulary`() {
        val allowed = setOf("Quran", "Sahih", "Hasan Sahih", "Hasan", "Mauqoof")
        duas.forEach { assertTrue("${it.id}: ${it.gradeLabel}", it.gradeLabel in allowed) }
        assertEquals("Quran", DuaDatasetRepository.byId("dua_parents")?.gradeLabel)
        assertEquals("Hasan Sahih", DuaDatasetRepository.byId("dua_leaving_home")?.gradeLabel)
        assertEquals("Mauqoof", DuaDatasetRepository.byId("dua_qunoot_hanafi")?.gradeLabel)
    }

    @Test
    fun `adhkar lists resolve to referenced duas in both sessions`() {
        val morning = AdhkarDatasetRepository.MORNING_ADHKAR
        val evening = AdhkarDatasetRepository.EVENING_ADHKAR
        assertTrue(morning.size >= 10 && evening.size >= 10)
        assertEquals(morning.size, morning.map { it.id }.toSet().size)
        assertEquals(evening.size, evening.map { it.id }.toSet().size)
        assertTrue(morning.any { it.id == "dua_morning_asbahna" })
        assertTrue(evening.any { it.id == "dua_evening_amsayna" })
        assertTrue(evening.none { it.arabicText.contains("أَصْبَحْنَا وَأَصْبَحَ") })
    }

    @Test
    fun `search ignores case, diacritics and transliteration marks`() {
        val ids = { query: String -> DuaDatasetRepository.searchDuas(query).map { it.id } }
        assertTrue(ids("subhanallahi wa bihamdihi").contains("dua_subhanallah_wa_bihamdihi"))
        assertTrue(ids("SAYYID istighfar").contains("dua_sayyidul_istighfar"))
        assertTrue(ids("الحمد لله الذي احيانا").contains("dua_waking_up"))
        assertTrue(ids("Bukhari 6312").contains("dua_waking_up"))
        assertTrue(ids("   ").size == duas.size)
        assertTrue(ids("zzzz-no-such-dua").isEmpty())
    }

    @Test
    fun `filter applies category and search together`() {
        val sleepOnly = DuaDatasetRepository.filter("Sleep & Awakening", "")
        assertTrue(sleepOnly.isNotEmpty())
        assertTrue(sleepOnly.all { it.category == "Sleep & Awakening" })

        val sleepAndBismika = DuaDatasetRepository.filter("Sleep & Awakening", "bismika")
        assertTrue(sleepAndBismika.isNotEmpty())
        assertTrue(sleepAndBismika.all { it.category == "Sleep & Awakening" })

        assertTrue(DuaDatasetRepository.filter("Weather & Nature", "bismika").isEmpty())
        assertEquals(duas, DuaDatasetRepository.filter(DuaDatasetRepository.ALL_CATEGORY, ""))
    }

    @Test
    fun `tasbeeh presets keep the persisted phrase format`() {
        assertEquals("Subhan Allah (سُبْحَانَ اللَّهِ)", TasbeehPreset.ALL.first().phrase)
        assertEquals(34, TasbeehPreset.forPhrase("Allahu Akbar (اللَّهُ أَكْبَرُ)")?.goal)
        assertEquals(null, TasbeehPreset.forPhrase("SubhanAllah"))
    }
}
