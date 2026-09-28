package com.example.prayer

import kotlin.math.*

object QiblaCalculator {

    // Kaaba Coordinates in Makkah, Saudi Arabia
    const val MAKKAH_LATITUDE = 21.422487
    const val MAKKAH_LONGITUDE = 39.826206

    /**
     * Calculates the Great Circle bearing from current device coordinates towards the Kaaba in Makkah.
     * Returns azimuth angle in degrees (0 to 360 clockwise from true North).
     */
    fun calculateQiblaBearing(currentLat: Double, currentLng: Double): Double {
        val phi1 = Math.toRadians(currentLat)
        val phi2 = Math.toRadians(MAKKAH_LATITUDE)
        val deltaLambda = Math.toRadians(MAKKAH_LONGITUDE - currentLng)

        val y = sin(deltaLambda)
        val x = cos(phi1) * tan(phi2) - sin(phi1) * cos(deltaLambda)

        var qiblaRad = atan2(y, x)
        var qiblaDeg = Math.toDegrees(qiblaRad)

        qiblaDeg = (qiblaDeg + 360.0) % 360.0
        return qiblaDeg
    }

    /**
     * Calculates distance to Makkah in kilometers using the Haversine formula.
     */
    fun calculateDistanceToMakkahKm(currentLat: Double, currentLng: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(MAKKAH_LATITUDE - currentLat)
        val dLon = Math.toRadians(MAKKAH_LONGITUDE - currentLng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(currentLat)) * cos(Math.toRadians(MAKKAH_LATITUDE)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
