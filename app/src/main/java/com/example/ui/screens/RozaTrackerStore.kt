package com.example.ui.screens

import android.content.SharedPreferences
import java.time.LocalDate

internal enum class RozaMark(val label: String, val debt: Int) {
    RAMADAN("Ramadan fast", 0), NAFL("Nafl fast", 0),
    MISSED("Missed fast (Qaza due)", 1), QAZA("Qaza fast completed", -1)
}

internal class RozaTrackerStore(private val prefs: SharedPreferences) {
    val balance: Int get() = prefs.getInt("roza_qaza", 0).coerceAtLeast(0)
    fun mark(date: LocalDate): RozaMark? = RozaMark.entries.firstOrNull {
        it.name == prefs.all["roza_log_$date"]
    }

    /** Repeated taps are idempotent; editing a log reverses its balance contribution. */
    fun setMark(date: LocalDate, mark: RozaMark?, today: LocalDate = LocalDate.now()): Boolean =
        setMarks(listOf(date), mark, today)

    /** All-or-nothing: either every date gets [mark] and the balance moves by the net change, or nothing is written. */
    fun setMarks(dates: Collection<LocalDate>, mark: RozaMark?, today: LocalDate = LocalDate.now()): Boolean {
        val days = dates.toSet()
        if (days.any { it > today }) return false
        val change = days.sumOf { (mark?.debt ?: 0).toLong() - (this.mark(it)?.debt ?: 0) }
        val nextBalance = balance.toLong() + change
        // Never discard debt when editing a missed fast that has already been made up.
        if (nextBalance !in 0..Int.MAX_VALUE.toLong()) return false
        prefs.edit().apply {
            days.forEach { date ->
                if (mark == null) remove("roza_log_$date").remove("roza_note_$date") else putString("roza_log_$date", mark.name)
            }
            putInt("roza_qaza", nextBalance.toInt())
        }.apply()
        return true
    }

    fun addMissed(count: Int) {
        require(count > 0 && balance.toLong() + count <= Int.MAX_VALUE)
        prefs.edit().putInt("roza_qaza", balance + count).apply()
    }

    /** Manual correction of an over-counted balance. */
    fun reduceBalance(count: Int): Boolean {
        if (count <= 0 || count > balance) return false
        prefs.edit().putInt("roza_qaza", balance - count).apply()
        return true
    }

    fun note(date: LocalDate): String? = (prefs.all["roza_note_$date"] as? String)?.takeIf { it.isNotBlank() }

    fun setNote(date: LocalDate, text: String?) {
        val note = text?.trim()?.take(NOTE_MAX_LENGTH)?.trim().orEmpty()
        prefs.edit().apply { if (note.isEmpty()) remove("roza_note_$date") else putString("roza_note_$date", note) }.apply()
    }

    fun notes(): Map<LocalDate, String> = entries("roza_note_") { (it as? String)?.takeIf(String::isNotBlank) }

    fun logs(): Map<LocalDate, RozaMark> = entries("roza_log_") { value -> RozaMark.entries.firstOrNull { it.name == value } }

    /** Restore from a backup: everything currently stored is replaced in one edit. */
    fun replaceAll(balance: Int, logs: Map<LocalDate, RozaMark>, notes: Map<LocalDate, String>) {
        prefs.edit().apply {
            prefs.all.keys.filter { it.startsWith("roza_log_") || it.startsWith("roza_note_") }.forEach { remove(it) }
            logs.forEach { (date, mark) -> putString("roza_log_$date", mark.name) }
            notes.forEach { (date, text) ->
                val note = text.trim().take(NOTE_MAX_LENGTH).trim()
                if (note.isNotEmpty()) putString("roza_note_$date", note)
            }
            putInt("roza_qaza", balance.coerceAtLeast(0))
        }.apply()
    }

    private fun <T : Any> entries(prefix: String, parse: (Any?) -> T?): Map<LocalDate, T> = prefs.all.mapNotNull { (key, value) ->
        if (!key.startsWith(prefix)) return@mapNotNull null
        val date = runCatching { LocalDate.parse(key.removePrefix(prefix)) }.getOrNull()
        val parsed = parse(value)
        if (date != null && parsed != null) date to parsed else null
    }.toMap()

    private companion object {
        const val NOTE_MAX_LENGTH = 200
    }
}
