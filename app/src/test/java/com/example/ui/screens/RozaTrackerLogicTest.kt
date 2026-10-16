package com.example.ui.screens

import com.example.prayer.HijriCalendarHelper
import com.example.prayer.HijriRegion
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class RozaTrackerLogicTest {
    private val region = HijriRegion.UMM_AL_QURA
    private var savedRegion: HijriRegion? = null

    private fun day(year: Int, month: Int, day: Int): LocalDate =
        HijriCalendarHelper.hijriToGregorian(year, month, day, region).let { (y, m, d) -> LocalDate.of(y, m, d) }

    @Before fun setup() { savedRegion = HijriRegion.preferred; HijriRegion.preferred = region }
    @After fun teardown() { HijriRegion.preferred = savedRegion }

    @Test fun `hijri date follows the app calendar`() {
        val date = RozaLogic.hijri(day(1447, 9, 15))
        assertEquals(Triple(1447, 9, 15), Triple(date.year, date.month, date.day))
    }

    @Test fun `ramadan days suggest a ramadan fast`() {
        assertTrue(RozaLogic.isRamadan(day(1447, 9, 1)))
        assertEquals(RozaMark.RAMADAN, RozaLogic.suggestedMark(day(1447, 9, 29)))
        assertFalse(RozaLogic.isRamadan(day(1447, 10, 1)))
        assertNull(RozaLogic.suggestedMark(day(1447, 8, 29)))
    }

    @Test fun `eid and tashreeq days are forbidden`() {
        assertTrue(RozaLogic.isForbidden(day(1447, 10, 1)))
        (10..13).forEach { assertTrue(RozaLogic.isForbidden(day(1447, 12, it))) }
        assertFalse(RozaLogic.isForbidden(day(1447, 10, 2)))
        assertFalse(RozaLogic.isForbidden(day(1447, 12, 9)))
        assertFalse(RozaLogic.isForbidden(day(1447, 12, 14)))
    }

    @Test fun `warnings are advisory and never raised for missed fasts`() {
        assertEquals(RozaWarning.FORBIDDEN_DAY, RozaLogic.warning(day(1447, 10, 1), RozaMark.NAFL))
        assertEquals(RozaWarning.FORBIDDEN_DAY, RozaLogic.warning(day(1447, 12, 11), RozaMark.QAZA))
        assertEquals(RozaWarning.RAMADAN_OUTSIDE_RAMADAN, RozaLogic.warning(day(1447, 8, 10), RozaMark.RAMADAN))
        assertEquals(RozaWarning.OTHER_FAST_IN_RAMADAN, RozaLogic.warning(day(1447, 9, 10), RozaMark.NAFL))
        assertEquals(RozaWarning.OTHER_FAST_IN_RAMADAN, RozaLogic.warning(day(1447, 9, 10), RozaMark.QAZA))
        assertNull(RozaLogic.warning(day(1447, 9, 10), RozaMark.RAMADAN))
        assertNull(RozaLogic.warning(day(1447, 8, 10), RozaMark.NAFL))
        assertNull(RozaLogic.warning(day(1447, 10, 1), RozaMark.MISSED))
        assertNull(RozaLogic.warning(day(1447, 9, 10), RozaMark.MISSED))
    }

    @Test fun `stats count by type and ignore future logs`() {
        val today = LocalDate.of(2026, 10, 10)
        val logs = mapOf(
            today to RozaMark.NAFL, today.minusDays(1) to RozaMark.QAZA, today.minusDays(2) to RozaMark.MISSED,
            today.minusDays(3) to RozaMark.RAMADAN, today.plusDays(1) to RozaMark.NAFL
        )
        assertEquals(RozaStats(3, 1, 1, 1, 1, 2, 2), RozaLogic.stats(logs, today))
        assertEquals(RozaStats(0, 0, 0, 0, 0, 0, 0), RozaLogic.stats(emptyMap(), today))
    }

    @Test fun `streak survives an unlogged today but not a missed one`() {
        val today = LocalDate.of(2026, 10, 10)
        val week = (1..5).associate { today.minusDays(it.toLong()) to RozaMark.NAFL }
        val older = (10..12).associate { today.minusDays(it.toLong()) to RozaMark.NAFL }
        assertEquals(5, RozaLogic.stats(week + older, today).currentStreak)
        assertEquals(6, RozaLogic.stats(week + older + (today to RozaMark.QAZA), today).currentStreak)
        val broken = RozaLogic.stats(week + older + (today to RozaMark.MISSED), today)
        assertEquals(0, broken.currentStreak)
        assertEquals(5, broken.longestStreak)
        assertEquals(0, RozaLogic.stats(older, today).currentStreak)
        assertEquals(3, RozaLogic.stats(older, today).longestStreak)
    }

    @Test fun `ongoing ramadan counts days up to today`() {
        val start = day(1447, 9, 1)
        val today = start.plusDays(9)
        val logs = (0..6).associate { start.plusDays(it.toLong()) to RozaMark.RAMADAN } +
            (start.plusDays(7) to RozaMark.MISSED) + (start.minusDays(3) to RozaMark.NAFL)
        assertEquals(RozaRamadanProgress(1447, start, 10, 7, 1, 2, true), RozaLogic.ramadanProgress(logs, today))
        assertEquals(RozaRamadanProgress(1447, start, 10, 0, 0, 10, true), RozaLogic.ramadanProgress(emptyMap(), today))
    }

    @Test fun `after ramadan the most recent logged one is summarised`() {
        val start = day(1447, 9, 1)
        val length = (day(1447, 10, 1).toEpochDay() - start.toEpochDay()).toInt()
        val today = day(1447, 11, 5)
        val logs = mapOf(start to RozaMark.RAMADAN, start.plusDays(1) to RozaMark.MISSED, today to RozaMark.NAFL)
        assertEquals(RozaRamadanProgress(1447, start, length, 1, 1, length - 2, false), RozaLogic.ramadanProgress(logs, today))
        assertNull(RozaLogic.ramadanProgress(mapOf(today to RozaMark.NAFL), today))
        assertNull(RozaLogic.ramadanProgress(emptyMap(), today))
        // Before the next Ramadan starts, last year's is still the one shown.
        assertEquals(1447, RozaLogic.ramadanProgress(logs, day(1448, 8, 20))?.hijriYear)
        val older = mapOf(day(1446, 9, 3) to RozaMark.RAMADAN)
        assertEquals(1446, RozaLogic.ramadanProgress(older, today)?.hijriYear)
    }
}
