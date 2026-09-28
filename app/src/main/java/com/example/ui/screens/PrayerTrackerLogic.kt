package com.example.ui.screens

import android.content.SharedPreferences
import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.max

internal val TRACKED_PRAYERS = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
internal val QAZA_PRAYER_NAMES = TRACKED_PRAYERS + "Witr"

/**
 * How a prayer was offered. [stored] values stay compatible with older logs, where
 * "Prayed" and "Missed" were the only two states.
 */
internal enum class PrayerMark(val stored: String, val label: String) {
    JAMAAT("Jamaat", "Jama'at"),
    ON_TIME("Prayed", "On time"),
    LATE("Late", "Late"),
    MISSED("Missed", "Missed");

    val prayed: Boolean get() = this != MISSED

    companion object {
        fun from(stored: String?): PrayerMark? = entries.firstOrNull { it.stored == stored }
    }
}

internal data class TrackerDay(
    val date: LocalDate,
    val marks: Map<String, PrayerMark?>,
    val excused: Boolean
) {
    val prayed: Int get() = marks.values.count { it?.prayed == true }
    val missed: Int get() = marks.values.count { it == PrayerMark.MISSED }
    val logged: Int get() = marks.values.count { it != null }
    val complete: Boolean get() = prayed == TRACKED_PRAYERS.size
}

internal data class PrayerStats(
    val trackedDays: Int,
    val prayed: Int,
    val jamaat: Int,
    val missed: Int,
    val completeDays: Int,
    /** Share of days each prayer was prayed, in [TRACKED_PRAYERS] order. */
    val perPrayer: Map<String, Float>
) {
    val completion: Float get() = if (trackedDays == 0) 0f else prayed.toFloat() / (trackedDays * TRACKED_PRAYERS.size)
    /** The prayer that most needs attention, once there is enough history to say. */
    val weakest: String? get() = perPrayer.takeIf { trackedDays >= 3 }?.minByOrNull { it.value }?.takeIf { it.value < 1f }?.key
}

/**
 * Streak of complete days ending today (or yesterday while today is still in progress).
 * Excused days neither break nor extend a streak.
 */
internal fun currentStreak(today: LocalDate, dayOf: (LocalDate) -> TrackerDay, limit: Int = 1000): Int {
    var date = if (dayOf(today).complete) today else today.minusDays(1)
    var streak = 0
    repeat(limit) {
        val day = dayOf(date)
        when {
            day.excused -> Unit
            day.complete -> streak++
            else -> return streak
        }
        date = date.minusDays(1)
    }
    return streak
}

internal fun bestStreak(today: LocalDate, dayOf: (LocalDate) -> TrackerDay, days: Int = 365): Int {
    var best = 0
    var run = 0
    for (offset in days - 1 downTo 0) {
        val day = dayOf(today.minusDays(offset.toLong()))
        when {
            day.excused -> Unit
            day.complete -> { run++; best = max(best, run) }
            else -> run = 0
        }
    }
    return best
}

/** Stats over the last [days] days. Excused days and days with nothing logged are left out. */
internal fun prayerStats(today: LocalDate, dayOf: (LocalDate) -> TrackerDay, days: Int = 30): PrayerStats {
    val tracked = (0 until days).map { dayOf(today.minusDays(it.toLong())) }.filter { !it.excused && it.logged > 0 }
    return PrayerStats(
        trackedDays = tracked.size,
        prayed = tracked.sumOf { it.prayed },
        jamaat = tracked.sumOf { d -> d.marks.values.count { it == PrayerMark.JAMAAT } },
        missed = tracked.sumOf { it.missed },
        completeDays = tracked.count { it.complete },
        perPrayer = TRACKED_PRAYERS.associateWith { prayer ->
            if (tracked.isEmpty()) 0f else tracked.count { it.marks[prayer]?.prayed == true }.toFloat() / tracked.size
        }
    )
}

/** Days needed to clear the balance when [setsPerDay] of each prayer are made up daily. */
internal fun qazaDaysToFinish(balance: Map<String, Int>, setsPerDay: Int): Int =
    ceil((balance.values.maxOrNull() ?: 0) / setsPerDay.coerceAtLeast(1).toDouble()).toInt()

/** SharedPreferences-backed tracker. Keys match the earlier Daily tools screen, so old logs carry over. */
internal class PrayerTrackerStore(private val prefs: SharedPreferences) {

    private fun statusKey(date: LocalDate, prayer: String) = "prayer_${date}_$prayer"
    private fun autoQazaKey(date: LocalDate, prayer: String) = "qaza_auto_${date}_$prayer"

    var autoQaza: Boolean
        get() = prefs.getBoolean("qaza_auto_link", true)
        set(value) = prefs.edit().putBoolean("qaza_auto_link", value).apply()

    var qazaSetsPerDay: Int
        get() = prefs.getInt("qaza_daily_sets", 1)
        set(value) = prefs.edit().putInt("qaza_daily_sets", value.coerceIn(1, 20)).apply()

    val qazaMadeUp: Int get() = prefs.getInt("qaza_made_up_total", 0)

    fun isExcused(date: LocalDate): Boolean =
        prefs.getBoolean("muslimah_mode", false) && prefs.getBoolean("cycle_$date", false)

    fun day(date: LocalDate) = TrackerDay(
        date = date,
        marks = TRACKED_PRAYERS.associateWith { PrayerMark.from(prefs.getString(statusKey(date, it), null)) },
        excused = isExcused(date)
    )

    /**
     * Logs one prayer. With [autoQaza] on, marking it missed adds it to the Qaza balance and
     * changing that mark later takes it back out, so the balance never counts a day twice.
     */
    fun setMark(date: LocalDate, prayer: String, mark: PrayerMark?) {
        val editor = prefs.edit()
        if (mark == null) editor.remove(statusKey(date, prayer)) else editor.putString(statusKey(date, prayer), mark.stored)
        val autoAdded = prefs.getBoolean(autoQazaKey(date, prayer), false)
        if (mark == PrayerMark.MISSED && !autoAdded && autoQaza) {
            editor.putInt(qazaKey(prayer), qazaCount(prayer) + 1).putBoolean(autoQazaKey(date, prayer), true)
        } else if (mark != PrayerMark.MISSED && autoAdded) {
            editor.putInt(qazaKey(prayer), max(0, qazaCount(prayer) - 1)).remove(autoQazaKey(date, prayer))
        }
        editor.apply()
    }

    /** Marks every prayer in [prayers] that has no log yet as prayed on time; returns the ones changed. */
    fun markRemainingPrayed(date: LocalDate, prayers: List<String>): List<String> {
        val current = day(date).marks
        return prayers.filter { current[it] == null }.onEach { setMark(date, it, PrayerMark.ON_TIME) }
    }

    private fun qazaKey(prayer: String) = "qaza_$prayer"
    fun qazaCount(prayer: String): Int = prefs.getInt(qazaKey(prayer), 0)
    fun qazaBalance(): Map<String, Int> = QAZA_PRAYER_NAMES.associateWith(::qazaCount)

    fun addQaza(prayer: String, amount: Int = 1) {
        prefs.edit().putInt(qazaKey(prayer), max(0, qazaCount(prayer) + amount)).apply()
    }

    /** Adds a whole number of missed days: one of each fard prayer plus Witr per day. */
    fun addQazaDays(days: Int) {
        if (days <= 0) return
        val editor = prefs.edit()
        QAZA_PRAYER_NAMES.forEach { editor.putInt(qazaKey(it), qazaCount(it) + days) }
        editor.apply()
    }

    /** Records one made-up prayer: lowers its balance and counts it towards overall progress. */
    fun makeUpQaza(prayer: String) {
        val count = qazaCount(prayer)
        if (count <= 0) return
        prefs.edit()
            .putInt(qazaKey(prayer), count - 1)
            .putInt("qaza_made_up_total", qazaMadeUp + 1)
            .apply()
    }

    fun clearQaza() {
        val editor = prefs.edit()
        QAZA_PRAYER_NAMES.forEach { editor.putInt(qazaKey(it), 0) }
        editor.putInt("qaza_made_up_total", 0).apply()
    }
}
