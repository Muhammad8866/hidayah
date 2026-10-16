package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow

internal data class DuaSpeechState(val ready: Boolean = false, val activeId: String? = null, val error: String? = null)

/** Reads the actual Arabic text, so every bundled dua is covered without guessed recording URLs. */
internal class DuaSpeech(context: Context) {
    val state = MutableStateFlow(DuaSpeechState())
    private val main = Handler(Looper.getMainLooper())
    private var closed = false
    private var generation = 0
    private var engine: TextToSpeech? = null
    private var finalUtterance: String? = null
    private val audioManager = context.applicationContext.getSystemService(AudioManager::class.java)
    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        if (change < 0) main.post { if (!closed) stop() }
    }

    init {
        engine = TextToSpeech(context.applicationContext) { status ->
            main.post {
                if (!closed) {
                    val tts = engine
                    val supported = status == TextToSpeech.SUCCESS && tts != null &&
                        tts.setLanguage(Locale.forLanguageTag("ar")) >= TextToSpeech.LANG_AVAILABLE
                    if (supported) {
                        tts?.voices?.filter { it.locale.language == "ar" && !it.isNetworkConnectionRequired }
                            ?.maxByOrNull { it.quality }?.let { tts?.setVoice(it) }
                        tts?.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                    }
                    state.value = DuaSpeechState(ready = supported,
                        error = if (supported) null else "Arabic voice unavailable. Install an Arabic text-to-speech voice in device settings.")
                }
            }
        }
        engine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit
            override fun onDone(utteranceId: String?) { main.post {
                if (!closed && utteranceId == finalUtterance) stop()
            } }
            @Deprecated("Required by Android")
            override fun onError(utteranceId: String?) { main.post {
                if (!closed && utteranceId?.startsWith("$generation:") == true) {
                    stop()
                    state.value = state.value.copy(error = "Audio could not play. Try again.")
                }
            } }
        })
    }

    @Suppress("DEPRECATION")
    fun play(id: String, text: String, slow: Boolean) {
        if (!state.value.ready || closed) return
        if (state.value.activeId == id) { stop(); return }
        stop()
        com.example.quran.QuranAudioManager.stop()
        com.example.quran.TafsirAudioManager.stop()
        com.example.naat.NaatAudioManager.stop()
        com.example.azan.AzanAudioManager.stop()
        if (audioManager?.requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            state.value = state.value.copy(error = "Audio could not play. Try again.")
            return
        }
        engine?.setSpeechRate(if (slow) .65f else .85f)
        val chunks = splitDuaSpeech(text, TextToSpeech.getMaxSpeechInputLength() - 1)
        if (chunks.isEmpty()) { stop(); return }
        finalUtterance = "$generation:${chunks.lastIndex}"
        state.value = state.value.copy(activeId = id, error = null)
        for ((index, chunk) in chunks.withIndex()) {
            if (engine?.speak(chunk, TextToSpeech.QUEUE_ADD, null, "$generation:$index") != TextToSpeech.SUCCESS) {
                stop()
                state.value = state.value.copy(error = "Audio could not play. Try again.")
                break
            }
        }
    }

    @Suppress("DEPRECATION")
    fun stop() {
        generation++
        finalUtterance = null
        engine?.stop()
        audioManager?.abandonAudioFocus(focusListener)
        state.value = state.value.copy(activeId = null)
    }
    fun close() { closed = true; stop(); engine?.shutdown(); engine = null }
}

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
                modifier = Modifier.testTag("dua_audio_$id")) {
                LocalizedText(if (state.activeId == id) "Stop" else "Listen")
            }
            FilterChip(selected = slow, onClick = { if (state.activeId == id) audio.stop(); slow = !slow },
                label = { LocalizedText("Slow") })
        }
        LocalizedText("Arabic device voice", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        state.error?.let {
            LocalizedText(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            if (!state.ready) TextButton(onClick = {
                val intent = Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                runCatching { context.startActivity(intent) }
                    .recoverCatching { context.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            }) { LocalizedText("Settings") }
        }
    }
}

