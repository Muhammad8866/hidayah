package com.example.ui.screens

import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class RozaTrackerFeaturesTest {
    private val start = LocalDate.of(2026, 1, 1)
    private val year = (0L until 720L).map { start.plusDays(it) }

    private fun firstHijri(month: Int, day: Int) = year.first { RozaLogic.hijri(it).let { h -> h.month == month && h.day == day } }

    @Test fun `special days outrank weekly fasts`() {
        assertEquals(RozaSunnahFasts.ARAFAH, RozaSunnahFasts.occasion(firstHijri(12, 9))?.title)
        assertEquals(RozaSunnahFasts.ASHURA, RozaSunnahFasts.occasion(firstHijri(1, 10))?.title)
        assertEquals(RozaSunnahFasts.TASUA, RozaSunnahFasts.occasion(firstHijri(1, 9))?.title)
        assertEquals(RozaSunnahFasts.WHITE_DAYS, RozaSunnahFasts.occasion(firstHijri(3, 14))?.title)
        assertEquals(RozaSunnahFasts.SHAWWAL, RozaSunnahFasts.occasion(firstHijri(10, 2))?.title)
    }

    @Test fun `ramadan and forbidden days never have an occasion`() {
        year.filter { RozaLogic.isRamadan(it) || RozaLogic.isForbidden(it) }.also { assertTrue(it.isNotEmpty()) }
            .forEach { assertNull(RozaSunnahFasts.occasion(it)) }
        assertNull(RozaSunnahFasts.occasion(firstHijri(12, 13)))
        assertNull(RozaSunnahFasts.occasion(firstHijri(10, 1)))
    }

    @Test fun `every occasion carries a reference and plain days fall back to monday and thursday`() {
        year.forEach { date ->
            val day = RozaSunnahFasts.occasion(date)
            if (day != null) {
                assertEquals(date, day.date)
                assertTrue(day.reference.isNotBlank())
            }
            if (day?.title == RozaSunnahFasts.MONDAY) assertEquals(DayOfWeek.MONDAY, date.dayOfWeek)
            if (day?.title == RozaSunnahFasts.THURSDAY) assertEquals(DayOfWeek.THURSDAY, date.dayOfWeek)
        }
    }

    @Test fun `upcoming is ordered, sized and lists the Shawwal window once`() {
        val list = RozaSunnahFasts.upcoming(start, 5)
        assertEquals(5, list.size)
        assertEquals(list.map { it.date }.sorted(), list.map { it.date })
        assertTrue(list.all { it.date >= start })
        val shawwal = RozaSunnahFasts.upcoming(firstHijri(10, 1), 12)
        assertEquals(1, shawwal.count { it.title == RozaSunnahFasts.SHAWWAL })
        assertEquals(firstHijri(10, 2), shawwal.first().date)
        assertTrue(RozaSunnahFasts.upcoming(start, 0).isEmpty())
    }

    @Test fun `planner rounds weeks up and rejects invalid input`() {
        val today = LocalDate.of(2026, 10, 10)
        assertEquals(today.plusWeeks(5), RozaQazaPlanner.finishDate(10, 2, today))
        assertEquals(today.plusWeeks(4), RozaQazaPlanner.finishDate(10, 3, today))
        assertEquals(today.plusWeeks(1), RozaQazaPlanner.finishDate(1, 7, today))
        assertEquals(4L, RozaQazaPlanner.weeksNeeded(10, 3))
        assertNull(RozaQazaPlanner.finishDate(0, 2, today))
        assertNull(RozaQazaPlanner.finishDate(5, 0, today))
        assertNull(RozaQazaPlanner.finishDate(5, 8, today))
        assertNotNull(RozaQazaPlanner.finishDate(Int.MAX_VALUE, 1, today))
    }
}
