package com.example.festival

import android.content.Context
import android.content.SharedPreferences

/**
 * Which recommended deeds (aamal) the user ticked for one occurrence of an occasion. Progress is kept
 * per Hijri year, so next year's Shab-e-Baraat starts with a fresh checklist.
 */
class FestivalAamalProgress(private val prefs: SharedPreferences) {

    private fun key(festivalId: String, hijriYear: Int) = "aamal_${festivalId}_$hijriYear"

    fun done(festivalId: String, hijriYear: Int): Set<Int> =
        prefs.getStringSet(key(festivalId, hijriYear), emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }.toSet()

    /** Ticks or unticks deed [index]; returns the updated set. */
    fun toggle(festivalId: String, hijriYear: Int, index: Int): Set<Int> {
        val current = done(festivalId, hijriYear)
        val next = if (index in current) current - index else current + index
        prefs.edit().putStringSet(key(festivalId, hijriYear), next.map { it.toString() }.toSet()).apply()
        return next
    }

    companion object {
        fun prefs(context: Context): SharedPreferences =
            context.getSharedPreferences("festival_aamal", Context.MODE_PRIVATE)
    }
}
