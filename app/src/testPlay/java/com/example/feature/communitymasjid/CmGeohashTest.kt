package com.example.feature.communitymasjid

import com.example.feature.communitymasjid.domain.CmGeohash
import com.example.feature.communitymasjid.domain.CmTimingRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CmGeohashTest {
    @Test
    fun encodesKnownPoints() {
        assertEquals("u4pru", CmGeohash.encode(57.64911, 10.40744))
        assertEquals("ezs42", CmGeohash.encode(42.605, -5.603))
    }

    @Test
    fun coveringCellsIncludeEveryPointWithinRadius() {
        val lat = 28.6139
        val lon = 77.2090
        val cells = CmGeohash.coveringCells(lat, lon, 5_000.0).toSet()
        assertTrue(cells.size <= 30)
        // Walk a fine grid over the circle: every point's cell must be one we query.
        for (i in -50..50) for (j in -50..50) {
            val pLat = lat + i * 0.001
            val pLon = lon + j * 0.001
            if (CmTimingRules.distanceMetres(lat, lon, pLat, pLon) <= 5_000.0) {
                assertTrue("missing cell for $pLat,$pLon", CmGeohash.encode(pLat, pLon) in cells)
            }
        }
    }
}
