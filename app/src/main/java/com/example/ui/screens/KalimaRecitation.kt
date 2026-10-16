package com.example.ui.screens

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import org.json.JSONObject

/** Bundled recordings live in assets/kalima_audio as `<entry id>.mp3` (or .ogg / .m4a). */
internal const val KALIMA_AUDIO_DIR = "kalima_audio"
/** Optional `{ "<entry id>": [phrase start ms, ...] }`, one start per phrase of the entry. */
internal const val KALIMA_TIMINGS_FILE = "timings.json"
internal const val KALIMA_SLOW_SPEED = .75f
private val KALIMA_AUDIO_EXTENSIONS = setOf("mp3", "ogg", "m4a")
private const val KALIMA_TICK_MS = 100L

/** Entry id to asset path for every recording among [files] of the audio folder. */
internal fun kalimaRecordings(files: List<String>): Map<String, String> =
    files.filter { it.substringAfterLast('.', "").lowercase() in KALIMA_AUDIO_EXTENSIONS }
        .associateBy({ it.substringBeforeLast('.') }, { "$KALIMA_AUDIO_DIR/$it" })

/** Phrase start times per entry id; anything malformed is skipped rather than guessed. */
internal fun parseKalimaTimings(json: String): Map<String, List<Long>> = runCatching {
    val root = JSONObject(json)
    root.keys().asSequence().mapNotNull { id ->
        val array = root.optJSONArray(id) ?: return@mapNotNull null
        val starts = (0 until array.length()).map { array.optLong(it, -1L) }
        if (starts.isEmpty() || starts.any { it < 0 } || starts != starts.sorted()) null else id to starts
    }.toMap()
}.getOrDefault(emptyMap())

/** Index of the phrase being recited at [positionMs], or -1 before the first one starts. */
internal fun kalimaPhraseAt(starts: List<Long>, positionMs: Long): Int = starts.indexOfLast { it <= positionMs }

/** Plays the bundled human recording of an entry, reporting the phrase reached when timings exist. */
internal class KalimaRecitation(
    context: Context,
    private val onPhrase: (Int) -> Unit,
    private val onDone: () -> Unit,
    private val onError: () -> Unit
) {
    private val assets = context.applicationContext.assets
    private val main = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null

    val clips: Map<String, String> = runCatching { kalimaRecordings(assets.list(KALIMA_AUDIO_DIR).orEmpty().toList()) }
        .getOrDefault(emptyMap())
    private val timings: Map<String, List<Long>> = if (clips.isEmpty()) emptyMap() else runCatching {
        parseKalimaTimings(assets.open("$KALIMA_AUDIO_DIR/$KALIMA_TIMINGS_FILE").bufferedReader().use { it.readText() })
    }.getOrDefault(emptyMap())

    fun has(id: String) = id in clips

    /** True when single phrases of [id] can be played from the recording. */
    fun hasTimings(id: String, phraseCount: Int) = has(id) && timings[id]?.size == phraseCount

    /** Starts the whole recording of [id], or only [phrase] of it; false when it cannot be played. */
    fun play(id: String, slow: Boolean, phrase: Int? = null): Boolean {
        stop()
        val path = clips[id] ?: return false
        val starts = timings[id]
        val from = if (phrase == null) null else starts?.getOrNull(phrase) ?: return false
        val until = phrase?.let { starts?.getOrNull(it + 1) }
        return runCatching {
            val mp = MediaPlayer()
            player = mp
            mp.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            assets.openFd(path).use { mp.setDataSource(it.fileDescriptor, it.startOffset, it.length) }
            mp.setOnCompletionListener { if (player === mp) { stop(); onDone() } }
            mp.setOnErrorListener { _, _, _ -> if (player === mp) { stop(); onError() }; true }
            mp.prepare()
            if (from != null) mp.seekTo(from.toInt())
            mp.start()
            if (slow) mp.playbackParams = mp.playbackParams.setSpeed(KALIMA_SLOW_SPEED)
            if (starts != null) tick(mp, starts, phrase, until)
        }.onFailure { stop() }.isSuccess
    }

    private fun tick(mp: MediaPlayer, starts: List<Long>, phrase: Int?, until: Long?) {
        main.postDelayed({
            if (player === mp) {
                val position = runCatching { mp.currentPosition.toLong() }.getOrDefault(0L)
                if (until != null && position >= until) { stop(); onDone() }
                else {
                    if (phrase == null) onPhrase(kalimaPhraseAt(starts, position))
                    tick(mp, starts, phrase, until)
                }
            }
        }, KALIMA_TICK_MS)
    }

    fun stop() {
        val mp = player ?: return
        player = null
        runCatching { mp.stop() }
        mp.release()
    }
}
