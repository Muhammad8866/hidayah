package com.example.ui.screens

import android.content.SharedPreferences
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/** One run of consecutive excused days. [ongoing] periods have no logged end yet and run up to today. */
internal data class MuslimahPeriod(val start: LocalDate, val end: LocalDate, val ongoing: Boolean) {
    val length: Int get() = (ChronoUnit.DAYS.between(start, end) + 1).toInt()
    operator fun contains(date: LocalDate) = date in start..end
}

internal data class MuslimahStats(
    val averagePeriodLength: Int?,
    val averageCycleLength: Int?,
    val cyclesUsed: Int,
    val nextEstimate: LocalDate?
)

/**
 * Private, on-device cycle log. Storage stays compatible with the original screen: each excused day
 * is a `cycle_<ISO date>` boolean in the daily-tools prefs, and the prayer tracker reads the same
 * keys. An ongoing period is kept as its start date only, so it keeps covering each new day
 * (including in the prayer tracker) without the screen having to be opened.
 *
 * Dates are civil [LocalDate]s on the phone, never instants, so changing time zone never moves a
 * logged day.
 */
internal class MuslimahStore(private val prefs: SharedPreferences) {

    var enabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    /** Start of the period that has no end logged yet; ignored if it is in the future. */
    fun ongoingStart(today: LocalDate = LocalDate.now()): LocalDate? =
        prefs.getString(KEY_ONGOING, null)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?.takeIf { !it.isAfter(today) }

    private fun storedDays(): Set<LocalDate> = prefs.all.mapNotNullTo(HashSet()) { (key, value) ->
        if (!key.startsWith(DAY_PREFIX) || value != true) null
        else runCatching { LocalDate.parse(key.removePrefix(DAY_PREFIX)) }.getOrNull()
    }

    /** Every excused day up to [today], whether or not the mode is switched on. */
    fun excusedDays(today: LocalDate = LocalDate.now()): Set<LocalDate> {
        val days = storedDays().filterTo(HashSet()) { !it.isAfter(today) }
        ongoingStart(today)?.let { start -> days += daysBetween(start, today) }
        return days
    }

    fun isExcused(date: LocalDate, today: LocalDate = LocalDate.now()): Boolean {
        if (!enabled || date.isAfter(today)) return false
        if (prefs.getBoolean(DAY_PREFIX + date, false)) return true
        val start = ongoingStart(today) ?: return false
        return !date.isBefore(start)
    }

    /** Excused days grouped into periods, newest first. Overlapping or touching logs merge into one. */
    fun periods(today: LocalDate = LocalDate.now()): List<MuslimahPeriod> {
        val ongoing = ongoingStart(today)
        val sorted = excusedDays(today).sorted()
        val result = mutableListOf<MuslimahPeriod>()
        var runStart: LocalDate? = null
        var previous: LocalDate? = null
        fun close() {
            val s = runStart ?: return
            val e = previous ?: return
            result += MuslimahPeriod(s, e, ongoing != null && e == today && !ongoing.isBefore(s))
        }
        for (day in sorted) {
            if (previous == null || day != previous.plusDays(1)) {
                close()
                runStart = day
            }
            previous = day
        }
        close()
        return result.reversed()
    }

    /** One tap: a period begins on [date] and stays open until it is ended. */
    fun startPeriod(date: LocalDate, today: LocalDate = LocalDate.now()): Boolean {
        if (date.isAfter(today)) return false
        prefs.edit().putString(KEY_ONGOING, date.toString()).apply()
        return true
    }

    /** Closes the open period on [end] (inclusive). Rejected before the start or in the future. */
    fun endPeriod(end: LocalDate, today: LocalDate = LocalDate.now()): Boolean {
        val start = ongoingStart(today) ?: return false
        if (end.isBefore(start) || end.isAfter(today)) return false
        prefs.edit().apply {
            daysBetween(start, end).forEach { putBoolean(DAY_PREFIX + it, true) }
            remove(KEY_ONGOING)
        }.apply()
        return true
    }

    /** Replaces [old] with [start]..[end]; a null [end] keeps it open (only allowed for the latest period). */
    fun editPeriod(old: MuslimahPeriod, start: LocalDate, end: LocalDate?, today: LocalDate = LocalDate.now()): Boolean {
        if (start.isAfter(today)) return false
        if (end != null && (end.isBefore(start) || end.isAfter(today))) return false
        prefs.edit().apply {
            daysBetween(old.start, old.end).forEach { remove(DAY_PREFIX + it) }
            if (old.ongoing) remove(KEY_ONGOING)
            if (end == null) putString(KEY_ONGOING, start.toString())
            else daysBetween(start, end).forEach { putBoolean(DAY_PREFIX + it, true) }
        }.apply()
        return true
    }

    fun deletePeriod(period: MuslimahPeriod) {
        prefs.edit().apply {
            daysBetween(period.start, period.end).forEach { remove(DAY_PREFIX + it) }
            if (period.ongoing) remove(KEY_ONGOING)
        }.apply()
    }

    /** Calendar tap on a single past day. Days inside the open period are changed through that period. */
    fun toggleDay(date: LocalDate, today: LocalDate = LocalDate.now()): Boolean {
        if (date.isAfter(today)) return false
        val start = ongoingStart(today)
        if (start != null && !date.isBefore(start)) return false
        val key = DAY_PREFIX + date
        prefs.edit().apply { if (prefs.getBoolean(key, false)) remove(key) else putBoolean(key, true) }.apply()
        return true
    }

    /** Removes every cycle log. The Roza tracker's balance is left alone. */
    fun deleteAll() {
        prefs.edit().apply {
            prefs.all.keys.filter { it.startsWith(DAY_PREFIX) }.forEach { remove(it) }
            remove(KEY_ONGOING)
        }.apply()
    }

    fun stats(today: LocalDate = LocalDate.now()): MuslimahStats = muslimahStats(periods(today))

    companion object {
        const val KEY_ENABLED = "muslimah_mode"
        const val KEY_ONGOING = "muslimah_ongoing_start"
        const val DAY_PREFIX = "cycle_"
        const val MAX_PERIOD_DAYS = 60L
    }
}

private fun daysBetween(start: LocalDate, end: LocalDate): List<LocalDate> {
    if (end.isBefore(start)) return emptyList()
    // A guard against a corrupt or mistyped start date flooding the prefs file.
    val count = ChronoUnit.DAYS.between(start, end).coerceAtMost(366L * 2)
    return (0..count).map { start.plusDays(it) }
}

/**
 * Gentle averages over the most recent logs. Cycle lengths are start-to-start and only gaps of
 * 15–60 days count, so a forgotten month or a split entry doesn't skew the estimate.
 */
internal fun muslimahStats(periodsNewestFirst: List<MuslimahPeriod>): MuslimahStats {
    val completed = periodsNewestFirst.filter { !it.ongoing }.take(6)
    val avgPeriod = completed.takeIf { it.isNotEmpty() }?.map { it.length }?.average()?.roundToInt()
    val starts = periodsNewestFirst.map { it.start }
    val cycles = starts.zipWithNext { newer, older -> ChronoUnit.DAYS.between(older, newer).toInt() }
        .filter { it in 15..60 }
        .take(6)
    val avgCycle = cycles.takeIf { it.isNotEmpty() }?.average()?.roundToInt()
    val next = if (avgCycle != null) starts.firstOrNull()?.plusDays(avgCycle.toLong()) else null
    return MuslimahStats(avgPeriod, avgCycle, cycles.size, next)
}
