package com.example.naat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.example.core.audio.PlaybackSpeed
import com.example.core.audio.applySpeed
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.File
import kotlin.coroutines.coroutineContext

data class NaatTrack(
    val id: String,
    val title: String,
    val performer: String,
    val category: String,
    val url: String,
    val sizeBytes: Long = 0
)

/** Voice-only naats bundled in assets/naat_catalog.json; GitHub can publish an updated list. */
object NaatCatalog {
    private const val ASSET = "naat_catalog.json"
    private const val CATALOG_URL = "https://raw.githubusercontent.com/Muhammad8866/deen-companion-naat/main/catalog.json"
    private val allowedHosts = setOf("archive.org", "alhajowaisrazaqadri.com", "github.com")

    private val _current = MutableStateFlow<List<NaatTrack>>(emptyList())
    val current = _current.asStateFlow()

    fun load(context: Context) {
        if (_current.value.isNotEmpty()) return
        _current.value = runCatching {
            parse(context.assets.open(ASSET).bufferedReader().use { it.readText() })
        }.getOrDefault(emptyList())
    }

    fun refresh(client: OkHttpClient, scope: CoroutineScope) {
        scope.launch {
            val fetched = withContext(Dispatchers.IO) {
                runCatching {
                    client.newCall(Request.Builder().url(CATALOG_URL).build()).execute().use { response ->
                        if (!response.isSuccessful) error("HTTP ${response.code}")
                        parse(response.body?.string() ?: error("Empty catalog"))
                    }
                }.getOrNull()
            }
            // Only replace the bundled list with a complete, well-formed remote list.
            if (fetched != null && fetched.size >= _current.value.size / 2) _current.value = fetched
        }
    }

    private fun parse(text: String): List<NaatTrack> {
        val json = JSONArray(text)
        val tracks = (0 until json.length()).map { index ->
            val item = json.getJSONObject(index)
            val url = item.getString("url")
            val host = Uri.parse(url).host.orEmpty()
            require(url.startsWith("https://") && allowedHosts.any { host == it || host.endsWith(".$it") }) { "Untrusted URL" }
            if (host == "github.com") require(url.startsWith("https://github.com/Muhammad8866/deen-companion-naat/releases/download/"))
            NaatTrack(item.getString("id"), item.getString("title"), item.getString("performer"),
                item.optString("category", "Naat"), url, item.optLong("size", 0))
        }
        require(tracks.isNotEmpty() && tracks.map(NaatTrack::id).toSet().size == tracks.size) { "Invalid catalog" }
        return tracks
    }
}

enum class NaatRepeat { OFF, ALL, ONE }

data class NaatAudioState(
    val activeId: String? = null,
    val playing: Boolean = false,
    val buffering: Boolean = false,
    val repeat: NaatRepeat = NaatRepeat.OFF,
    val shuffle: Boolean = false,
    val hasNext: Boolean = false,
    val downloaded: Set<String> = emptySet(),
    val downloading: Set<String> = emptySet(),
    val progress: Map<String, Int> = emptyMap(),
    val error: String? = null,
    val speed: Float = PlaybackSpeed.NORMAL
)

/** Kept apart from [NaatAudioState] so the seek bar can tick without recomposing the whole list. */
data class NaatPosition(val positionMs: Int = 0, val durationMs: Int = 0)

object NaatAudioManager {
    private const val PLAY_ERROR = "Audio could not be played. Check your internet connection or download."
    private lateinit var context: Context
    private lateinit var dir: File
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val client = OkHttpClient()
    private val jobs = mutableMapOf<String, Job>()
    private val downloadSlots = Semaphore(3)
    private var player: MediaPlayer? = null
    private var prepared = false
    private var ticker: Job? = null
    private var queue: List<NaatTrack> = emptyList()
    private var history = ArrayDeque<String>()
    private val _state = MutableStateFlow(NaatAudioState())
    val state = _state.asStateFlow()
    private val _position = MutableStateFlow(NaatPosition())
    val position = _position.asStateFlow()

    private val audioManager by lazy { context.getSystemService(AudioManager::class.java) }
    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
        }
    }
    private val focusRequest by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attributes).setOnAudioFocusChangeListener(focusListener).build() else null
    }
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
    private var noisyRegistered = false
    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) { pause() } // headphones unplugged
    }

    fun init(applicationContext: Context) {
        if (::context.isInitialized) return
        context = applicationContext.applicationContext
        dir = File(context.filesDir, "naat_audio").apply { mkdirs() }
        NaatCatalog.load(context)
        val saved = dir.listFiles().orEmpty().filter { it.isFile && it.extension == "mp3" }.map { it.nameWithoutExtension }.toSet()
        val speed = PlaybackSpeed.sanitize(prefs().getFloat("speed", PlaybackSpeed.NORMAL))
        _state.update { it.copy(downloaded = saved, speed = speed) }
        NaatCatalog.refresh(client, scope)
    }

    fun setSpeed(speed: Float) {
        _state.update { it.copy(speed = speed) }
        player?.takeIf { prepared && it.isPlaying }?.applySpeed(speed)
        if (::context.isInitialized) prefs().edit().putFloat("speed", speed).apply()
    }

    private fun prefs() = context.getSharedPreferences("naat_audio", Context.MODE_PRIVATE)

    /** Plays [track]; [list] becomes the queue used for next, previous and auto-play. */
    fun play(track: NaatTrack, list: List<NaatTrack> = queue.ifEmpty { listOf(track) }) {
        if (!::context.isInitialized) return
        queue = list
        if (_state.value.activeId == track.id && player != null) {
            if (player?.isPlaying == true) pause() else resume()
            return
        }
        start(track)
    }

    fun togglePlayPause() {
        val current = activeTrack() ?: return
        if (player == null) start(current) else if (player?.isPlaying == true) pause() else resume()
    }

    fun next() = pickNext(userAction = true)?.let(::start) ?: Unit

    fun previous() {
        val active = activeTrack() ?: return
        if (_position.value.positionMs > 3_000 || queue.size < 2) { seekTo(0); return }
        val previousId = if (_state.value.shuffle) history.removeLastOrNull() else null
        val index = queue.indexOfFirst { it.id == active.id }
        val target = queue.firstOrNull { it.id == previousId } ?: queue.getOrNull(if (index <= 0) queue.lastIndex else index - 1) ?: return
        start(target, remember = false)
    }

    fun seekTo(ms: Int) {
        if (!prepared) return
        player?.seekTo(ms)
        _position.update { it.copy(positionMs = ms) }
    }

    fun cycleRepeat() = _state.update {
        it.copy(repeat = NaatRepeat.entries[(it.repeat.ordinal + 1) % NaatRepeat.entries.size])
    }.also { refreshHasNext() }

    fun toggleShuffle() = _state.update { it.copy(shuffle = !it.shuffle) }.also { refreshHasNext() }

    private fun activeTrack() = _state.value.activeId?.let { id -> queue.firstOrNull { it.id == id } }

    private fun pickNext(userAction: Boolean): NaatTrack? {
        val active = activeTrack() ?: return null
        val s = _state.value
        if (!userAction && s.repeat == NaatRepeat.ONE) return active
        if (s.shuffle && queue.size > 1) return queue.filter { it.id != active.id }.random()
        val index = queue.indexOfFirst { it.id == active.id }
        return when {
            index in 0 until queue.lastIndex -> queue[index + 1]
            s.repeat != NaatRepeat.OFF || userAction -> queue.firstOrNull()
            else -> null
        }
    }

    private fun refreshHasNext() = _state.update { s ->
        val index = queue.indexOfFirst { it.id == s.activeId }
        s.copy(hasNext = queue.size > 1 && (s.shuffle || s.repeat != NaatRepeat.OFF || index < queue.lastIndex))
    }

    private fun start(track: NaatTrack, remember: Boolean = true) {
        _state.value.activeId?.takeIf { remember && it != track.id }?.let { history.addLast(it); if (history.size > 50) history.removeFirst() }
        releasePlayer()
        _state.update { it.copy(activeId = track.id, playing = false, buffering = true, error = null) }
        _position.value = NaatPosition(durationMs = 0)
        refreshHasNext()
        try {
            player = MediaPlayer().apply {
                setAudioAttributes(attributes)
                setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
                setDataSource(file(track).takeIf(File::isFile)?.absolutePath ?: track.url)
                setOnPreparedListener { mp ->
                    prepared = true
                    _position.value = NaatPosition(0, mp.duration.coerceAtLeast(0))
                    _state.update { it.copy(buffering = false) }
                    resume()
                }
                setOnInfoListener { _, what, _ ->
                    when (what) {
                        MediaPlayer.MEDIA_INFO_BUFFERING_START -> _state.update { it.copy(buffering = true) }
                        MediaPlayer.MEDIA_INFO_BUFFERING_END -> _state.update { it.copy(buffering = false) }
                    }
                    false
                }
                setOnCompletionListener {
                    val upcoming = pickNext(userAction = false)
                    if (upcoming != null) start(upcoming) else {
                        _state.update { it.copy(playing = false) }
                        _position.update { it.copy(positionMs = 0) }
                        releasePlayer()
                    }
                }
                setOnErrorListener { _, _, _ ->
                    releasePlayer()
                    _state.update { it.copy(playing = false, buffering = false, error = PLAY_ERROR) }
                    true
                }
                prepareAsync()
            }
        } catch (_: Exception) {
            releasePlayer()
            _state.update { it.copy(playing = false, buffering = false, error = PLAY_ERROR) }
        }
    }

    private fun resume() {
        val current = player?.takeIf { prepared } ?: return
        requestFocus()
        current.start()
        current.applySpeed(_state.value.speed)
        if (!noisyRegistered) {
            ContextCompat.registerReceiver(context, noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),
                ContextCompat.RECEIVER_NOT_EXPORTED)
            noisyRegistered = true
        }
        _state.update { it.copy(playing = true) }
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                player?.takeIf { prepared }?.let { p -> _position.update { it.copy(positionMs = p.currentPosition) } }
                delay(500)
            }
        }
    }

    fun pause() {
        player?.takeIf { prepared && it.isPlaying }?.pause()
        ticker?.cancel()
        unregisterNoisy()
        _state.update { it.copy(playing = false) }
    }

    fun stop() {
        releasePlayer()
        queue = emptyList()
        history.clear()
        _state.update { it.copy(activeId = null, playing = false, buffering = false, hasNext = false) }
        _position.value = NaatPosition()
    }

    private fun releasePlayer() {
        ticker?.cancel()
        player?.release()
        player = null
        prepared = false
        unregisterNoisy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) focusRequest?.let(audioManager::abandonAudioFocusRequest)
        else @Suppress("DEPRECATION") audioManager.abandonAudioFocus(focusListener)
    }

    private fun requestFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) focusRequest?.let(audioManager::requestAudioFocus)
        else @Suppress("DEPRECATION") audioManager.requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
    }

    private fun unregisterNoisy() {
        if (noisyRegistered) runCatching { context.unregisterReceiver(noisyReceiver) }
        noisyRegistered = false
    }

    fun download(track: NaatTrack) {
        if (!::dir.isInitialized || file(track).isFile || jobs.containsKey(track.id)) return
        _state.update { it.copy(downloading = it.downloading + track.id, error = null) }
        jobs[track.id] = scope.launch {
            var result: Result<Unit> = Result.failure(CancellationException())
            try {
                result = downloadSlots.withPermit { withContext(Dispatchers.IO) { runCatching { fetch(track) } } }
            } finally {
                jobs.remove(track.id)
                val ok = result.isSuccess
                val failed = result.exceptionOrNull().let { it != null && it !is CancellationException }
                _state.update { it.copy(
                    downloading = it.downloading - track.id,
                    downloaded = if (ok) it.downloaded + track.id else it.downloaded,
                    progress = it.progress - track.id,
                    error = if (failed) "Download failed. Check your internet connection and try again." else it.error
                ) }
            }
        }
    }

    /** Queues every track that is not yet on the device; at most three download at once. */
    fun downloadAll(tracks: List<NaatTrack>) = tracks.forEach(::download)

    fun cancelDownloads() = jobs.values.toList().forEach(Job::cancel)

    private suspend fun fetch(track: NaatTrack) {
        val temp = File(dir, "${track.id}.part")
        try {
            client.newCall(Request.Builder().url(track.url).build()).execute().use { response ->
                if (!response.isSuccessful) error("HTTP ${response.code}")
                val body = response.body ?: error("Empty audio")
                val total = body.contentLength()
                var copied = 0L
                var shown = -1
                body.byteStream().use { input -> temp.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        coroutineContext.ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                        copied += count
                        val percent = if (total > 0) (copied * 100 / total).toInt() else -1
                        if (percent != shown) {
                            shown = percent
                            _state.update { it.copy(progress = it.progress + (track.id to percent)) }
                        }
                    }
                } }
                if (total > 0 && copied != total) error("Incomplete audio")
            }
            if (temp.length() == 0L || !temp.renameTo(file(track))) error("Could not save audio")
        } finally { if (temp.exists()) temp.delete() }
    }

    fun delete(track: NaatTrack) = deleteAll(listOf(track))

    fun deleteAll(tracks: List<NaatTrack>) {
        val ids = tracks.map(NaatTrack::id).toSet()
        // Files cannot be removed from under a playing MediaPlayer, so stop it first.
        if (_state.value.activeId in ids && player != null) {
            releasePlayer()
            _state.update { it.copy(playing = false, buffering = false) }
            _position.value = NaatPosition()
        }
        tracks.forEach { file(it).delete() }
        _state.update { it.copy(downloaded = it.downloaded - ids) }
    }

    fun clearError() { _state.update { it.copy(error = null) } }

    private fun file(track: NaatTrack) = File(dir, "${track.id}.mp3")
}
