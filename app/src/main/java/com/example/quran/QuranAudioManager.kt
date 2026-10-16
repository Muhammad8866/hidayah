package com.example.quran

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import com.example.core.audio.PlaybackSpeed
import com.example.core.net.FastNet
import com.example.core.net.StreamPlayer
import com.example.core.net.playerMillis
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File

data class QuranAudioState(
    val playingSurah: Int? = null,
    val isPlaying: Boolean = false,
    val downloading: Set<Int> = emptySet(),
    val downloaded: Set<Int> = emptySet(),
    val progress: Map<Int, Int> = emptyMap(),
    val error: String? = null,
    /** True from the moment Play is tapped until the stream is ready, so the UI can show a spinner. */
    val isBuffering: Boolean = false,
    /** Playback position of [playingSurah] in milliseconds. */
    val positionMs: Int = 0,
    /** Duration of [playingSurah] in milliseconds; 0 while still unknown. */
    val durationMs: Int = 0,
    /** Last chapter listened to, restorable even after the app was killed. */
    val resumeSurah: Int? = null,
    val resumePositionMs: Int = 0,
    /** Playback speed chosen by the listener; remembered across launches. */
    val speed: Float = PlaybackSpeed.NORMAL
)

/**
 * Process wide Quran audio engine. Playback survives navigation and app backgrounding because the
 * player lives here instead of in a composable, and [QuranAudioService] keeps the process
 * foregrounded while something is playing. The last position is persisted so listening can be
 * resumed later, even after a cold start.
 */
object QuranAudioManager {

    private const val PREFS = "quran_audio"
    private const val KEY_SURAH = "resume_surah"
    private const val KEY_POSITION = "resume_position"
    private const val KEY_SPEED = "speed"

    private lateinit var appContext: Context
    private lateinit var audioDir: File
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var player: ExoPlayer? = null
    private var queue: List<Int> = emptyList()
    private var queueIndex = 0
    private var ticker: Job? = null
    private var focusRequest: AudioFocusRequest? = null
    private var noisyRegistered = false
    // Unplugged headphones or a dropped Bluetooth link must not move the recitation to the speaker.
    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) pause()
        }
    }
    private val downloadJobs = mutableMapOf<Int, Job>()
    // A whole Para queues dozens of chapters; only a few download at once.
    private val downloadSlots = Semaphore(3)
    private val _state = MutableStateFlow(QuranAudioState())
    val state: StateFlow<QuranAudioState> = _state.asStateFlow()

    /** Safe to call repeatedly; the first call wires up storage and the saved resume point. */
    fun init(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext
        audioDir = File(appContext.filesDir, "quran_audio").apply { mkdirs() }
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val saved = prefs.getInt(KEY_SURAH, 0).takeIf { it in 1..114 }
        _state.value = _state.value.copy(
            downloaded = scanDownloads(),
            resumeSurah = saved,
            resumePositionMs = if (saved != null) prefs.getInt(KEY_POSITION, 0) else 0,
            speed = PlaybackSpeed.sanitize(prefs.getFloat(KEY_SPEED, PlaybackSpeed.NORMAL))
        )
    }

    fun setSpeed(speed: Float) {
        com.example.core.analytics.AppAnalytics.media("quran_audio", "speed_change")
        _state.value = _state.value.copy(speed = speed)
        player?.setPlaybackSpeed(speed)
        if (::appContext.isInitialized) {
            appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putFloat(KEY_SPEED, speed).apply()
        }
    }

    fun playOrPause(surah: Int) {
        if (_state.value.playingSurah == surah && player != null) {
            if (_state.value.isPlaying) pause() else resume()
        } else playQueue(listOf(surah))
    }

    fun playQueue(surahs: List<Int>, startPositionMs: Int = 0) {
        queue = surahs.distinct().filter { it in 1..114 }
        if (queue.isEmpty()) return
        TafsirAudioManager.stop()
        com.example.naat.NaatAudioManager.stop()
        com.example.azan.AzanAudioManager.stop()
        queueIndex = 0
        prepare(queue.first(), startPositionMs)
    }

    /** Continues the chapter last listened to, from where it was left off. */
    fun resumeLast() {
        val surah = _state.value.resumeSurah ?: return
        if (_state.value.playingSurah == surah && player != null) resume()
        else playQueue(listOf(surah), _state.value.resumePositionMs)
    }

    fun pause() {
        com.example.core.analytics.AppAnalytics.media("quran_audio", "pause")
        player?.pause()
        stopTicker()
        rememberPosition()
        _state.value = _state.value.copy(isPlaying = false, isBuffering = false)
        QuranAudioService.update(contextOrNull)
    }

    fun resume() {
        com.example.core.analytics.AppAnalytics.media("quran_audio", "resume")
        val current = player ?: return resumeLast()
        if (!requestFocus()) return
        contextOrNull?.let(QuranAudioService::start)
        if (current.playbackState == Player.STATE_IDLE) current.prepare()
        current.play()
        startTicker()
        _state.value = _state.value.copy(isPlaying = true)
        QuranAudioService.update(contextOrNull)
    }

    fun toggle() {
        if (_state.value.isPlaying) pause() else resume()
    }

    fun next() {
        com.example.core.analytics.AppAnalytics.media("quran_audio", "next")
        if (queueIndex + 1 < queue.size) prepare(queue[++queueIndex])
        else _state.value.playingSurah?.takeIf { it < 114 }?.let { playQueue(listOf(it + 1)) }
    }

    fun previous() {
        com.example.core.analytics.AppAnalytics.media("quran_audio", "previous")
        if (queueIndex > 0) prepare(queue[--queueIndex])
        else _state.value.playingSurah?.takeIf { it > 1 }?.let { playQueue(listOf(it - 1)) }
    }

    fun seekTo(positionMs: Int) {
        com.example.core.analytics.AppAnalytics.media("quran_audio", "seek")
        player?.seekTo(positionMs.toLong())
        _state.value = _state.value.copy(positionMs = positionMs)
        rememberPosition(positionMs)
    }

    private fun prepare(surah: Int, startPositionMs: Int = 0) {
        com.example.core.analytics.AppAnalytics.media("quran_audio", "play")
        com.example.core.analytics.AppAnalytics.content("quran_audio", "surah_$surah", "play")
        if (!requestFocus()) return
        player?.release()
        stopTicker()
        contextOrNull?.let(QuranAudioService::start)
        val candidate = StreamPlayer.build(appContext)
        player = candidate
        // Audio focus is requested by hand above, so the player must not take it too.
        candidate.setAudioAttributes(
            androidx.media3.common.AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA).build(), false
        )
        candidate.setWakeMode(C.WAKE_MODE_NETWORK)
        candidate.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (player !== candidate) return
                com.example.core.analytics.AppAnalytics.playbackState("quran_audio", playbackState)
                if (playbackState == Player.STATE_BUFFERING) {
                    _state.value = _state.value.copy(isBuffering = true)
                } else if (playbackState == Player.STATE_READY) {
                    _state.value = _state.value.copy(
                        playingSurah = surah, isPlaying = candidate.playWhenReady, error = null, isBuffering = false,
                        positionMs = candidate.currentPosition.playerMillis(),
                        durationMs = candidate.duration.playerMillis()
                    )
                    if (candidate.playWhenReady) startTicker()
                    QuranAudioService.update(contextOrNull)
                } else if (playbackState == Player.STATE_ENDED) {
                    rememberPosition(0)
                    if (++queueIndex < queue.size) prepare(queue[queueIndex])
                    else {
                        stopTicker()
                        candidate.pause()
                        candidate.seekTo(0)
                        _state.value = _state.value.copy(isPlaying = false, isBuffering = false, positionMs = 0)
                        QuranAudioService.update(contextOrNull)
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                if (player !== candidate) return
                com.example.core.analytics.AppAnalytics.mediaState("quran_audio", "failed", "playback_error")
                stopTicker()
                _state.value = _state.value.copy(
                    isPlaying = false, isBuffering = false,
                    error = "Audio could not be played. Check your internet connection or download."
                )
                QuranAudioService.update(contextOrNull)
            }
        })
        candidate.setMediaItem(MediaItem.fromUri(
            audioFile(surah).takeIf(File::exists)?.let(Uri::fromFile) ?: Uri.parse(audioUrl(surah))
        ))
        if (startPositionMs > 0) candidate.seekTo(startPositionMs.toLong())
        candidate.setPlaybackSpeed(_state.value.speed)
        candidate.prepare()
        candidate.play()
        _state.value = _state.value.copy(
            playingSurah = surah, isPlaying = false, error = null, isBuffering = true,
            positionMs = startPositionMs, durationMs = 0,
            resumeSurah = surah, resumePositionMs = startPositionMs
        )
        persist(surah, startPositionMs)
        QuranAudioService.update(contextOrNull)
    }

    private fun startTicker() {
        stopTicker()
        registerNoisy()
        ticker = scope.launch {
            while (true) {
                val current = player ?: break
                val position = current.currentPosition.playerMillis()
                _state.value = _state.value.copy(
                    positionMs = position,
                    durationMs = current.duration.playerMillis()
                )
                rememberPosition(position)
                delay(1000)
            }
        }
    }

    private fun stopTicker() { ticker?.cancel(); ticker = null; unregisterNoisy() }

    /** Registered only while playing, alongside the position ticker. */
    private fun registerNoisy() {
        if (noisyRegistered || !::appContext.isInitialized) return
        ContextCompat.registerReceiver(appContext, noisyReceiver,
            IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY), ContextCompat.RECEIVER_NOT_EXPORTED)
        noisyRegistered = true
    }

    private fun unregisterNoisy() {
        if (noisyRegistered) runCatching { appContext.unregisterReceiver(noisyReceiver) }
        noisyRegistered = false
    }

    private fun rememberPosition(positionMs: Int = _state.value.positionMs) {
        val surah = _state.value.playingSurah ?: return
        _state.value = _state.value.copy(resumeSurah = surah, resumePositionMs = positionMs)
        persist(surah, positionMs)
    }

    private fun persist(surah: Int, positionMs: Int) {
        if (!::appContext.isInitialized) return
        appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt(KEY_SURAH, surah).putInt(KEY_POSITION, positionMs).apply()
    }

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> player?.volume = 0.2f
            AudioManager.AUDIOFOCUS_GAIN -> player?.volume = 1f
        }
    }

    private val contextOrNull: Context? get() = if (::appContext.isInitialized) appContext else null

    private fun audioManager() = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private fun requestFocus(): Boolean {
        if (!::appContext.isInitialized) return false
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA).build()
                )
                .setOnAudioFocusChangeListener(focusListener)
                .build().also { focusRequest = it }
            audioManager().requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager().requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        return granted == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonFocus() {
        if (!::appContext.isInitialized) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager().abandonAudioFocusRequest(it) }
            focusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager().abandonAudioFocus(focusListener)
        }
    }

    fun download(surah: Int) {
        if (audioFile(surah).exists() || downloadJobs.containsKey(surah)) return
        downloadJobs[surah] = scope.launch {
            _state.value = _state.value.copy(downloading = _state.value.downloading + surah, error = null)
            val result = downloadSlots.withPermit { withContext(Dispatchers.IO) {
                runCatching {
                    val temp = File(audioDir, "%03d.part".format(surah))
                    var shown = -1
                    FastNet.download(audioUrl(surah), temp) { copied, total ->
                        val percent = if (total > 0) (copied * 100 / total).toInt() else -1
                        if (percent >= 0 && percent != shown) {
                            shown = percent
                            _state.update { it.copy(progress = it.progress + (surah to percent)) }
                        }
                    }
                    if (!temp.renameTo(audioFile(surah))) error("Could not save audio")
                }
            } }
            downloadJobs.remove(surah)
            _state.value = if (result.isSuccess) _state.value.copy(
                downloading = _state.value.downloading - surah,
                downloaded = _state.value.downloaded + surah,
                progress = _state.value.progress - surah
            ) else _state.value.copy(
                downloading = _state.value.downloading - surah,
                progress = _state.value.progress - surah,
                error = "Download failed. Check your internet connection and try again."
            )
        }
    }

    fun downloadAll(surahs: List<Int>) = surahs.distinct().forEach(::download)

    /** Stops a running download; the partial file is discarded and the row goes back to "Download". */
    fun cancelDownload(surah: Int) {
        val job = downloadJobs.remove(surah) ?: return
        job.cancel()
        _state.update { it.copy(downloading = it.downloading - surah, progress = it.progress - surah) }
    }

    fun delete(surah: Int) {
        if (_state.value.playingSurah == surah) stop()
        audioFile(surah).delete()
        _state.value = _state.value.copy(downloaded = _state.value.downloaded - surah)
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }

    fun stop() {
        com.example.core.analytics.AppAnalytics.media("quran_audio", "stop")
        rememberPosition()
        stopTicker()
        player?.release(); player = null
        abandonFocus()
        _state.value = _state.value.copy(playingSurah = null, isPlaying = false, isBuffering = false, positionMs = 0, durationMs = 0)
        if (::appContext.isInitialized) QuranAudioService.stop(appContext)
    }

    /** Title shown in the playback notification. */
    fun nowPlayingTitle(): String = _state.value.playingSurah?.let { number ->
        FullQuranCatalog.surahs.firstOrNull { it.number == number }
            ?.let { "$number. ${it.englishName}" } ?: "Surah $number"
    } ?: "Quran Audio"

    fun surahName(number: Int): String =
        FullQuranCatalog.surahs.firstOrNull { it.number == number }?.englishName ?: "Surah $number"

    private fun audioFile(surah: Int) = File(audioDir, "%03d.mp3".format(surah))
    private fun scanDownloads() = (1..114).filter { audioFile(it).exists() }.toSet()
    private fun audioUrl(surah: Int) = "https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/%03d.mp3".format(surah)
}
