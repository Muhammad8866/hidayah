package com.example.ui.screens

import com.example.prayer.HijriCalendarHelper
import com.example.prayer.HijriRegion
import java.time.LocalDate
import java.util.Calendar
import java.util.TimeZone

internal data class RozaStats(
    val total: Int, val ramadan: Int, val nafl: Int, val qazaDone: Int, val missed: Int,
    val currentStreak: Int, val longestStreak: Int
)

internal data class RozaRamadanProgress(
    val hijriYear: Int, val start: LocalDate, val daysElapsed: Int,
    val fasted: Int, val missed: Int, val unlogged: Int, val ongoing: Boolean
)

/** Advisory only: local moon sighting can differ from the app's calendar, so these never block saving. */
internal enum class RozaWarning(val message: String) {
    FORBIDDEN_DAY("Fasting is not permitted on Eid and the days of Tashreeq"),
    RAMADAN_OUTSIDE_RAMADAN("This date is outside Ramadan in your Hijri calendar"),
    OTHER_FAST_IN_RAMADAN("This date is in Ramadan in your Hijri calendar")
}

internal object RozaLogic {
    private const val RAMADAN = 9
    private const val SHAWWAL = 10
    private const val DHU_AL_HIJJAH = 12

    private val region: HijriRegion get() = HijriRegion.forTimeZone(TimeZone.getDefault().id)

    /** Hijri date of the civil day: a fast belongs to the daytime, so there is no Maghrib offset. */
    fun hijri(date: LocalDate): HijriCalendarHelper.HijriDate = HijriCalendarHelper.getHijriDate(
        Calendar.getInstance().apply { clear(); set(date.year, date.monthValue - 1, date.dayOfMonth, 12, 0, 0) }
    )

    fun isRamadan(date: LocalDate): Boolean = hijri(date).month == RAMADAN

    fun isForbidden(date: LocalDate): Boolean = hijri(date).let {
        (it.month == SHAWWAL && it.day == 1) || (it.month == DHU_AL_HIJJAH && it.day in 10..13)
    }

    fun suggestedMark(date: LocalDate): RozaMark? = if (isRamadan(date)) RozaMark.RAMADAN else null

    fun warning(date: LocalDate, mark: RozaMark): RozaWarning? {
        if (mark == RozaMark.MISSED) return null
        val ramadan = isRamadan(date)
        return when {
            isForbidden(date) -> RozaWarning.FORBIDDEN_DAY
            mark == RozaMark.RAMADAN && !ramadan -> RozaWarning.RAMADAN_OUTSIDE_RAMADAN
            mark != RozaMark.RAMADAN && ramadan -> RozaWarning.OTHER_FAST_IN_RAMADAN
            else -> null
        }
    }

    fun stats(logs: Map<LocalDate, RozaMark>, today: LocalDate): RozaStats {
        val past = logs.filterKeys { it <= today }
        val fasted = past.filterValues { it != RozaMark.MISSED }.keys.sorted()
        var longest = 0
        var run = 0
        var previous: LocalDate? = null
        for (date in fasted) {
            run = if (previous?.plusDays(1) == date) run + 1 else 1
            if (run > longest) longest = run
            previous = date
        }
        // An unlogged today does not break a streak that reached yesterday.
        var day = if (today in past) today else today.minusDays(1)
        var current = 0
        while (past[day].let { it != null && it != RozaMark.MISSED }) { current++; day = day.minusDays(1) }
        return RozaStats(
            total = fasted.size,
            ramadan = past.values.count { it == RozaMark.RAMADAN },
            nafl = past.values.count { it == RozaMark.NAFL },
            qazaDone = past.values.count { it == RozaMark.QAZA },
            missed = past.values.count { it == RozaMark.MISSED },
            currentStreak = current, longestStreak = longest
        )
    }

    fun ramadanProgress(logs: Map<LocalDate, RozaMark>, today: LocalDate): RozaRamadanProgress? {
        val now = hijri(today)
        if (now.month == RAMADAN) return progress(now.year, today.minusDays(now.day - 1L), today, logs, ongoing = true)
        val earliest = logs.keys.minOrNull() ?: return null
        val earliestYear = hijri(earliest).year
        var year = if (now.month > RAMADAN) now.year else now.year - 1
        while (year >= earliestYear) {
            val start = gregorian(year, RAMADAN, 1)
            val last = gregorian(year, SHAWWAL, 1).minusDays(1)
            if (logs.keys.any { it in start..last }) return progress(year, start, last, logs, ongoing = false)
            year--
        }
        return null
    }

    private fun progress(year: Int, start: LocalDate, last: LocalDate, logs: Map<LocalDate, RozaMark>, ongoing: Boolean): RozaRamadanProgress {
        val marks = logs.filterKeys { it in start..last }.values
        val elapsed = (last.toEpochDay() - start.toEpochDay() + 1).toInt()
        val missed = marks.count { it == RozaMark.MISSED }
        return RozaRamadanProgress(year, start, elapsed, marks.size - missed, missed, elapsed - marks.size, ongoing)
    }

    private fun gregorian(year: Int, month: Int, day: Int): LocalDate =
        HijriCalendarHelper.hijriToGregorian(year, month, day, region).let { (y, m, d) -> LocalDate.of(y, m, d) }
}
