package com.example.quran

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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
    private var ayahs: List<TranslatedAyah> = emptyList()
    private var perAyah = 2
    private var repeatAyah = false
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
        val items = surahAyahs.flatMap { mediaFor(it, audio, mode) }
        perAyah = items.size / surahAyahs.size
        val exo = ExoPlayer.Builder(context.applicationContext).build()
        exo.setMediaItems(items, startIndex.coerceIn(surahAyahs.indices) * perAyah, 0L)
        exo.setPlaybackSpeed(speed)
        exo.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) = publish()
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val exo = player ?: return
                // Repeat: when playback rolls into the next ayah, jump back to the start of the previous one.
                if (repeatAyah && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO && exo.currentMediaItemIndex % perAyah == 0) {
                    exo.seekTo(exo.currentMediaItemIndex - perAyah, 0L)
                }
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState != Player.STATE_ENDED) return
                val exo = player ?: return
                if (repeatAyah) { exo.seekTo((ayahs.size - 1) * perAyah, 0L); exo.play(); return }
                val surah = ayahs.firstOrNull()?.surah
                stop()
                _state.value = TranslationAudioState(finishedSurah = surah)
            }
            override fun onPlayerError(error: PlaybackException) {
                stop()
                _state.value = TranslationAudioState(error = true)
            }
        })
        player = exo
        exo.prepare()
        exo.play()
        publish()
    }

    internal fun mediaFor(ayah: TranslatedAyah, audio: QuranAudioTranslation, mode: TranslationPlayMode): List<MediaItem> =
        when (mode) {
            TranslationPlayMode.ARABIC_AND_TRANSLATION -> listOf(arabicAyahUrl(ayah.globalNumber), audio.ayahUrl(ayah.globalNumber))
            TranslationPlayMode.TRANSLATION_ONLY -> listOf(audio.ayahUrl(ayah.globalNumber))
            TranslationPlayMode.ARABIC_ONLY -> listOf(arabicAyahUrl(ayah.globalNumber))
        }.map(MediaItem::fromUri)

    fun toggle() {
        val exo = player ?: return
        if (exo.isPlaying) exo.pause() else exo.play()
    }

    fun next() = jump(+1)
    fun previous() = jump(-1)

    private fun jump(delta: Int) {
        val exo = player ?: return
        val target = (exo.currentMediaItemIndex / perAyah + delta).coerceIn(ayahs.indices)
        exo.seekTo(target * perAyah, 0L)
        exo.play()
    }

    fun setSpeed(speed: Float) { player?.setPlaybackSpeed(speed) }
    fun setRepeat(repeat: Boolean) { repeatAyah = repeat }

    /** Clears a consumed end-of-surah signal. */
    fun consumeFinished() {
        if (_state.value.finishedSurah != null) _state.value = _state.value.copy(finishedSurah = null)
    }

    fun stop() {
        player?.release()
        player = null
        _state.value = TranslationAudioState()
    }

    private fun publish() {
        val exo = player ?: return
        val current = ayahs.getOrNull(exo.currentMediaItemIndex / perAyah)
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
