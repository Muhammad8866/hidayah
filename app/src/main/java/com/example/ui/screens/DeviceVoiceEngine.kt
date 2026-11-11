package com.example.ui.screens

import android.content.Context
import android.media.AudioAttributes
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException

/** Utterance progress from the voice engine, reported on one of its binder threads. */
internal interface SpeechProgress {
    fun onStart(utteranceId: String?)
    fun onDone(utteranceId: String?)
    fun onError(utteranceId: String?)
}

/**
 * The TextToSpeech calls the device-voice players make. Each one is a binder call into the voice
 * engine and can stall for seconds while it loads a voice or holds its setup lock, so they run
 * only on a [SpeechEngineThread], never on the main thread.
 */
internal interface SpeechEngine {
    /** Selects Arabic and the best offline Arabic voice; false when the engine has no Arabic. */
    fun useArabic(): Boolean
    fun setSpeechRate(rate: Float)
    fun speak(text: String, utteranceId: String): Int
    fun playSilence(durationMs: Long, utteranceId: String): Int
    fun stop()
    fun shutdown()
}

/** Creates an engine; [onInit] receives the TextToSpeech init status on any thread. */
internal fun interface SpeechEngineFactory {
    fun create(context: Context, progress: SpeechProgress, onInit: (status: Int) -> Unit): SpeechEngine
}

internal val deviceSpeechEngine = SpeechEngineFactory { context, progress, onInit ->
    AndroidSpeechEngine(context, progress, onInit)
}

/** One serial background thread per player; a daemon, so a stuck engine never keeps the app alive. */
internal fun newSpeechExecutor(): ExecutorService = Executors.newSingleThreadExecutor { task ->
    Thread(task, "device-voice").apply { isDaemon = true }
}

private class AndroidSpeechEngine(
    context: Context,
    progress: SpeechProgress,
    onInit: (Int) -> Unit
) : SpeechEngine {
    private val tts = TextToSpeech(context.applicationContext) { status -> onInit(status) }.apply {
        setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = progress.onStart(utteranceId)
            override fun onDone(utteranceId: String?) = progress.onDone(utteranceId)
            @Deprecated("Required by Android")
            override fun onError(utteranceId: String?) = progress.onError(utteranceId)
        })
    }

    override fun useArabic(): Boolean {
        if (tts.setLanguage(Locale.forLanguageTag("ar")) < TextToSpeech.LANG_AVAILABLE) return false
        tts.voices?.filter { it.locale.language == "ar" && !it.isNetworkConnectionRequired }
            ?.maxByOrNull { it.quality }?.let { tts.setVoice(it) }
        tts.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
        return true
    }

    override fun setSpeechRate(rate: Float) { tts.setSpeechRate(rate) }
    // A failing engine reports ERROR, so the player shows its error instead of staying "playing".
    override fun speak(text: String, utteranceId: String): Int =
        runCatching { tts.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId) }.getOrDefault(TextToSpeech.ERROR)
    override fun playSilence(durationMs: Long, utteranceId: String): Int =
        runCatching { tts.playSilentUtterance(durationMs, TextToSpeech.QUEUE_ADD, utteranceId) }.getOrDefault(TextToSpeech.ERROR)
    override fun stop() { tts.stop() }
    override fun shutdown() { tts.shutdown() }
}

/**
 * Owns one voice engine and the single background thread that creates it and makes every call to
 * it, in the order they were asked for. [onReady] reaches the main thread once Arabic is set up.
 */
internal class SpeechEngineThread(
    context: Context,
    progress: SpeechProgress,
    onReady: (supported: Boolean) -> Unit,
    factory: SpeechEngineFactory = deviceSpeechEngine,
    private val executor: ExecutorService = newSpeechExecutor()
) {
    private val main = Handler(Looper.getMainLooper())
    /** Only touched on [executor]'s thread. */
    private var engine: SpeechEngine? = null

    init {
        val app = context.applicationContext
        post {
            engine = runCatching {
                factory.create(app, progress) { status ->
                    // The init status arrives on the main thread; Arabic setup is binder work too.
                    post {
                        val supported = status == TextToSpeech.SUCCESS &&
                            runCatching { engine?.useArabic() == true }.getOrDefault(false)
                        main.post { onReady(supported) }
                    }
                }
            }.getOrNull()
            if (engine == null) main.post { onReady(false) }
        }
    }

    /** Runs [action] on the engine thread; dropped after [close] or when the engine never started. */
    fun call(action: (SpeechEngine) -> Unit) = post { engine?.let(action) }

    /** Queues stop and shutdown behind any pending call, then lets the thread end; never waits. */
    fun close() {
        post {
            val closing = engine
            engine = null
            closing?.let { runCatching { it.stop() }; it.shutdown() }
        }
        executor.shutdown()
    }

    private fun post(task: () -> Unit) {
        try {
            executor.execute {
                // A misbehaving engine must not take the app down from a background thread.
                try { task() } catch (_: RuntimeException) { }
            }
        } catch (_: RejectedExecutionException) {
            // Closed: the call is dropped, as calls on a shut-down engine always were.
        }
    }
}
