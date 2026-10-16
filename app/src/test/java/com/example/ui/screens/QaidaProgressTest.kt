package com.example.ui.screens

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QaidaProgressTest {

    private class FakePrefs : SharedPreferences {
        val values = mutableMapOf<String, Any?>()

        override fun getAll(): MutableMap<String, *> = values
        override fun getString(key: String, defValue: String?): String? = values[key] as? String ?: defValue
        @Suppress("UNCHECKED_CAST")
        override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? =
            values[key] as? MutableSet<String> ?: defValues
        override fun getInt(key: String, defValue: Int): Int = values[key] as? Int ?: defValue
        override fun getLong(key: String, defValue: Long): Long = values[key] as? Long ?: defValue
        override fun getFloat(key: String, defValue: Float): Float = values[key] as? Float ?: defValue
        override fun getBoolean(key: String, defValue: Boolean): Boolean = values[key] as? Boolean ?: defValue
        override fun contains(key: String): Boolean = key in values
        override fun edit(): SharedPreferences.Editor = FakeEditor()
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        inner class FakeEditor : SharedPreferences.Editor {
            private val pending = mutableMapOf<String, Any?>()
            private val removed = mutableSetOf<String>()
            private var clearAll = false
            private fun put(key: String, value: Any?): SharedPreferences.Editor { pending[key] = value; return this }
            override fun putString(key: String, value: String?) = put(key, value)
            override fun putStringSet(key: String, value: MutableSet<String>?) = put(key, value?.toMutableSet())
            override fun putInt(key: String, value: Int) = put(key, value)
            override fun putLong(key: String, value: Long) = put(key, value)
            override fun putFloat(key: String, value: Float) = put(key, value)
            override fun putBoolean(key: String, value: Boolean) = put(key, value)
            override fun remove(key: String): SharedPreferences.Editor { removed += key; return this }
            override fun clear(): SharedPreferences.Editor { clearAll = true; return this }
            override fun commit(): Boolean { apply(); return true }
            override fun apply() {
                if (clearAll) values.clear()
                removed.forEach { values.remove(it) }
                values.putAll(pending)
            }
        }
    }

    @Test
    fun lessonIdsAreUniqueAndOldIdsRemain() {
        val ids = qaidaLessons.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        val original = listOf("letters", "shapes", "fatha", "kasra", "damma", "tanween", "sukoon", "shadda",
            "madd", "ghunnah", "izhar", "idgham", "iqlab", "ikhfa", "qalqalah", "meem")
        assertTrue(ids.containsAll(original))
        assertTrue(ids.containsAll(listOf("makharij", "heavy_light", "madd_types", "laam", "raa", "waqf")))
        assertEquals(29, qaidaAlphabet.size)
    }

    @Test
    fun everyLessonHasValidQuizzesAndBothLanguages() {
        qaidaLessons.forEach { lesson ->
            assertTrue(lesson.id, lesson.quizzes.size >= 3)
            assertTrue(lesson.id, lesson.examples.isNotEmpty())
            listOf(lesson.title, lesson.romanTitle, lesson.rule, lesson.romanRule, lesson.tip, lesson.romanTip)
                .forEach { assertTrue(lesson.id, it.isNotBlank()) }
            lesson.quizzes.forEach { quiz ->
                assertTrue(lesson.id, quiz.question.isNotBlank() && quiz.romanQuestion.isNotBlank())
                assertTrue(lesson.id, quiz.options.size >= 2)
                assertEquals(lesson.id, quiz.options.size, quiz.options.toSet().size)
                assertTrue(lesson.id, quiz.answer in quiz.options.indices)
            }
        }
    }

    @Test
    fun searchMatchesTitlesAndBlankReturnsAll() {
        assertEquals(qaidaLessons, qaidaSearch("  ", roman = false))
        assertTrue(qaidaSearch("ikhfa", roman = true).any { it.id == "ikhfa" })
        assertTrue(qaidaSearch("Tashdeed", roman = false).any { it.id == "shadda" })
        assertTrue(qaidaSearch("zzzz", roman = false).isEmpty())
    }

    @Test
    fun existingCompletedSetIsReadAndFullScoreCompletesLesson() {
        val prefs = FakePrefs().apply { values["completed"] = mutableSetOf("letters") }
        val progress = QaidaProgress(prefs) { 100L }
        assertEquals(setOf("letters"), progress.state.value.completed)

        progress.recordQuiz("shapes", 2, 4)
        assertFalse("shapes" in progress.state.value.completed)
        assertEquals(2, progress.state.value.scores["shapes"])

        progress.recordQuiz("shapes", 4, 4)
        progress.recordQuiz("shapes", 1, 4)
        assertEquals(4, progress.state.value.scores["shapes"])
        assertEquals(setOf("letters", "shapes"), progress.state.value.completed)
        assertEquals(setOf("letters", "shapes"), QaidaProgress(prefs) { 100L }.state.value.completed)
        assertEquals(4, QaidaProgress(prefs) { 100L }.state.value.scores["shapes"])
    }

    @Test
    fun streakCountsConsecutiveDaysOnly() {
        val prefs = FakePrefs()
        var day = 10L
        val progress = QaidaProgress(prefs) { day }
        assertEquals(0, progress.state.value.streakDays)
        progress.recordQuiz("letters", 1, 4)
        progress.recordQuiz("letters", 2, 4)
        assertEquals(1, progress.state.value.streakDays)
        day = 11L
        progress.recordQuiz("letters", 4, 4)
        assertEquals(2, progress.state.value.streakDays)
        assertEquals(2, QaidaProgress(prefs) { 12L }.state.value.streakDays)
        assertEquals(0, QaidaProgress(prefs) { 13L }.state.value.streakDays)
        day = 14L
        progress.recordQuiz("letters", 4, 4)
        assertEquals(1, progress.state.value.streakDays)
    }

    @Test
    fun bookmarksLastLessonNextLessonAndReset() {
        val prefs = FakePrefs().apply { values["roman"] = true }
        val progress = QaidaProgress(prefs) { 1L }
        assertEquals("letters", qaidaNextLesson(progress.state.value, false).id)
        assertEquals("madd", qaidaNextLesson(progress.state.value, true).id)

        progress.toggleBookmark("raa")
        assertEquals(setOf("raa"), progress.state.value.bookmarks)
        progress.toggleBookmark("raa")
        assertTrue(progress.state.value.bookmarks.isEmpty())

        progress.opened("kasra")
        assertEquals("kasra", qaidaNextLesson(progress.state.value, false).id)
        assertEquals("madd", qaidaNextLesson(progress.state.value, true).id)
        progress.recordQuiz("kasra", 4, 4)
        assertEquals("letters", qaidaNextLesson(progress.state.value, false).id)
        assertEquals(1 to qaidaLessons.count { !it.tajweed }, qaidaTrackProgress(progress.state.value, false))

        progress.reset()
        assertEquals(QaidaProgressState(), progress.state.value)
        assertNull(QaidaProgress(prefs) { 1L }.state.value.lastLessonId)
        assertTrue(prefs.getBoolean("roman", false))
    }
}
