package com.example.prayer.domain.services

import com.example.core.id.GeoCoordinate
import com.example.core.result.Result
import com.example.core.error.AppFailure
import com.example.prayer.domain.model.QiblaBearing
import com.example.prayer.domain.model.QiblaComputation
import kotlin.math.*

/**
 * Pure mathematical service for computing geodesic Qibla bearing
 * and distance to the Kaaba using Great-Circle navigation equations.
 * Independent of Android SDK, UI, or sensors.
 */
object GeodesicQiblaCalculator {

    val KAABA_COORDINATE = GeoCoordinate(21.422487, 39.826206)
    private const val EARTH_RADIUS_KM = 6371.0088

    /**
     * Calculates the forward azimuth bearing towards Kaaba from given [coordinate].
     */
    fun calculateBearing(coordinate: GeoCoordinate): Result<QiblaBearing, AppFailure.PrayerFailure> {
        val phi1 = Math.toRadians(coordinate.latitude)
        val phi2 = Math.toRadians(KAABA_COORDINATE.latitude)
        val deltaLambda = Math.toRadians(KAABA_COORDINATE.longitude - coordinate.longitude)

        val y = sin(deltaLambda)
        val x = cos(phi1) * tan(phi2) - sin(phi1) * cos(deltaLambda)

        val rad = atan2(y, x)
        var deg = Math.toDegrees(rad)
        deg = (deg + 360.0) % 360.0

        return Result.Success(QiblaBearing(deg))
    }

    /**
     * Calculates great circle distance to the Kaaba in kilometers via Haversine formula.
     */
    fun calculateDistanceKm(coordinate: GeoCoordinate): Double {
        val lat1Rad = Math.toRadians(coordinate.latitude)
        val lat2Rad = Math.toRadians(KAABA_COORDINATE.latitude)
        val dLat = Math.toRadians(KAABA_COORDINATE.latitude - coordinate.latitude)
        val dLon = Math.toRadians(KAABA_COORDINATE.longitude - coordinate.longitude)

        val a = sin(dLat / 2.0).pow(2.0) +
                cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2.0).pow(2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Computes full Qibla state given current coordinate and device heading azimuth (0..360 deg).
     */
    fun compute(coordinate: GeoCoordinate, deviceHeadingAzimuth: Double): Result<QiblaComputation, AppFailure.PrayerFailure> {
        return calculateBearing(coordinate).map { bearing ->
            val dist = calculateDistanceKm(coordinate)
            // Relative angle device needs to rotate to face Kaaba (0 = perfectly facing Kaaba)
            val diff = (bearing.degrees - deviceHeadingAzimuth + 360.0) % 360.0
            QiblaComputation(
                coordinate = coordinate,
                bearing = bearing,
                distanceKm = dist,
                relativeHeadingToQibla = diff
            )
        }
    }
}
