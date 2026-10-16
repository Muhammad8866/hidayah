package com.example.quran

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.core.net.FastNet
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.persistedAppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File

data class WbwDownloadState(
    val downloaded: Set<Int> = emptySet(),
    /** Surahs waiting in the queue or downloading, with progress 0..1 (0 while still queued). */
    val active: Map<Int, Float> = emptyMap(),
    val usedBytes: Long = 0
) {
    fun isDownloaded(surah: Int) = surah in downloaded
    fun isActive(surah: Int) = surah in active
}

/**
 * Keeps the word-by-word recitation (one Alafasy file per surah) on the device for offline listening.
 * Downloads run one at a time in the app process, so they continue while the user reads elsewhere.
 */
object WbwAudioDownloads {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val main = Handler(Looper.getMainLooper())
    private val _state = MutableStateFlow(WbwDownloadState())
    val state: StateFlow<WbwDownloadState> = _state.asStateFlow()

    private lateinit var appContext: Context
    private lateinit var dir: File
    private val pending = ArrayDeque<Pair<Int, SurahAudio>>()
    private var worker: Job? = null
    private var current: Int? = null
    private var currentJob: Job? = null
    private var cancelled = false

    fun init(context: Context) {
        if (::dir.isInitialized) return
        appContext = context.applicationContext
        dir = File(appContext.filesDir, "wbw_audio").apply { mkdirs() }
        dir.listFiles { f -> f.name.endsWith(".part") }?.forEach(File::delete)
        refresh()
    }

    /** The downloaded recitation for [surah], or null when it must be streamed. */
    fun localFile(surah: Int): File? = if (::dir.isInitialized) file(surah).takeIf(File::exists) else null

    fun enqueue(surahs: List<Int>, audio: List<SurahAudio>) {
        synchronized(this) {
            surahs.distinct()
                .filter { it !in _state.value.downloaded && it != current && pending.none { p -> p.first == it } }
                .forEach { pending.addLast(it to audio[it - 1]) }
            publishQueue()
            if (worker?.isActive != true) worker = scope.launch { drain() }
        }
    }

    fun cancel(surahs: Collection<Int>) = synchronized(this) {
        pending.removeAll { it.first in surahs }
        if (current in surahs) { cancelled = true; currentJob?.cancel() }
        publishQueue()
    }

    fun delete(surahs: Collection<Int>) {
        cancel(surahs)
        surahs.forEach { file(it).delete() }
        refresh()
    }

    fun deleteAll() = delete((1..114).toList())

    private suspend fun drain() {
        while (true) {
            val (surah, audio) = synchronized(this) {
                pending.removeFirstOrNull()?.also { current = it.first; cancelled = false }
            } ?: break
            try {
                // A child job so one surah can be cancelled without stopping the queue.
                coroutineScope {
                    val job = launch { fetch(surah, audio) }
                    synchronized(this@WbwAudioDownloads) { currentJob = job; if (cancelled) job.cancel() }
                }
            } catch (e: Exception) {
                if (!cancelled) toast("Download stopped. Check your internet connection and try again.")
            } finally {
                file(surah, ".part").delete()
                synchronized(this) { current = null; currentJob = null }
                refresh()
            }
        }
    }

    /** Tries each mirror in turn; a cancel stops immediately instead of moving on to the next one. */
    private suspend fun fetch(surah: Int, audio: SurahAudio) {
        var failure: Exception? = null
        for (url in audio.urls) {
            try { fetch(surah, url, audio.bytes); return } catch (e: Exception) {
                if (cancelled) throw e
                failure = e
            }
        }
        throw failure ?: IllegalStateException("No download source")
    }

    private suspend fun fetch(surah: Int, url: String, expectedBytes: Long) {
        val temp = file(surah, ".part")
        try {
            var lastPublished = 0f
            FastNet.download(url, temp) { copied, total ->
                val progress = (copied.toFloat() / (if (total > 0) total else expectedBytes)).coerceIn(0f, 1f)
                if (progress - lastPublished >= .01f) {
                    lastPublished = progress
                    _state.update { it.copy(active = it.active + (surah to progress)) }
                }
            }
            if (!temp.renameTo(file(surah))) error("Could not save audio")
        } finally {
            temp.delete()
        }
    }

    private fun publishQueue() {
        val queued = pending.map { it.first } + listOfNotNull(current)
        _state.update { s -> s.copy(active = queued.associateWith { s.active[it] ?: 0f }) }
    }

    private fun refresh() {
        val files = (1..114).associateWith(::file).filterValues(File::exists)
        synchronized(this) {
            val queued = pending.map { it.first } + listOfNotNull(current)
            _state.update { s ->
                s.copy(downloaded = files.keys, usedBytes = files.values.sumOf(File::length), active = queued.associateWith { s.active[it] ?: 0f })
            }
        }
    }

    private fun file(surah: Int, suffix: String = ".mp3") = File(dir, "%03d%s".format(surah, suffix))

    private fun toast(message: String) = main.post {
        val text = localizeUiText(message, persistedAppLanguage(appContext))
        Toast.makeText(appContext, text, Toast.LENGTH_LONG).show()
    }
}
