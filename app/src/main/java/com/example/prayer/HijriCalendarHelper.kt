package com.example.prayer

import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

/**
 * The single source of Hijri dates in the app: home screen, notifications, Ramadan and festivals
 * all go through here, so they always agree. Works fully offline.
 *
 * Month starts come from [HijriRegion]: the Umm al-Qura calendar, or predicted local moon sighting
 * for countries whose committees start months only after the crescent is seen.
 */
object HijriCalendarHelper {
    data class HijriDate(val day: Int, val month: Int, val year: Int, val monthNameArabic: String, val monthNameEnglish: String)

    private val MONTH_NAMES_EN = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Ula", "Jumada al-Akhirah",
        "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )
    private val MONTH_NAMES_AR = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الثاني", "جمادى الأولى", "جمادى الآخرة",
        "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    /**
     * Hijri date of the calendar's civil date moved by [dayOffset] days. The Hijri day begins at
     * Maghrib, so callers pass 1 after Maghrib. The region defaults to the calendar's time zone.
     */
    fun getHijriDate(
        calendar: Calendar,
        dayOffset: Int = 0,
        region: HijriRegion = HijriRegion.forTimeZone(calendar.timeZone.id)
    ): HijriDate = fromJulianDay(julianDayNumber(calendar) + dayOffset, region)

    /**
     * Days by which the date shown for the calendar's day differs from the astronomical prediction
     * because of month starts the user confirmed (see [adjustDate]); 0 once the committee's
     * calendar is back in step with the prediction.
     */
    fun adjustmentDays(
        calendar: Calendar,
        dayOffset: Int = 0,
        region: HijriRegion = HijriRegion.forTimeZone(calendar.timeZone.id)
    ): Int {
        val jdn = julianDayNumber(calendar) + dayOffset
        val starts = HijriMonthStarts.of(region)
        val shown = fromJulianDay(jdn, starts)
        return (toJulianDay(shown.year, shown.month, shown.day, predictionAround(starts, jdn)) - jdn).toInt()
    }

    /**
     * Makes the date shown for the calendar's day [days] days away from the prediction, the user's
     * correction after a local moon-sighting announcement. The start of the month that day falls in
     * is confirmed; later months follow the 29th-evening rule from there, so the correction carries
     * forward only while the committee's calendar is actually shifted.
     */
    fun adjustDate(
        calendar: Calendar,
        dayOffset: Int,
        days: Int,
        region: HijriRegion = HijriRegion.forTimeZone(calendar.timeZone.id)
    ) {
        val jdn = julianDayNumber(calendar) + dayOffset
        val prediction = predictionAround(HijriMonthStarts.of(region), jdn)
        val currentIndex = monthIndexContaining(jdn, prediction)
        // Older confirmations no longer influence today's dates once the 29th-evening rule has resynced.
        val confirmed = prediction.confirmedStarts.filterKeys { it >= currentIndex - 24 }.toMutableMap()
        if (days != 0) {
            val target = fromJulianDay(jdn + days, prediction)
            val targetIndex = UmmAlQuraCalendar.monthIndex(target.year, target.month)
            if (targetIndex != null) {
                confirmed[targetIndex] = jdn - target.day + 1
                // A day 30 the committee reached by not sighting the crescent twice: keep the next month after it.
                if (HijriMonthStarts(region, confirmed).start(targetIndex + 1) <= jdn) confirmed[targetIndex + 1] = jdn + 1
            }
        }
        HijriMonthStarts.confirm(region, confirmed)
    }

    /** The confirmed month starts of [region] as text for storage, restored by [restoreConfirmedMonths]. */
    fun confirmedMonths(region: HijriRegion): String =
        HijriMonthStarts.of(region).confirmedStarts.entries.joinToString(",") { "${it.key}:${it.value}" }

    fun restoreConfirmedMonths(region: HijriRegion, saved: String) {
        val starts = saved.split(',').mapNotNull { entry ->
            val (index, start) = entry.split(':').takeIf { it.size == 2 } ?: return@mapNotNull null
            (index.toIntOrNull() ?: return@mapNotNull null) to (start.toLongOrNull() ?: return@mapNotNull null)
        }.toMap()
        HijriMonthStarts.confirm(region, starts)
    }

    fun create(day: Int, month: Int, year: Int): HijriDate {
        val safeMonth = month.coerceIn(1, 12)
        return HijriDate(day.coerceIn(1, 30), safeMonth, year, MONTH_NAMES_AR[safeMonth - 1], MONTH_NAMES_EN[safeMonth - 1])
    }

    fun format(date: HijriDate): String = "${date.day} ${date.monthNameEnglish} ${date.year} AH"

    /** Days from [from] to the given Hijri date (negative if it lies in the past). */
    fun daysUntil(from: HijriDate, year: Int, month: Int, day: Int, region: HijriRegion): Int =
        (toJulianDay(year, month, day, region) - toJulianDay(from.year, from.month, from.day, region)).toInt()

    /** Gregorian date of a Hijri date, returned as (year, month 1-12, day). */
    fun hijriToGregorian(year: Int, month: Int, day: Int, region: HijriRegion): Triple<Int, Int, Int> =
        julianDayToGregorian(toJulianDay(year, month, day, region))

    internal fun fromJulianDay(jdn: Long, region: HijriRegion): HijriDate = fromJulianDay(jdn, HijriMonthStarts.of(region))

    internal fun toJulianDay(year: Int, month: Int, day: Int, region: HijriRegion): Long =
        toJulianDay(year, month, day, HijriMonthStarts.of(region))

    private fun fromJulianDay(jdn: Long, starts: HijriMonthStarts): HijriDate {
        if (UmmAlQuraCalendar.monthIndexOf(jdn) == null) return tabularFromJulianDay(jdn)
        val index = monthIndexContaining(jdn, starts)
        val day = (jdn - starts.start(index) + 1).toInt()
        return create(day, UmmAlQuraCalendar.monthOf(index), UmmAlQuraCalendar.yearOf(index))
    }

    private fun toJulianDay(year: Int, month: Int, day: Int, starts: HijriMonthStarts): Long {
        val index = UmmAlQuraCalendar.monthIndex(year, month) ?: return tabularToJulianDay(year, month, day)
        return starts.start(index) + day - 1
    }

    private fun monthIndexContaining(jdn: Long, starts: HijriMonthStarts): Int {
        // A locally sighted or confirmed month can start a day or two before Umm al-Qura, or a few days after it.
        var index = min((UmmAlQuraCalendar.monthIndexOf(jdn) ?: 0) + 2, UmmAlQuraCalendar.MONTH_COUNT - 1)
        while (index > 0 && starts.start(index) > jdn) index--
        return index
    }

    /** [starts] without the user's confirmations for the months around [jdn]: the plain prediction there. */
    private fun predictionAround(starts: HijriMonthStarts, jdn: Long): HijriMonthStarts {
        val index = UmmAlQuraCalendar.monthIndexOf(jdn) ?: return starts
        val nearby = (index - 2)..(index + 2)
        if (starts.confirmedStarts.keys.none { it in nearby }) return starts
        return HijriMonthStarts(starts.region, starts.confirmedStarts.filterKeys { it !in nearby })
    }

    internal fun julianDayNumber(calendar: Calendar): Long =
        gregorianToJulianDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))

    internal fun gregorianToJulianDay(year: Int, month: Int, day: Int): Long {
        val a = (14 - month) / 12
        val y = year.toLong() + 4800 - a
        val m = month + 12 * a - 3
        return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    }

    /** Fliegel–Van Flandern conversion, returned as (year, month 1-12, day). */
    internal fun julianDayToGregorian(jdn: Long): Triple<Int, Int, Int> {
        var l = jdn + 68569
        val n = 4 * l / 146097
        l -= (146097 * n + 3) / 4
        val i = 4000 * (l + 1) / 1461001
        l = l - 1461 * i / 4 + 31
        val j = 80 * l / 2447
        val d = l - 2447 * j / 80
        l = j / 11
        val m = j + 2 - 12 * l
        val y = 100 * (n - 49) + i + l
        return Triple(y.toInt(), m.toInt(), d.toInt())
    }

    // Arithmetic (tabular) Hijri calendar, only used outside the Umm al-Qura table (before 1882 or after 2174).

    private fun tabularFromJulianDay(jdn: Long): HijriDate {
        val year = floor((30.0 * (jdn - TABULAR_EPOCH) + 10646.0) / 10631.0).toInt()
        val month = min(12, ceil((jdn - 29.0 - tabularToJulianDay(year, 1, 1)) / 29.5).toInt() + 1).coerceAtLeast(1)
        val day = (jdn - tabularToJulianDay(year, month, 1) + 1).toInt()
        return create(day, month, year)
    }

    private fun tabularToJulianDay(year: Int, month: Int, day: Int): Long =
        day + ceil(29.5 * (month - 1)).toLong() + (year - 1) * 354L + floor((3 + 11 * year) / 30.0).toLong() + TABULAR_EPOCH - 1

    private const val TABULAR_EPOCH = 1948440L
}
