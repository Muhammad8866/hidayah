package com.example.azan

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.core.net.FastNet
import com.example.core.net.StreamPlayer
import com.example.prayer.PrayerSettingsStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class AzanAudioState(
    val activeId: String? = null,
    val playing: Boolean = false,
    val buffering: Boolean = false,
    val downloaded: Set<String> = emptySet(),
    val downloading: Set<String> = emptySet(),
    val progress: Map<String, Int> = emptyMap(),
    val fajrAlertId: String? = null,
    val otherAlertId: String? = null,
    val error: String? = null
)

/** In-app listening, offline downloads and the alert-tone choice for the Azan screen. */
object AzanAudioManager {
    private const val PLAY_ERROR = "Audio could not be played. Check your internet connection or download."
    private const val DOWNLOAD_ERROR = "Download failed. Check your internet connection and try again."
    private lateinit var context: Context
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val jobs = mutableMapOf<String, Job>()
    /** Azans chosen as an alert tone before their download finished. */
    private val alertAfterDownload = mutableSetOf<String>()
    private var player: ExoPlayer? = null
    private val _state = MutableStateFlow(AzanAudioState())
    val state = _state.asStateFlow()

    private val attributes = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build()

    /** Safe to call on every visit: it re-reads what is on the device and which azans are chosen. */
    fun init(applicationContext: Context) {
        context = applicationContext.applicationContext
        val dir = AzanAlertStore.audioDir(context).apply { mkdirs() }
        val saved = dir.listFiles().orEmpty().filter { it.isFile && it.extension == "mp3" }
            .map { it.nameWithoutExtension }.toSet()
        val prefs = PrayerSettingsStore.prefs(context)
        _state.update {
            it.copy(
                downloaded = saved,
                fajrAlertId = AzanAlertStore.selectedId(prefs, fajr = true),
                otherAlertId = AzanAlertStore.selectedId(prefs, fajr = false)
            )
        }
    }

    /** Plays [track], or stops it when it is already the one playing. */
    fun play(track: AzanTrack) {
        com.example.core.analytics.AppAnalytics.media("azan_audio", "play")
        com.example.core.analytics.AppAnalytics.content("azan_audio", track.id, "play")
        if (!::context.isInitialized) return
        if (_state.value.activeId == track.id) { stop(); return }
        // Only one of Azan, Naat, Quran and Tafseer plays at a time.
        com.example.quran.QuranAudioManager.stop()
        com.example.quran.TafsirAudioManager.stop()
        com.example.naat.NaatAudioManager.stop()
        releasePlayer()
        _state.update { it.copy(activeId = track.id, playing = false, buffering = true, error = null) }
        try {
            // Offline copy, then the copy inside the APK, then the fast mirror, then the original host.
            val sources = when {
                file(track).isFile -> listOf(Uri.fromFile(file(track)))
                track.bundled -> listOf(Uri.parse("asset:///${AzanCatalog.BUNDLED_DIR}/${track.fileName}"))
                else -> track.sources.map(Uri::parse)
            }
            var next = 0
            player = StreamPlayer.build(context).apply {
                // Handles audio focus for us: a call or another player pauses it, and we then stop.
                setAudioAttributes(attributes, true)
                setHandleAudioBecomingNoisy(true)
                setWakeMode(C.WAKE_MODE_NETWORK)
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                com.example.core.analytics.AppAnalytics.playbackState("azan_audio", state)
                        when (state) {
                            Player.STATE_READY -> _state.update { it.copy(playing = playWhenReady, buffering = false) }
                            Player.STATE_BUFFERING -> _state.update { it.copy(buffering = true) }
                            Player.STATE_ENDED -> stop()
                        }
                    }

                    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                        if (!playWhenReady && reason == Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS) stop()
                    }

                    override fun onPlayerError(error: PlaybackException) {
                com.example.core.analytics.AppAnalytics.mediaState("azan_audio", "failed", "playback_error")
                        if (next < sources.size) {
                            // This source failed; try the next one without the listener noticing.
                            setMediaItem(MediaItem.fromUri(sources[next++]))
                            prepare()
                        } else {
                            stop()
                            _state.update { it.copy(error = PLAY_ERROR) }
                        }
                    }
                })
                setMediaItem(MediaItem.fromUri(sources[next++]))
                playWhenReady = true
                prepare()
            }
        } catch (_: Exception) {
            stop()
            _state.update { it.copy(error = PLAY_ERROR) }
        }
    }

    fun stop() {
        com.example.core.analytics.AppAnalytics.media("azan_audio", "stop")
        if (!::context.isInitialized) return
        releasePlayer()
        _state.update { it.copy(activeId = null, playing = false, buffering = false) }
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    /**
     * Uses [track] for the prayer alert: Fajr azans for Fajr, the rest for the other four prayers.
     * The alert plays from the device, so a track that is not yet offline is downloaded first.
     */
    fun setAsAlert(track: AzanTrack) {
        if (!::context.isInitialized) return
        if (file(track).isFile) {
            saveAlert(track.fajr, track.id)
        } else {
            alertAfterDownload += track.id
            download(track)
        }
    }

    /** Returns the Fajr or the other-prayers alert to the normal notification sound. */
    fun clearAlert(fajr: Boolean) {
        if (::context.isInitialized) saveAlert(fajr, null)
    }

    private fun saveAlert(fajr: Boolean, id: String?) {
        AzanAlertStore.select(PrayerSettingsStore.prefs(context), fajr, id)
        com.example.core.analytics.AppAnalytics.setting(if (fajr) "set_fajr_azan_alert" else "set_azan_alert", id ?: "none")
        com.example.core.analytics.AnalyticsProfile.sync(context)
        _state.update { if (fajr) it.copy(fajrAlertId = id) else it.copy(otherAlertId = id) }
    }

    fun download(track: AzanTrack) {
        if (!::context.isInitialized || file(track).isFile || jobs.containsKey(track.id)) return
        _state.update { it.copy(downloading = it.downloading + track.id, error = null) }
        jobs[track.id] = scope.launch {
            var result: Result<Unit> = Result.failure(CancellationException())
            try {
                result = withContext(Dispatchers.IO) { runCatching { fetch(track) } }
            } finally {
                jobs.remove(track.id)
                val ok = result.isSuccess
                _state.update { it.copy(
                    downloading = it.downloading - track.id,
                    downloaded = if (ok) it.downloaded + track.id else it.downloaded,
                    progress = it.progress - track.id,
                    error = if (ok) it.error else DOWNLOAD_ERROR
                ) }
                if (alertAfterDownload.remove(track.id) && ok) saveAlert(track.fajr, track.id)
            }
        }
    }

    private suspend fun fetch(track: AzanTrack) {
        val target = file(track)
        val temp = File(target.parentFile, "${track.id}.part")
        try {
            if (track.bundled) {
                context.assets.open("${AzanCatalog.BUNDLED_DIR}/${track.fileName}").use { input ->
                    temp.outputStream().use { input.copyTo(it) }
                }
            } else {
                var failure: Exception? = null
                // The GitHub mirror is much faster; the Internet Archive original is the fallback.
                val done = track.sources.any { url ->
                    try { download(track, url, temp); true } catch (e: CancellationException) { throw e } catch (e: Exception) { failure = e; false }
                }
                if (!done) throw failure ?: IllegalStateException("Download failed")
            }
            if (temp.length() == 0L || !temp.renameTo(target)) error("Could not save audio")
        } finally { if (temp.exists()) temp.delete() }
    }

    private suspend fun download(track: AzanTrack, url: String, temp: File) {
        var shown = -1
        FastNet.download(url, temp) { copied, total ->
            val percent = if (total > 0) (copied * 100 / total).toInt() else -1
            if (percent != shown) {
                shown = percent
                _state.update { it.copy(progress = it.progress + (track.id to percent)) }
            }
        }
    }

    /** Removes the offline copy; an alert that used it goes back to the normal notification sound. */
    fun delete(track: AzanTrack) {
        if (!::context.isInitialized) return
        // Files cannot be removed from under a playing MediaPlayer, so stop it first.
        if (_state.value.activeId == track.id) stop()
        file(track).delete()
        _state.update { it.copy(downloaded = it.downloaded - track.id) }
        if (_state.value.fajrAlertId == track.id) saveAlert(fajr = true, id = null)
        if (_state.value.otherAlertId == track.id) saveAlert(fajr = false, id = null)
    }

    fun clearError() { _state.update { it.copy(error = null) } }

    private fun file(track: AzanTrack) = AzanAlertStore.audioFile(context, track.id)
}

