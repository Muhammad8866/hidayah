package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.example.core.net.StreamPlayer
import com.example.ui.components.OnReconnect
import com.example.ui.components.rememberIsOnline
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

/**
 * Recorded recitations for the Manzil / Ruqyah / Wazaif screen, bundled in assets/recitation_audio
 * so they play offline.
 *
 * ayah/SSSAAA.mp3 — original Mishary Rashid Alafasy recordings (everyayah.com, 128 kbps).
 * dua/<dua id>.mp3 — the Hisn al-Muslim readings published at hisnmuslim.com. Only duas whose
 * recording reads the same wording as the bundled Arabic are listed; the rest keep the device voice.
 */
internal object ManzilRuqyahAudioCatalog {
    const val QURAN_RECITER = "Mishary Rashid Alafasy"
    const val DUA_RECITER = "Recorded recitation • Hisn al-Muslim"

    private val HISN_RECORDED = setOf(
        "dua_waking_up", "dua_waking_afani", "dua_before_sleep", "dua_sleep_qina_adhab", "dua_sleep_atamana",
        "dua_bad_dream", "dua_abu_bakr", "dua_ma_qaddamtu_akhkhartu", "dua_majlis_kaffarah",
        "dua_astaghfirullah_atubu", "dua_salawat", "dua_tahleel_100", "dua_beneficial_knowledge",
        "dua_visiting_sick", "dua_seeing_afflicted", "dua_fear_people", "dua_shirk_protection"
    )

    /** Duas that are a Quran ayah, played from the ayah they cite. */
    private val DUA_AYAHS = mapOf(
        "dua_adam_forgiveness" to RecitationPassage(7, 23, 23),
        "dua_rabbighfir_warham" to RecitationPassage(23, 118, 118)
    )

    private val bundledAyahs: Set<Pair<Int, Int>> by lazy {
        (RecitationCollection.manzil + RecitationCollection.ruqyah + DUA_AYAHS.values)
            .flatMap { passage -> (passage.first..passage.last).map { passage.surah to it } }.toSet()
    }

    /** The bundled recording of an ayah, or the stream for ayahs that are not bundled. */
    fun ayahUri(surah: Int, ayah: Int): String =
        if (surah to ayah in bundledAyahs) "asset:///recitation_audio/ayah/%03d%03d.mp3".format(Locale.ROOT, surah, ayah)
        else "https://everyayah.com/data/Alafasy_128kbps/%03d%03d.mp3".format(Locale.ROOT, surah, ayah)

    /** What to play for [duaId] in order, or null when the dua has no recorded recitation. */
    fun duaUris(duaId: String): List<String>? = when {
        duaId in HISN_RECORDED -> listOf("asset:///recitation_audio/dua/$duaId.mp3")
        duaId in DUA_AYAHS -> recitationPlaybackUris(DUA_AYAHS.getValue(duaId))
        else -> AdhkarAudioCatalog.uriFor(duaId)?.let(::listOf)
    }

    fun hasDuaRecording(duaId: String) = duaUris(duaId) != null

    fun duaReciter(duaId: String) =
        if (duaId in DUA_AYAHS || duaId == "dua_muawwidhat") QURAN_RECITER else DUA_RECITER
}

/** Like [recitationAudioUrls], but reads the copies bundled with the app when there are any. */
internal fun recitationPlaybackUris(passage: RecitationPassage): List<String> =
    (passage.first..passage.last).map { ManzilRuqyahAudioCatalog.ayahUri(passage.surah, it) }

internal data class RecordedDuaState(val id: String? = null, val playing: Boolean = false,
    val failed: Boolean = false, val slow: Boolean = false, val loading: Boolean = false,
    val repeat: Boolean = false)

/** Plays the recorded recitation of one dua at a time. */
internal class RecordedDuaAudio(context: Context) {
    val state = MutableStateFlow(RecordedDuaState())
    private val engine = lazy { StreamPlayer.build(context.applicationContext).apply {
        setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH).build(), true)
        setHandleAudioBecomingNoisy(true)
        addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                if (state.value.id == null || state.value.failed) return
                if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) && player.playbackState == Player.STATE_ENDED) {
                    stop()
                    return
                }
                state.value = state.value.copy(playing = player.playWhenReady &&
                    player.playbackState != Player.STATE_IDLE,
                    loading = player.playbackState == Player.STATE_BUFFERING)
            }
            override fun onPlayerError(error: PlaybackException) {
                if (state.value.id == null) return
                state.value = state.value.copy(playing = false, loading = false, failed = true)
            }
        })
    } }
    private val player get() = engine.value

    /** Starts [duaId]; pauses or resumes it when it is already the active one. */
    fun play(duaId: String) {
        val current = state.value
        if (current.id == duaId && !current.failed) {
            if (player.playWhenReady) player.pause() else player.play()
            return
        }
        val uris = ManzilRuqyahAudioCatalog.duaUris(duaId) ?: return
        com.example.quran.QuranAudioManager.stop()
        com.example.quran.TafsirAudioManager.stop()
        com.example.naat.NaatAudioManager.stop()
        com.example.azan.AzanAudioManager.stop()
        state.value = current.copy(id = duaId, playing = true, loading = true, failed = false)
        player.setPlaybackSpeed(if (current.slow) .75f else 1f)
        player.repeatMode = if (current.repeat) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
        player.setMediaItems(uris.map(MediaItem::fromUri))
        player.prepare()
        player.play()
    }

    fun setSlow(slow: Boolean) {
        state.value = state.value.copy(slow = slow)
        if (engine.isInitialized()) player.setPlaybackSpeed(if (slow) .75f else 1f)
    }

    fun setRepeat(repeat: Boolean) {
        state.value = state.value.copy(repeat = repeat)
        if (engine.isInitialized()) player.repeatMode = if (repeat) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
    }

    fun stop() {
        // Reset first so the listener ignores the events the player emits while stopping.
        state.value = RecordedDuaState(slow = state.value.slow, repeat = state.value.repeat)
        if (engine.isInitialized()) { player.pause(); player.stop() }
    }

    fun close() { if (engine.isInitialized()) player.release() }
}

/** Listen / pause, slow, repeat and stop for a dua that has a recorded recitation, in one compact row. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RecordedDuaControls(id: String, audio: RecordedDuaAudio, modifier: Modifier = Modifier,
    onStart: () -> Unit = {}) {
    val state by audio.state.collectAsState()
    val active = state.id == id
    val isOnline by rememberIsOnline()
    // A recording that failed while offline resumes by itself when the connection returns.
    OnReconnect(enabled = active && state.failed) { audio.play(id) }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FilledTonalButton(onClick = { if (!active) onStart(); audio.play(id) },
                modifier = Modifier.heightIn(min = 40.dp).testTag("collection_recorded_$id"),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)) {
                Icon(if (active && state.playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                LocalizedText(if (active && state.playing) "Pause" else "Listen")
            }
            FilterChip(state.slow, { audio.setSlow(!state.slow) }, label = { LocalizedText("Slow") })
            FilterChip(state.repeat, { audio.setRepeat(!state.repeat) }, label = { LocalizedText("Repeat") })
            if (active) IconButton(onClick = { audio.stop() }) {
                Icon(Icons.Default.Stop, contentDescription = localizedUiText("Stop"))
            }
        }
        if (active && state.loading) LinearProgressIndicator(Modifier.fillMaxWidth().height(2.dp))
        if (active && state.failed) Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            LocalizedText(if (isOnline) "Audio could not play. Try again." else "You're offline",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f))
            TextButton(onClick = { audio.play(id) }) { LocalizedText("Retry") }
        }
        LocalizedText(ManzilRuqyahAudioCatalog.duaReciter(id), style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
