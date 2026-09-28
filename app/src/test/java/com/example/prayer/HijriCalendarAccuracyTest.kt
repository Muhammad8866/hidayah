package com.example.prayer

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.JulianFields
import java.util.Calendar
import java.util.TimeZone

class HijriCalendarAccuracyTest {

    @After
    fun clearConfirmations() {
        HijriRegion.preferred = null
        HijriRegion.ALL.forEach { HijriMonthStarts.confirm(it, emptyMap()) }
    }

    @Test
    fun ummAlQuraMatchesJavaTimeForEveryMonth() {
        for (year in UmmAlQuraCalendar.FIRST_YEAR..UmmAlQuraCalendar.LAST_YEAR) {
            for (month in 1..12) {
                val (y, m, d) = HijriCalendarHelper.hijriToGregorian(year, month, 1, HijriRegion.UMM_AL_QURA)
                assertEquals("$year-$month", LocalDate.from(HijrahDate.of(year, month, 1)), LocalDate.of(y, m, d))
            }
        }
    }

    @Test
    fun ummAlQuraMatchesJavaTimeEveryDay() {
        var date = LocalDate.of(2000, 1, 1)
        while (date.year < 2060) {
            val expected = HijrahDate.from(date)
            val actual = HijriCalendarHelper.fromJulianDay(date.getLong(JulianFields.JULIAN_DAY), HijriRegion.UMM_AL_QURA)
            assertEquals(
                date.toString(),
                listOf(expected.get(ChronoField.YEAR), expected.get(ChronoField.MONTH_OF_YEAR), expected.get(ChronoField.DAY_OF_MONTH)),
                listOf(actual.year, actual.month, actual.day)
            )
            date = date.plusDays(1)
        }
    }

    @Test
    fun moonPositionMatchesMeeusExample47a() {
        val moon = CrescentVisibility.moonPosition(2448724.5)
        assertEquals(133.162655, moon.longitude, 1e-5)
        assertEquals(-3.229126, moon.latitude, 1e-5)
        assertEquals(368409.7, moon.distanceKm, 0.1)
    }

    /** First days announced by Indian committees (Delhi, Lucknow, Hyderabad) or implied by the announced Eid/Ashura. */
    @Test
    fun indiaMatchesAnnouncedMonthStarts() = assertMonthStarts(
        HijriRegion.INDIA,
        listOf(
            "1444-9" to "2023-03-24", "1444-10" to "2023-04-22", "1445-1" to "2023-07-20", "1445-10" to "2024-04-11",
            "1446-1" to "2024-07-08", "1446-3" to "2024-09-05", "1446-9" to "2025-03-02", "1446-10" to "2025-03-31",
            "1446-12" to "2025-05-29", "1447-1" to "2025-06-27", "1447-3" to "2025-08-25", "1447-9" to "2026-02-19",
            "1447-10" to "2026-03-21", "1447-12" to "2026-05-19", "1448-3" to "2026-08-15"
        )
    )

    /**
     * First days announced by Pakistan's Central Ruet-e-Hilal Committee. Rabi al-Thani 1448 is not
     * listed: clouds hid a crescent that was astronomically visible, see [confirmedMonthStartCarriesForwardUntilResync].
     */
    @Test
    fun pakistanMatchesAnnouncedMonthStarts() = assertMonthStarts(
        HijriRegion.PAKISTAN,
        listOf(
            "1444-9" to "2023-03-23", "1444-12" to "2023-06-20", "1445-1" to "2023-07-20", "1445-10" to "2024-04-10",
            "1446-1" to "2024-07-08", "1446-6" to "2024-12-04", "1446-9" to "2025-03-02", "1446-10" to "2025-03-31",
            "1446-11" to "2025-04-29", "1447-1" to "2025-06-27", "1447-2" to "2025-07-27", "1447-3" to "2025-08-25",
            "1447-6" to "2025-11-23", "1447-9" to "2026-02-19", "1447-10" to "2026-03-21", "1447-12" to "2026-05-18"
        )
    )

    /**
     * Months fixed by Indonesia's sidang isbat (Kemenag, Neo-MABIMS) or implied by its national
     * holidays. 1 Ramadan 1446 and 1 Dhu al-Hijjah 1446 were decided by Aceh just meeting the criterion.
     */
    @Test
    fun indonesiaMatchesOfficialMonthStarts() = assertMonthStarts(
        HijriRegion.INDONESIA,
        listOf(
            "1444-9" to "2023-03-23", "1444-10" to "2023-04-22", "1444-12" to "2023-06-20", "1445-9" to "2024-03-12",
            "1445-10" to "2024-04-10", "1445-12" to "2024-06-08", "1446-1" to "2024-07-07", "1446-9" to "2025-03-01",
            "1446-10" to "2025-03-31", "1446-12" to "2025-05-28", "1447-1" to "2025-06-27", "1447-7" to "2025-12-21",
            "1447-9" to "2026-02-19", "1447-10" to "2026-03-21", "1448-4" to "2026-09-13"
        )
    )

    /** Months announced by Malaysia's Keeper of the Rulers' Seal (also followed by Brunei and Singapore). */
    @Test
    fun malaysiaMatchesOfficialMonthStarts() = assertMonthStarts(
        HijriRegion.MALAYSIA,
        listOf(
            "1445-9" to "2024-03-12", "1445-10" to "2024-04-10", "1446-9" to "2025-03-02", "1446-10" to "2025-03-31",
            "1446-12" to "2025-05-29", "1447-7" to "2025-12-22", "1447-9" to "2026-02-19", "1447-10" to "2026-03-21"
        )
    )

    @Test
    fun ummAlQuraMatchesSaudiDateOn26September2026() {
        val saudi = dateAt("Asia/Riyadh", 2026, Calendar.SEPTEMBER, 26)
        assertEquals(listOf(15, 4, 1448), listOf(saudi.day, saudi.month, saudi.year))
    }

    @Test
    fun chosenCalendarOverridesTheLocation() {
        HijriRegion.preferred = HijriRegion.PAKISTAN
        assertEquals(HijriRegion.PAKISTAN, HijriRegion.forTimeZone("Europe/London"))
        assertEquals(HijriRegion.UMM_AL_QURA, HijriRegion.automaticFor("Europe/London"))
        assertEquals(12, dateAt("Europe/London", 2026, Calendar.SEPTEMBER, 24).day)
        HijriRegion.preferred = null
        assertEquals(13, dateAt("Europe/London", 2026, Calendar.SEPTEMBER, 24).day)
    }

    @Test
    fun southAsiaIsADayBehindSaudiArabiaOn24September2026() {
        assertEquals(12, dateAt("Asia/Kolkata", 2026, Calendar.SEPTEMBER, 24).day)
        assertEquals(12, dateAt("Asia/Karachi", 2026, Calendar.SEPTEMBER, 24).day)
        val saudi = dateAt("Asia/Riyadh", 2026, Calendar.SEPTEMBER, 24)
        assertEquals(listOf(13, 4, 1448), listOf(saudi.day, saudi.month, saudi.year))
    }

    @Test
    fun confirmedMonthStartCarriesForwardUntilResync() {
        // Pakistan announced 1 Rabi al-Thani 1448 on 14 Sep 2026 instead of the predicted 13 Sep.
        val today = noon("Asia/Karachi", 2026, Calendar.SEPTEMBER, 24)
        HijriCalendarHelper.adjustDate(today, dayOffset = 0, days = -1, region = HijriRegion.PAKISTAN)

        assertEquals(11, HijriCalendarHelper.getHijriDate(today).day)
        assertEquals(-1, HijriCalendarHelper.adjustmentDays(today))
        // The committee looks again on its own 29th (12 Oct), sees the crescent, and is back in step.
        assertEquals(Triple(2026, 10, 13), HijriCalendarHelper.hijriToGregorian(1448, 5, 1, HijriRegion.PAKISTAN))
        assertEquals(0, HijriCalendarHelper.adjustmentDays(noon("Asia/Karachi", 2026, Calendar.OCTOBER, 20)))

        val saved = HijriCalendarHelper.confirmedMonths(HijriRegion.PAKISTAN)
        HijriMonthStarts.confirm(HijriRegion.PAKISTAN, emptyMap())
        HijriCalendarHelper.restoreConfirmedMonths(HijriRegion.PAKISTAN, saved)
        assertEquals(11, HijriCalendarHelper.getHijriDate(today).day)

        HijriCalendarHelper.adjustDate(today, dayOffset = 0, days = 0, region = HijriRegion.PAKISTAN)
        assertEquals(12, HijriCalendarHelper.getHijriDate(today).day)
        assertEquals(0, HijriCalendarHelper.adjustmentDays(today))
    }

    @Test
    fun adjustingOnTheFirstKeepsTheDayBeforeInThePreviousMonth() {
        // 13 Oct 2026 is predicted as 1 Jumada al-Ula 1448 in Pakistan; the committee may still call it 30 Rabi al-Thani.
        val day = noon("Asia/Karachi", 2026, Calendar.OCTOBER, 13)
        assertEquals(listOf(1, 5), HijriCalendarHelper.getHijriDate(day).let { listOf(it.day, it.month) })
        HijriCalendarHelper.adjustDate(day, dayOffset = 0, days = -1, region = HijriRegion.PAKISTAN)
        assertEquals(listOf(30, 4), HijriCalendarHelper.getHijriDate(day).let { listOf(it.day, it.month) })
        assertEquals(listOf(1, 5), HijriCalendarHelper.getHijriDate(day, dayOffset = 1).let { listOf(it.day, it.month) })
    }

    @Test
    fun localMonthsHave29Or30DaysAndStayNearUmmAlQura() {
        for (region in HijriRegion.ALL.filter { it.followsLocalSighting }) {
            for (year in 1446..1480) {
                for (month in 1..12) {
                    val start = HijriCalendarHelper.toJulianDay(year, month, 1, region)
                    val next = if (month == 12) HijriCalendarHelper.toJulianDay(year + 1, 1, 1, region)
                    else HijriCalendarHelper.toJulianDay(year, month + 1, 1, region)
                    assertTrue("${region.id} $year-$month has ${next - start} days", next - start in 29..30)
                    val lag = start - HijriCalendarHelper.toJulianDay(year, month, 1, HijriRegion.UMM_AL_QURA)
                    assertTrue("${region.id} $year-$month starts $lag days after Umm al-Qura", lag in -1..3)
                }
            }
        }
    }

    @Test
    fun everyDayRoundTripsThroughGregorian() {
        val from = HijriCalendarHelper.gregorianToJulianDay(2024, 1, 1)
        val to = HijriCalendarHelper.gregorianToJulianDay(2031, 1, 1)
        for (region in HijriRegion.ALL) {
            for (jdn in from until to) {
                val date = HijriCalendarHelper.fromJulianDay(jdn, region)
                assertEquals("${region.id} $jdn", jdn, HijriCalendarHelper.toJulianDay(date.year, date.month, date.day, region))
            }
        }
    }

    @Test
    fun regionFollowsTimeZone() {
        assertEquals(HijriRegion.INDIA, HijriRegion.forTimeZone("Asia/Kolkata"))
        assertEquals(HijriRegion.PAKISTAN, HijriRegion.forTimeZone("Asia/Karachi"))
        assertEquals(HijriRegion.UMM_AL_QURA, HijriRegion.forTimeZone("Asia/Riyadh"))
        assertEquals(HijriRegion.UMM_AL_QURA, HijriRegion.forTimeZone("Europe/London"))
        assertEquals(HijriRegion.INDONESIA, HijriRegion.forTimeZone("Asia/Jakarta"))
        assertEquals(HijriRegion.MALAYSIA, HijriRegion.forTimeZone("Asia/Kuala_Lumpur"))
        assertEquals(HijriRegion.MALAYSIA, HijriRegion.forTimeZone("Asia/Singapore"))
    }

    private fun assertMonthStarts(region: HijriRegion, announced: List<Pair<String, String>>) {
        val mismatches = announced.mapNotNull { (hijri, gregorian) ->
            val (year, month) = hijri.split('-').map(String::toInt)
            val (y, m, d) = HijriCalendarHelper.hijriToGregorian(year, month, 1, region)
            val calculated = LocalDate.of(y, m, d).toString()
            if (calculated == gregorian) null else "$hijri: announced $gregorian, calculated $calculated"
        }
        assertTrue(mismatches.joinToString("\n"), mismatches.isEmpty())
    }

    private fun noon(timeZoneId: String, year: Int, month: Int, day: Int): Calendar =
        Calendar.getInstance(TimeZone.getTimeZone(timeZoneId)).apply { clear(); set(year, month, day, 12, 0) }

    private fun dateAt(timeZoneId: String, year: Int, month: Int, day: Int) =
        HijriCalendarHelper.getHijriDate(noon(timeZoneId, year, month, day))
}
