package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.core.net.StreamPlayer
import com.example.ui.localization.LocalizedText
import com.example.domain.model.Dua
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Recorded recitations bundled in assets/adhkar_audio for the morning and evening adhkar, keyed by
 * dua id. Only adhkar whose recording reads the same wording as the bundled Arabic are listed; the
 * rest use the actual Arabic text with the best installed offline Arabic device voice.
 *
 * The three Quls are Mishary Alafasy's recitation (everyayah.com); the others are the Hisn
 * al-Muslim readings (chapter 27, items 78–97), with original source URLs in sources.json.
 */
internal object AdhkarAudioCatalog {
    private val RECORDED = setOf(
        "dua_muawwidhat",
        "dua_allahumma_bika_asbahna",
        "dua_allahumma_bika_amsayna",
        "dua_sayyidul_istighfar",
        "dua_morning_witness",
        "dua_evening_witness",
        "dua_morning_blessing",
        "dua_evening_blessing",
        "dua_hasbiyallah",
        "dua_bismillah_la_yadurru",
        "dua_raditu_billahi",
        "dua_ya_hayyu_aslih",
        "dua_fitratil_islam",
        "dua_amsayna_fitrah",
        "dua_subhanallah_wa_bihamdihi",
        "dua_subhanallah_adada_khalqihi",
        "dua_kalimat_tammat"
    )

    fun has(duaId: String) = duaId in RECORDED

    /** The asset URI of the recording for [duaId], or null when none is bundled. */
    fun uriFor(duaId: String): String? =
        if (has(duaId)) "asset:///adhkar_audio/$duaId.mp3" else null
}

internal val LocalAdhkarSpeech = compositionLocalOf<DuaSpeech?> { null }

/** Recorded audio first; the actual displayed Arabic through a device voice for missing recordings. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AdhkarAudioControls(dua: Dua) {
    val speech = LocalAdhkarSpeech.current
    if (!AdhkarAudioCatalog.has(dua.id)) {
        if (speech != null) DuaAudioControls(dua.id, dua.arabicText, speech)
        return
    }
    val context = LocalContext.current
    val activeId by AdhkarAudioPlayer.activeId.collectAsState()
    val paused by AdhkarAudioPlayer.paused.collectAsState()
    val loading by AdhkarAudioPlayer.loading.collectAsState()
    val failedId by AdhkarAudioPlayer.failedId.collectAsState()
    val slow by AdhkarAudioPlayer.slow.collectAsState()
    val repeat by AdhkarAudioPlayer.repeat.collectAsState()
    val active = activeId == dua.id
    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { speech?.stop(); AdhkarAudioPlayer.toggle(context, dua.id) },
                modifier = Modifier.testTag("adhkar_audio_${dua.id}")) {
                LocalizedText(if (active && !paused) "Pause" else "Listen")
            }
            FilterChip(slow, { AdhkarAudioPlayer.setSlow(!slow) }, label = { LocalizedText("Slow") })
            FilterChip(repeat, { AdhkarAudioPlayer.setRepeat(!repeat) }, label = { LocalizedText("Repeat") })
            if (active) TextButton(onClick = { AdhkarAudioPlayer.stop() }) { LocalizedText("Stop") }
        }
        if (active && loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        LocalizedText(if (dua.id == "dua_muawwidhat") "Mishary Rashid Alafasy" else "Recorded recitation • Hisn al-Muslim",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (failedId == dua.id) {
            LocalizedText("Audio could not play. Try again.", color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { speech?.stop(); AdhkarAudioPlayer.toggle(context, dua.id) }) { LocalizedText("Retry") }
        }
    }
}

/** Plays one bundled dhikr recording at a time; [activeId] is the dua being played. */
internal object AdhkarAudioPlayer {
    val activeId = MutableStateFlow<String?>(null)
    val loading = MutableStateFlow(false)
    val failedId = MutableStateFlow<String?>(null)
    val paused = MutableStateFlow(false)
    val slow = MutableStateFlow(false)
    val repeat = MutableStateFlow(false)
    private var player: ExoPlayer? = null

    private fun player(context: Context): ExoPlayer = player ?: StreamPlayer.build(context).apply {
        setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH).build(), true)
        setHandleAudioBecomingNoisy(true)
        addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                loading.value = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_ENDED) { activeId.value = null; paused.value = false }
            }
            // Losing audio focus or unplugging headphones pauses the player; show that as stopped.
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                paused.value = !playWhenReady && activeId.value != null
            }
            override fun onPlayerError(error: PlaybackException) {
                failedId.value = activeId.value
                activeId.value = null
                loading.value = false
                paused.value = false
            }
        })
    }.also { player = it }

    /** Starts the recording for [duaId], or stops it when it is the one already playing. */
    fun toggle(context: Context, duaId: String) {
        if (activeId.value == duaId) {
            player?.let { if (it.playWhenReady) it.pause() else it.play() }
            return
        }
        val uri = AdhkarAudioCatalog.uriFor(duaId) ?: return
        com.example.quran.QuranAudioManager.stop()
        com.example.quran.TafsirAudioManager.stop()
        com.example.naat.NaatAudioManager.stop()
        com.example.azan.AzanAudioManager.stop()
        val exo = player(context.applicationContext)
        failedId.value = null
        activeId.value = duaId
        loading.value = true
        paused.value = false
        exo.setPlaybackSpeed(if (slow.value) .75f else 1f)
        exo.repeatMode = if (repeat.value) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        exo.setMediaItem(MediaItem.fromUri(uri))
        exo.prepare()
        exo.play()
    }

    fun setSlow(value: Boolean) { slow.value = value; player?.setPlaybackSpeed(if (value) .75f else 1f) }
    fun setRepeat(value: Boolean) { repeat.value = value; player?.repeatMode = if (value) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF }

    fun stop() {
        activeId.value = null
        loading.value = false
        paused.value = false
        failedId.value = null
        player?.let { it.pause(); it.stop() }
    }

    fun release() {
        activeId.value = null
        player?.release()
        player = null
    }
}

/** Stops adhkar audio when the app goes to the background and frees the player on leaving the screen. */
@Composable
internal fun AdhkarAudioLifecycle() {
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) AdhkarAudioPlayer.stop()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); AdhkarAudioPlayer.release() }
    }
}

