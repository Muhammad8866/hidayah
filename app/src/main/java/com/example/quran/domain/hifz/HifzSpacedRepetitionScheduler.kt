package com.example.quran.domain.hifz

import com.example.core.id.AyahReference
import java.util.Calendar
import java.util.TimeZone

enum class HifzConfidenceLevel(val score: Int) {
    AGAIN(1),    // Failed recall / repeated stumbles
    HARD(2),     // Recalled with significant difficulty
    GOOD(3),     // Solid recall with minor pause
    EASY(4)      // Flawless, instantaneous recall
}

data class HifzCardState(
    val reference: AyahReference,
    val repetitions: Int = 0,
    val easeFactor: Double = 2.5,
    val intervalDays: Int = 1,
    val lastReviewedEpochMs: Long = 0L,
    val nextReviewEpochMs: Long = 0L
)

/**
 * Deterministic Spaced-Repetition Scheduler for Quranic Memorization (Hifz).
 * Adapts SuperMemo-2 spaced interval mechanics specifically tailored to Quranic Ayah retention.
 * Rules:
 * - Pure domain logic with no platform or UI dependencies.
 * - Minimum ease factor clamped at 1.3 to prevent interval stagnation.
 */
object HifzSpacedRepetitionScheduler {

    fun calculateNextReview(
        current: HifzCardState,
        confidence: HifzConfidenceLevel,
        reviewTimestampMs: Long
    ): HifzCardState {
        val q = confidence.score // 1..4 scale mapped to SM-2 calculation
        // New Ease Factor calculation: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        // Mapping 1..4 scale: score 1 -> 2, score 2 -> 3, score 3 -> 4, score 4 -> 5
        val smGrade = q + 1
        val newEaseFactor = maxOf(
            1.3,
            current.easeFactor + (0.1 - (5 - smGrade) * (0.08 + (5 - smGrade) * 0.02))
        )

        val newReps: Int
        val newIntervalDays: Int

        if (confidence == HifzConfidenceLevel.AGAIN) {
            // Failed recall resets consecutive repetition counter
            newReps = 0
            newIntervalDays = 1
        } else {
            newReps = current.repetitions + 1
            newIntervalDays = when (newReps) {
                1 -> 1
                2 -> if (confidence == HifzConfidenceLevel.EASY) 4 else 2
                else -> (current.intervalDays * newEaseFactor).toInt().coerceAtLeast(current.intervalDays + 1)
            }
        }

        val nextReviewMs = reviewTimestampMs + (newIntervalDays.toLong() * 86_400_000L)

        return current.copy(
            repetitions = newReps,
            easeFactor = newEaseFactor,
            intervalDays = newIntervalDays,
            lastReviewedEpochMs = reviewTimestampMs,
            nextReviewEpochMs = nextReviewMs
        )
    }

    /**
     * Determines whether an Ayah is due for review at the given timestamp.
     */
    fun isDue(cardState: HifzCardState, currentTimestampMs: Long): Boolean {
        if (cardState.repetitions == 0 && cardState.lastReviewedEpochMs == 0L) {
            return true // New card, immediately due
        }
        return currentTimestampMs >= cardState.nextReviewEpochMs
    }
}
