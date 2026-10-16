package com.example.naat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.core.audio.PlaybackSpeed
import com.example.core.net.FastNet
import com.example.core.net.StreamPlayer
import com.example.core.net.playerMillis
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.File

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
    val speed: Float = PlaybackSpeed.NORMAL,
    /** Downloads that failed last time, so their row can offer a retry. */
    val failed: Set<String> = emptySet(),
    val favorites: Set<String> = emptySet(),
    /** Most recently started first. */
    val recent: List<String> = emptyList(),
    /** The last naat heard and where it was left, for "Continue listening". */
    val resumeId: String? = null,
    val resumePositionMs: Int = 0
)

/** Kept apart from [NaatAudioState] so the seek bar can tick without recomposing the whole list. */
data class NaatPosition(val positionMs: Int = 0, val durationMs: Int = 0)

object NaatAudioManager {
    private const val PLAY_ERROR = "Audio could not be played. Check your internet connection or download."
    private lateinit var context: Context
    private lateinit var dir: File
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val jobs = mutableMapOf<String, Job>()
    private val downloadSlots = Semaphore(3)
    private var player: ExoPlayer? = null
    private var prepared = false
    private var ticker: Job? = null
    private var queue: List<NaatTrack> = emptyList()
    private var history = ArrayDeque<String>()
    /** Shuffle plays every naat in the queue once before any repeats. */
    private var shuffleBag = mutableListOf<String>()
    /** Where the next prepared track should start, set by [resumeLast]. */
    private var pendingSeekMs = 0
    private const val MAX_RECENT = 12
    private const val LIST_SEPARATOR = "\n"
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
        val prefs = prefs()
        val speed = PlaybackSpeed.sanitize(prefs.getFloat("speed", PlaybackSpeed.NORMAL))
        _state.update { it.copy(
            downloaded = saved,
            speed = speed,
            favorites = loadList("favorites").toSet(),
            recent = loadList("recent").take(MAX_RECENT),
            resumeId = prefs.getString("resume_id", null),
            resumePositionMs = prefs.getInt("resume_pos", 0).coerceAtLeast(0)
        ) }
        NaatCatalog.refresh(FastNet.client, scope)
    }

    fun setSpeed(speed: Float) {
        com.example.core.analytics.AppAnalytics.media("naat_audio", "speed_change")
        _state.update { it.copy(speed = speed) }
        player?.setPlaybackSpeed(speed)
        if (::context.isInitialized) prefs().edit().putFloat("speed", speed).apply()
    }

    private fun prefs() = context.getSharedPreferences("naat_audio", Context.MODE_PRIVATE)

    private fun loadList(key: String): List<String> =
        prefs().getString(key, null)?.split(LIST_SEPARATOR)?.filter(String::isNotBlank).orEmpty()

    private fun saveList(key: String, ids: Collection<String>) {
        if (::context.isInitialized) prefs().edit().putString(key, ids.joinToString(LIST_SEPARATOR)).apply()
    }

    fun toggleFavorite(id: String) {
        com.example.core.analytics.AppAnalytics.media("naat_audio", "favorite_toggle")
        _state.update { s -> s.copy(favorites = if (id in s.favorites) s.favorites - id else s.favorites + id) }
        saveList("favorites", _state.value.favorites)
    }

    /** Starts the last naat heard from where it was left. */
    fun resumeLast(list: List<NaatTrack>) {
        val s = _state.value
        val track = list.firstOrNull { it.id == s.resumeId } ?: return
        com.example.core.analytics.AppAnalytics.media("naat_audio", "resume_last")
        pendingSeekMs = s.resumePositionMs.takeIf { it > 3_000 } ?: 0
        play(track, list)
    }

    /** Saves the active naat and its position; near the end it counts as finished and restarts next time. */
    private fun rememberPosition() {
        val id = _state.value.activeId ?: return
        val (pos, duration) = _position.value
        val keep = if (duration > 0 && pos > duration - 5_000) 0 else pos
        _state.update { it.copy(resumeId = id, resumePositionMs = keep) }
        if (::context.isInitialized) prefs().edit().putString("resume_id", id).putInt("resume_pos", keep).apply()
    }

    private fun refillShuffle(excluding: String?) {
        shuffleBag = queue.map(NaatTrack::id).filter { it != excluding }.shuffled().toMutableList()
    }

    /** Plays [track]; [list] becomes the queue used for next, previous and auto-play. */
    fun play(track: NaatTrack, list: List<NaatTrack> = queue.ifEmpty { listOf(track) }) {
        if (!::context.isInitialized) return
        if (list != queue) {
            queue = list
            shuffleBag.clear()
            if (_state.value.shuffle) refillShuffle(track.id)
        }
        if (_state.value.activeId == track.id && player != null) {
            pendingSeekMs = 0
            if (player?.playWhenReady == true) pause() else resume()
            return
        }
        start(track)
    }

    fun togglePlayPause() {
        val current = activeTrack() ?: return
        if (player == null) start(current) else if (player?.playWhenReady == true) pause() else resume()
    }

    fun next() {
        com.example.core.analytics.AppAnalytics.media("naat_audio", "next")
        pickNext(userAction = true)?.let(::start)
    }

    fun previous() {
        com.example.core.analytics.AppAnalytics.media("naat_audio", "previous")
        val active = activeTrack() ?: return
        if (_position.value.positionMs > 3_000 || queue.size < 2) { seekTo(0); return }
        val previousId = if (_state.value.shuffle) history.removeLastOrNull() else null
        val index = queue.indexOfFirst { it.id == active.id }
        val target = queue.firstOrNull { it.id == previousId } ?: queue.getOrNull(if (index <= 0) queue.lastIndex else index - 1) ?: return
        start(target, remember = false)
    }

    fun seekTo(ms: Int) {
        com.example.core.analytics.AppAnalytics.media("naat_audio", "seek")
        if (!prepared) return
        player?.seekTo(ms.toLong())
        _position.update { it.copy(positionMs = ms) }
    }

    fun cycleRepeat() = _state.update {
        it.copy(repeat = NaatRepeat.entries[(it.repeat.ordinal + 1) % NaatRepeat.entries.size])
    }.also {
        com.example.core.analytics.AppAnalytics.media("naat_audio", "repeat_change")
        refreshHasNext()
    }

    fun toggleShuffle() = _state.update { it.copy(shuffle = !it.shuffle) }.also {
        com.example.core.analytics.AppAnalytics.media("naat_audio", "shuffle_change")
        if (_state.value.shuffle) refillShuffle(_state.value.activeId) else shuffleBag.clear()
        refreshHasNext()
    }

    private fun activeTrack() = _state.value.activeId?.let { id -> queue.firstOrNull { it.id == id } }

    private fun pickNext(userAction: Boolean): NaatTrack? {
        val active = activeTrack() ?: return null
        val s = _state.value
        if (!userAction && s.repeat == NaatRepeat.ONE) return active
        if (s.shuffle && queue.size > 1) {
            shuffleBag.removeAll { id -> id == active.id || queue.none { it.id == id } }
            if (shuffleBag.isEmpty()) {
                // One full pass is done: stop, unless repeating or the listener asked for more.
                if (s.repeat == NaatRepeat.OFF && !userAction) return null
                refillShuffle(active.id)
            }
            val id = shuffleBag.removeFirstOrNull() ?: return null
            return queue.firstOrNull { it.id == id }
        }
        val index = queue.indexOfFirst { it.id == active.id }
        return when {
            index in 0 until queue.lastIndex -> queue[index + 1]
            s.repeat != NaatRepeat.OFF || userAction -> queue.firstOrNull()
            else -> null
        }
    }

    private fun refreshHasNext() = _state.update { s ->
        val index = queue.indexOfFirst { it.id == s.activeId }
        val shuffleLeft = shuffleBag.any { it != s.activeId }
        s.copy(hasNext = queue.size > 1 && when {
            s.repeat != NaatRepeat.OFF -> true
            s.shuffle -> shuffleLeft
            else -> index < queue.lastIndex
        })
    }

    private fun start(track: NaatTrack, remember: Boolean = true) {
        com.example.core.analytics.AppAnalytics.media("naat_audio", "play")
        com.example.core.analytics.AppAnalytics.content("naat_audio", track.performer, "play")
        // Only one of Naat, Quran and Tafseer plays at a time.
        com.example.quran.QuranAudioManager.stop()
        com.example.quran.TafsirAudioManager.stop()
        com.example.azan.AzanAudioManager.stop()
        _state.value.activeId?.takeIf { remember && it != track.id }?.let { history.addLast(it); if (history.size > 50) history.removeFirst() }
        // Leaving a naat part-way keeps its place; the new one starts where [resumeLast] asked.
        if (prepared && _state.value.activeId != track.id) rememberPosition()
        val seekTo = pendingSeekMs
        pendingSeekMs = 0
        shuffleBag.remove(track.id)
        val recent = (listOf(track.id) + _state.value.recent.filter { it != track.id }).take(MAX_RECENT)
        _state.update { it.copy(recent = recent) }
        saveList("recent", recent)
        releasePlayer()
        _state.update { it.copy(activeId = track.id, playing = false, buffering = true, error = null) }
        _position.value = NaatPosition(durationMs = 0)
        refreshHasNext()
        // Streaming with no connection would spin for a long timeout first; say so at once instead.
        if (!file(track).isFile && !com.example.core.net.Connectivity.isOnline.value) {
            com.example.core.analytics.AppAnalytics.mediaState("naat_audio", "failed", "offline")
            _state.update { it.copy(activeId = null, playing = false, buffering = false, hasNext = false, error = PLAY_ERROR) }
            NaatAudioService.stop(context)
            return
        }
        NaatAudioService.start(context)
        try {
            val candidate = StreamPlayer.build(context)
            player = candidate
            // Audio focus is requested by hand in resume(), so the player must not take it too.
            candidate.setAudioAttributes(
                androidx.media3.common.AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(), false
            )
            candidate.setWakeMode(C.WAKE_MODE_NETWORK)
            candidate.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (player !== candidate) return
                com.example.core.analytics.AppAnalytics.playbackState("naat_audio", playbackState)
                    when (playbackState) {
                        Player.STATE_BUFFERING -> _state.update { it.copy(buffering = true) }
                        Player.STATE_READY -> {
                            _state.update { it.copy(buffering = false, error = null) }
                            _position.update { it.copy(durationMs = candidate.duration.playerMillis()) }
                            if (!prepared) {
                                prepared = true
                                if (seekTo > 0) {
                                    candidate.seekTo(seekTo.toLong())
                                    _position.update { it.copy(positionMs = seekTo) }
                                }
                                resume()
                            }
                        }
                        Player.STATE_ENDED -> {
                            // A finished naat starts from the beginning next time.
                            _position.update { it.copy(positionMs = it.durationMs) }
                            rememberPosition()
                            val upcoming = pickNext(userAction = false)
                            if (upcoming != null) start(upcoming) else {
                                _state.update { it.copy(playing = false) }
                                _position.update { it.copy(positionMs = 0) }
                                releasePlayer()
                                NaatAudioService.update(context)
                            }
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    if (player !== candidate) return
                com.example.core.analytics.AppAnalytics.mediaState("naat_audio", "failed", "playback_error")
                    releasePlayer()
                    _state.update { it.copy(playing = false, buffering = false, error = PLAY_ERROR) }
                    NaatAudioService.update(context)
                }
            })
            candidate.setMediaItem(MediaItem.fromUri(file(track).takeIf(File::isFile)?.let(Uri::fromFile) ?: Uri.parse(track.url)))
            candidate.setPlaybackSpeed(_state.value.speed)
            candidate.prepare()
        } catch (_: Exception) {
            releasePlayer()
            _state.update { it.copy(playing = false, buffering = false, error = PLAY_ERROR) }
        }
    }

    private fun resume() {
        com.example.core.analytics.AppAnalytics.media("naat_audio", "resume")
        val current = player?.takeIf { prepared } ?: return
        requestFocus()
        current.play()
        if (!noisyRegistered) {
            ContextCompat.registerReceiver(context, noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),
                ContextCompat.RECEIVER_NOT_EXPORTED)
            noisyRegistered = true
        }
        _state.update { it.copy(playing = true) }
        NaatAudioService.update(context)
        ticker?.cancel()
        ticker = scope.launch {
            var ticks = 0
            while (isActive) {
                player?.takeIf { prepared }?.let { p -> _position.update { it.copy(positionMs = p.currentPosition.playerMillis()) } }
                // Every ten seconds, so a killed app still knows roughly where the listener was.
                if (++ticks % 20 == 0) rememberPosition()
                delay(500)
            }
        }
    }

    fun pause() {
        com.example.core.analytics.AppAnalytics.media("naat_audio", "pause")
        player?.takeIf { prepared }?.pause()
        ticker?.cancel()
        unregisterNoisy()
        if (prepared) rememberPosition()
        _state.update { it.copy(playing = false) }
        if (::context.isInitialized) NaatAudioService.update(context)
    }

    fun stop() {
        com.example.core.analytics.AppAnalytics.media("naat_audio", "stop")
        if (!::context.isInitialized) return
        if (prepared) rememberPosition()
        releasePlayer()
        queue = emptyList()
        history.clear()
        shuffleBag.clear()
        pendingSeekMs = 0
        _state.update { it.copy(activeId = null, playing = false, buffering = false, hasNext = false) }
        _position.value = NaatPosition()
        NaatAudioService.stop(context)
    }

    /** Title and toggle for the playback notification and media session. */
    internal fun activeTitle(): String? = activeTrack()?.title

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
        if (!com.example.core.net.Connectivity.isOnline.value) {
            _state.update { it.copy(error = "Download failed. Check your internet connection and try again.") }
            return
        }
        _state.update { it.copy(downloading = it.downloading + track.id, failed = it.failed - track.id, error = null) }
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
                    failed = if (failed) it.failed + track.id else it.failed,
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
            var shown = -1
            FastNet.download(track.url, temp) { copied, total ->
                val percent = if (total > 0) (copied * 100 / total).toInt() else -1
                if (percent != shown) {
                    shown = percent
                    _state.update { it.copy(progress = it.progress + (track.id to percent)) }
                }
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
