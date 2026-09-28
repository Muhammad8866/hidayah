package com.example.prayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QiblaGuidanceTest {
    @Test fun declinationChangesMagneticHeadingToTrueNorth() {
        assertEquals(5.0, QiblaGuidance.trueHeading(355.0, 10.0), 0.0001)
        assertEquals(345.0, QiblaGuidance.trueHeading(5.0, -20.0), 0.0001)
    }

    @Test fun shortestTurnCrossesNorthInBothDirections() {
        assertEquals(20.0, QiblaGuidance.turnDegrees(10.0, 350.0), 0.0001)
        assertEquals(-20.0, QiblaGuidance.turnDegrees(350.0, 10.0), 0.0001)
        assertEquals(-180.0, QiblaGuidance.turnDegrees(0.0, 180.0), 0.0001)
    }

    @Test fun alignmentAllowsSmallHeadingError() {
        assertTrue(QiblaGuidance.isAligned(5.0))
        assertTrue(QiblaGuidance.isAligned(-4.9))
        assertFalse(QiblaGuidance.isAligned(5.1))
    }
}
