package com.example.quran

import com.example.core.id.AyahReference
import com.example.quran.domain.hifz.HifzRevisionSession
import com.example.quran.domain.hifz.SmartHifzCard
import com.example.quran.domain.hifz.SmartHifzRevisionPlanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartHifzRevisionPlannerTest {
    @Test
    fun weakRecallIsDueTomorrowAndIsFlaggedForFocus() {
        val start = 1_700_000_000_000L
        val card = SmartHifzRevisionPlanner.record(SmartHifzCard(AyahReference.of(1, 1)), 60, start)

        assertTrue(SmartHifzRevisionPlanner.isWeak(card))
        assertEquals(0, card.schedule.repetitions)
        assertEquals(start + 86_400_000L, card.schedule.nextReviewEpochMs)
    }

    @Test
    fun summaryReportsCompletedRevisionAndAccuracy() {
        val now = 1_700_000_000_000L
        val card = SmartHifzRevisionPlanner.record(SmartHifzCard(AyahReference.of(1, 1)), 92, now)
        val summary = SmartHifzRevisionPlanner.dailySummary(
            listOf(card), listOf(HifzRevisionSession(now, 1, 92)), now
        )

        assertEquals(1, summary.completedToday)
        assertEquals(92, summary.accuracyToday)
        assertEquals(1, summary.streakDays)
        assertEquals(1f, summary.progress)
    }
}
