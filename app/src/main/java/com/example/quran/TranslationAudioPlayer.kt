package com.example.quran

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.core.net.StreamPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TranslationAudioState(
    val surah: Int? = null,
    val ayah: Int? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val error: Boolean = false,
    /** Set once when a surah plays to its end, so the screen can continue to the next one. */
    val finishedSurah: Int? = null
)

/** Plays a surah's spoken translation ayah by ayah, so the reader can follow the current verse. */
object TranslationAudioPlayer {
    private var player: ExoPlayer? = null
    private var audioLoad: com.example.core.analytics.AppAnalytics.AudioLoad? = null
    private var ayahs: List<TranslatedAyah> = emptyList()
    private var perAyah = 2
    private var repeatAyah = false
    private var items: List<MediaItem> = emptyList()
    /** Index of the first ayah in the player's playlist; the playlist holds one ayah while repeating. */
    private var firstAyah = 0
    private val _state = MutableStateFlow(TranslationAudioState())
    val state: StateFlow<TranslationAudioState> = _state.asStateFlow()

    fun play(
        context: Context,
        surahAyahs: List<TranslatedAyah>,
        audio: QuranAudioTranslation,
        startIndex: Int = 0,
        mode: TranslationPlayMode = TranslationPlayMode.ARABIC_AND_TRANSLATION,
        speed: Float = 1f,
        repeat: Boolean = false
    ) {
        if (surahAyahs.isEmpty()) return
        stop()
        QuranAudioManager.stop()
        TafsirAudioManager.stop()
        com.example.naat.NaatAudioManager.stop()
        com.example.azan.AzanAudioManager.stop()
        ayahs = surahAyahs
        repeatAyah = repeat
        items = surahAyahs.flatMap { mediaFor(it, audio, mode) }
        perAyah = items.size / surahAyahs.size
        com.example.core.analytics.AppAnalytics.media("translation_audio", "play")
        com.example.core.analytics.AppAnalytics.content("translation_audio", "surah_${surahAyahs.first().surah}", "play")
        // Cached player: replay, previous and repeat play from disk instead of refetching each clip.
        audioLoad = com.example.core.analytics.AppAnalytics.audioLoad("translation_audio")
        val exo = StreamPlayer.build(context)
        exo.setPlaybackSpeed(speed)
        exo.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) = publish()
            override fun onPlaybackStateChanged(playbackState: Int) {
                com.example.core.analytics.AppAnalytics.playbackState("translation_audio", playbackState)
                if (playbackState == Player.STATE_READY) audioLoad?.ready()
                if (playbackState != Player.STATE_ENDED) return
                val surah = ayahs.firstOrNull()?.surah
                stop()
                _state.value = TranslationAudioState(finishedSurah = surah)
            }
            override fun onPlayerError(error: PlaybackException) {
                com.example.core.analytics.AppAnalytics.mediaState("translation_audio", "failed", "playback_error")
                audioLoad?.failed()
                stop()
                _state.value = TranslationAudioState(error = true)
            }
        })
        player = exo
        load(startIndex.coerceIn(surahAyahs.indices))
        exo.prepare()
        exo.play()
        publish()
    }

    /**
     * Loads the playlist from [ayah]. While repeating, the playlist is only that ayah's clips on a
     * loop, so the repeat is seamless and the highlight never jumps ahead to the next verse.
     */
    private fun load(ayah: Int) {
        val exo = player ?: return
        if (repeatAyah) {
            firstAyah = ayah
            exo.setMediaItems(items.subList(ayah * perAyah, (ayah + 1) * perAyah), 0, 0L)
            exo.repeatMode = Player.REPEAT_MODE_ALL
        } else {
            firstAyah = 0
            exo.setMediaItems(items, ayah * perAyah, 0L)
            exo.repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    private fun currentAyah(exo: Player): Int = firstAyah + exo.currentMediaItemIndex / perAyah

    internal fun mediaFor(ayah: TranslatedAyah, audio: QuranAudioTranslation, mode: TranslationPlayMode): List<MediaItem> =
        when (mode) {
            TranslationPlayMode.ARABIC_AND_TRANSLATION -> listOf(arabicAyahUrl(ayah.globalNumber), audio.ayahUrl(ayah.globalNumber))
            TranslationPlayMode.TRANSLATION_ONLY -> listOf(audio.ayahUrl(ayah.globalNumber))
            TranslationPlayMode.ARABIC_ONLY -> listOf(arabicAyahUrl(ayah.globalNumber))
        }.map(MediaItem::fromUri)

    fun toggle() {
        val exo = player ?: return
        com.example.core.analytics.AppAnalytics.media("translation_audio", if (exo.isPlaying) "pause" else "resume")
        if (exo.isPlaying) exo.pause() else exo.play()
    }

    fun next() { com.example.core.analytics.AppAnalytics.media("translation_audio", "next"); jump(+1) }
    fun previous() { com.example.core.analytics.AppAnalytics.media("translation_audio", "previous"); jump(-1) }

    private fun jump(delta: Int) {
        val exo = player ?: return
        val target = (currentAyah(exo) + delta).coerceIn(ayahs.indices)
        if (repeatAyah) load(target) else exo.seekTo(target * perAyah, 0L)
        exo.play()
    }

    fun setSpeed(speed: Float) { com.example.core.analytics.AppAnalytics.media("translation_audio", "speed_change"); player?.setPlaybackSpeed(speed) }
    fun setRepeat(repeat: Boolean) {
        if (repeat == repeatAyah) return
        com.example.core.analytics.AppAnalytics.setting("translation_audio_repeat", repeat.toString())
        val exo = player
        if (exo == null) { repeatAyah = repeat; return }
        // Keep the current clip and position; only the playlist around it changes.
        val ayah = currentAyah(exo)
        val clip = exo.currentMediaItemIndex % perAyah
        val position = exo.currentPosition
        repeatAyah = repeat
        load(ayah)
        exo.seekTo(if (repeat) clip else ayah * perAyah + clip, position)
    }

    /** Clears a consumed end-of-surah signal. */
    fun consumeFinished() {
        if (_state.value.finishedSurah != null) _state.value = _state.value.copy(finishedSurah = null)
    }

    fun stop() {
        player?.release()
        player = null
        firstAyah = 0
        _state.value = TranslationAudioState()
    }

    private fun publish() {
        val exo = player ?: return
        val current = ayahs.getOrNull(currentAyah(exo))
        _state.value = TranslationAudioState(
            surah = current?.surah,
            ayah = current?.number,
            isPlaying = exo.isPlaying,
            isBuffering = exo.playbackState == Player.STATE_BUFFERING
        )
    }

    private fun arabicAyahUrl(globalAyah: Int): String {
        require(globalAyah in 1..6236)
        return "https://cdn.islamic.network/quran/audio/128/ar.alafasy/$globalAyah.mp3"
    }
}
