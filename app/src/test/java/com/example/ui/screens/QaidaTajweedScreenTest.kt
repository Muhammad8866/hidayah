package com.example.ui.screens

import android.content.Context
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w412dp-h915dp-xhdpi", sdk = [36])
class QaidaTajweedScreenTest {
    @get:Rule val compose = createComposeRule()
    private val prefs get() = ApplicationProvider.getApplicationContext<Context>()
        .getSharedPreferences("qaida_learning", Context.MODE_PRIVATE)
    /** Fresh read of whatever the screen persisted. */
    private val saved get() = QaidaProgress(prefs).state.value

    private class FakeAudio : QaidaAudioController {
        val mutable = MutableStateFlow(QaidaAudioState())
        override val state = mutable
        var last: QaidaAudioClip? = null
        override fun play(clip: QaidaAudioClip) {
            val toggle = mutable.value.clipId == clip.id
            last = clip
            mutable.value = mutable.value.copy(clipId = clip.id, playing = if (toggle) !mutable.value.playing else true)
        }
        override fun stop() { mutable.value = mutable.value.copy(clipId = null, playing = false, queue = emptyList(), queueIndex = -1) }
        override fun pause() { mutable.value = mutable.value.copy(playing = false) }
        override fun slow(enabled: Boolean) { mutable.value = mutable.value.copy(slow = enabled) }
        override fun repeat(enabled: Boolean) { mutable.value = mutable.value.copy(repeat = enabled) }
        override fun close() {}
        override fun speed(value: Float) { mutable.value = mutable.value.copy(speed = value, slow = value < 1f) }
        override fun playAll(clips: List<QaidaAudioClip>, startIndex: Int) {
            last = clips[startIndex]
            mutable.value = mutable.value.copy(clipId = clips[startIndex].id, playing = true, queue = clips.map { it.id }, queueIndex = startIndex)
        }
        override fun next() { move(1) }
        override fun previous() { move(-1) }
        private fun move(by: Int) {
            val s = mutable.value
            val i = (s.queueIndex + by).coerceIn(0, s.queue.lastIndex)
            mutable.value = s.copy(queueIndex = i, clipId = s.queue[i])
        }
    }

    @Before fun clear() { prefs.edit().clear().commit() }
    private fun show(language: String = "en", dark: Boolean = false) {
        compose.setContent {
            CompositionLocalProvider(LocalAppLanguage provides language) {
                MyApplicationTheme(darkTheme = dark) { Surface { QaidaTajweedScreen(onBack = {}) } }
            }
        }
    }
    private fun scrollTo(tag: String) = compose.onNodeWithTag("qaida_tajweed_screen").performScrollToNode(hasTestTag(tag))
    private fun tap(tag: String) {
        scrollTo(tag)
        compose.onNodeWithTag(tag).performScrollTo().performClick()
    }
    /** Answers every question of [lesson]; the first one wrongly when [failFirst]. */
    private fun answerQuiz(lesson: QaidaLesson, failFirst: Boolean = false) {
        lesson.quizzes.forEachIndexed { i, quiz ->
            val choice = if (failFirst && i == 0) (quiz.answer + 1) % quiz.options.size else quiz.answer
            tap("qaida_answer_$choice")
            if (i < lesson.quizzes.lastIndex) tap("qaida_quiz_next")
        }
    }
    private fun lesson(id: String) = qaidaLessons.first { it.id == id }

    @Test fun imperfectQuizDoesNotCompleteAndPerfectRetryPersists() {
        show()
        compose.onNodeWithTag("qaida_continue").performClick()
        val alif = compose.onNodeWithText("Alif").getUnclippedBoundsInRoot()
        val baa = compose.onNodeWithText("Baa").getUnclippedBoundsInRoot()
        assertTrue("Arabic alphabet should start on the right", alif.left > baa.left)
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/qaida_alphabet_light.png")
        answerQuiz(lesson("letters"), failFirst = true)
        compose.onNodeWithTag("qaida_feedback").assertTextEquals("Not complete yet. Re-read the rule above and try again.")
        compose.onNodeWithTag("qaida_next").assertDoesNotExist()
        assertTrue(saved.completed.isEmpty())
        tap("qaida_retry")
        answerQuiz(lesson("letters"))
        compose.onNodeWithTag("qaida_feedback").assertTextEquals("Correct! Lesson completed.")
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/qaida_practice_light.png")
        assertEquals(setOf("letters"), saved.completed)
        tap("qaida_next")
        compose.onNodeWithText("Joining letters").assertExists()
    }

    @Test fun savedProgressContinuesAtNextLessonAndTracksStaySeparate() {
        val quizzes = lesson("letters").quizzes.size
        QaidaProgress(prefs).recordQuiz("letters", quizzes, quizzes)
        show()
        compose.onNodeWithTag("qaida_continue").performClick()
        compose.onNodeWithText("Joining letters").assertExists()
        tap("qaida_all_lessons")
        tap("tajweed_tab")
        val expected = qaidaNextLesson(saved, true)
        assertTrue(expected.tajweed)
        tap("qaida_continue")
        compose.onNodeWithText(expected.title).assertExists()
        assertEquals(setOf("letters"), saved.completed)
        assertTrue("Selected track should be remembered", prefs.getBoolean("track_tajweed", false))
    }

    @Test
    @Config(qualifiers = "w320dp-h700dp-xhdpi", sdk = [36])
    fun romanDarkNarrowLesson() {
        show("hinglish", dark = true)
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/qaida_roman_dark.png")
        tap("tajweed_tab")
        val first = qaidaNextLesson(saved, true)
        tap("qaida_continue")
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/tajweed_roman_lesson.png")
        answerQuiz(first)
        compose.onNodeWithTag("qaida_feedback").assertTextEquals("Sahi! Sabaq mukammal.")
        assertEquals(setOf(first.id), saved.completed)
    }

    @Test fun audioControlsQueueAndLessonExit() {
        val audio = FakeAudio()
        compose.setContent { MyApplicationTheme { Surface { QaidaTajweedContent(onBack = {}, audio = audio) } } }
        tap("qaida_continue")
        tap("qaida_letter_1")
        compose.runOnIdle { assertEquals("name-1", audio.last?.id); assertTrue(audio.state.value.playing) }
        tap("qaida_letter_1")
        compose.runOnIdle { assertFalse(audio.state.value.playing) }
        tap(qaidaSpeedTag(0.75f))
        tap("qaida_audio_repeat")
        compose.runOnIdle { assertEquals(0.75f, audio.state.value.speed); assertTrue(audio.state.value.repeat) }
        tap("qaida_audio_play_all")
        compose.runOnIdle { assertEquals(qaidaAlphabet.size, audio.state.value.queue.size); assertEquals(0, audio.state.value.queueIndex) }
        tap("qaida_audio_next")
        compose.runOnIdle { assertEquals(1, audio.state.value.queueIndex) }
        tap("qaida_audio_toggle")
        compose.runOnIdle { assertFalse(audio.state.value.playing) }
        tap("qaida_audio_stop")
        compose.runOnIdle { assertNull(audio.state.value.clipId) }
        tap("qaida_letter_1")
        tap("qaida_all_lessons")
        compose.runOnIdle { assertNull(audio.state.value.clipId) }
    }

    @Test fun bookmarkFilterAndReset() {
        show()
        tap("qaida_lesson_shapes")
        tap("qaida_bookmark")
        assertEquals(setOf("shapes"), saved.bookmarks)
        tap("qaida_all_lessons")
        tap("qaida_bookmarks_filter")
        scrollTo("qaida_lesson_shapes")
        compose.onNodeWithTag("qaida_lesson_letters").assertDoesNotExist()
        tap("qaida_reset")
        compose.onNodeWithTag("qaida_reset_confirm").performClick()
        compose.runOnIdle { assertTrue(saved.bookmarks.isEmpty()) }
        scrollTo("qaida_lesson_letters")
    }

    @Test fun searchFindsLessonsAcrossTracksAndShowsEmptyState() {
        show()
        scrollTo("qaida_search")
        compose.onNodeWithTag("qaida_search").performTextInput("Ikhfa")
        scrollTo("qaida_lesson_ikhfa")
        scrollTo("qaida_search")
        compose.onNodeWithTag("qaida_search").performTextClearance()
        compose.onNodeWithTag("qaida_search").performTextInput("zzzzqq")
        compose.onNodeWithTag("qaida_tajweed_screen").performScrollToNode(hasText("No lesson matches", substring = true))
    }

    @Test fun lessonNavigationFollowsTheTrack() {
        show()
        tap("qaida_lesson_shapes")
        tap("qaida_nav_prev")
        compose.onNodeWithText("Arabic letters").assertExists()
        scrollTo("qaida_nav_prev")
        compose.onNodeWithTag("qaida_nav_prev").assertIsNotEnabled()
        assertEquals("letters", saved.lastLessonId)
    }

    @Test fun listeningDrillPlaysHiddenClipAndScoresAnswers() {
        val audio = FakeAudio()
        compose.setContent { MyApplicationTheme { Surface { QaidaTajweedContent(onBack = {}, audio = audio) } } }
        tap("qaida_lesson_fatha")
        scrollTo("qaida_drill")
        // Choices stay locked until the sound has been heard.
        scrollTo("qaida_drill_option_0")
        compose.onNodeWithTag("qaida_drill_option_0").assertIsNotEnabled()
        tap("qaida_drill_play")
        val played = compose.runOnIdle { audio.last!! }
        assertTrue("Drill clip must not reuse a grid id", played.id.startsWith("drill-"))
        val answer = (0 until 4).first { i ->
            compose.onNodeWithTag("qaida_drill_option_$i").fetchSemanticsNode()
                .config[androidx.compose.ui.semantics.SemanticsProperties.Text].joinToString("") == played.arabic
        }
        tap("qaida_drill_option_$answer")
        compose.onNodeWithTag("qaida_drill_feedback").assertTextEquals("Correct!")
        tap("qaida_drill_next")
        compose.onNodeWithTag("qaida_drill_feedback").assertDoesNotExist()
    }

    @Test fun drillRoundsAreStableAndFair() {
        val pool = qaidaLessonAudio("fatha")
        repeat(QAIDA_DRILL_ROUNDS) { round ->
            val a = qaidaDrillRound(pool, seed = 7, round = round)
            assertEquals(a, qaidaDrillRound(pool, seed = 7, round = round))
            assertEquals(4, a.options.size)
            assertEquals(4, a.options.map { it.arabic }.distinct().size)
            assertTrue(a.target in a.options)
        }
    }

    @Test fun letterClipsAreGridAndAyahClipsAreRows() {
        assertTrue(qaidaLessonAudio("fatha").all { qaidaIsLetterClip(it) })
        assertTrue(qaidaLessonAudio("tanween").none { qaidaIsLetterClip(it) })
        assertTrue(qaidaLessonAudio("ikhfa").none { qaidaIsLetterClip(it) })
    }

    @Test fun lessonClipsAreWellFormed() {
        assertEquals(qaidaAlphabet.size, qaidaAlphabet.indices.map { qaidaLetterAudio(it).id }.distinct().size)
        qaidaLessons.forEach { lesson ->
            val clips = qaidaLessonAudio(lesson.id)
            assertEquals("Duplicate clip ids: ${lesson.id}", clips.size, clips.map { it.id }.distinct().size)
            assertTrue("Blank clip url: ${lesson.id}", clips.all { it.url.startsWith("https://") })
            assertTrue("Quiz answers out of range: ${lesson.id}", lesson.quizzes.isNotEmpty() && lesson.quizzes.all { it.answer in it.options.indices })
        }
    }
}
