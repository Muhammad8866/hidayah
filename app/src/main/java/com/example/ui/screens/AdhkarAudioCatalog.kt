package com.example.ui.screens

import com.example.ui.tour.tourTarget

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Recorded recitations for the morning and evening adhkar, keyed by dua id: the bundled ones
 * [ManzilRuqyahAudioCatalog] lists (the three Quls are Mishary Alafasy's ayahs). Other recordings
 * come only from licensed sources listed in assets/dua_audio_remote.json and are downloaded on first
 * play ([DuaRemoteAudio]); the rest use the actual Arabic text with the best offline Arabic device voice.
 */
internal object AdhkarAudioCatalog {
    fun has(duaId: String) = ManzilRuqyahAudioCatalog.hasDuaRecording(duaId) || DuaRemoteAudio.has(duaId)

    /** The bundled recording of [duaId] in play order, or null when none is bundled. */
    fun urisFor(duaId: String): List<String>? = ManzilRuqyahAudioCatalog.duaUris(duaId)

    /** A bundled recording, or a licensed one already downloaded and verified. */
    fun playableUris(context: Context, duaId: String): List<String>? =
        urisFor(duaId) ?: DuaRemoteAudio.cached(context, duaId)?.let { listOf(android.net.Uri.fromFile(it).toString()) }
}

internal val LocalAdhkarSpeech = compositionLocalOf<DuaSpeech?> { null }

/** Recorded audio first; the actual displayed Arabic through a device voice for missing recordings. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AdhkarAudioControls(dua: Dua) {
    val speech = LocalAdhkarSpeech.current
    val context = LocalContext.current
    DuaRemoteAudio.load(context)
    if (!AdhkarAudioCatalog.has(dua.id)) {
        if (speech != null) DuaAudioControls(dua.id, dua.arabicText, speech)
        return
    }
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
                modifier = Modifier.testTag("adhkar_audio_${dua.id}").tourTarget("duas_adhkar_listen")) {
                LocalizedText(if (active && !paused) "Pause" else "Listen")
            }
            FilterChip(slow, { AdhkarAudioPlayer.setSlow(!slow) }, label = { LocalizedText("Slow") }, modifier = Modifier.tourTarget("duas_adhkar_slow"))
            FilterChip(repeat, { AdhkarAudioPlayer.setRepeat(!repeat) }, label = { LocalizedText("Repeat") }, modifier = Modifier.tourTarget("duas_adhkar_repeat_audio"))
            if (active) TextButton(onClick = { AdhkarAudioPlayer.stop() }, modifier = Modifier.tourTarget("duas_adhkar_stop")) { LocalizedText("Stop") }
        }
        if (active && loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        LocalizedText(ManzilRuqyahAudioCatalog.duaReciter(dua.id),
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (failedId == dua.id) {
            LocalizedText("Audio could not play. Try again.", color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { speech?.stop(); AdhkarAudioPlayer.toggle(context, dua.id) }, modifier = Modifier.tourTarget("duas_adhkar_retry")) { LocalizedText("Retry") }
            // A download that failed (offline, or changed at source) still leaves the dua audible.
            if (AdhkarAudioCatalog.urisFor(dua.id) == null && speech != null) DuaAudioControls(dua.id, dua.arabicText, speech)
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
    private var audioLoad: com.example.core.analytics.AppAnalytics.AudioLoad? = null
    /** The adhkar list on screen; a recording from it carries on into the next dhikr that has one. */
    private var queue: List<String> = emptyList()
    private val downloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    /** Bumped per download, so a slow one that finishes after the user moved on is ignored. */
    private var downloads = 0

    fun setQueue(ids: List<String>) { queue = ids }

    private fun player(context: Context): ExoPlayer = player ?: StreamPlayer.build(context).apply {
        setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH).build(), true)
        setHandleAudioBecomingNoisy(true)
        addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.mediaId?.takeIf { it.isNotEmpty() }?.let { activeId.value = it }
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                loading.value = playbackState == Player.STATE_BUFFERING
                com.example.core.analytics.AppAnalytics.playbackState("adhkar_audio", playbackState)
                if (playbackState == Player.STATE_READY) audioLoad?.ready()
                if (playbackState == Player.STATE_ENDED) { activeId.value = null; paused.value = false }
            }
            // Losing audio focus or unplugging headphones pauses the player; show that as stopped.
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                paused.value = !playWhenReady && activeId.value != null
            }
            override fun onPlayerError(error: PlaybackException) {
                com.example.core.analytics.AppAnalytics.mediaState("adhkar_audio", "failed", "playback_error")
                audioLoad?.failed()
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
            // Still downloading: a second tap cancels it rather than toggling an idle player.
            if (player?.playbackState.let { it == null || it == Player.STATE_IDLE }) { stop(); return }
            player?.let {
                com.example.core.analytics.AppAnalytics.media("adhkar_audio", if (it.playWhenReady) "pause" else "resume")
                if (it.playWhenReady) it.pause() else it.play()
            }
            return
        }
        val app = context.applicationContext
        val uris = AdhkarAudioCatalog.playableUris(app, duaId)
        if (uris == null) {
            if (!DuaRemoteAudio.has(duaId)) return
            stop()
            val request = ++downloads
            activeId.value = duaId
            loading.value = true
            downloadScope.launch {
                val fetched = runCatching { DuaRemoteAudio.fetch(app, duaId) }
                withContext(Dispatchers.Main) {
                    if (request != downloads || activeId.value != duaId) return@withContext
                    if (fetched.isSuccess) { activeId.value = null; toggle(app, duaId) }
                    else {
                        com.example.core.analytics.AppAnalytics.mediaState("adhkar_audio", "failed", "download_error")
                        activeId.value = null; loading.value = false; failedId.value = duaId
                    }
                }
            }
            return
        }
        com.example.core.analytics.AppAnalytics.media("adhkar_audio", "play")
        com.example.core.analytics.AppAnalytics.content("adhkar_audio", duaId, "play")
        com.example.quran.QuranAudioManager.stop()
        com.example.quran.TafsirAudioManager.stop()
        com.example.naat.NaatAudioManager.stop()
        com.example.azan.AzanAudioManager.stop()
        val exo = player(app)
        failedId.value = null
        activeId.value = duaId
        loading.value = true
        paused.value = false
        exo.setPlaybackSpeed(if (slow.value) .75f else 1f)
        exo.repeatMode = if (repeat.value) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        val start = queue.indexOf(duaId)
        val ids = if (start >= 0) queue.drop(start).filter { AdhkarAudioCatalog.playableUris(app, it) != null } else listOf(duaId)
        // A dua recorded as several ayahs is several items sharing its id; repeat-one loops one ayah of it.
        exo.setMediaItems(ids.flatMap { id ->
            (if (id == duaId) uris else AdhkarAudioCatalog.playableUris(app, id).orEmpty()).map {
                duaMediaItem(it, id)
            }
        })
        audioLoad = com.example.core.analytics.AppAnalytics.audioLoad("adhkar_audio")
        exo.prepare()
        exo.play()
    }

    fun setSlow(value: Boolean) { com.example.core.analytics.AppAnalytics.setting("adhkar_audio_slow", value.toString()); slow.value = value; player?.setPlaybackSpeed(if (value) .75f else 1f) }
    fun setRepeat(value: Boolean) { com.example.core.analytics.AppAnalytics.setting("adhkar_audio_repeat", value.toString()); repeat.value = value; player?.repeatMode = if (value) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF }

    fun stop() {
        downloads++
        if (activeId.value != null) com.example.core.analytics.AppAnalytics.media("adhkar_audio", "stop")
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

