package com.example.festival

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class FestivalScheduleTest {
    private val zone = "Asia/Kolkata"

    private fun day(year: Int, month: Int, dayOfMonth: Int): Calendar =
        Calendar.getInstance(TimeZone.getTimeZone(zone)).apply { clear(); set(year, month - 1, dayOfMonth, 12, 0) }

    @Test
    fun lateDecemberStillListsTheComingYear() {
        val schedule = IslamicFestivalRepository.build(zone, day(2026, 12, 28))
        val upcoming = schedule.festivals.filter { it.daysRemaining >= 0 }
        // Every occasion recurs within a Hijri year, so all twelve lie in the next 370 days.
        assertEquals(IslamicFestivalRepository.FESTIVALS.size, upcoming.map { it.definition.id }.toSet().size)
    }

    @Test
    fun occasionsAreSortedSoonestFirstAndInsideTheWindow() {
        val schedule = IslamicFestivalRepository.build(zone, day(2026, 6, 1))
        val days = schedule.festivals.map { it.daysRemaining }
        assertEquals(days.sorted(), days)
        assertTrue(days.all { it <= IslamicFestivalRepository.UPCOMING_WINDOW_DAYS })
        // Earlier occasions only reach back to 1 January of the same Gregorian year.
        schedule.festivals.filter { it.daysRemaining < 0 }.forEach { assertEquals(2026, it.gregorianYear) }
    }

    @Test
    fun todayIsZeroDaysAway() {
        val first = IslamicFestivalRepository.build(zone, day(2026, 3, 1)).festivals.first { it.daysRemaining >= 0 }
        val onTheDay = IslamicFestivalRepository.build(zone, day(first.gregorianYear, first.gregorianMonth, first.gregorianDay))
        assertTrue(onTheDay.festivals.any { it.definition.id == first.definition.id && it.daysRemaining == 0 })
    }
}
