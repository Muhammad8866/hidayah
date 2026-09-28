package com.example.prayer

import com.example.domain.model.CityLocation
import com.example.domain.model.PrayerTimeConfig
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Builds the Ramadan Sehri/Iftar timetable for the selected location.
 *
 * Ramadan start is derived from the live Hijri date (the region's moon-sighting calendar,
 * including the user's manual moon-sighting adjustment), and daily Sehri/Iftar times come
 * from the same astronomical calculator used for prayer times: Sehri ends at Fajr, Iftar at Maghrib.
 */
object RamadanTimetable {
    const val RAMADAN_MONTH = 9

    data class RamadanDay(
        val roza: Int,
        val dateMillis: Long,
        val sehriEndsMillis: Long,
        val iftarMillis: Long,
        val sehriFormatted: String,
        val iftarFormatted: String,
        /** Day 30 only happens if the Shawwal moon is not sighted on the 29th. */
        val dependsOnMoonSighting: Boolean
    ) {
        /** 1 = Rahmat (1-10), 2 = Maghfirat (11-20), 3 = Nijat (21-30). */
        val ashra: Int get() = ashraOf(roza)

        /** The night before this fast is one of the odd nights of the last Ashra (21, 23, 25, 27, 29). */
        val oddNightBefore: Boolean get() = isOddNight(roza)

        val fastDurationMillis: Long get() = iftarMillis - sehriEndsMillis
    }

    enum class Phase {
        /** Night before a fast: Sehri is still allowed until [LiveStatus.targetMillis]. */
        SEHRI,
        /** Fasting: Iftar is at [LiveStatus.targetMillis]. */
        FASTING
    }

    /** What is happening right now in the timetable, derived from the clock so it stays live. */
    data class LiveStatus(
        val phase: Phase,
        val day: RamadanDay,
        val targetMillis: Long,
        /** 0..1 of the current fast (Sehri end -> Iftar); 0 while it is Sehri time. */
        val fastProgress: Float,
        /** Night that starts at the next Maghrib (night N precedes fast N). */
        val upcomingNight: Int
    )

    data class RamadanSchedule(
        val hijriYear: Int,
        /** Roza number currently being fasted (or the upcoming one on the night before). */
        val currentRoza: Int,
        val days: List<RamadanDay>
    )

    /** The next Ramadan, projected from the live Hijri date while it is not Ramadan yet. */
    data class UpcomingRamadan(
        val hijriYear: Int,
        /** Maghrib on the eve of 1 Ramadan: the first night (Taraweeh) begins here. */
        val startsAtMillis: Long,
        /** Tentative timetable; the actual start depends on moon sighting. */
        val days: List<RamadanDay>
    )

    fun isRamadan(hijri: HijriCalendarHelper.HijriDate): Boolean = hijri.month == RAMADAN_MONTH

    fun ashraOf(roza: Int): Int = ((roza - 1) / 10 + 1).coerceIn(1, 3)

    fun isOddNight(night: Int): Boolean = night in 21..29 && night % 2 == 1

    /**
     * The fast whose Iftar has not happened yet decides the phase: before its Sehri ends it is
     * Sehri time, afterwards the user is fasting. Returns null once the last Iftar has passed.
     */
    fun liveStatus(days: List<RamadanDay>, nowMillis: Long): LiveStatus? {
        val day = days.firstOrNull { it.iftarMillis > nowMillis } ?: return null
        return if (nowMillis < day.sehriEndsMillis) {
            LiveStatus(Phase.SEHRI, day, day.sehriEndsMillis, 0f, upcomingNight = day.roza)
        } else {
            val progress = (nowMillis - day.sehriEndsMillis).toFloat() / day.fastDurationMillis.coerceAtLeast(1)
            LiveStatus(Phase.FASTING, day, day.iftarMillis, progress.coerceIn(0f, 1f), upcomingNight = day.roza + 1)
        }
    }

    fun build(
        hijri: HijriCalendarHelper.HijriDate,
        city: CityLocation,
        config: PrayerTimeConfig,
        now: Calendar = Calendar.getInstance(TimeZone.getTimeZone(city.timeZoneId)),
        locale: Locale = Locale.getDefault()
    ): RamadanSchedule? {
        if (!isRamadan(hijri)) return null
        val firstFastDate = startOfDay(now).apply {
            add(Calendar.DAY_OF_MONTH, maghribRollover(now, city, config) - (hijri.day - 1))
        }
        return RamadanSchedule(hijri.year, hijri.day, buildDays(firstFastDate, city, config, locale))
    }

    fun buildUpcoming(
        hijri: HijriCalendarHelper.HijriDate,
        city: CityLocation,
        config: PrayerTimeConfig,
        now: Calendar = Calendar.getInstance(TimeZone.getTimeZone(city.timeZoneId)),
        locale: Locale = Locale.getDefault()
    ): UpcomingRamadan? {
        if (isRamadan(hijri)) return null
        val targetYear = if (hijri.month < RAMADAN_MONTH) hijri.year else hijri.year + 1
        val daysToStart = HijriCalendarHelper.daysUntil(
            hijri, targetYear, RAMADAN_MONTH, 1, HijriRegion.forTimeZone(city.timeZoneId)
        )
        val firstFastDate = startOfDay(now).apply {
            add(Calendar.DAY_OF_MONTH, maghribRollover(now, city, config) + daysToStart)
        }
        val eve = (firstFastDate.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -1) }
        val eveTimes = AstronomicalPrayerCalculator.calculateTimes(eve, city.latitude, city.longitude, config)
        return UpcomingRamadan(
            hijriYear = targetYear,
            startsAtMillis = AstronomicalPrayerCalculator.formatHoursToTimeInMillis(eve, eveTimes.maghribHours),
            days = buildDays(firstFastDate, city, config, locale)
        )
    }

    /** The Hijri day already advances at Maghrib, so after Maghrib today's hijri day belongs to tomorrow. */
    private fun maghribRollover(now: Calendar, city: CityLocation, config: PrayerTimeConfig): Int {
        val todayTimes = AstronomicalPrayerCalculator.calculateTimes(now, city.latitude, city.longitude, config)
        val currentHour = now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0
        return if (currentHour >= todayTimes.maghribHours) 1 else 0
    }

    private fun buildDays(
        firstFastDate: Calendar,
        city: CityLocation,
        config: PrayerTimeConfig,
        locale: Locale
    ): List<RamadanDay> =
        (1..30).map { roza ->
            val date = (firstFastDate.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, roza - 1) }
            val times = AstronomicalPrayerCalculator.calculateTimes(date, city.latitude, city.longitude, config)
            RamadanDay(
                roza = roza,
                dateMillis = date.timeInMillis,
                sehriEndsMillis = AstronomicalPrayerCalculator.formatHoursToTimeInMillis(date, times.sehriEndHours),
                iftarMillis = AstronomicalPrayerCalculator.formatHoursToTimeInMillis(date, times.maghribHours),
                sehriFormatted = AstronomicalPrayerCalculator.formatHoursToAmPm(times.sehriEndHours, locale),
                iftarFormatted = AstronomicalPrayerCalculator.formatHoursToAmPm(times.maghribHours, locale),
                dependsOnMoonSighting = roza == 30
            )
        }

    private fun startOfDay(calendar: Calendar): Calendar = (calendar.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 12) // noon avoids DST edge cases when shifting by days
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}
