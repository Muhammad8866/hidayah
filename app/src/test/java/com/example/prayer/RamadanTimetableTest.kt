package com.example.prayer

import com.example.domain.model.CityLocation
import com.example.domain.model.PrayerTimeConfig
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class RamadanTimetableTest {
    private val city = CityLocation("Mumbai", "India", 19.076, 72.8777, "Asia/Kolkata")

    private fun at(hour: Int) = Calendar.getInstance(TimeZone.getTimeZone(city.timeZoneId)).apply {
        clear(); set(2027, Calendar.FEBRUARY, 15, hour, 0)
    }

    @Test
    fun noScheduleOutsideRamadan() {
        assertNull(RamadanTimetable.build(HijriCalendarHelper.create(1, 10, 1448), city, PrayerTimeConfig(), at(12)))
        assertNull(RamadanTimetable.build(HijriCalendarHelper.create(29, 8, 1448), city, PrayerTimeConfig(), at(12)))
    }

    @Test
    fun buildsThirtyDaysWithSehriBeforeIftar() {
        val schedule = RamadanTimetable.build(HijriCalendarHelper.create(5, 9, 1448), city, PrayerTimeConfig(), at(12))!!
        assertEquals(30, schedule.days.size)
        assertEquals(5, schedule.currentRoza)
        schedule.days.forEach { assertTrue(it.sehriEndsMillis < it.iftarMillis) }
        // Roza 5 is today (15 Feb) during daytime, so Roza 1 is 11 Feb.
        val first = Calendar.getInstance(TimeZone.getTimeZone(city.timeZoneId)).apply { timeInMillis = schedule.days[0].dateMillis }
        assertEquals(11, first.get(Calendar.DAY_OF_MONTH))
        assertTrue(schedule.days.last().dependsOnMoonSighting)
    }

    @Test
    fun upcomingRamadanOnlyOutsideRamadan() {
        assertNull(RamadanTimetable.buildUpcoming(HijriCalendarHelper.create(5, 9, 1448), city, PrayerTimeConfig(), at(12)))
    }

    @Test
    fun upcomingRamadanStartsAtMaghribOnTheEve() {
        // 10 Sha'ban at noon on 15 Feb: Sha'ban 1448 has 30 days in India's sighting calendar,
        // so 1 Ramadan is 21 days later → 8 Mar.
        val upcoming = RamadanTimetable.buildUpcoming(HijriCalendarHelper.create(10, 8, 1448), city, PrayerTimeConfig(), at(12))!!
        assertEquals(1448, upcoming.hijriYear)
        assertEquals(30, upcoming.days.size)
        val first = Calendar.getInstance(TimeZone.getTimeZone(city.timeZoneId)).apply { timeInMillis = upcoming.days[0].dateMillis }
        assertEquals(Calendar.MARCH, first.get(Calendar.MONTH))
        assertEquals(8, first.get(Calendar.DAY_OF_MONTH))
        val eve = Calendar.getInstance(TimeZone.getTimeZone(city.timeZoneId)).apply { timeInMillis = upcoming.startsAtMillis }
        assertEquals(7, eve.get(Calendar.DAY_OF_MONTH))
        assertTrue(eve.get(Calendar.HOUR_OF_DAY) in 17..19)
    }

    @Test
    fun afterShawwalCountsToNextYear() {
        val upcoming = RamadanTimetable.buildUpcoming(HijriCalendarHelper.create(2, 10, 1448), city, PrayerTimeConfig(), at(12))!!
        assertEquals(1449, upcoming.hijriYear)
        // 2 Shawwal → 1 Ramadan next year is ~323 days away.
        assertEquals(323.0, (upcoming.startsAtMillis - at(12).timeInMillis) / 86_400_000.0, 1.0)
    }

    @Test
    fun afterMaghribHijriDayMapsToTomorrowsFast() {
        // After Maghrib the Hijri date already shows the upcoming roza (6), which is fasted on 16 Feb.
        val schedule = RamadanTimetable.build(HijriCalendarHelper.create(6, 9, 1448), city, PrayerTimeConfig(), at(21))!!
        val first = Calendar.getInstance(TimeZone.getTimeZone(city.timeZoneId)).apply { timeInMillis = schedule.days[0].dateMillis }
        assertEquals(11, first.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun liveStatusFollowsTheClockThroughSehriAndIftar() {
        val days = RamadanTimetable.build(HijriCalendarHelper.create(5, 9, 1448), city, PrayerTimeConfig(), at(12))!!.days
        val day5 = days[4]

        val beforeSehri = RamadanTimetable.liveStatus(days, day5.sehriEndsMillis - 60_000)!!
        assertEquals(RamadanTimetable.Phase.SEHRI, beforeSehri.phase)
        assertEquals(5, beforeSehri.day.roza)
        assertEquals(day5.sehriEndsMillis, beforeSehri.targetMillis)
        assertEquals(0f, beforeSehri.fastProgress, 0f)

        val midday = RamadanTimetable.liveStatus(days, (day5.sehriEndsMillis + day5.iftarMillis) / 2)!!
        assertEquals(RamadanTimetable.Phase.FASTING, midday.phase)
        assertEquals(day5.iftarMillis, midday.targetMillis)
        assertEquals(0.5f, midday.fastProgress, 0.01f)
        assertEquals(6, midday.upcomingNight)

        // Right after Iftar the next fast's Sehri is the next event.
        val afterIftar = RamadanTimetable.liveStatus(days, day5.iftarMillis + 1)!!
        assertEquals(RamadanTimetable.Phase.SEHRI, afterIftar.phase)
        assertEquals(6, afterIftar.day.roza)

        assertNull(RamadanTimetable.liveStatus(days, days.last().iftarMillis + 1))
    }

    @Test
    fun ashrasAndOddNights() {
        assertEquals(listOf(1, 1, 2, 2, 3, 3), listOf(1, 10, 11, 20, 21, 30).map(RamadanTimetable::ashraOf))
        assertEquals(listOf(21, 23, 25, 27, 29), (1..30).filter(RamadanTimetable::isOddNight))
    }
}
