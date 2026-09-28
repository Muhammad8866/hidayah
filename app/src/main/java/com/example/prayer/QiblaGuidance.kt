package com.example.prayer

import kotlin.math.abs

/** Compass and great-circle bearings must use the same north reference. */
object QiblaGuidance {
    fun normalize(degrees: Double): Double = ((degrees % 360.0) + 360.0) % 360.0

    fun trueHeading(magneticHeading: Double, declination: Double): Double =
        normalize(magneticHeading + declination)

    /** Signed shortest turn: positive is clockwise/right, negative is left. */
    fun turnDegrees(qiblaBearing: Double, trueHeading: Double): Double =
        ((normalize(qiblaBearing - trueHeading) + 540.0) % 360.0) - 180.0

    fun isAligned(turnDegrees: Double): Boolean = abs(turnDegrees) <= 5.0
}
