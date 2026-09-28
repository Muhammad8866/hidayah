package com.example.prayer.domain.model

import com.example.core.id.GeoCoordinate
import com.example.core.result.Result
import com.example.core.error.AppFailure

/**
 * Value object representing a validated Qibla bearing in degrees (0.0 .. 359.999...)
 * measured clockwise relative to True North.
 */
@JvmInline
value class QiblaBearing(val degrees: Double) {
    init {
        require(degrees in 0.0..360.0) { "Bearing must be between 0 and 360 degrees. Received: $degrees" }
    }
}

/**
 * Sensor accuracy classification for geomagnetic field readings.
 */
enum class SensorAccuracy {
    UNRELIABLE,
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Qibla computation result bundling geographic bearing, distance to Kaaba in kilometers,
 * and compass alignment relative to current device azimuth heading.
 */
data class QiblaComputation(
    val coordinate: GeoCoordinate,
    val bearing: QiblaBearing,
    val distanceKm: Double,
    val relativeHeadingToQibla: Double
)
