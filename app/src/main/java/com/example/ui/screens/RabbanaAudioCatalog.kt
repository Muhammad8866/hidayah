package com.example.ui.screens

import android.content.Context
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
import androidx.media3.common.*
import com.example.core.net.StreamPlayer
import com.example.ui.localization.LocalizedText
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * The bundled recitation of each of the forty Rabbana duas: Mishary Rashid Alafasy's recitation of
 * the ayah, cut to the words of the dua where the dua is only part of its ayah.
 */
internal object RabbanaAudioCatalog {
    const val DIR = "rabbana_audio"
    const val COUNT = 40
    const val RECITER = "Reciter: Mishary Rashid Alafasy"

    fun assetUri(number: Int): String? =
        if (number in 1..COUNT) "asset:///$DIR/rabbana_%02d.mp3".format(Locale.ROOT, number) else null
}

internal data class RabbanaAudioState(val active: Int? = null, val playing: Boolean = false, val failed: Boolean = false)

internal class RabbanaAudio(context: Context) {
    val state = MutableStateFlow(RabbanaAudioState())
    private val engine = lazy { StreamPlayer.build(context.applicationContext).apply {
        setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH).build(), true)
        setHandleAudioBecomingNoisy(true)
        addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                if (state.value.active == null || state.value.failed) return
                if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) && player.playbackState == Player.STATE_ENDED) {
                    stop()
                    return
                }
                state.value = state.value.copy(playing = player.playWhenReady)
            }
            override fun onPlayerError(error: PlaybackException) {
                if (state.value.active == null) return
                state.value = state.value.copy(playing = false, failed = true)
            }
        })
    } }
    private val player get() = engine.value

    /** Starts the dua, or pauses and resumes it when it is already the active one. */
    fun play(number: Int, slow: Boolean) {
        val current = state.value
        if (current.active == number && !current.failed) {
            if (player.playWhenReady) player.pause() else player.play()
            return
        }
        val uri = RabbanaAudioCatalog.assetUri(number) ?: return
        com.example.quran.QuranAudioManager.stop()
        com.example.quran.TafsirAudioManager.stop()
        com.example.naat.NaatAudioManager.stop()
        com.example.azan.AzanAudioManager.stop()
        state.value = RabbanaAudioState(active = number, playing = true)
        player.setPlaybackSpeed(if (slow) .75f else 1f)
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.play()
    }

    fun setSlow(slow: Boolean) { if (engine.isInitialized()) player.setPlaybackSpeed(if (slow) .75f else 1f) }

    fun stop() {
        // Reset first so the listener ignores the events the player emits while stopping.
        state.value = RabbanaAudioState()
        if (engine.isInitialized()) { player.pause(); player.stop() }
    }
    fun close() { if (engine.isInitialized()) player.release() }
}

@Composable
internal fun rememberRabbanaAudio(): RabbanaAudio {
    val context = LocalContext.current
    val audio = remember(context) { RabbanaAudio(context) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(audio, owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) audio.stop() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); audio.close() }
    }
    return audio
}

@Composable
internal fun RabbanaAudioControls(number: Int, audio: RabbanaAudio, slow: Boolean, onSlowChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier) {
    val state by audio.state.collectAsState()
    val active = state.active == number
    Column(modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { audio.play(number, slow) }, modifier = Modifier.testTag("rabbana_audio_$number")) {
                LocalizedText(when {
                    active && state.failed -> "Retry"
                    active && state.playing -> "Pause"
                    else -> "Listen"
                })
            }
            if (active) OutlinedButton(onClick = audio::stop) { LocalizedText("Stop") }
            FilterChip(selected = slow, onClick = { onSlowChange(!slow); audio.setSlow(!slow) },
                label = { LocalizedText("Slow") })
        }
        LocalizedText(RabbanaAudioCatalog.RECITER, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (active && state.failed) LocalizedText("Audio could not play. Try again.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
    }
}
