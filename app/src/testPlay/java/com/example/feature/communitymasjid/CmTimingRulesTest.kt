package com.example.feature.communitymasjid

import com.example.feature.communitymasjid.domain.CmTimingRules
import com.example.feature.communitymasjid.model.CmPrayer
import com.example.feature.communitymasjid.model.CmPrayerTime
import com.example.feature.communitymasjid.model.CmTimings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class CmTimingRulesTest {
    // Delhi, 21 March: Fajr ~05:05, sunrise ~06:25, Zuhr ~12:30, Asr ~15:50/16:45, Maghrib ~18:35, Isha ~19:45
    private val lat = 28.6139
    private val lon = 77.2090
    private val ist = TimeZone.getTimeZone("Asia/Kolkata")
    private val date = Calendar.getInstance(ist).apply { set(2026, Calendar.MARCH, 21, 12, 0, 0) }

    private fun timings(vararg pairs: Pair<CmPrayer, Pair<String, String>>) =
        CmTimings(pairs.associate { (p, t) -> p to CmPrayerTime(t.first, t.second) })

    private val valid = timings(
        CmPrayer.FAJR to ("05:15" to "05:40"),
        CmPrayer.DHUHR to ("12:45" to "13:30"),
        CmPrayer.ASR to ("16:50" to "17:15"),
        CmPrayer.MAGHRIB to ("18:37" to "18:42"),
        CmPrayer.ISHA to ("20:00" to "20:30")
    )

    @Test fun validTimingsPass() {
        assertEquals(emptyList<String>(), CmTimingRules.validate(valid, lat, lon, date))
    }

    @Test fun jamaatBeforeAzanRejected() {
        val bad = CmTimings(valid.times + (CmPrayer.ISHA to CmPrayerTime("20:30", "20:00")))
        assertTrue(CmTimingRules.validate(bad, lat, lon, date).any { "before Azan" in it })
    }

    @Test fun jamaatBeforePrayerStartRejected() {
        val bad = CmTimings(valid.times + (CmPrayer.FAJR to CmPrayerTime("04:00", "04:15")))
        assertTrue(CmTimingRules.validate(bad, lat, lon, date).any { "before the prayer starts" in it })
    }

    @Test fun fajrAfterSunriseRejected() {
        val bad = CmTimings(valid.times + (CmPrayer.FAJR to CmPrayerTime("06:20", "06:50")))
        assertTrue(CmTimingRules.validate(bad, lat, lon, date).any { "sunrise" in it })
    }

    @Test fun missingPrayerRejected() {
        val bad = CmTimings(valid.times - CmPrayer.ISHA)
        assertFalse(CmTimingRules.validate(bad, lat, lon, date).isEmpty())
    }

    @Test fun votedDownNeedsThresholdAndMajority() {
        assertFalse(CmTimingRules.isVotedDown(up = 0, down = 2))
        assertTrue(CmTimingRules.isVotedDown(up = 0, down = 3))
        assertTrue(CmTimingRules.isVotedDown(up = 2, down = 3))
        assertFalse(CmTimingRules.isVotedDown(up = 3, down = 3))
        assertFalse(CmTimingRules.isVotedDown(up = 10, down = 4))
    }

    @Test fun eidMustBeAfterSunriseAndBeforeZuhr() {
        val ok = CmTimings(valid.times + (CmPrayer.EID_FITR to CmPrayerTime("08:00", "08:00")))
        assertEquals(emptyList<String>(), CmTimingRules.validate(ok, lat, lon, date))
        val tooEarly = CmTimings(valid.times + (CmPrayer.EID_ADHA to CmPrayerTime("06:20", "06:20")))
        assertTrue(CmTimingRules.validate(tooEarly, lat, lon, date).isNotEmpty())
        val tooLate = CmTimings(valid.times + (CmPrayer.EID_FITR to CmPrayerTime("13:00", "13:00")))
        assertTrue(CmTimingRules.validate(tooLate, lat, lon, date).any { "Zuhr" in it })
    }

    @Test fun taraweehMustBeAfterIsha() {
        val ok = CmTimings(valid.times + (CmPrayer.TARAWEEH to CmPrayerTime("20:45", "20:45")))
        assertEquals(emptyList<String>(), CmTimingRules.validate(ok, lat, lon, date))
        val early = CmTimings(valid.times + (CmPrayer.TARAWEEH to CmPrayerTime("19:00", "19:00")))
        assertTrue(CmTimingRules.validate(early, lat, lon, date).any { "Isha" in it })
    }

    @Test fun distanceIsMetres() {
        val d = CmTimingRules.distanceMetres(lat, lon, lat + 0.0018, lon)
        assertTrue(d in 190.0..210.0)
    }
}
