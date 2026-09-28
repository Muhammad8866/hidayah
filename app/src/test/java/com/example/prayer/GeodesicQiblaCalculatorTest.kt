package com.example.prayer.domain.services

import com.example.core.id.GeoCoordinate
import com.example.prayer.QiblaCalculator
import com.example.prayer.domain.model.QiblaBearing
import org.junit.Assert.*
import org.junit.Test

class GeodesicQiblaCalculatorTest {

    @Test
    fun testQiblaBearingFromNewDelhi() {
        val newDelhi = GeoCoordinate(28.6139, 77.2090)
        val result = GeodesicQiblaCalculator.calculateBearing(newDelhi)

        assertTrue(result.isSuccess)
        val bearing = result.getOrNull()!!.degrees
        val distance = GeodesicQiblaCalculator.calculateDistanceKm(newDelhi)
        
        // Let's assert against the exact computed bearing from QiblaCalculator
        val expectedBearing = QiblaCalculator.calculateQiblaBearing(28.6139, 77.2090)
        assertEquals(expectedBearing, bearing, 0.0001)
        assertTrue("Distance to Makkah from Delhi should be ~3800-4000 km, was $distance", distance in 3800.0..4000.0)
    }

    @Test
    fun testQiblaBearingFromLondon() {
        // London: 51.5074° N, -0.1278° W
        // Qibla from London is roughly 118° - 120° (East-South-East)
        val london = GeoCoordinate(51.5074, -0.1278)
        val result = GeodesicQiblaCalculator.calculateBearing(london)

        assertTrue(result.isSuccess)
        val bearing = result.getOrNull()!!.degrees
        assertTrue("London Qibla should be ~118° - 120°, was $bearing", bearing in 118.0..120.0)
    }

    @Test
    fun testCompassHeadingFilterCircularSmoothing() {
        val filter = CompassHeadingFilter(smoothingFactor = 0.5f)

        // Seed with 358°
        val first = filter.filter(358.0)
        assertEquals(358.0, first, 0.01)

        // Step to 2° across the 0° wrap-around boundary
        val second = filter.filter(2.0)
        // Linear averaging would erroneously give 180°, but circular filter should be ~0°
        assertTrue("Circular smoothing across 0° should be around 0°/360°, was $second", second < 5.0 || second > 355.0)
    }
}
