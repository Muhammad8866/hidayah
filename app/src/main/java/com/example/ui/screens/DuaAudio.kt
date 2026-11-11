package com.example.ui.screens

import com.example.ui.tour.tourTarget

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.localization.LocalizedText
import java.util.concurrent.ExecutorService
import kotlinx.coroutines.flow.MutableStateFlow

internal data class DuaSpeechState(val ready: Boolean = false, val activeId: String? = null, val error: String? = null)

/**
 * Reads the actual Arabic text, so every bundled dua is covered without guessed recording URLs.
 * Session state lives on the main thread; every voice-engine call runs on [engine]'s own thread.
 */
internal class DuaSpeech(
    context: Context,
    factory: SpeechEngineFactory = deviceSpeechEngine,
    executor: ExecutorService = newSpeechExecutor()
) {
    val state = MutableStateFlow(DuaSpeechState())
    private val main = Handler(Looper.getMainLooper())
    private var closed = false
    private var generation = 0
    private var finalUtterance: String? = null
    private val audioManager = context.applicationContext.getSystemService(AudioManager::class.java)
    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        if (change < 0) main.post { if (!closed) stop() }
    }
    private val engine = SpeechEngineThread(context, object : SpeechProgress {
        override fun onStart(utteranceId: String?) = Unit
        override fun onDone(utteranceId: String?) { main.post {
            if (!closed && utteranceId == finalUtterance) { com.example.core.analytics.AppAnalytics.mediaState("dua_speech", "completed"); stop() }
        } }
        override fun onError(utteranceId: String?) { main.post {
            if (!closed && utteranceId?.startsWith("$generation:") == true) fail("playback_error")
        } }
    }, onReady = { supported ->
        if (!closed) {
            if (!supported) com.example.core.analytics.AppAnalytics.mediaState("dua_speech", "failed", "voice_unavailable")
            state.value = DuaSpeechState(ready = supported,
                error = if (supported) null else "Arabic voice unavailable. Install an Arabic text-to-speech voice in device settings.")
        }
    }, factory = factory, executor = executor)

    private fun fail(code: String) {
        com.example.core.analytics.AppAnalytics.mediaState("dua_speech", "failed", code)
        stop()
        state.value = state.value.copy(error = "Audio could not play. Try again.")
    }

    @Suppress("DEPRECATION")
    fun play(id: String, text: String, slow: Boolean) {
        if (!state.value.ready || closed) return
        if (state.value.activeId == id) { com.example.core.analytics.AppAnalytics.media("dua_speech", "stop"); stop(); return }
        stop()
        com.example.core.analytics.AppAnalytics.media("dua_speech", if (slow) "play_slow" else "play")
        com.example.core.analytics.AppAnalytics.content("dua_speech", id, "play")
        com.example.quran.QuranAudioManager.stop()
        com.example.quran.TafsirAudioManager.stop()
        com.example.naat.NaatAudioManager.stop()
        com.example.azan.AzanAudioManager.stop()
        if (audioManager?.requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            com.example.core.analytics.AppAnalytics.mediaState("dua_speech", "failed", "audio_focus")
            state.value = state.value.copy(error = "Audio could not play. Try again.")
            return
        }
        val rate = if (slow) .65f else .85f
        val chunks = splitDuaSpeech(speakableArabic(text), TextToSpeech.getMaxSpeechInputLength() - 1)
        if (chunks.isEmpty()) { stop(); return }
        val session = generation
        finalUtterance = "$session:${chunks.lastIndex}"
        state.value = state.value.copy(activeId = id, error = null)
        engine.call { tts ->
            tts.setSpeechRate(rate)
            for ((index, chunk) in chunks.withIndex()) {
                if (tts.speak(chunk, "$session:$index") != TextToSpeech.SUCCESS) {
                    // A newer session (or close) already replaced this one; its failure is stale.
                    main.post { if (!closed && generation == session) fail("speak_error") }
                    break
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    fun stop() {
        generation++
        finalUtterance = null
        engine.call { it.stop() }
        audioManager?.abandonAudioFocus(focusListener)
        state.value = state.value.copy(activeId = null)
    }

    /** Never blocks: the engine is stopped and shut down on its own thread. */
    fun close() {
        if (closed) return
        closed = true
        stop()
        engine.close()
    }
}

/**
 * The displayed Arabic as device voices read it correctly: Quranic orthography (superscript alef,
 * alef wasla, pause and small high marks, tatweel) in standard spelling, ayah numbers removed and
 * ﷺ spelled out. Harakat stay, since they carry the pronunciation.
 */
internal fun speakableArabic(text: String): String = text
    .replace("ﷺ", "صَلَّى اللهُ عَلَيْهِ وَسَلَّمَ")
    .replace(Regex("""[﴿(\[]\s*[0-9٠-٩۰-۹]+\s*[﴾)\]]"""), " ")
    .replace(Regex("""[0-9٠-٩۰-۹ـٰۖ-ۭ]"""), "")
    .replace('ٱ', 'ا')
    .replace(Regex("""\s+"""), " ")
    .trim()

internal fun splitDuaSpeech(text: String, limit: Int): List<String> {
    require(limit > 0)
    val chunks = mutableListOf<String>()
    var remaining = text.trim()
    while (remaining.length > limit) {
        val boundary = remaining.lastIndexOf(' ', limit).takeIf { it > 0 } ?: limit
        chunks += remaining.substring(0, boundary)
        remaining = remaining.substring(boundary).trimStart()
    }
    if (remaining.isNotEmpty()) chunks += remaining
    return chunks
}

@Composable
internal fun rememberDuaSpeech(): DuaSpeech {
    val context = LocalContext.current
    val audio = remember(context) { DuaSpeech(context) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(audio, owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) audio.stop() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); audio.close() }
    }
    return audio
}

@Composable
internal fun DuaAudioControls(id: String, arabic: String, audio: DuaSpeech, modifier: Modifier = Modifier) {
    val state by audio.state.collectAsState()
    var slow by remember(id) { mutableStateOf(false) }
    val context = LocalContext.current
    Column(modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { audio.play(id, arabic, slow) }, enabled = state.ready,
                modifier = Modifier.testTag("dua_audio_$id").tourTarget("duas_voice_listen")) {
                LocalizedText(if (state.activeId == id) "Stop" else "Listen")
            }
            FilterChip(selected = slow, onClick = {
                com.example.core.analytics.AppAnalytics.setting("dua_speech_slow", (!slow).toString())
                if (state.activeId == id) audio.stop(); slow = !slow
            },
                label = { LocalizedText("Slow") }, modifier = Modifier.tourTarget("duas_voice_slow"))
        }
        LocalizedText("Arabic device voice", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        state.error?.let {
            LocalizedText(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            if (!state.ready) TextButton(modifier = Modifier.tourTarget("duas_voice_settings"), onClick = {
                com.example.core.analytics.AppAnalytics.media("dua_speech", "open_tts_settings")
                val intent = Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                runCatching { context.startActivity(intent) }
                    .recoverCatching { context.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            }) { LocalizedText("Settings") }
        }
    }
}

