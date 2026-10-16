package com.example.ui.screens

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal data class QaidaProgressState(
    val completed: Set<String> = emptySet(),
    val bookmarks: Set<String> = emptySet(),
    /** Best correct-answer count per lesson. */
    val scores: Map<String, Int> = emptyMap(),
    val lastLessonId: String? = null,
    val streakDays: Int = 0
)

/** Learning progress in the "qaida_learning" prefs; the "completed" set predates this class and keeps its key. */
internal class QaidaProgress(
    private val prefs: SharedPreferences,
    private val today: () -> Long = { java.time.LocalDate.now().toEpochDay() }
) {
    private val mutable = MutableStateFlow(load())
    val state: StateFlow<QaidaProgressState> = mutable

    private fun load(): QaidaProgressState {
        val streakDay = prefs.getLong(KEY_STREAK_DAY, Long.MIN_VALUE)
        // A streak is still alive if the last practice was today or yesterday.
        val streak = if (streakDay != Long.MIN_VALUE && today() - streakDay in 0..1) prefs.getInt(KEY_STREAK, 0) else 0
        return QaidaProgressState(
            completed = prefs.getStringSet(KEY_COMPLETED, emptySet()).orEmpty().toSet(),
            bookmarks = prefs.getStringSet(KEY_BOOKMARKS, emptySet()).orEmpty().toSet(),
            scores = decodeScores(prefs.getString(KEY_SCORES, "").orEmpty()),
            lastLessonId = prefs.getString(KEY_LAST, null),
            streakDays = streak
        )
    }

    /** Saves the best score, marks the lesson completed on a full score, and counts today towards the streak. */
    fun recordQuiz(lessonId: String, correct: Int, total: Int) {
        val current = mutable.value
        val score = correct.coerceIn(0, total.coerceAtLeast(0))
        val scores = current.scores + (lessonId to maxOf(score, current.scores[lessonId] ?: 0))
        val completed = if (total > 0 && score == total) current.completed + lessonId else current.completed
        val day = today()
        val streakDay = prefs.getLong(KEY_STREAK_DAY, Long.MIN_VALUE)
        val stored = prefs.getInt(KEY_STREAK, 0)
        val streak = when {
            streakDay == day -> stored.coerceAtLeast(1)
            streakDay != Long.MIN_VALUE && day - streakDay == 1L -> stored + 1
            else -> 1
        }
        mutable.value = current.copy(completed = completed, scores = scores, streakDays = streak)
        prefs.edit()
            .putStringSet(KEY_COMPLETED, completed.toSet())
            .putString(KEY_SCORES, encodeScores(scores))
            .putInt(KEY_STREAK, streak)
            .putLong(KEY_STREAK_DAY, day)
            .apply()
    }

    fun toggleBookmark(lessonId: String) {
        val current = mutable.value
        val bookmarks = if (lessonId in current.bookmarks) current.bookmarks - lessonId else current.bookmarks + lessonId
        mutable.value = current.copy(bookmarks = bookmarks)
        prefs.edit().putStringSet(KEY_BOOKMARKS, bookmarks.toSet()).apply()
    }

    fun opened(lessonId: String) {
        if (mutable.value.lastLessonId == lessonId) return
        mutable.value = mutable.value.copy(lastLessonId = lessonId)
        prefs.edit().putString(KEY_LAST, lessonId).apply()
    }

    /** Clears learning progress only; other keys in the file (e.g. the language choice) are kept. */
    fun reset() {
        mutable.value = QaidaProgressState()
        prefs.edit().remove(KEY_COMPLETED).remove(KEY_BOOKMARKS).remove(KEY_SCORES).remove(KEY_LAST)
            .remove(KEY_STREAK).remove(KEY_STREAK_DAY).apply()
    }

    private companion object {
        const val KEY_COMPLETED = "completed"
        const val KEY_BOOKMARKS = "bookmarks"
        const val KEY_SCORES = "scores"
        const val KEY_LAST = "last_lesson"
        const val KEY_STREAK = "streak"
        const val KEY_STREAK_DAY = "streak_day"

        fun encodeScores(scores: Map<String, Int>) = scores.entries.joinToString(",") { "${it.key}=${it.value}" }

        fun decodeScores(raw: String): Map<String, Int> = raw.split(',').mapNotNull { entry ->
            val key = entry.substringBefore('=', "")
            val value = entry.substringAfter('=', "").toIntOrNull()
            if (key.isEmpty() || value == null) null else key to value
        }.toMap()
    }
}

/** The lesson last opened in this track if still unfinished, else the first unfinished one, else the first. */
internal fun qaidaNextLesson(state: QaidaProgressState, tajweed: Boolean): QaidaLesson {
    val track = qaidaLessons.filter { it.tajweed == tajweed }
    return track.firstOrNull { it.id == state.lastLessonId && it.id !in state.completed }
        ?: track.firstOrNull { it.id !in state.completed }
        ?: track.first()
}

/** Completed lessons to total lessons for one track. */
internal fun qaidaTrackProgress(state: QaidaProgressState, tajweed: Boolean): Pair<Int, Int> {
    val track = qaidaLessons.filter { it.tajweed == tajweed }
    return track.count { it.id in state.completed } to track.size
}
