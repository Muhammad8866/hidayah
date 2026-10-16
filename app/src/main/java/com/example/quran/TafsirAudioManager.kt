package com.example.quran

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.core.audio.PlaybackSpeed
import com.example.core.net.FastNet
import com.example.core.net.StreamPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File

data class TafsirAudioState(
    val playingId: String? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Int = 0,
    val durationMs: Int = 0,
    val resumeId: String? = null,
    val resumePositionMs: Int = 0,
    val downloaded: Set<String> = emptySet(),
    val downloading: Set<String> = emptySet(),
    val error: String? = null,
    val speed: Float = PlaybackSpeed.NORMAL
)

/** Streams official Sirat ul Jinan MP3s and saves only user-requested offline copies. */
object TafsirAudioManager {
    private const val PREFS = "tafsir_audio"
    private lateinit var context: Context
    private lateinit var directory: File
    private var tracks: List<TafsirAudioTrack> = emptyList()
    private var queue: List<TafsirAudioTrack> = emptyList()
    private var queueIndex = 0
    private var player: ExoPlayer? = null
    private var ticker: Job? = null
    private val downloads = mutableMapOf<String, Job>()
    private val downloadSlots = Semaphore(3)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(TafsirAudioState())
    val state = _state.asStateFlow()

    fun init(app: Context) {
        if (::context.isInitialized) return
        context = app.applicationContext
        tracks = TafsirAudioCatalog.tracks(context)
        directory = File(context.filesDir, "tafsir_audio").apply { mkdirs() }
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val saved = prefs.getString("resume_id", null)?.takeIf { id -> tracks.any { it.id == id } }
        _state.value = _state.value.copy(
            resumeId = saved,
            resumePositionMs = if (saved == null) 0 else prefs.getInt("resume_position", 0),
            speed = PlaybackSpeed.sanitize(prefs.getFloat("speed", PlaybackSpeed.NORMAL)),
            downloaded = tracks.filter { file(it).exists() }.map { it.id }.toSet()
        )
    }

    fun track(id: String?): TafsirAudioTrack? = tracks.firstOrNull { it.id == id }

    fun playOrPause(track: TafsirAudioTrack) {
        if (!track.isStreamable) return
        if (_state.value.playingId == track.id && player != null) {
            if (_state.value.error != null) playQueue(listOf(track))
            else if (_state.value.isPlaying) pause() else resume()
        } else playQueue(listOf(track))
    }

    fun playQueue(requested: List<TafsirAudioTrack>, positionMs: Int = 0) {
        if (!::context.isInitialized) return
        queue = requested.filter { it.isStreamable }.distinctBy { it.id }
        if (queue.isEmpty()) return
        QuranAudioManager.stop()
        com.example.naat.NaatAudioManager.stop()
        com.example.azan.AzanAudioManager.stop()
        queueIndex = 0
        prepare(queue[0], positionMs)
    }

    fun resumeLast() {
        val saved = track(_state.value.resumeId) ?: return
        if (_state.value.playingId == saved.id && player != null) resume()
        else playQueue(listOf(saved), _state.value.resumePositionMs)
    }

    fun pause() {
        com.example.core.analytics.AppAnalytics.media("tafsir_audio", "pause")
        player?.pause()
        ticker?.cancel()
        rememberPosition()
        _state.value = _state.value.copy(isPlaying = false)
        TafsirAudioService.update(contextOrNull)
    }

    fun resume() {
        com.example.core.analytics.AppAnalytics.media("tafsir_audio", "resume")
        val current = player ?: return resumeLast()
        TafsirAudioService.start(context)
        if (current.playbackState == Player.STATE_IDLE) current.prepare()
        current.play()
        startTicker()
        TafsirAudioService.update(context)
    }

    fun toggle() { if (_state.value.isPlaying) pause() else resume() }
    fun next() {
        com.example.core.analytics.AppAnalytics.media("tafsir_audio", "next")
        if (queueIndex + 1 < queue.size) prepare(queue[++queueIndex])
    }
    fun previous() {
        com.example.core.analytics.AppAnalytics.media("tafsir_audio", "previous")
        if (queueIndex > 0) prepare(queue[--queueIndex]) else seekTo(0)
    }

    fun seekTo(positionMs: Int) {
        com.example.core.analytics.AppAnalytics.media("tafsir_audio", "seek")
        player?.seekTo(positionMs.toLong())
        _state.value = _state.value.copy(positionMs = positionMs)
        rememberPosition(positionMs)
    }

    fun setSpeed(speed: Float) {
        com.example.core.analytics.AppAnalytics.media("tafsir_audio", "speed_change")
        val safe = PlaybackSpeed.sanitize(speed)
        _state.value = _state.value.copy(speed = safe)
        player?.setPlaybackSpeed(safe)
        if (::context.isInitialized) context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putFloat("speed", safe).apply()
    }

    private fun prepare(track: TafsirAudioTrack, positionMs: Int = 0) {
        com.example.core.analytics.AppAnalytics.media("tafsir_audio", "play")
        com.example.core.analytics.AppAnalytics.content("tafsir_audio", "surah_${track.surah}", "play")
        ticker?.cancel()
        player?.release()
        player = null
        _state.value = _state.value.copy(
            playingId = track.id, isPlaying = false, isBuffering = true, positionMs = positionMs,
            durationMs = 0, resumeId = track.id, resumePositionMs = positionMs, error = null
        )
        persist(track.id, positionMs)
        val candidate = StreamPlayer.build(context)
        player = candidate
        candidate.setAudioAttributes(
            AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH).build(), true
        )
        // Pauses when headphones are unplugged instead of switching to the loudspeaker.
        candidate.setHandleAudioBecomingNoisy(true)
        candidate.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (player !== candidate) return
                _state.value = _state.value.copy(isPlaying = isPlaying)
                TafsirAudioService.update(context)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (player !== candidate) return
                com.example.core.analytics.AppAnalytics.playbackState("tafsir_audio", playbackState)
                if (playbackState == Player.STATE_BUFFERING) {
                    _state.value = _state.value.copy(isBuffering = true)
                } else if (playbackState == Player.STATE_READY) {
                    _state.value = _state.value.copy(isBuffering = false,
                        durationMs = candidate.duration.safeMillis())
                    startTicker()
                } else if (playbackState == Player.STATE_ENDED) {
                    if (queueIndex + 1 < queue.size) prepare(queue[++queueIndex])
                    else {
                        ticker?.cancel()
                        candidate.pause()
                        candidate.seekTo(0)
                        rememberPosition(0)
                        _state.value = _state.value.copy(isPlaying = false, isBuffering = false, positionMs = 0)
                        TafsirAudioService.update(context)
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                if (player !== candidate) return
                com.example.core.analytics.AppAnalytics.mediaState("tafsir_audio", "failed", "playback_error")
                ticker?.cancel()
                _state.value = _state.value.copy(isPlaying = false, isBuffering = false,
                    error = "Tafseer audio could not be played. Tap Play to retry.")
                TafsirAudioService.update(context)
            }
        })
        val uri = file(track).takeIf(File::exists)?.let(Uri::fromFile) ?: Uri.parse(track.audioUrl)
        candidate.setMediaItem(MediaItem.fromUri(uri))
        if (positionMs > 0) candidate.seekTo(positionMs.toLong())
        candidate.setPlaybackSpeed(_state.value.speed)
        TafsirAudioService.start(context)
        candidate.prepare()
        candidate.play()
    }

    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (true) {
                val current = player ?: break
                val position = current.currentPosition.safeMillis()
                _state.value = _state.value.copy(positionMs = position,
                    durationMs = current.duration.safeMillis())
                rememberPosition(position)
                delay(1000)
            }
        }
    }

    private fun rememberPosition(positionMs: Int = _state.value.positionMs) {
        val id = _state.value.playingId ?: return
        _state.value = _state.value.copy(resumeId = id, resumePositionMs = positionMs)
        persist(id, positionMs)
    }

    private fun persist(id: String, positionMs: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("resume_id", id).putInt("resume_position", positionMs).apply()
    }

    fun download(track: TafsirAudioTrack) {
        if (!::context.isInitialized || !track.isStreamable || file(track).exists() || track.id in downloads) return
        downloads[track.id] = scope.launch {
            _state.value = _state.value.copy(downloading = _state.value.downloading + track.id)
            val result = downloadSlots.withPermit { withContext(Dispatchers.IO) {
                runCatching {
                    val temp = File(directory, "${track.id}.part")
                    FastNet.download(track.audioUrl, temp)
                    if (!temp.renameTo(file(track))) error("Could not save audio")
                }
            } }
            downloads.remove(track.id)
            _state.value = _state.value.copy(
                downloading = _state.value.downloading - track.id,
                downloaded = if (result.isSuccess) _state.value.downloaded + track.id else _state.value.downloaded,
                error = if (result.isFailure) "Tafseer download failed. Please try again." else null
            )
        }
    }

    fun downloadAll(tracks: List<TafsirAudioTrack>) = tracks.forEach(::download)

    /** Stops the running downloads of [tracks]; partial files are discarded. */
    fun cancelDownloads(tracks: List<TafsirAudioTrack>) {
        val ids = tracks.map { it.id }.filter { it in downloads }
        if (ids.isEmpty()) return
        ids.forEach { downloads.remove(it)?.cancel() }
        _state.value = _state.value.copy(downloading = _state.value.downloading - ids.toSet())
    }

    fun delete(track: TafsirAudioTrack) {
        if (_state.value.playingId == track.id) stop()
        file(track).delete()
        _state.value = _state.value.copy(downloaded = _state.value.downloaded - track.id)
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }

    fun stop() {
        com.example.core.analytics.AppAnalytics.media("tafsir_audio", "stop")
        if (!::context.isInitialized) return
        rememberPosition()
        ticker?.cancel()
        player?.release()
        player = null
        _state.value = _state.value.copy(playingId = null, isPlaying = false, isBuffering = false,
            positionMs = 0, durationMs = 0)
        TafsirAudioService.stop(context)
    }

    private fun file(track: TafsirAudioTrack) = File(directory, "${track.id}.mp3")
    private val contextOrNull: Context? get() = if (::context.isInitialized) context else null

    private fun Long.safeMillis(): Int =
        if (this == C.TIME_UNSET || this < 0) 0 else coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
}
