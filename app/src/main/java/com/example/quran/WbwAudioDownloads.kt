package com.example.quran

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
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
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

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
    private val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val main = Handler(Looper.getMainLooper())
    private val _state = MutableStateFlow(WbwDownloadState())
    val state: StateFlow<WbwDownloadState> = _state.asStateFlow()

    private lateinit var appContext: Context
    private lateinit var dir: File
    private val pending = ArrayDeque<Pair<Int, SurahAudio>>()
    private var worker: Job? = null
    private var current: Int? = null
    private var currentCall: Call? = null
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
        if (current in surahs) { cancelled = true; currentCall?.cancel() }
        publishQueue()
    }

    fun delete(surahs: Collection<Int>) {
        cancel(surahs)
        surahs.forEach { file(it).delete() }
        refresh()
    }

    fun deleteAll() = delete((1..114).toList())

    private fun drain() {
        while (true) {
            val (surah, audio) = synchronized(this) {
                pending.removeFirstOrNull()?.also { current = it.first; cancelled = false }
            } ?: break
            try {
                fetch(surah, audio)
            } catch (e: Exception) {
                if (!cancelled) toast("Download stopped. Check your internet connection and try again.")
            } finally {
                file(surah, ".part").delete()
                synchronized(this) { current = null; currentCall = null }
                refresh()
            }
        }
    }

    /** Tries each mirror in turn; a cancel stops immediately instead of moving on to the next one. */
    private fun fetch(surah: Int, audio: SurahAudio) {
        var failure: Exception? = null
        for (url in audio.urls) {
            try { fetch(surah, url, audio.bytes); return } catch (e: Exception) {
                if (cancelled) throw e
                failure = e
            }
        }
        throw failure ?: IllegalStateException("No download source")
    }

    private fun fetch(surah: Int, url: String, expectedBytes: Long) {
        val call = client.newCall(Request.Builder().url(url).build())
        synchronized(this) { currentCall = call; if (cancelled) call.cancel() }
        call.execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code}")
            val body = response.body ?: error("Empty response")
            if (body.contentType()?.type.equals("text", ignoreCase = true)) error("Source returned a web page instead of audio")
            val total = body.contentLength().takeIf { it > 0 } ?: expectedBytes
            val temp = file(surah, ".part")
            try {
            var copied = 0L
            var lastPublished = 0f
            body.byteStream().use { input ->
                temp.outputStream().buffered().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                        copied += count
                        val progress = (copied.toFloat() / total).coerceIn(0f, 1f)
                        if (progress - lastPublished >= .01f) {
                            lastPublished = progress
                            _state.update { it.copy(active = it.active + (surah to progress)) }
                        }
                    }
                }
            }
            if (copied < total) error("Incomplete download")
            if (!temp.renameTo(file(surah))) error("Could not save audio")
            } finally {
                temp.delete()
            }
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
