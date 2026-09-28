package com.example.ui.screens

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

internal const val MADANI_PAGES = 604
internal const val PARA_PAGES = 20

/** First page of each para in the 604-page Madani mushaf. */
internal val MADANI_PARA_STARTS = intArrayOf(
    1, 22, 42, 62, 82, 102, 121, 142, 162, 182, 201, 222, 242, 262, 282,
    302, 322, 342, 362, 382, 402, 422, 442, 462, 482, 502, 522, 542, 562, 582
)

/** Para (1..30) that holds Madani [page] (1..604). */
internal fun madaniParaOf(page: Int): Int =
    MADANI_PARA_STARTS.indexOfLast { it <= page.coerceIn(1, MADANI_PAGES) } + 1

/**
 * Converts a page of a printed mushaf, described by its para start pages, to the matching Madani
 * page, interpolating inside the para. Pages before para 1 map to 1 and pages after the Quran to 604.
 */
internal fun toMadaniPage(page: Int, paraPages: IntArray, pageCount: Int): Int {
    if (paraPages.size != 30 || page < paraPages[0]) return 1
    val para = paraPages.indexOfLast { it <= page }
    val start = paraPages[para]
    val end = if (para == 29) pageCount + 1 else paraPages[para + 1]
    val madaniStart = MADANI_PARA_STARTS[para]
    val madaniEnd = if (para == 29) MADANI_PAGES + 1 else MADANI_PARA_STARTS[para + 1]
    val fraction = (page - start).toDouble() / (end - start).coerceAtLeast(1)
    return (madaniStart + floor(fraction * (madaniEnd - madaniStart)).toInt()).coerceIn(1, MADANI_PAGES)
}

/** Madani pages that one page of a mushaf with these para starts is worth (1 for a Madani-sized book). */
internal fun madaniPagesPerPage(paraPages: IntArray): Float {
    if (paraPages.size != 30) return 1f
    val span = (paraPages[29] - paraPages[0]).coerceAtLeast(1)
    return (MADANI_PARA_STARTS[29] - MADANI_PARA_STARTS[0]).toFloat() / span
}

/** The store over the Daily tools preferences, which the Quran reader also writes to. */
internal fun quranGoalStore(context: Context) =
    QuranGoalStore(context.getSharedPreferences("daily_tools", Context.MODE_PRIVATE))

internal enum class QuranGoalMode(val stored: String) {
    PAGES("pages"),
    FINISH_BY("date");

    companion object {
        fun from(stored: String?) = entries.firstOrNull { it.stored == stored } ?: PAGES
    }
}

internal data class QuranReadingStats(
    val weekPages: Int,
    val monthPages: Int,
    val activeDays: Int,
    /** Pages per calendar day over the last 14 days, used for the finish forecast. */
    val recentPace: Double
)

/**
 * SharedPreferences-backed Quran reading goal. The daily log ("quran_<date>"), goal ("quran_goal")
 * and running page total ("quran_khatm") keep the keys of the earlier screen, so old logs carry over.
 * The running total counts every page ever logged: whole multiples of 604 are completed khatms.
 */
internal class QuranGoalStore(private val prefs: SharedPreferences) {

    var dailyGoal: Int
        get() = prefs.getInt("quran_goal", 2)
        set(value) = prefs.edit().putInt("quran_goal", value.coerceIn(1, MADANI_PAGES)).apply()

    var mode: QuranGoalMode
        get() = QuranGoalMode.from(prefs.getString("quran_goal_mode", null))
        set(value) = prefs.edit().putString("quran_goal_mode", value.stored).apply()

    var finishBy: LocalDate?
        get() = prefs.getString("quran_goal_date", null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        set(value) = prefs.edit().apply { if (value == null) remove("quran_goal_date") else putString("quran_goal_date", value.toString()) }.apply()

    var readerAutoLog: Boolean
        get() = prefs.getBoolean("quran_reader_auto", true)
        set(value) = prefs.edit().putBoolean("quran_reader_auto", value).apply()

    fun pagesOn(date: LocalDate): Int = prefs.getInt("quran_$date", 0)
    fun readerPagesOn(date: LocalDate): Int = prefs.getInt("quran_reader_$date", 0)

    val totalPages: Int get() = prefs.getInt("quran_khatm", 0)
    val completedKhatms: Int get() = totalPages / MADANI_PAGES
    /** Pages read in the current khatm, 0 until 604. */
    val position: Int get() = totalPages % MADANI_PAGES
    val remaining: Int get() = MADANI_PAGES - position

    /** Dates on which a khatm was completed here, oldest first. Earlier khatms have no date. */
    val khatmDates: List<LocalDate>
        get() = prefs.getString("quran_khatm_dates", "").orEmpty().split(',')
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }

    private fun saveKhatmDates(editor: SharedPreferences.Editor, dates: List<LocalDate>) {
        editor.putString("quran_khatm_dates", dates.joinToString(","))
    }

    /** The target for [date]: fixed pages, or what is still needed each day to finish by [finishBy]. */
    fun targetOn(date: LocalDate, today: LocalDate): Int {
        if (date != today) return prefs.getInt("quran_goal_$date", dailyGoal)
        val deadline = finishBy
        if (mode != QuranGoalMode.FINISH_BY || deadline == null) return dailyGoal
        // Today's pages are added back so the target does not shrink while reading today.
        val left = remaining + pagesOn(today)
        val days = ChronoUnit.DAYS.between(today, deadline).toInt() + 1
        return if (days <= 0) left.coerceAtLeast(1) else ceil(left / days.toDouble()).toInt().coerceIn(1, MADANI_PAGES)
    }

    fun daysLeft(today: LocalDate): Int? =
        finishBy?.takeIf { mode == QuranGoalMode.FINISH_BY }?.let { ChronoUnit.DAYS.between(today, it).toInt() + 1 }

    /**
     * Adds [delta] pages (negative to take back) to [date]'s log and the khatm total.
     * Returns true when this completed a khatm.
     */
    fun log(date: LocalDate, delta: Int, today: LocalDate = LocalDate.now()): Boolean {
        val before = pagesOn(date)
        val after = max(0, before + delta).coerceAtMost(MADANI_PAGES * 10)
        val change = after - before
        if (change == 0) return false
        val oldTotal = totalPages
        val newTotal = max(0, oldTotal + change)
        val editor = prefs.edit()
            .putInt("quran_$date", after)
            .putInt("quran_khatm", newTotal)
            .putInt("quran_goal_$date", targetOn(date, today))
        val dates = khatmDates.toMutableList()
        val finished = newTotal / MADANI_PAGES > oldTotal / MADANI_PAGES
        if (finished) repeat(newTotal / MADANI_PAGES - oldTotal / MADANI_PAGES) { dates += date }
        while (dates.size > newTotal / MADANI_PAGES) dates.removeAt(dates.lastIndex)
        saveKhatmDates(editor, dates)
        editor.apply()
        return finished
    }

    /** Moves the current khatm to [pagesDone] (0..603) without changing the daily log. */
    fun setPosition(pagesDone: Int) {
        prefs.edit().putInt("quran_khatm", completedKhatms * MADANI_PAGES + pagesDone.coerceIn(0, MADANI_PAGES - 1)).apply()
    }

    /** Starts the current khatm again from page 1; completed khatms are kept. */
    fun startNewKhatm() = setPosition(0)

    private fun goalMet(date: LocalDate, today: LocalDate) = pagesOn(date).let { it > 0 && it >= targetOn(date, today) }

    /** Days in a row the goal was met, ending today (or yesterday while today is in progress). */
    fun currentStreak(today: LocalDate, limit: Int = 1000): Int {
        var date = if (goalMet(today, today)) today else today.minusDays(1)
        var streak = 0
        while (streak < limit && goalMet(date, today)) {
            streak++
            date = date.minusDays(1)
        }
        return streak
    }

    fun bestStreak(today: LocalDate, days: Int = 366): Int {
        var best = 0
        var run = 0
        for (offset in days - 1 downTo 0) {
            if (goalMet(today.minusDays(offset.toLong()), today)) { run++; best = max(best, run) } else run = 0
        }
        return best
    }

    fun stats(today: LocalDate): QuranReadingStats {
        val month = (0L until 30L).map { pagesOn(today.minusDays(it)) }
        return QuranReadingStats(
            weekPages = month.take(7).sum(),
            monthPages = month.sum(),
            activeDays = month.count { it > 0 },
            recentPace = month.take(14).sum() / 14.0
        )
    }

    /** When the current khatm ends at the recent pace, or null with no recent reading. */
    fun forecastFinish(today: LocalDate): LocalDate? {
        val pace = stats(today).recentPace
        if (pace <= 0.0) return null
        return today.plusDays(ceil(remaining / pace).toLong().coerceAtLeast(0))
    }

    /**
     * Counts a page turned in the Quran reader. Only forward turns of one page inside the Quran text
     * count, each page at most once a day, scaled to Madani pages ([pageWorth]). Returns true when
     * this completed a khatm.
     */
    fun recordReaderPage(bookId: String, index: Int, firstQuranIndex: Int, pageWorth: Float, today: LocalDate = LocalDate.now()): Boolean {
        val previous = prefs.getString("quran_reader_last", null)
        val current = "$bookId:$index"
        prefs.edit().putString("quran_reader_last", current).apply()
        if (!readerAutoLog || previous != "$bookId:${index - 1}" || index - 1 < firstQuranIndex) return false

        val day = today.toString()
        val seen = if (prefs.getString("quran_reader_day", null) == day) prefs.getStringSet("quran_reader_seen", emptySet()).orEmpty() else emptySet()
        if (current in seen) return false
        val carry = prefs.getFloat("quran_reader_carry", 0f) + pageWorth
        val whole = floor(carry).toInt()
        prefs.edit()
            .putString("quran_reader_day", day)
            .putStringSet("quran_reader_seen", seen + current)
            .putFloat("quran_reader_carry", carry - whole)
            .putInt("quran_reader_$today", readerPagesOn(today) + whole)
            .apply()
        return whole > 0 && log(today, whole, today)
    }
}
