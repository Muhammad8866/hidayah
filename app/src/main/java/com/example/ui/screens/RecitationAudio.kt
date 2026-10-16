package com.example.ui.screens

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.*
import com.example.core.net.StreamPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

internal fun recitationAudioUrls(passage: RecitationPassage): List<String> =
    (passage.first..passage.last).map {
        "https://everyayah.com/data/Alafasy_128kbps/%03d%03d.mp3".format(Locale.ROOT, passage.surah, it)
    }

internal val recitationSpeeds = listOf(1f, 1.25f, 1.5f, .75f)
internal val recitationRepeats = listOf(1, 3, 7, 0)
internal val recitationSleepMinutes = listOf(10, 20, 30)

internal fun <T> List<T>.recitationNext(current: T): T = this[(indexOf(current) + 1) % size]

/** Off → 10 → 20 → 30 → off, judged by the minutes still [left] on a running timer. */
internal fun nextRecitationSleep(left: Int?): Int? {
    if (left == null) return recitationSleepMinutes.first()
    val active = recitationSleepMinutes.firstOrNull { it >= left } ?: return null
    return recitationSleepMinutes.firstOrNull { it > active }
}

/** Whether a passage that just finished its [playsDone]th play should start again; 0 repeats forever. */
internal fun recitationShouldReplay(repeat: Int, playsDone: Int) = repeat == 0 || playsDone < repeat

/**
 * The queue position and ayah one step forward or back from [ayah], crossing into the neighbouring
 * passage at a boundary. Null when stepping forward past the end of the queue.
 */
internal fun recitationStep(queue: List<RecitationPassage>, index: Int, ayah: Int, forward: Boolean): Pair<Int, Int>? {
    val passage = queue.getOrNull(index) ?: return null
    return if (forward) when {
        ayah < passage.last -> index to ayah + 1
        index < queue.lastIndex -> index + 1 to queue[index + 1].first
        else -> null
    } else when {
        ayah > passage.first -> index to ayah - 1
        index > 0 -> index - 1 to queue[index - 1].last
        else -> index to passage.first
    }
}

internal data class RecitationAudioState(val id: String? = null, val ayah: Int? = null,
    val playing: Boolean = false, val loading: Boolean = false, val failed: Boolean = false,
    val queueIndex: Int = 0, val queueSize: Int = 0, val speed: Float = 1f,
    val repeat: Int = 1, val sleepMinutesLeft: Int? = null)

internal class RecitationAudio(context: Context) {
    val state = MutableStateFlow(RecitationAudioState())
    private val main = Handler(Looper.getMainLooper())
    private var queue = emptyList<RecitationPassage>()
    private var queueIndex = 0
    private var playsDone = 0
    private var first = 1
    private val engine = lazy { StreamPlayer.build(context.applicationContext).apply {
        setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH).build(), true)
        setHandleAudioBecomingNoisy(true)
        setPlaybackSpeed(state.value.speed)
        addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                if (state.value.id == null || state.value.failed) return
                if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) && player.playbackState == Player.STATE_ENDED) {
                    ended()
                    return
                }
                state.value = state.value.copy(ayah = first + player.currentMediaItemIndex,
                    playing = player.playWhenReady && player.playbackState != Player.STATE_ENDED,
                    loading = player.playbackState == Player.STATE_BUFFERING)
            }
            override fun onPlayerError(error: PlaybackException) {
                if (state.value.id == null) return
                state.value = state.value.copy(ayah = first + currentMediaItemIndex,
                    playing = false, loading = false, failed = true)
            }
        })
    } }
    private val player get() = engine.value
    private val sleepTick = object : Runnable {
        override fun run() {
            val left = (state.value.sleepMinutesLeft ?: return) - 1
            if (left <= 0) stop()
            else {
                state.value = state.value.copy(sleepMinutesLeft = left)
                main.postDelayed(this, 60_000)
            }
        }
    }

    /** Called with a passage id each time that passage has been heard to its last ayah. */
    var onPassageFinished: ((String) -> Unit)? = null

    /** The passage being played, also while it is outside the list currently on screen. */
    val current: RecitationPassage? get() = if (state.value.id == null) null else queue.getOrNull(queueIndex)

    fun play(passage: RecitationPassage) {
        if (state.value.id == passage.id) { if (state.value.failed) retry() else toggle(); return }
        start(listOf(passage), 0, passage.first)
    }

    /** Plays the passages one after another, each as many times as the repeat setting says. */
    fun playAll(passages: List<RecitationPassage>, startIndex: Int = 0) {
        if (passages.isEmpty()) return
        val index = startIndex.coerceIn(0, passages.lastIndex)
        start(passages, index, passages[index].first)
    }

    fun playFrom(passage: RecitationPassage, ayah: Int) {
        val index = queue.indexOf(passage)
        if (state.value.id != null && index >= 0) {
            if (index != queueIndex) playsDone = 0
            jump(index, ayah)
        } else start(listOf(passage), 0, ayah)
    }

    fun toggle() {
        if (state.value.id == null) return
        if (state.value.failed) { retry(); return }
        if (player.playWhenReady) player.pause() else player.play()
    }

    /** Starts again at the ayah that failed, not at the top of the passage. */
    fun retry() {
        if (state.value.id == null || !state.value.failed || queue.isEmpty()) return
        load(queueIndex, state.value.ayah ?: queue[queueIndex].first)
    }

    fun nextAyah() = step(true)
    fun previousAyah() = step(false)

    fun setSpeed(speed: Float) {
        val value = speed.coerceIn(.5f, 2f)
        state.value = state.value.copy(speed = value)
        if (engine.isInitialized()) player.setPlaybackSpeed(value)
    }
    fun slow(enabled: Boolean) = setSpeed(if (enabled) .75f else 1f)
    fun setRepeat(times: Int) { state.value = state.value.copy(repeat = times.coerceAtLeast(0)) }

    fun setSleepTimer(minutes: Int?) {
        main.removeCallbacks(sleepTick)
        val left = minutes?.takeIf { it > 0 }
        state.value = state.value.copy(sleepMinutesLeft = left)
        if (left != null) main.postDelayed(sleepTick, 60_000)
    }

    fun stop() {
        main.removeCallbacks(sleepTick)
        queue = emptyList()
        queueIndex = 0
        playsDone = 0
        val settings = RecitationAudioState(speed = state.value.speed, repeat = state.value.repeat)
        // Reset first so the listener ignores the events the player emits while stopping.
        state.value = settings
        if (engine.isInitialized()) { player.pause(); player.stop() }
    }
    fun close() {
        main.removeCallbacks(sleepTick)
        if (engine.isInitialized()) player.release()
    }

    private fun start(passages: List<RecitationPassage>, index: Int, ayah: Int) {
        com.example.quran.QuranAudioManager.stop()
        com.example.quran.TafsirAudioManager.stop()
        com.example.naat.NaatAudioManager.stop()
        com.example.azan.AzanAudioManager.stop()
        queue = passages
        playsDone = 0
        load(index, ayah)
    }

    private fun load(index: Int, ayah: Int) {
        val passage = queue[index]
        val target = ayah.coerceIn(passage.first, passage.last)
        queueIndex = index
        first = passage.first
        state.value = state.value.copy(id = passage.id, ayah = target, playing = true, loading = true,
            failed = false, queueIndex = index, queueSize = queue.size)
        player.setMediaItems(recitationPlaybackUris(passage).map(MediaItem::fromUri), target - first, 0L)
        player.prepare()
        player.play()
    }

    private fun jump(index: Int, ayah: Int) {
        val passage = queue[index]
        val target = ayah.coerceIn(passage.first, passage.last)
        if (index != queueIndex || state.value.failed || player.playbackState == Player.STATE_IDLE) {
            load(index, target)
            return
        }
        state.value = state.value.copy(ayah = target, playing = true)
        player.seekTo(target - first, 0L)
        player.play()
    }

    private fun step(forward: Boolean) {
        val ayah = state.value.ayah
        if (state.value.id == null || ayah == null) return
        val (index, target) = recitationStep(queue, queueIndex, ayah, forward) ?: return
        if (index != queueIndex) playsDone = 0
        jump(index, target)
    }

    private fun ended() {
        queue.getOrNull(queueIndex)?.let { onPassageFinished?.invoke(it.id) }
        playsDone++
        if (recitationShouldReplay(state.value.repeat, playsDone)) load(queueIndex, queue[queueIndex].first)
        else if (queueIndex < queue.lastIndex) {
            playsDone = 0
            load(queueIndex + 1, queue[queueIndex + 1].first)
        } else stop()
    }
}

