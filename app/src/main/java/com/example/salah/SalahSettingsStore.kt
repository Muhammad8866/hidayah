package com.example.salah

import android.content.SharedPreferences

/** The selected teaching school is independent of the prayer-time calculation method. */
object SalahSettingsStore {
    private const val KEY = "salah_madhhab"

    fun load(prefs: SharedPreferences): Madhhab =
        Madhhab.entries.firstOrNull { it.name == prefs.getString(KEY, null) } ?: Madhhab.HANAFI

    fun save(prefs: SharedPreferences, madhhab: Madhhab) {
        prefs.edit().putString(KEY, madhhab.name).apply()
    }
}
