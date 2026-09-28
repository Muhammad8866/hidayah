package com.example.quran

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import com.example.core.audio.PlaybackSpeed
import com.example.core.audio.applySpeed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

data class QuranAudioState(
    val playingSurah: Int? = null,
    val isPlaying: Boolean = false,
    val downloading: Set<Int> = emptySet(),
    val downloaded: Set<Int> = emptySet(),
    val progress: Map<Int, Int> = emptyMap(),
    val error: String? = null,
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
    private val client = OkHttpClient()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var player: MediaPlayer? = null
    private var queue: List<Int> = emptyList()
    private var queueIndex = 0
    private var pendingSeekMs = 0
    private var ticker: Job? = null
    private var focusRequest: AudioFocusRequest? = null
    private val downloadJobs = mutableMapOf<Int, Job>()
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
        _state.value = _state.value.copy(speed = speed)
        player?.takeIf { _state.value.isPlaying }?.applySpeed(speed)
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
        player?.takeIf { it.isPlaying }?.pause()
        stopTicker()
        rememberPosition()
        _state.value = _state.value.copy(isPlaying = false)
        QuranAudioService.update(contextOrNull)
    }

    fun resume() {
        val current = player ?: return resumeLast()
        if (!requestFocus()) return
        contextOrNull?.let(QuranAudioService::start)
        current.start()
        current.applySpeed(_state.value.speed)
        startTicker()
        _state.value = _state.value.copy(isPlaying = true)
        QuranAudioService.update(contextOrNull)
    }

    fun toggle() {
        if (_state.value.isPlaying) pause() else resume()
    }

    fun next() {
        if (queueIndex + 1 < queue.size) prepare(queue[++queueIndex])
        else _state.value.playingSurah?.takeIf { it < 114 }?.let { playQueue(listOf(it + 1)) }
    }

    fun previous() {
        if (queueIndex > 0) prepare(queue[--queueIndex])
        else _state.value.playingSurah?.takeIf { it > 1 }?.let { playQueue(listOf(it - 1)) }
    }

    fun seekTo(positionMs: Int) {
        player?.seekTo(positionMs)
        _state.value = _state.value.copy(positionMs = positionMs)
        rememberPosition(positionMs)
    }

    private fun prepare(surah: Int, startPositionMs: Int = 0) {
        if (!requestFocus()) return
        pendingSeekMs = startPositionMs
        player?.release()
        stopTicker()
        contextOrNull?.let(QuranAudioService::start)
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA).build()
            )
            setWakeMode(appContext, PowerManager.PARTIAL_WAKE_LOCK)
            setDataSource(audioFile(surah).takeIf(File::exists)?.absolutePath ?: audioUrl(surah))
            setOnPreparedListener {
                if (pendingSeekMs > 0) it.seekTo(pendingSeekMs)
                it.start()
                it.applySpeed(_state.value.speed)
                _state.value = _state.value.copy(
                    playingSurah = surah, isPlaying = true, error = null,
                    positionMs = pendingSeekMs, durationMs = it.duration.coerceAtLeast(0)
                )
                pendingSeekMs = 0
                startTicker()
                QuranAudioService.update(contextOrNull)
            }
            setOnCompletionListener {
                rememberPosition(0)
                if (++queueIndex < queue.size) prepare(queue[queueIndex])
                else {
                    stopTicker()
                    _state.value = _state.value.copy(isPlaying = false, positionMs = 0)
                    QuranAudioService.update(contextOrNull)
                }
            }
            setOnErrorListener { _, _, _ ->
                stopTicker()
                _state.value = _state.value.copy(
                    isPlaying = false,
                    error = "Audio could not be played. Check your internet connection or download."
                )
                QuranAudioService.update(contextOrNull)
                true
            }
            prepareAsync()
        }
        _state.value = _state.value.copy(
            playingSurah = surah, isPlaying = false, error = null,
            positionMs = startPositionMs, durationMs = 0,
            resumeSurah = surah, resumePositionMs = startPositionMs
        )
        persist(surah, startPositionMs)
        QuranAudioService.update(contextOrNull)
    }

    private fun startTicker() {
        stopTicker()
        ticker = scope.launch {
            while (true) {
                val current = player ?: break
                val position = runCatching { current.currentPosition }.getOrDefault(0)
                _state.value = _state.value.copy(
                    positionMs = position,
                    durationMs = runCatching { current.duration }.getOrDefault(0).coerceAtLeast(0)
                )
                rememberPosition(position)
                delay(1000)
            }
        }
    }

    private fun stopTicker() { ticker?.cancel(); ticker = null }

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
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> player?.setVolume(0.2f, 0.2f)
            AudioManager.AUDIOFOCUS_GAIN -> player?.setVolume(1f, 1f)
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
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val request = Request.Builder().url(audioUrl(surah)).build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) error("HTTP ${response.code}")
                        val body = response.body ?: error("Empty audio")
                        val temp = File(audioDir, "%03d.part".format(surah))
                        val total = body.contentLength()
                        body.byteStream().use { input -> temp.outputStream().use { output ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE); var copied = 0L
                            while (true) {
                                val count = input.read(buffer); if (count < 0) break
                                output.write(buffer, 0, count); copied += count
                                if (total > 0) _state.value = _state.value.copy(progress = _state.value.progress + (surah to (copied * 100 / total).toInt()))
                            }
                        } }
                        if (!temp.renameTo(audioFile(surah))) error("Could not save audio")
                    }
                }
            }
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

    fun delete(surah: Int) {
        if (_state.value.playingSurah == surah) stop()
        audioFile(surah).delete()
        _state.value = _state.value.copy(downloaded = _state.value.downloaded - surah)
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }

    fun stop() {
        rememberPosition()
        stopTicker()
        player?.release(); player = null
        abandonFocus()
        _state.value = _state.value.copy(playingSurah = null, isPlaying = false, positionMs = 0, durationMs = 0)
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
