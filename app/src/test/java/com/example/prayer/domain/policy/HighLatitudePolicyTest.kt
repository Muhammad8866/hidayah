package com.example.prayer.domain.policy

import com.example.domain.model.HighLatitudeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HighLatitudePolicyTest {
    private val latitudes = listOf(48.0, 60.0, 66.5, 80.0)
    private val eps = 1e-12

    private fun portions(rule: HighLatitudeRule, lat: Double, fajr: Double = 18.0, isha: Double = 17.0) =
        HighLatitudePolicy.nightPortions(rule, lat, fajr, isha)

    @Test fun fixedRulesReturnTheirJuristicPortionsAtEveryLatitude() {
        for (lat in latitudes) {
            assertEquals(0.5 to 0.5, portions(HighLatitudeRule.AUTOMATIC, lat))
            assertEquals(0.5 to 0.5, portions(HighLatitudeRule.MIDDLE_OF_NIGHT, lat))
            assertEquals((1.0 / 7.0) to (1.0 / 7.0), portions(HighLatitudeRule.SEVENTH_OF_NIGHT, lat))
            assertEquals(1.0 to 1.0, portions(HighLatitudeRule.NONE, lat))
            val angle = portions(HighLatitudeRule.TWILIGHT_ANGLE, lat)
            assertEquals(0.3, angle.first, eps)
            assertEquals(17.0 / 60.0, angle.second, eps)
        }
    }

    @Test fun portionsDoNotDependOnLatitude() {
        for (rule in HighLatitudeRule.values()) {
            val reference = portions(rule, latitudes.first())
            latitudes.forEach { assertEquals(reference, portions(rule, it)) }
        }
    }

    @Test fun twilightAnglePortionIsMonotonicAndClamped() {
        var previous = -1.0
        for (angle in listOf(-5.0, 0.0, 12.0, 15.0, 18.0, 19.5, 30.0, 45.0)) {
            val (fajr, isha) = portions(HighLatitudeRule.TWILIGHT_ANGLE, 66.5, angle, angle)
            assertEquals(fajr, isha, eps)
            assertTrue(fajr in 0.0..0.5)
            assertTrue(fajr >= previous)
            previous = fajr
        }
        assertEquals(0.0 to 0.0, portions(HighLatitudeRule.TWILIGHT_ANGLE, 80.0, -5.0, -5.0))
        assertEquals(0.5 to 0.5, portions(HighLatitudeRule.TWILIGHT_ANGLE, 80.0, 45.0, 45.0))
    }

    @Test fun portionOrderingIsSeventhThenAngleThenMiddleThenNone() {
        for (lat in latitudes) {
            val seventh = portions(HighLatitudeRule.SEVENTH_OF_NIGHT, lat).first
            val angle = portions(HighLatitudeRule.TWILIGHT_ANGLE, lat).first
            val middle = portions(HighLatitudeRule.MIDDLE_OF_NIGHT, lat).first
            val none = portions(HighLatitudeRule.NONE, lat).first
            assertTrue(seventh < angle && angle < middle && middle < none)
        }
    }

    @Test fun fallbackOffsetsAreHalfTheNightAtEveryLatitude() {
        for (lat in latitudes) for (night in listOf(0.5, 4.0, 8.0, 12.0, 23.5)) {
            val (fajr, isha) = HighLatitudePolicy.calculateFallbackTwilightOffsets(lat, night, 18.0, 17.0)
            assertEquals(night / 2.0, fajr, eps)
            assertEquals(night / 2.0, isha, eps)
        }
    }

    @Test fun fallbackOffsetsGrowWithNightLength() {
        var previous = 0.0
        for (night in listOf(1.0, 3.0, 6.0, 9.0, 15.0)) {
            val fajr = HighLatitudePolicy.calculateFallbackTwilightOffsets(60.0, night, 18.0, 18.0).first
            assertTrue(fajr > previous)
            previous = fajr
        }
    }

    @Test fun nonPositiveOrNanNightFallsBackToEightHours() {
        for (night in listOf(0.0, -3.0, Double.NaN)) {
            assertEquals(4.0 to 4.0, HighLatitudePolicy.calculateFallbackTwilightOffsets(80.0, night, 18.0, 17.0))
        }
    }
}
