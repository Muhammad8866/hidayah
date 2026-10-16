package com.example.ui.screens

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.core.analytics.AppAnalytics
import com.example.core.net.StreamPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal data class QaidaAudioClip(val id: String, val title: String, val url: String, val arabic: String = "")
internal data class QaidaAudioState(val clipId: String? = null, val playing: Boolean = false,
    val loading: Boolean = false, val failed: Boolean = false, val slow: Boolean = false, val repeat: Boolean = false,
    val speed: Float = 1f, val queue: List<String> = emptyList(), val queueIndex: Int = -1)
internal val qaidaAudioSpeeds = listOf(0.5f, 0.75f, 1f)
internal interface QaidaAudioController {
    val state: StateFlow<QaidaAudioState>
    fun play(clip: QaidaAudioClip)
    fun stop()
    fun pause()
    fun slow(enabled: Boolean)
    fun repeat(enabled: Boolean)
    fun close()
    fun speed(value: Float)
    fun playAll(clips: List<QaidaAudioClip>, startIndex: Int = 0)
    fun next()
    fun previous()
}

/** Harakah shown next to a letter for each QuranFor.me pronunciation set; shadda is voiced with a fatha. */
private val qaidaMarkSigns = mapOf("fatha" to "َ", "kasra" to "ِ", "damma" to "ُ",
    "sukun" to "ْ", "shadda" to "َّ")

/** Letter and syllable clips (ids like "fatha-3") versus whole-ayah recordings (ids like "112004"). */
internal fun qaidaIsLetterClip(clip: QaidaAudioClip): Boolean = '-' in clip.id && clip.arabic.isNotBlank()

internal fun qaidaLetterAudio(index: Int, mark: String = "name"): QaidaAudioClip {
    val (letter, name) = qaidaAlphabet[index]
    val arabic = letter + qaidaMarkSigns[mark].orEmpty()
    return QaidaAudioClip("$mark-$index", if (mark == "name") name else "$arabic · $name",
        "https://quranfor.me/api/arabic/$mark/$index", arabic)
}

private fun qaidaAyahAudio(surah: Int, ayah: Int, arabic: String): QaidaAudioClip {
    val file = "%03d%03d".format(java.util.Locale.ROOT, surah, ayah)
    return QaidaAudioClip(file, "Quran $surah:$ayah", "https://everyayah.com/data/Husary_64kbps/$file.mp3", arabic)
}

internal fun qaidaLessonAudio(id: String): List<QaidaAudioClip> = when (id) {
    "letters" -> qaidaAlphabet.indices.map { qaidaLetterAudio(it) }
    "fatha", "kasra", "damma" -> qaidaAlphabet.indices.map { qaidaLetterAudio(it, id) }
    // Alif carries no sukoon or shadda of its own, so these drills start from Baa.
    "sukoon", "makharij" -> (1 until qaidaAlphabet.size).map { qaidaLetterAudio(it, "sukun") }
    "shadda" -> (1 until qaidaAlphabet.size).map { qaidaLetterAudio(it, "shadda") }
    // Light letter first, then its heavy partner: Taa/Taa, Daal/Daad, Dhaal/Dhaa, Seen/Saad, Kaaf/Qaaf, then Khaa and Ghayn.
    "heavy_light" -> listOf(2, 15, 7, 14, 8, 16, 11, 13, 21, 20, 6, 18).map { qaidaLetterAudio(it, "fatha") }
    "shapes" -> listOf(qaidaAyahAudio(1, 1, "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"))
    "tanween" -> listOf(qaidaAyahAudio(112, 4, "كُفُوًا أَحَدٌ"), qaidaAyahAudio(101, 11, "نَارٌ حَامِيَةٌ"))
    "madd" -> listOf(qaidaAyahAudio(1, 4, "مَالِكِ"), qaidaAyahAudio(114, 1, "أَعُوذُ"))
    "madd_types" -> listOf(qaidaAyahAudio(1, 7, "وَلَا ٱلضَّآلِّينَ"), qaidaAyahAudio(110, 1, "إِذَا جَآءَ"),
        qaidaAyahAudio(1, 2, "ٱلْعَالَمِينَ"))
    "ghunnah" -> listOf(qaidaAyahAudio(114, 1, "بِرَبِّ ٱلنَّاسِ"), qaidaAyahAudio(78, 1, "عَمَّ يَتَسَآءَلُونَ"))
    "izhar" -> listOf(qaidaAyahAudio(112, 4, "كُفُوًا أَحَدٌ"), qaidaAyahAudio(106, 4, "مِنْ خَوْفٍ"))
    "idgham" -> listOf(qaidaAyahAudio(99, 7, "فَمَن يَعْمَلْ"), qaidaAyahAudio(2, 5, "مِّن رَّبِّهِمْ"))
    "iqlab" -> listOf(qaidaAyahAudio(104, 4, "لَيُنۢبَذَنَّ"))
    "ikhfa" -> listOf(qaidaAyahAudio(114, 4, "مِن شَرِّ"), qaidaAyahAudio(113, 2, "مِن شَرِّ مَا خَلَقَ"))
    "qalqalah" -> listOf(qaidaAyahAudio(112, 1, "أَحَدٌ"), qaidaAyahAudio(113, 1, "ٱلْفَلَقِ"), qaidaAyahAudio(111, 1, "وَتَبَّ"))
    "meem" -> listOf(qaidaAyahAudio(105, 4, "تَرْمِيهِم بِحِجَارَةٍ"), qaidaAyahAudio(106, 4, "أَطْعَمَهُم مِّن"))
    "laam" -> listOf(qaidaAyahAudio(91, 1, "وَٱلشَّمْسِ"), qaidaAyahAudio(1, 2, "ٱلْحَمْدُ لِلَّهِ"), qaidaAyahAudio(110, 1, "نَصْرُ ٱللَّهِ"))
    "raa" -> listOf(qaidaAyahAudio(108, 2, "لِرَبِّكَ وَٱنْحَرْ"), qaidaAyahAudio(89, 10, "وَفِرْعَوْنَ"))
    "waqf" -> listOf(qaidaAyahAudio(112, 2, "ٱللَّهُ ٱلصَّمَدُ"), qaidaAyahAudio(101, 11, "نَارٌ حَامِيَةٌ"))
    else -> emptyList()
}

internal class StreamingQaidaAudio(private val context: Context) : QaidaAudioController {
    private val mutable = MutableStateFlow(QaidaAudioState())
    override val state: StateFlow<QaidaAudioState> = mutable
    private var player: ExoPlayer? = null
    private val queued get() = state.value.queue.isNotEmpty()
    private fun repeatMode() = when {
        !state.value.repeat -> Player.REPEAT_MODE_OFF
        queued -> Player.REPEAT_MODE_ALL
        else -> Player.REPEAT_MODE_ONE
    }
    private fun player(): ExoPlayer = player ?: StreamPlayer.build(context).also { p ->
        player = p
        p.setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_SPEECH).build(), true)
        p.setHandleAudioBecomingNoisy(true)
        p.setPlaybackSpeed(state.value.speed)
        p.repeatMode = repeatMode()
        p.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                val current = mutable.value
                val item = player.currentMediaItem?.mediaId
                // Follow the playlist as it advances, but never resurrect a clip after stop().
                val inQueue = current.clipId != null && current.queue.isNotEmpty() && item != null
                mutable.value = current.copy(
                    clipId = if (inQueue) item else current.clipId,
                    queueIndex = if (inQueue) player.currentMediaItemIndex else current.queueIndex,
                    playing = player.playWhenReady && player.playbackState != Player.STATE_ENDED &&
                        player.playbackState != Player.STATE_IDLE && !current.failed,
                    loading = player.playbackState == Player.STATE_BUFFERING && !current.failed)
            }
            override fun onPlayerError(error: PlaybackException) {
                mutable.value = mutable.value.copy(playing = false, loading = false, failed = true)
            }
        })
    }
    private fun stopOtherAudio() {
        com.example.quran.QuranAudioManager.stop()
        com.example.quran.TafsirAudioManager.stop()
        com.example.naat.NaatAudioManager.stop()
        com.example.azan.AzanAudioManager.stop()
    }
    private fun QaidaAudioClip.item() = MediaItem.Builder().setMediaId(id).setUri(url).build()

    override fun play(clip: QaidaAudioClip) {
        AppAnalytics.content("qaida_audio", clip.id, "play")
        val p = player()
        val current = state.value
        if (current.clipId == clip.id && !current.failed && p.playbackState != Player.STATE_ENDED && p.playbackState != Player.STATE_IDLE) {
            if (p.playWhenReady) p.pause() else p.play()
            return
        }
        stopOtherAudio()
        // Retrying the clip a queue failed on resumes that queue instead of dropping it.
        if (current.clipId == clip.id && current.failed && queued && p.mediaItemCount > 0) {
            mutable.value = current.copy(playing = true, loading = true, failed = false)
            p.prepare()
            p.play()
            return
        }
        mutable.value = current.copy(clipId = clip.id, playing = true, loading = true, failed = false, queue = emptyList(), queueIndex = -1)
        p.repeatMode = repeatMode()
        p.setMediaItem(clip.item())
        p.prepare()
        p.play()
    }
    override fun playAll(clips: List<QaidaAudioClip>, startIndex: Int) {
        if (clips.isEmpty()) return
        val start = startIndex.coerceIn(0, clips.lastIndex)
        val p = player()
        stopOtherAudio()
        mutable.value = state.value.copy(clipId = clips[start].id, playing = true, loading = true, failed = false,
            queue = clips.map { it.id }, queueIndex = start)
        p.repeatMode = repeatMode()
        p.setMediaItems(clips.map { it.item() }, start, 0L)
        p.prepare()
        p.play()
    }
    private fun jump(index: Int) {
        val p = player ?: return
        if (!queued || index !in 0 until p.mediaItemCount) return
        mutable.value = state.value.copy(clipId = state.value.queue[index], queueIndex = index, playing = true, loading = true, failed = false)
        p.seekTo(index, 0L)
        p.prepare()
        p.play()
    }
    override fun next() {
        val size = state.value.queue.size
        val target = state.value.queueIndex + 1
        if (size > 0) jump(if (target >= size && state.value.repeat) 0 else target)
    }
    override fun previous() = jump(state.value.queueIndex - 1)
    override fun stop() {
        player?.stop()
        player?.clearMediaItems()
        mutable.value = mutable.value.copy(clipId = null, playing = false, loading = false, failed = false, queue = emptyList(), queueIndex = -1)
    }
    override fun pause() { player?.pause() }
    override fun speed(value: Float) {
        val speed = value.coerceIn(qaidaAudioSpeeds.first(), qaidaAudioSpeeds.last())
        mutable.value = mutable.value.copy(speed = speed, slow = speed < 1f)
        player?.setPlaybackSpeed(speed)
    }
    override fun slow(enabled: Boolean) = speed(if (enabled) .75f else 1f)
    override fun repeat(enabled: Boolean) { mutable.value = mutable.value.copy(repeat = enabled); player?.repeatMode = repeatMode() }
    override fun close() { player?.release(); player = null }
}
