package com.example.quran.domain.hifz

import com.example.core.id.AyahReference
import java.util.Calendar
import java.util.TimeZone

/** A persisted snapshot of an Ayah's recall quality and spaced-repetition state. */
data class SmartHifzCard(
    val reference: AyahReference,
    val schedule: HifzCardState = HifzCardState(reference),
    val averageAccuracy: Int = 0,
    val attempts: Int = 0
)

data class HifzRevisionSession(
    val reviewedAtEpochMs: Long,
    val completedTasks: Int,
    val accuracy: Int
)

data class SmartHifzDailySummary(
    val dueCards: List<SmartHifzCard>,
    val completedToday: Int,
    val accuracyToday: Int,
    val streakDays: Int
) {
    val totalToday: Int get() = dueCards.size + completedToday
    val progress: Float get() = if (totalToday == 0) 0f else completedToday.toFloat() / totalToday
}

/**
 * Small, platform-free policy layer over the existing scheduler.
 * It translates speech-assessment accuracy into a recall grade, keeps a rolling accuracy score,
 * and derives the queue, daily completion, and streak from review history.
 */
object SmartHifzRevisionPlanner {
    fun confidenceFor(accuracy: Int): HifzConfidenceLevel = when (accuracy.coerceIn(0, 100)) {
        in 0..69 -> HifzConfidenceLevel.AGAIN
        in 70..84 -> HifzConfidenceLevel.HARD
        in 85..94 -> HifzConfidenceLevel.GOOD
        else -> HifzConfidenceLevel.EASY
    }

    fun record(card: SmartHifzCard, accuracy: Int, reviewedAtEpochMs: Long): SmartHifzCard {
        val safeAccuracy = accuracy.coerceIn(0, 100)
        val newAttempts = card.attempts + 1
        val rollingAverage = ((card.averageAccuracy * card.attempts) + safeAccuracy) / newAttempts
        return card.copy(
            schedule = HifzSpacedRepetitionScheduler.calculateNextReview(
                card.schedule, confidenceFor(safeAccuracy), reviewedAtEpochMs
            ),
            averageAccuracy = rollingAverage,
            attempts = newAttempts
        )
    }

    fun isWeak(card: SmartHifzCard): Boolean =
        card.attempts > 0 && (card.averageAccuracy < 85 || card.schedule.repetitions == 0)

    fun dailySummary(
        cards: List<SmartHifzCard>,
        sessions: List<HifzRevisionSession>,
        nowEpochMs: Long
    ): SmartHifzDailySummary {
        val today = dayKey(nowEpochMs)
        val todaySessions = sessions.filter { dayKey(it.reviewedAtEpochMs) == today }
        val completed = todaySessions.sumOf { it.completedTasks }
        val weightedAccuracy = todaySessions.sumOf { it.accuracy * it.completedTasks }
        return SmartHifzDailySummary(
            dueCards = cards.filter { isDueToday(it, nowEpochMs) }
                .sortedWith(compareBy<SmartHifzCard> { !isWeak(it) }.thenBy { it.averageAccuracy }),
            completedToday = completed,
            accuracyToday = if (completed == 0) 0 else weightedAccuracy / completed,
            streakDays = streak(sessions, nowEpochMs)
        )
    }

    /**
     * Revision is planned by calendar day, not by the minute: an ayah practised at 9 pm with a one-day
     * interval belongs to tomorrow's plan from the morning, instead of appearing only after 9 pm.
     */
    fun isDueToday(card: SmartHifzCard, nowEpochMs: Long): Boolean {
        val schedule = card.schedule
        if (schedule.repetitions == 0 && schedule.lastReviewedEpochMs == 0L) return true
        return dayKey(schedule.nextReviewEpochMs) <= dayKey(nowEpochMs)
    }

    /**
     * Adds one graded ayah to the history, folding it into that day's entry. History is kept per day,
     * so the stored list covers [keepDays] days of streak regardless of how many ayahs are practised.
     */
    fun addSession(
        sessions: List<HifzRevisionSession>,
        session: HifzRevisionSession,
        keepDays: Int = 400
    ): List<HifzRevisionSession> {
        val byDay = (sessions + session).groupBy { dayKey(it.reviewedAtEpochMs) }
        return byDay.toSortedMap().values.map { day ->
            val tasks = day.sumOf { it.completedTasks }
            HifzRevisionSession(
                reviewedAtEpochMs = day.maxOf { it.reviewedAtEpochMs },
                completedTasks = tasks,
                accuracy = if (tasks == 0) 0 else day.sumOf { it.accuracy * it.completedTasks } / tasks
            )
        }.takeLast(keepDays)
    }

    fun dayKey(epochMs: Long): Long {
        val calendar = Calendar.getInstance(TimeZone.getDefault()).apply { timeInMillis = epochMs }
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun streak(sessions: List<HifzRevisionSession>, nowEpochMs: Long): Int {
        val completedDays = sessions.filter { it.completedTasks > 0 }.map { dayKey(it.reviewedAtEpochMs) }.toSet()
        var cursor = dayKey(nowEpochMs)
        // A user who has not reviewed yet today keeps yesterday's streak visible.
        if (cursor !in completedDays) cursor -= 86_400_000L
        var count = 0
        while (cursor in completedDays) { count++; cursor -= 86_400_000L }
        return count
    }
}
