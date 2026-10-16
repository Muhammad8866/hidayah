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

    @Test
    fun anAyahDueTomorrowEveningIsInTomorrowsPlanFromMorning() {
        val evening = java.util.Calendar.getInstance().apply {
            set(2026, 0, 10, 21, 0, 0); set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        val card = SmartHifzRevisionPlanner.record(SmartHifzCard(AyahReference.of(1, 1)), 60, evening)
        val nextMorning = evening + 12 * 3_600_000L

        assertTrue(SmartHifzRevisionPlanner.isDueToday(card, nextMorning))
        assertEquals(false, SmartHifzRevisionPlanner.isDueToday(card, evening + 3_600_000L))
        assertEquals(1, SmartHifzRevisionPlanner.dailySummary(listOf(card), emptyList(), nextMorning).dueCards.size)
    }

    @Test
    fun historyKeepsOneWeightedEntryPerDaySoLongStreaksSurvive() {
        val day = 86_400_000L
        val start = 1_700_000_000_000L
        var history = emptyList<HifzRevisionSession>()
        // 500 days with ten graded ayahs each would overflow a per-ayah history.
        repeat(500) { d -> repeat(10) { history = SmartHifzRevisionPlanner.addSession(history, HifzRevisionSession(start + d * day, 1, 90)) } }

        assertEquals(400, history.size)
        assertEquals(10, history.last().completedTasks)
        assertEquals(400, SmartHifzRevisionPlanner.dailySummary(emptyList(), history, start + 499 * day).streakDays)

        val mixed = SmartHifzRevisionPlanner.addSession(listOf(HifzRevisionSession(start, 1, 100)), HifzRevisionSession(start + 1000, 3, 60))
        assertEquals(1, mixed.size)
        assertEquals(4, mixed.single().completedTasks)
        assertEquals(70, mixed.single().accuracy)
    }
}
