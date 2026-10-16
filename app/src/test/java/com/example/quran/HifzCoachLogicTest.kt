package com.example.quran

import com.example.core.id.AyahReference
import com.example.quran.domain.hifz.HifzCoachLogic
import com.example.quran.domain.hifz.HifzGrade
import com.example.quran.domain.hifz.HifzQueueItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HifzCoachLogicTest {
    private val totals: (Int) -> Int = { FullQuranCatalog.surahs[it - 1].totalAyat }

    @Test
    fun newPassageStopsAtSurahEndAndOnlyLearnsUnknownAyahs() {
        val known = setOf(AyahReference.of(1, 6))
        val passage = HifzCoachLogic.newPassage(AyahReference.of(1, 5), 10, totals, known)
        assertEquals(listOf(5, 6, 7), passage.map { it.reference.ayah.value })
        assertTrue(passage[0].learnFirst)
        assertFalse(passage[1].learnFirst)
    }

    @Test
    fun forgottenAyahIsRetriedOnlyOnce() {
        val queue = listOf(HifzQueueItem(AyahReference.of(1, 1), learnFirst = false))
        val retried = HifzCoachLogic.afterGrade(queue, 0, HifzGrade.FORGOT)
        assertEquals(2, retried.size)
        assertTrue(retried[1].isRetry && retried[1].learnFirst)
        assertEquals(retried, HifzCoachLogic.afterGrade(retried, 1, HifzGrade.FORGOT))
        assertEquals(queue, HifzCoachLogic.afterGrade(queue, 0, HifzGrade.GOOD))
    }

    @Test
    fun nextAyahCrossesSurahsAndWrapsAfterAnNas() {
        assertEquals(AyahReference.of(2, 1), HifzCoachLogic.nextAyah(AyahReference.of(1, 7), totals))
        assertEquals(AyahReference.of(1, 1), HifzCoachLogic.nextAyah(AyahReference.of(114, 6), totals))
        assertEquals(AyahReference.of(2, 5), HifzCoachLogic.nextAyah(AyahReference.of(2, 4), totals))
    }

    @Test
    fun hintsCapTheSuggestedGrade() {
        assertNull(HifzCoachLogic.suggestedGrade(null, 0, 10))
        assertEquals(HifzGrade.PERFECT, HifzCoachLogic.suggestedGrade(98, 0, 10))
        assertEquals(HifzGrade.HARD, HifzCoachLogic.suggestedGrade(98, 1, 10))
        assertEquals(HifzGrade.FORGOT, HifzCoachLogic.suggestedGrade(null, 5, 10))
        assertEquals(HifzGrade.FORGOT, HifzCoachLogic.suggestedGrade(60, 1, 10))
    }

    @Test
    fun todaysPlanPutsDueRevisionBeforeNewAyahsAndCountsSurahProgress() {
        assertEquals(HifzCoachLogic.PlanFocus.REVISE, HifzCoachLogic.planFocus(3))
        assertEquals(HifzCoachLogic.PlanFocus.MEMORISE, HifzCoachLogic.planFocus(0))
        val cards = listOf(
            com.example.quran.domain.hifz.SmartHifzCard(AyahReference.of(1, 1), attempts = 2),
            com.example.quran.domain.hifz.SmartHifzCard(AyahReference.of(1, 2), attempts = 1),
            com.example.quran.domain.hifz.SmartHifzCard(AyahReference.of(1, 3), attempts = 0),
            com.example.quran.domain.hifz.SmartHifzCard(AyahReference.of(2, 1), attempts = 1)
        )
        assertEquals(2, HifzCoachLogic.practisedInSurah(cards, 1))
        assertEquals(1, HifzCoachLogic.practisedInSurah(cards, 2))
    }
}
