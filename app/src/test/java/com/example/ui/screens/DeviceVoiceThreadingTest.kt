package com.example.ui.screens

import android.content.Context
import android.os.Looper
import android.speech.tts.TextToSpeech
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Every TextToSpeech call is a binder call that can stall while the voice engine loads, which
 * froze the adhkar screen (ANR in TextToSpeech.setVoice and behind the engine's setup lock).
 * These tests pin that the device-voice players never touch the engine from the main thread.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DeviceVoiceThreadingTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainThread: Thread = Looper.getMainLooper().thread
    @Volatile private var engine: FakeEngine? = null
    @Volatile private var createdOn: Thread? = null
    private val factory = SpeechEngineFactory { _, progress, onInit ->
        createdOn = Thread.currentThread()
        FakeEngine(progress, onInit).also { engine = it }
    }

    @After
    fun tearDown() {
        engine?.stopGate?.countDown()
        executor.shutdownNow()
    }

    private class FakeEngine(val progress: SpeechProgress, val onInit: (Int) -> Unit) : SpeechEngine {
        val calls = CopyOnWriteArrayList<Pair<String, Thread>>()
        @Volatile var arabic = true
        @Volatile var speakResult: (String) -> Int = { TextToSpeech.SUCCESS }
        @Volatile var stopGate: CountDownLatch? = null

        private fun record(call: String) { calls += call to Thread.currentThread() }
        fun names() = calls.map { it.first }
        fun spokenIds() = names().filter { it.startsWith("speak|") }.map { it.split('|')[1] }

        override fun useArabic(): Boolean { record("useArabic"); return arabic }
        override fun setSpeechRate(rate: Float) = record("rate|$rate")
        override fun speak(text: String, utteranceId: String): Int { record("speak|$utteranceId|$text"); return speakResult(text) }
        override fun playSilence(durationMs: Long, utteranceId: String): Int {
            record("silence|$durationMs|$utteranceId"); return TextToSpeech.SUCCESS
        }
        override fun stop() { record("stop"); stopGate?.await(10, TimeUnit.SECONDS) }
        override fun shutdown() = record("shutdown")
    }

    /** Lets the engine thread finish its queue, then runs what it handed back to the main thread. */
    private fun settle() = repeat(3) {
        if (executor.isShutdown) assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS))
        else executor.submit(Runnable {}).get(5, TimeUnit.SECONDS)
        shadowOf(Looper.getMainLooper()).idle()
    }

    /** The real engine reports its init status on the main thread. */
    private fun initEngine(status: Int = TextToSpeech.SUCCESS) {
        settle()
        requireNotNull(engine).onInit(status)
        settle()
    }

    private fun assertNoEngineCallOnMain() {
        assertNotEquals("engine created on the main thread", mainThread, createdOn)
        val onMain = requireNotNull(engine).calls.filter { it.second == mainThread }.map { it.first }
        assertTrue("engine calls on the main thread: $onMain", onMain.isEmpty())
    }

    private fun duaSpeech() = DuaSpeech(context, factory, executor)

    // DuaSpeech

    @Test
    fun duaEngineIsCreatedSetUpUsedStoppedAndShutDownOffTheMainThread() {
        val speech = duaSpeech()
        initEngine()
        assertTrue(speech.state.value.ready)
        speech.play("a", "بِسْمِ اللهِ", slow = true)
        assertEquals("a", speech.state.value.activeId)
        settle()
        speech.stop()
        speech.close()
        settle()
        val names = requireNotNull(engine).names()
        assertTrue(names.containsAll(listOf("useArabic", "rate|0.65", "stop", "shutdown")))
        assertEquals(1, requireNotNull(engine).spokenIds().size)
        assertEquals("shutdown", names.last())
        assertNoEngineCallOnMain()
    }

    @Test
    fun duaCloseReturnsAtOnceWhileTheEngineIsStuck() {
        val speech = duaSpeech()
        initEngine()
        speech.play("a", "بِسْمِ اللهِ", slow = false)
        settle()
        val gate = CountDownLatch(1)
        requireNotNull(engine).stopGate = gate
        speech.stop() // the engine thread now hangs inside stop()
        val started = System.nanoTime()
        speech.close()
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started)
        assertTrue("close() blocked the main thread for $tookMs ms", tookMs < 1_000)
        assertNull(speech.state.value.activeId)
        assertFalse(requireNotNull(engine).names().contains("shutdown"))
        gate.countDown()
        settle()
        assertEquals("shutdown", requireNotNull(engine).names().last())
        assertNoEngineCallOnMain()
    }

    @Test
    fun duaCallsAfterCloseAreIgnored() {
        val speech = duaSpeech()
        initEngine()
        speech.close()
        settle()
        val before = requireNotNull(engine).calls.size
        speech.play("a", "بِسْمِ اللهِ", slow = false)
        speech.stop()
        speech.close()
        settle()
        assertEquals(before, requireNotNull(engine).calls.size)
        assertNull(speech.state.value.activeId)
    }

    @Test
    fun duaPlayBeforeTheVoiceIsReadyDoesNothing() {
        val speech = duaSpeech()
        settle()
        speech.play("a", "بِسْمِ اللهِ", slow = false)
        settle()
        assertFalse(speech.state.value.ready)
        assertNull(speech.state.value.activeId)
        assertTrue(requireNotNull(engine).spokenIds().isEmpty())
    }

    @Test
    fun duaWithoutAnArabicVoiceIsUnavailable() {
        val speech = duaSpeech()
        settle()
        requireNotNull(engine).arabic = false
        initEngine()
        assertFalse(speech.state.value.ready)
        assertTrue(speech.state.value.error.orEmpty().startsWith("Arabic voice unavailable"))
        assertNoEngineCallOnMain()
    }

    @Test
    fun duaEngineThatFailedToStartIsUnavailableAndNeverSetUp() {
        val speech = duaSpeech()
        initEngine(TextToSpeech.ERROR)
        assertFalse(speech.state.value.ready)
        assertNotNull(speech.state.value.error)
        assertFalse(requireNotNull(engine).names().contains("useArabic"))
    }

    @Test
    fun duaEngineThatThrowsDuringSetUpIsUnavailableInsteadOfCrashing() {
        val speech = DuaSpeech(context, { _, progress, onInit ->
            createdOn = Thread.currentThread()
            object : SpeechEngine by FakeEngine(progress, onInit).also { engine = it } {
                override fun useArabic(): Boolean = throw IllegalStateException("engine died")
            }
        }, executor)
        initEngine()
        assertFalse(speech.state.value.ready)
        assertNotNull(speech.state.value.error)
    }

    @Test
    fun duaBlankTextIsNotSpoken() {
        val speech = duaSpeech()
        initEngine()
        speech.play("a", "   ", slow = false)
        settle()
        assertNull(speech.state.value.activeId)
        assertTrue(requireNotNull(engine).spokenIds().isEmpty())
    }

    @Test
    fun duaFinalUtteranceEndsTheSessionAndStaleCallbacksAreIgnored() {
        val speech = duaSpeech()
        initEngine()
        speech.play("a", "بِسْمِ اللهِ", slow = false)
        settle()
        val id = requireNotNull(engine).spokenIds().single()
        requireNotNull(engine).progress.onDone("0:0") // from a session that already ended
        settle()
        assertEquals("a", speech.state.value.activeId)
        requireNotNull(engine).progress.onDone(id)
        settle()
        assertNull(speech.state.value.activeId)
        assertNoEngineCallOnMain()
    }

    @Test
    fun duaTappingThePlayingDuaAgainStopsIt() {
        val speech = duaSpeech()
        initEngine()
        speech.play("a", "بِسْمِ اللهِ", slow = false)
        speech.play("a", "بِسْمِ اللهِ", slow = false)
        settle()
        assertNull(speech.state.value.activeId)
    }

    @Test
    fun duaSpeakFailureStopsAndShowsAnError() {
        val speech = duaSpeech()
        initEngine()
        requireNotNull(engine).speakResult = { TextToSpeech.ERROR }
        speech.play("a", "بِسْمِ اللهِ", slow = false)
        settle()
        assertNull(speech.state.value.activeId)
        assertEquals("Audio could not play. Try again.", speech.state.value.error)
    }

    @Test
    fun duaFailureFromAnOlderSessionDoesNotStopTheNewOne() {
        val speech = duaSpeech()
        initEngine()
        requireNotNull(engine).speakResult = { text -> if (text == "اول") TextToSpeech.ERROR else TextToSpeech.SUCCESS }
        speech.play("a", "اول", slow = false)
        speech.play("b", "ثاني", slow = false)
        settle()
        assertEquals("b", speech.state.value.activeId)
        assertNull(speech.state.value.error)
    }

    // KalimaAudio

    private fun kalimaEntry(vararg phrases: String) = KalimaEntry(
        "k1", "K", phrases.joinToString(" "), "r", "u", "e", KalimaGroup.KALIMA, 1, "", "", "",
        phrases.map { KalimaPhrase(it, "", "", "") }, "", "", ""
    )

    @Test
    fun kalimaSpeaksPhraseByPhraseOffTheMainThread() {
        val audio = KalimaAudio(context, factory, executor)
        initEngine()
        assertTrue(audio.state.value.ready)
        audio.play(kalimaEntry("لا إله", "إلا الله"))
        settle()
        val first = requireNotNull(engine).spokenIds().single()
        requireNotNull(engine).progress.onStart(first)
        settle()
        assertEquals(0, audio.state.value.activePhrase)
        requireNotNull(engine).progress.onDone(first)
        settle()
        val ids = requireNotNull(engine).spokenIds()
        assertEquals(2, ids.size)
        assertTrue(requireNotNull(engine).names().any { it.startsWith("silence|$KALIMA_PHRASE_GAP_MS|") })
        requireNotNull(engine).progress.onDone(ids[1])
        settle()
        assertNull(audio.state.value.activeId)
        audio.close()
        settle()
        assertEquals("shutdown", requireNotNull(engine).names().last())
        assertNoEngineCallOnMain()
    }

    @Test
    fun kalimaCloseReturnsAtOnceAndLaterCallsAreIgnored() {
        val audio = KalimaAudio(context, factory, executor)
        initEngine()
        audio.play(kalimaEntry("لا إله"))
        settle()
        val gate = CountDownLatch(1)
        requireNotNull(engine).stopGate = gate
        audio.stop()
        val started = System.nanoTime()
        audio.close()
        assertTrue(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started) < 1_000)
        gate.countDown()
        settle()
        val before = requireNotNull(engine).calls.size
        audio.play(kalimaEntry("لا إله"))
        audio.playAll(listOf(kalimaEntry("لا إله")))
        audio.stop()
        settle()
        assertEquals(before, requireNotNull(engine).calls.size)
        assertNull(audio.state.value.activeId)
        assertNoEngineCallOnMain()
    }

    @Test
    fun kalimaSpeakFailureShowsAnErrorAndEmptyInputIsIgnored() {
        val audio = KalimaAudio(context, factory, executor)
        initEngine()
        audio.playAll(emptyList())
        audio.playPhrase(kalimaEntry("لا إله"), index = 5)
        settle()
        assertTrue(requireNotNull(engine).spokenIds().isEmpty())
        requireNotNull(engine).speakResult = { TextToSpeech.ERROR }
        audio.play(kalimaEntry("لا إله"))
        settle()
        assertNull(audio.state.value.activeId)
        assertEquals("Audio could not play. Try again.", audio.state.value.error)
        assertNoEngineCallOnMain()
    }
}
