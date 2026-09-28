package com.example.prayer

import com.example.domain.model.AsrJuristicMethod
import com.example.domain.model.CalculationMethod
import com.example.domain.model.HighLatitudeRule
import com.example.domain.model.PrayerTimeConfig
import kotlin.math.abs
import kotlin.math.roundToLong
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class AstronomicalPrayerCalculatorTest {

    @Test
    fun matchesPublishedAdhanReferenceFixture() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York")).apply {
            clear()
            set(2015, Calendar.JULY, 12, 12, 0, 0)
        }
        val times = AstronomicalPrayerCalculator.calculateTimes(
            cal,
            latitude = 35.7750,
            longitude = -78.6336,
            config = PrayerTimeConfig(
                method = CalculationMethod.ISNA,
                asrMethod = AsrJuristicMethod.HANAFI,
                maghribIhtiyatMin = 0
            )
        )

        // The reference rounds to the nearest minute; this app rounds prayer starts up and
        // sunrise down, so each value is the reference or one minute on the safe side.
        fun minuteOf(hours: Double) = (hours * 60).roundToLong()
        fun assertSafe(reference: Double, actual: Double, laterIsSafe: Boolean) {
            val diff = minuteOf(actual) - minuteOf(reference)
            assertTrue("$actual vs reference $reference", if (laterIsSafe) diff in 0..1 else diff in -1..0)
        }
        assertSafe(4 + 42 / 60.0, times.fajrHours, laterIsSafe = true)
        assertSafe(6 + 8 / 60.0, times.sunriseHours, laterIsSafe = false)
        assertSafe(13 + 21 / 60.0, times.dhuhrHours, laterIsSafe = true)
        assertSafe(18 + 22 / 60.0, times.asrHours, laterIsSafe = true)
        assertSafe(20 + 32 / 60.0, times.maghribHours, laterIsSafe = true)
        assertSafe(21 + 57 / 60.0, times.ishaHours, laterIsSafe = true)
    }

    @Test
    fun highLatitudeRulesMatchPublishedAdhanFixtures() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/London")).apply {
            clear()
            set(2020, Calendar.JUNE, 15, 12, 0, 0)
        }
        fun calculated(rule: HighLatitudeRule): Pair<Double, Double> {
            val times = AstronomicalPrayerCalculator.calculateTimes(
                cal,
                latitude = 55.983226,
                longitude = -3.216649,
                config = PrayerTimeConfig(
                    method = CalculationMethod.MWL,
                    asrMethod = AsrJuristicMethod.STANDARD,
                    highLatitudeRule = rule
                )
            )
            return times.fajrHours to times.ishaHours
        }

        val oneMinuteTolerance = 1.1 / 60.0
        val middle = calculated(HighLatitudeRule.MIDDLE_OF_NIGHT)
        assertEquals(1 + 14.0 / 60.0, middle.first, oneMinuteTolerance)
        assertEquals(25 + 14.0 / 60.0, middle.second, oneMinuteTolerance)

        val seventh = calculated(HighLatitudeRule.SEVENTH_OF_NIGHT)
        assertEquals(3 + 31.0 / 60.0, seventh.first, oneMinuteTolerance)
        assertEquals(22 + 56.0 / 60.0, seventh.second, oneMinuteTolerance)

        val twilight = calculated(HighLatitudeRule.TWILIGHT_ANGLE)
        assertEquals(2 + 31.0 / 60.0, twilight.first, oneMinuteTolerance)
        assertEquals(23 + 50.0 / 60.0, twilight.second, oneMinuteTolerance)
    }

    @Test
    fun timeConversionPreservesAfterMidnightIshaAndDstWallClock() {
        val london = Calendar.getInstance(TimeZone.getTimeZone("Europe/London")).apply {
            clear()
            set(2026, Calendar.JUNE, 15, 12, 0, 0)
        }
        val afterMidnight = Calendar.getInstance(london.timeZone).apply {
            timeInMillis = AstronomicalPrayerCalculator.formatHoursToTimeInMillis(london, 25.25)
        }
        assertEquals(16, afterMidnight.get(Calendar.DAY_OF_MONTH))
        assertEquals(1, afterMidnight.get(Calendar.HOUR_OF_DAY))
        assertEquals(15, afterMidnight.get(Calendar.MINUTE))

        val newYorkBeforeDst = Calendar.getInstance(TimeZone.getTimeZone("America/New_York")).apply {
            clear()
            set(2026, Calendar.MARCH, 8, 0, 30, 0)
        }
        val times = AstronomicalPrayerCalculator.calculateTimes(
            newYorkBeforeDst,
            40.7128,
            -74.0060,
            PrayerTimeConfig(method = CalculationMethod.ISNA)
        )
        assertTrue("Sunrise must use the post-transition daylight offset", times.sunriseHours in 6.5..8.0)
    }

    @Test
    fun ummAlQuraUsesLongerIshaIntervalInRamadan() {
        fun ishaIntervalOn(year: Int, month: Int, day: Int): Double {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Riyadh")).apply {
                clear()
                set(year, month, day, 12, 0, 0)
            }
            val times = AstronomicalPrayerCalculator.calculateTimes(
                cal,
                latitude = 21.4225,
                longitude = 39.8262,
                config = PrayerTimeConfig(method = CalculationMethod.MAKKAH, maghribIhtiyatMin = 0)
            )
            return times.ishaHours - times.maghribHours
        }

        assertEquals(2.0, ishaIntervalOn(2026, Calendar.MARCH, 1), 0.0001)
        assertEquals(1.5, ishaIntervalOn(2026, Calendar.APRIL, 1), 0.0001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidCoordinatesInsteadOfReturningPlausibleTimes() {
        AstronomicalPrayerCalculator.calculateTimes(
            Calendar.getInstance(TimeZone.getTimeZone("UTC")),
            latitude = 999.0,
            longitude = 0.0,
            config = PrayerTimeConfig()
        )
    }

    @Test
    fun testPrayerTimesCalculation_Mecca() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Riyadh")).apply {
            set(2026, Calendar.SEPTEMBER, 22, 12, 0, 0)
        }
        val config = PrayerTimeConfig(
            method = CalculationMethod.MAKKAH,
            asrMethod = AsrJuristicMethod.STANDARD
        )

        val times = AstronomicalPrayerCalculator.calculateTimes(
            calendar = cal,
            latitude = 21.4225,
            longitude = 39.8262,
            config = config
        )

        // Verify logical progression of daily prayers: Fajr < Sunrise < Dhuhr < Asr < Maghrib < Isha
        assertTrue("Fajr must be earlier than sunrise", times.fajrHours < times.sunriseHours)
        assertTrue("Sunrise must be earlier than dhuhr", times.sunriseHours < times.dhuhrHours)
        assertTrue("Dhuhr must be earlier than asr", times.dhuhrHours < times.asrHours)
        assertTrue("Asr must be earlier than maghrib", times.asrHours < times.maghribHours)
        assertTrue("Maghrib must be earlier than isha", times.maghribHours < times.ishaHours)
    }

    @Test
    fun testAsrJuristicComparison_HanafiIsLaterThanStandard() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata")).apply {
            set(2026, Calendar.SEPTEMBER, 22, 12, 0, 0)
        }
        val standardConfig = PrayerTimeConfig(
            method = CalculationMethod.KARACHI,
            asrMethod = AsrJuristicMethod.STANDARD
        )
        val hanafiConfig = PrayerTimeConfig(
            method = CalculationMethod.KARACHI,
            asrMethod = AsrJuristicMethod.HANAFI
        )

        val standardTimes = AstronomicalPrayerCalculator.calculateTimes(cal, 28.6139, 77.2090, standardConfig)
        val hanafiTimes = AstronomicalPrayerCalculator.calculateTimes(cal, 28.6139, 77.2090, hanafiConfig)

        // Hanafi Asr uses shadow factor 2.0x, so Asr starts strictly later than Standard 1.0x
        assertTrue(
            "Hanafi Asr (${hanafiTimes.asrHours}) should be strictly later than Standard Asr (${standardTimes.asrHours})",
            hanafiTimes.asrHours > standardTimes.asrHours
        )
    }

    @Test
    fun testSolarEphemerisCalculation() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.SEPTEMBER, 22, 12, 0, 0)
        }
        val ephemeris = AstronomicalPrayerCalculator.calculateSolarParameters(
            calendar = cal,
            latitude = 0.0,
            longitude = 0.0
        )

        // On autumn equinox (approx Sept 22/23), solar declination is very close to 0 degrees
        assertTrue(
            "Declination near equinox should be within +/- 3 degrees, was ${ephemeris.declinationDeg}",
            Math.abs(ephemeris.declinationDeg) < 3.0
        )
        // Julian Day in 2026 should be approx 2,461,300+
        assertTrue("Julian Date should be ~2461300+", ephemeris.julianDate > 2460000.0)
        // Equation of time in late September is around +7 to +8 minutes
        assertTrue("Equation of time should be a reasonable value (-20 to +20 min)", ephemeris.equationOfTimeMinutes in -20.0..20.0)
    }

    @Test
    fun testManualOffsetAdjustments() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.SEPTEMBER, 22, 12, 0, 0)
        }
        val baseConfig = PrayerTimeConfig(
            method = CalculationMethod.MWL,
            asrMethod = AsrJuristicMethod.STANDARD,
            manualFajrOffsetMin = 0,
            manualDhuhrOffsetMin = 0
        )
        val offsetConfig = PrayerTimeConfig(
            method = CalculationMethod.MWL,
            asrMethod = AsrJuristicMethod.STANDARD,
            manualFajrOffsetMin = 10,
            manualDhuhrOffsetMin = -5
        )

        val baseTimes = AstronomicalPrayerCalculator.calculateTimes(cal, 51.5074, -0.1278, baseConfig)
        val offsetTimes = AstronomicalPrayerCalculator.calculateTimes(cal, 51.5074, -0.1278, offsetConfig)

        assertEquals(baseTimes.fajrHours + (10.0 / 60.0), offsetTimes.fajrHours, 0.001)
        assertEquals(baseTimes.dhuhrHours - (5.0 / 60.0), offsetTimes.dhuhrHours, 0.001)
    }

    @Test
    fun testHijriCalendarConversion() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.SEPTEMBER, 22, 12, 0, 0)
        }

        val hijri = HijriCalendarHelper.getHijriDate(cal, dayOffset = 0)

        // 22 September 2026 is 11 Rabi al-Thani 1448 in the Umm al-Qura calendar (UTC has no local sighting).
        assertEquals(1448, hijri.year)
        assertEquals(4, hijri.month)
        assertEquals(11, hijri.day)
        assertTrue("Hijri day should be between 1 and 30", hijri.day in 1..30)
        assertTrue("Hijri month should be between 1 and 12", hijri.month in 1..12)
        assertNotNull(hijri.monthNameEnglish)
        assertNotNull(hijri.monthNameArabic)

        // After Maghrib the Hijri date is the next day's
        val adjusted = HijriCalendarHelper.getHijriDate(cal, dayOffset = 1)
        assertEquals(12, adjusted.day)
        val formatted = "${adjusted.day} ${adjusted.monthNameEnglish} ${adjusted.year} AH"
        assertTrue("Formatted string should contain AH", formatted.contains("AH"))
    }

    @Test
    fun testQiblaCalculator() {
        // From London (51.5074 N, 0.1278 W) to Makkah (21.4225 N, 39.8262 E)
        val bearing = QiblaCalculator.calculateQiblaBearing(51.5074, -0.1278)
        val distance = QiblaCalculator.calculateDistanceToMakkahKm(51.5074, -0.1278)

        // London bearing to Mecca is approx 118-120 degrees (South-East)
        assertTrue("Bearing should be approx 118-120 deg", bearing in 115.0..125.0)
        assertTrue("Distance should be roughly 4700-4900 km", distance in 4600.0..5000.0)
    }

    @Test
    fun timesAreWholeMinutesRoundedInTheSafeDirection() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata")).apply {
            clear()
            set(2026, Calendar.MARCH, 1, 12, 0, 0)
        }
        val precise = AstronomicalPrayerCalculator.calculateTimes(
            cal, 28.6139, 77.2090, PrayerTimeConfig(maghribIhtiyatMin = 0)
        )
        val withIhtiyat = AstronomicalPrayerCalculator.calculateTimes(
            cal, 28.6139, 77.2090, PrayerTimeConfig()
        )
        listOf(precise.fajrHours, precise.sunriseHours, precise.dhuhrHours, precise.asrHours,
            precise.maghribHours, precise.ishaHours, precise.sehriEndHours).forEach { hours ->
            assertEquals("Times must be whole minutes", 0.0, abs(hours * 60 - (hours * 60).roundToLong()), 1e-6)
        }
        assertTrue("Sehri ends before the Fajr azan", precise.sehriEndHours < precise.fajrHours)
        assertEquals(1.0 / 60.0, precise.fajrHours - precise.sehriEndHours, 1e-6)
        assertEquals("Default ihtiyat delays Maghrib by 2 minutes",
            2.0 / 60.0, withIhtiyat.maghribHours - precise.maghribHours, 1e-6)
    }

    @Test
    fun automaticRuleKeepsRealFajrWhenTwilightExists() {
        // London, mid-April: 18° twilight exists, so Automatic must not replace it with a later 1/7-night Fajr.
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/London")).apply {
            clear()
            set(2026, Calendar.APRIL, 15, 12, 0, 0)
        }
        fun times(rule: HighLatitudeRule) = AstronomicalPrayerCalculator.calculateTimes(
            cal, 51.5074, -0.1278, PrayerTimeConfig(method = CalculationMethod.KARACHI, highLatitudeRule = rule)
        )
        val automatic = times(HighLatitudeRule.AUTOMATIC)
        val seventh = times(HighLatitudeRule.SEVENTH_OF_NIGHT)
        assertTrue("Automatic Fajr ${automatic.fajrHours} should be earlier than 1/7-night ${seventh.fajrHours}",
            automatic.fajrHours < seventh.fajrHours - 0.25)
        assertTrue("Automatic Isha should be later than 1/7-night", automatic.ishaHours > seventh.ishaHours + 0.25)
    }

    @Test
    fun timezoneResolverFallsBackToLongitudeWhenPhoneZoneIsImplausible() {
        val newYork = TimeZone.getTimeZone("America/New_York")
        assertEquals("America/New_York", TimeZoneResolver.resolve(-74.0, null, newYork))
        assertEquals("Etc/GMT-5", TimeZoneResolver.resolve(74.3, null, newYork))
    }
}
