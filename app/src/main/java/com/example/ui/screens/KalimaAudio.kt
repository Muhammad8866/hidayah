package com.example.ui.screens

import com.example.ui.tour.tourTarget

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.localization.LocalizedText
import java.util.concurrent.ExecutorService
import kotlinx.coroutines.flow.MutableStateFlow

internal data class KalimaAudioState(
    val ready: Boolean = false,
    val activeId: String? = null,
    val activePhrase: Int = -1,
    val repeatsLeft: Int = 0,
    val playingAll: Boolean = false,
    val slow: Boolean = false,
    val error: String? = null
)

/** One spoken piece: a phrase of an entry (or a chunk of it when the phrase exceeds the engine limit). */
internal data class KalimaUtterance(
    val entryIndex: Int, val pass: Int, val phraseIndex: Int, val chunk: Int, val text: String
)

internal data class KalimaUtteranceRef(
    val generation: Int, val entryIndex: Int, val pass: Int, val phraseIndex: Int, val chunk: Int
)

internal const val KALIMA_PHRASE_GAP_MS = 350L
internal const val KALIMA_ENTRY_GAP_MS = 900L
internal val KALIMA_REPEAT_OPTIONS = listOf(1, 3, 5, 10)

/** Arabic text spoken for each phrase index; falls back to the whole entry when it has no phrases. */
internal fun kalimaSpeechTexts(entry: KalimaEntry): List<String> =
    entry.phrases.map { it.arabic }.ifEmpty { listOf(entry.arabic) }

/**
 * Orders speech for [entries] (each a list of phrase texts) from [startIndex]: every entry is
 * spoken [repeat] times, phrase by phrase, before moving to the next entry.
 */
internal fun planKalimaQueue(
    entries: List<List<String>>, repeat: Int = 1, startIndex: Int = 0, limit: Int = Int.MAX_VALUE
): List<KalimaUtterance> {
    val passes = repeat.coerceAtLeast(1)
    val queue = mutableListOf<KalimaUtterance>()
    for (entryIndex in startIndex.coerceAtLeast(0) until entries.size) {
        for (pass in 0 until passes) {
            for ((phraseIndex, phrase) in entries[entryIndex].withIndex()) {
                for ((chunk, text) in splitDuaSpeech(phrase, limit).withIndex()) {
                    queue += KalimaUtterance(entryIndex, pass, phraseIndex, chunk, text)
                }
            }
        }
    }
    return queue
}

internal fun kalimaUtteranceId(generation: Int, utterance: KalimaUtterance): String =
    "$generation:${utterance.entryIndex}:${utterance.pass}:${utterance.phraseIndex}:${utterance.chunk}"

/** Parses `generation:entryIndex:pass:phraseIndex[:chunk]`; silence gaps and foreign ids give null. */
internal fun parseKalimaUtteranceId(id: String?): KalimaUtteranceRef? {
    val parts = id?.split(':')?.map { it.toIntOrNull() ?: return null } ?: return null
    if (parts.size !in 4..5 || parts.any { it < 0 }) return null
    return KalimaUtteranceRef(parts[0], parts[1], parts[2], parts[3], parts.getOrElse(4) { 0 })
}

/** Silence inserted before [next]: none inside a split phrase, short between phrases, longer between passes/entries. */
internal fun kalimaGapMs(previous: KalimaUtterance, next: KalimaUtterance): Long = when {
    previous.entryIndex != next.entryIndex || previous.pass != next.pass -> KALIMA_ENTRY_GAP_MS
    previous.phraseIndex != next.phraseIndex -> KALIMA_PHRASE_GAP_MS
    else -> 0L
}

/**
 * Phrase-by-phrase reading of the bundled Arabic with the device voice; no guessed recording URLs.
 * Session state lives on the main thread; every voice-engine call runs on [engine]'s own thread.
 */
internal class KalimaAudio(
    context: Context,
    factory: SpeechEngineFactory = deviceSpeechEngine,
    executor: ExecutorService = newSpeechExecutor()
) {
    val state = MutableStateFlow(KalimaAudioState())
    private val main = Handler(Looper.getMainLooper())
    private var closed = false
    private var generation = 0
    private var queue: List<KalimaUtterance> = emptyList()
    private var sessionIds: List<String> = emptyList()
    private var position = 0
    private var repeatTotal = 1
    private var singlePhrase = false
    private val audioManager = context.applicationContext.getSystemService(AudioManager::class.java)
    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        if (change < 0) main.post { if (!closed) stop() }
    }

    private val engine = SpeechEngineThread(context, object : SpeechProgress {
        override fun onStart(utteranceId: String?) { main.post {
            val current = currentFor(utteranceId)
            if (current != null) state.value = state.value.copy(
                activeId = sessionIds.getOrNull(current.entryIndex),
                activePhrase = current.phraseIndex,
                repeatsLeft = (repeatTotal - 1 - current.pass).coerceAtLeast(0))
        } }
        override fun onDone(utteranceId: String?) { main.post {
            if (currentFor(utteranceId) != null) {
                if (position >= queue.lastIndex) { com.example.core.analytics.AppAnalytics.mediaState("kalima_speech", "completed"); stop() } else { position++; speakCurrent() }
            }
        } }
        override fun onError(utteranceId: String?) { main.post {
            if (!closed && parseKalimaUtteranceId(utteranceId)?.generation == generation) fail()
        } }
    }, onReady = { supported ->
        if (!closed) {
            if (!supported) com.example.core.analytics.AppAnalytics.mediaState("kalima_speech", "failed", "voice_unavailable")
            state.value = state.value.copy(ready = supported,
                error = if (supported) null else "Arabic voice unavailable. Install an Arabic text-to-speech voice in device settings.")
        }
    }, factory = factory, executor = executor)

    /** The queued utterance [utteranceId] refers to, only when it is the one this session is waiting on. */
    private fun currentFor(utteranceId: String?): KalimaUtterance? {
        if (closed) return null
        val ref = parseKalimaUtteranceId(utteranceId) ?: return null
        val current = queue.getOrNull(position) ?: return null
        return current.takeIf {
            ref.generation == generation && ref.entryIndex == it.entryIndex && ref.pass == it.pass &&
                ref.phraseIndex == it.phraseIndex && ref.chunk == it.chunk
        }
    }

    private fun limit() = TextToSpeech.getMaxSpeechInputLength() - 1

    fun play(entry: KalimaEntry, repeat: Int = 1) {
        val current = state.value
        if (current.activeId == entry.id && !current.playingAll && !singlePhrase) { com.example.core.analytics.AppAnalytics.media("kalima_speech", "stop"); stop(); return }
        com.example.core.analytics.AppAnalytics.media("kalima_speech", "play")
        com.example.core.analytics.AppAnalytics.content("kalima_speech", entry.id, "play")
        val passes = repeat.coerceAtLeast(1)
        start(listOf(entry.id), planKalimaQueue(listOf(kalimaSpeechTexts(entry)), passes, limit = limit()),
            passes, playingAll = false, single = false)
    }

    fun playPhrase(entry: KalimaEntry, index: Int) {
        val current = state.value
        if (singlePhrase && current.activeId == entry.id && current.activePhrase == index) { com.example.core.analytics.AppAnalytics.media("kalima_speech", "stop"); stop(); return }
        com.example.core.analytics.AppAnalytics.media("kalima_speech", "play_phrase")
        val text = kalimaSpeechTexts(entry).getOrNull(index) ?: return
        val plan = splitDuaSpeech(text, limit()).mapIndexed { chunk, part -> KalimaUtterance(0, 0, index, chunk, part) }
        start(listOf(entry.id), plan, 1, playingAll = false, single = true)
    }

    fun playAll(entries: List<KalimaEntry>, startId: String? = null) {
        if (entries.isEmpty()) return
        com.example.core.analytics.AppAnalytics.media("kalima_speech", "play_all")
        val startIndex = entries.indexOfFirst { it.id == startId }.coerceAtLeast(0)
        start(entries.map { it.id }, planKalimaQueue(entries.map(::kalimaSpeechTexts), 1, startIndex, limit()),
            1, playingAll = true, single = false)
    }

    fun setSlow(slow: Boolean) { com.example.core.analytics.AppAnalytics.setting("kalima_speech_slow", slow.toString()); state.value = state.value.copy(slow = slow) }

    @Suppress("DEPRECATION")
    private fun start(ids: List<String>, plan: List<KalimaUtterance>, repeat: Int, playingAll: Boolean, single: Boolean) {
        if (!state.value.ready || closed) return
        stop()
        if (plan.isEmpty()) return
        com.example.quran.QuranAudioManager.stop()
        com.example.quran.TafsirAudioManager.stop()
        com.example.naat.NaatAudioManager.stop()
        com.example.azan.AzanAudioManager.stop()
        if (audioManager?.requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            com.example.core.analytics.AppAnalytics.mediaState("kalima_speech", "failed", "audio_focus")
            state.value = state.value.copy(error = "Audio could not play. Try again.")
            return
        }
        queue = plan
        sessionIds = ids
        position = 0
        repeatTotal = repeat
        singlePhrase = single
        state.value = state.value.copy(activeId = ids.getOrNull(plan[0].entryIndex), activePhrase = plan[0].phraseIndex,
            repeatsLeft = repeat - 1, playingAll = playingAll, error = null)
        speakCurrent()
    }

    /** Speaks one utterance at a time so a speed change applies from the next phrase. */
    private fun speakCurrent() {
        val utterance = queue.getOrNull(position)
        if (closed || utterance == null) { stop(); return }
        val rate = if (state.value.slow) .65f else .85f
        val gap = if (position > 0) kalimaGapMs(queue[position - 1], utterance) else 0L
        val session = generation
        val id = kalimaUtteranceId(session, utterance)
        engine.call { tts ->
            tts.setSpeechRate(rate)
            if (gap > 0) tts.playSilence(gap, "$session:gap")
            // A newer session (or close) already replaced this one; its failure is stale.
            if (tts.speak(utterance.text, id) != TextToSpeech.SUCCESS) main.post { if (!closed && generation == session) fail() }
        }
    }

    private fun fail() {
        com.example.core.analytics.AppAnalytics.mediaState("kalima_speech", "failed", "playback_error")
        stop()
        state.value = state.value.copy(error = "Audio could not play. Try again.")
    }

    @Suppress("DEPRECATION")
    fun stop() {
        generation++
        queue = emptyList()
        sessionIds = emptyList()
        position = 0
        singlePhrase = false
        engine.call { it.stop() }
        audioManager?.abandonAudioFocus(focusListener)
        state.value = state.value.copy(activeId = null, activePhrase = -1, repeatsLeft = 0, playingAll = false)
    }

    /** Never blocks: the engine is stopped and shut down on its own thread. */
    fun close() {
        if (closed) return
        closed = true
        stop()
        engine.close()
    }
}

@Composable
internal fun rememberKalimaAudio(): KalimaAudio {
    val context = LocalContext.current
    val audio = remember(context) { KalimaAudio(context) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(audio, owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) audio.stop() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); audio.close() }
    }
    return audio
}

/**
 * Reader audio controls in one calm row: a large Listen/Stop button, a "Slow" toggle and a repeat
 * button that cycles through [KALIMA_REPEAT_OPTIONS]. The voice-missing hint appears only on error.
 */
@Composable
internal fun KalimaAudioControls(entry: KalimaEntry, audio: KalimaAudio, modifier: Modifier = Modifier) {
    val state by audio.state.collectAsState()
    var repeat by rememberSaveable(entry.id) { mutableStateOf(1) }
    val active = state.activeId == entry.id && !state.playingAll
    val context = LocalContext.current
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { if (active) audio.stop() else audio.play(entry, repeat) },
                enabled = state.ready,
                modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("kalima_audio_${entry.id}").tourTarget("kalima_audio_listen")
            ) {
                Icon(if (active) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null,
                    modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                LocalizedText(if (active) "Stop" else "Listen", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            FilledTonalIconToggleButton(
                checked = state.slow,
                onCheckedChange = { audio.setSlow(it) },
                modifier = Modifier.size(52.dp).testTag("kalima_slow_${entry.id}").tourTarget("kalima_audio_slow")
            ) {
                Icon(Icons.Default.SlowMotionVideo, contentDescription = com.example.ui.localization.localizedUiText("Slow"))
            }
            // Cycles 1× → 3× → 5× → 10×; changing it stops the current recitation so the new count applies.
            FilledTonalButton(
                onClick = {
                    if (active) audio.stop()
                    val next = KALIMA_REPEAT_OPTIONS.indexOf(repeat) + 1
                    repeat = KALIMA_REPEAT_OPTIONS[next % KALIMA_REPEAT_OPTIONS.size]
                },
                modifier = Modifier.heightIn(min = 52.dp).testTag("kalima_repeat_${entry.id}").tourTarget("kalima_audio_repeat"),
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                Icon(Icons.Default.Repeat, contentDescription = com.example.ui.localization.localizedUiText("Repeat"),
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(repeat.toString() + "×", fontWeight = FontWeight.Bold)
            }
        }
        state.error?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LocalizedText(it, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                if (!state.ready) TextButton(modifier = Modifier.tourTarget("kalima_audio_voice_settings"), onClick = {
                    com.example.core.analytics.AppAnalytics.media("kalima_speech", "open_tts_settings")
                    val intent = Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    runCatching { context.startActivity(intent) }
                        .recoverCatching { context.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                }) { LocalizedText("Settings") }
            }
        }
    }
}

/** Mini-player shown only while something is being recited: what is playing, progress and Stop. */
@Composable
internal fun KalimaNowPlayingBar(audio: KalimaAudio, entries: List<KalimaEntry>, modifier: Modifier = Modifier) {
    val state by audio.state.collectAsState()
    val active = entries.firstOrNull { it.id == state.activeId }
    androidx.compose.animation.AnimatedVisibility(
        visible = active != null,
        enter = androidx.compose.animation.slideInVertically { it } + androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.slideOutVertically { it } + androidx.compose.animation.fadeOut(),
        modifier = modifier
    ) {
        val current = active ?: return@AnimatedVisibility
        val total = kalimaSpeechTexts(current).size
        Surface(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag("kalima_now_playing").tourTarget("kalima_now_playing"),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface, shadowElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    com.example.ui.components.IconBadge(Icons.Default.GraphicEq, MaterialTheme.colorScheme.primary,
                        size = 40.dp, iconSize = 22.dp, filled = true)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(current.title, style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val detail = buildList {
                            if (total > 0 && state.activePhrase >= 0) add("${state.activePhrase + 1}/$total")
                            if (state.repeatsLeft > 0) add("+${state.repeatsLeft}×")
                        }.joinToString("  ·  ")
                        if (detail.isNotEmpty()) Text(detail, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    FilledTonalButton(onClick = { com.example.core.analytics.AppAnalytics.media("kalima_speech", "stop"); audio.stop() }, modifier = Modifier.heightIn(min = 48.dp).testTag("kalima_stop_all").tourTarget("kalima_stop_all")) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        LocalizedText("Stop")
                    }
                }
                if (total > 0) LinearProgressIndicator(
                    progress = { ((state.activePhrase + 1).coerceIn(0, total)).toFloat() / total },
                    modifier = Modifier.fillMaxWidth().height(5.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(50)),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {})
            }
        }
    }
}
