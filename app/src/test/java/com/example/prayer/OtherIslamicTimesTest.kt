package com.example.prayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OtherIslamicTimesTest {
    private val prayers = AstronomicalPrayerCalculator.RawPrayerTimes(
        fajrHours = 5.0, sunriseHours = 6.0, dhuhrHours = 12.1,
        asrHours = 16.0, maghribHours = 18.05, ishaHours = 19.5,
        sehriEndHours = 4.983333333333333
    )

    @Test fun guidesUseTheDisplayedDayAndKeepFastingPrecautions() {
        val times = OtherIslamicTimes.from(prayers, solarNoonHours = 12.0)
        assertEquals(prayers.sehriEndHours, times.sehriStopHours, 1e-9)
        assertEquals(6.0 + 20.0 / 60.0, times.ishraqGuideHours!!, 1e-9)
        assertEquals(9.0, times.chashtGuideHours!!, 1e-9)
        assertEquals(12.0, times.solarNoonHours, 1e-9)
        assertEquals(prayers.maghribHours, times.iftarHours, 1e-9)
    }

    @Test fun shortMorningDoesNotShowAnImpossibleOptionalPrayerGuide() {
        val shortMorning = prayers.copy(sunriseHours = 11.5, dhuhrHours = 12.1)
        val times = OtherIslamicTimes.from(shortMorning, solarNoonHours = 12.0)
        assertNull(times.ishraqGuideHours)
        assertNull(times.chashtGuideHours)
    }
}
