package com.example.quran.domain.hifz

import com.example.core.id.AyahReference

/** The four answers a learner can give after reciting an ayah, mapped to the planner's accuracy scale. */
enum class HifzGrade(val score: Int) {
    FORGOT(45), HARD(77), GOOD(90), PERFECT(98)
}

/** One ayah in a practice session. [learnFirst] shows the ayah before hiding it. */
data class HifzQueueItem(val reference: AyahReference, val learnFirst: Boolean, val isRetry: Boolean = false)

/**
 * Platform-free rules behind the Hifz Coach screen:
 * - new ayahs are read first, already-memorised ayahs are tested first (recall before peeking);
 * - an ayah marked "Forgot" comes back once at the end of the same session;
 * - hints and the voice check suggest an honest grade;
 * - "continue memorising" always resumes right after the last new ayah learned.
 */
object HifzCoachLogic {
    const val MAX_REVISION = 10

    fun revisionQueue(due: List<SmartHifzCard>): List<HifzQueueItem> =
        due.take(MAX_REVISION).map { HifzQueueItem(it.reference, learnFirst = false) }

    fun newPassage(start: AyahReference, length: Int, totalAyat: (Int) -> Int, known: Set<AyahReference>): List<HifzQueueItem> {
        val surah = start.surah.value
        val last = minOf(totalAyat(surah), start.ayah.value + length - 1)
        return (start.ayah.value..last).map {
            val ref = AyahReference.of(surah, it)
            HifzQueueItem(ref, learnFirst = ref !in known)
        }
    }

    /** Appends a single retry for a forgotten ayah so it is re-learned before the session ends. */
    fun afterGrade(queue: List<HifzQueueItem>, index: Int, grade: HifzGrade): List<HifzQueueItem> {
        val item = queue.getOrNull(index) ?: return queue
        if (grade != HifzGrade.FORGOT || item.isRetry) return queue
        return queue + HifzQueueItem(item.reference, learnFirst = true, isRetry = true)
    }

    /** Ayah after [ref], moving into the next surah (and back to Al-Fatiha after An-Nas). */
    fun nextAyah(ref: AyahReference, totalAyat: (Int) -> Int): AyahReference {
        val surah = ref.surah.value
        return when {
            ref.ayah.value < totalAyat(surah) -> AyahReference.of(surah, ref.ayah.value + 1)
            surah < 114 -> AyahReference.of(surah + 1, 1)
            else -> AyahReference.of(1, 1)
        }
    }

    /**
     * Suggests a grade from objective signals. Returns null when there is nothing to go on,
     * so the learner decides alone. Hints cap the grade: needing half the ayah means it was forgotten.
     */
    fun suggestedGrade(voiceScore: Int?, revealedWords: Int, totalWords: Int): HifzGrade? {
        val fromVoice = voiceScore?.let {
            when {
                it < 70 -> HifzGrade.FORGOT
                it < 85 -> HifzGrade.HARD
                it < 95 -> HifzGrade.GOOD
                else -> HifzGrade.PERFECT
            }
        }
        val cap = when {
            revealedWords <= 0 -> null
            totalWords > 0 && revealedWords * 2 >= totalWords -> HifzGrade.FORGOT
            else -> HifzGrade.HARD
        }
        return when {
            fromVoice == null -> cap
            cap == null -> fromVoice
            else -> minOf(fromVoice, cap)
        }
    }

    /** What Today's plan puts first: due revision always comes before new memorisation. */
    enum class PlanFocus { REVISE, MEMORISE }

    fun planFocus(dueCount: Int): PlanFocus = if (dueCount > 0) PlanFocus.REVISE else PlanFocus.MEMORISE

    /** Practised ayahs of [surah], for the surah progress bar. */
    fun practisedInSurah(cards: List<SmartHifzCard>, surah: Int): Int =
        cards.count { it.reference.surah.value == surah && it.attempts > 0 }

    /** Strength buckets shown on the home screen. */
    fun isStrong(card: SmartHifzCard): Boolean = card.attempts > 0 && !SmartHifzRevisionPlanner.isWeak(card)

    fun intervalLabel(days: Int): String = when {
        days <= 1 -> "tomorrow"
        days < 7 -> "in $days days"
        days < 30 -> "in ${days / 7} week${if (days / 7 == 1) "" else "s"}"
        else -> "in ${days / 30} month${if (days / 30 == 1) "" else "s"}"
    }
}
