package com.example.prayer.domain.services

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.atan2

/**
 * Pure low-pass compass filter preventing needle jitter on mobile devices.
 * Uses circular trigonometric smoothing rather than linear interpolation to avoid wrap-around discontinuities (359° <-> 1°).
 */
class CompassHeadingFilter(
    private val smoothingFactor: Float = 0.15f // Higher = faster response, Lower = smoother
) {
    private var smoothedSin = 0.0
    private var smoothedCos = 0.0
    private var initialized = false

    fun filter(rawDegrees: Double): Double {
        val rad = Math.toRadians(rawDegrees)
        val s = sin(rad)
        val c = cos(rad)

        if (!initialized) {
            smoothedSin = s
            smoothedCos = c
            initialized = true
        } else {
            smoothedSin += smoothingFactor * (s - smoothedSin)
            smoothedCos += smoothingFactor * (c - smoothedCos)
        }

        var deg = Math.toDegrees(atan2(smoothedSin, smoothedCos))
        deg = (deg + 360.0) % 360.0
        return deg
    }

    fun reset() {
        initialized = false
        smoothedSin = 0.0
        smoothedCos = 0.0
    }
}
