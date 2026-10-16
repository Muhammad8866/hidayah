package com.example.feature.communitymasjid.domain

import kotlin.math.cos
import kotlin.math.max

/**
 * Minimal geohash support so community-added masjids can be found with a plain Firestore
 * `whereIn` on one field (no composite index, no geo library).
 */
object CmGeohash {
    /** Precision-5 cells are ~4.9 km tall and at most ~4.9 km wide. */
    const val PRECISION = 5

    private const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"
    private const val METRES_PER_DEGREE_LAT = 111_320.0
    /** Width and height of a precision-5 cell in degrees. */
    private const val CELL_DEGREES = 360.0 / 8192

    fun encode(latitude: Double, longitude: Double, precision: Int = PRECISION): String {
        var latMin = -90.0; var latMax = 90.0
        var lonMin = -180.0; var lonMax = 180.0
        val out = StringBuilder()
        var bit = 0; var ch = 0; var evenBit = true
        while (out.length < precision) {
            if (evenBit) {
                val mid = (lonMin + lonMax) / 2
                if (longitude >= mid) { ch = ch * 2 + 1; lonMin = mid } else { ch *= 2; lonMax = mid }
            } else {
                val mid = (latMin + latMax) / 2
                if (latitude >= mid) { ch = ch * 2 + 1; latMin = mid } else { ch *= 2; latMax = mid }
            }
            evenBit = !evenBit
            if (++bit == 5) { out.append(BASE32[ch]); bit = 0; ch = 0 }
        }
        return out.toString()
    }

    /**
     * Every precision-5 cell touching the square of side 2 × [radiusMetres] around the point.
     * Sampling on a grid finer than a cell guarantees each touching cell contains a sample.
     */
    fun coveringCells(latitude: Double, longitude: Double, radiusMetres: Double): List<String> {
        val metresPerDegreeLon = max(METRES_PER_DEGREE_LAT * cos(Math.toRadians(latitude)), 1_000.0)
        // Step below the narrowest cell side (cells get narrower towards the poles).
        val step = (CELL_DEGREES * metresPerDegreeLon * 0.9).coerceIn(500.0, 2_000.0)
        val steps = (radiusMetres / step).toInt() + 1
        val cells = linkedSetOf<String>()
        for (i in -steps..steps) {
            val dy = (i * step).coerceIn(-radiusMetres, radiusMetres)
            for (j in -steps..steps) {
                val dx = (j * step).coerceIn(-radiusMetres, radiusMetres)
                val lat = (latitude + dy / METRES_PER_DEGREE_LAT).coerceIn(-90.0, 90.0)
                var lon = longitude + dx / metresPerDegreeLon
                if (lon > 180) lon -= 360 else if (lon < -180) lon += 360
                cells += encode(lat, lon)
            }
        }
        return cells.toList()
    }
}
