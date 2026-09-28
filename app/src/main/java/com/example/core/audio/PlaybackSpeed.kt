package com.example.core.audio

import android.media.MediaPlayer
import kotlin.math.abs

/** Playback speeds offered by every audio player in the app. */
object PlaybackSpeed {
    const val NORMAL = 1f
    val options = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f)

    /** Snaps a stored value to the nearest offered speed so a bad preference can't break playback. */
    fun sanitize(speed: Float): Float = options.minBy { abs(it - speed) }

    fun label(speed: Float): String =
        if (speed % 1f == 0f) "${speed.toInt()}x" else "${speed}x"
}

/**
 * Changes speed without changing pitch. Call only after [MediaPlayer.start]: on a paused player a
 * non-zero speed resumes playback. Some decoders reject speeds, so failures are ignored.
 */
fun MediaPlayer.applySpeed(speed: Float) {
    runCatching { playbackParams = playbackParams.setSpeed(speed) }
}
