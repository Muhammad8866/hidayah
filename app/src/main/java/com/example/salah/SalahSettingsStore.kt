package com.example.salah

import android.content.SharedPreferences

/** The selected teaching school is independent of the prayer-time calculation method. */
object SalahSettingsStore {
    private const val KEY = "salah_madhhab"
    private const val KEY_WORSHIPPER = "salah_worshipper"

    fun load(prefs: SharedPreferences): Madhhab =
        Madhhab.entries.firstOrNull { it.name == prefs.getString(KEY, null) } ?: Madhhab.HANAFI

    fun save(prefs: SharedPreferences, madhhab: Madhhab) {
        prefs.edit().putString(KEY, madhhab.name).apply()
    }

    /** Whose posture the step-by-step guide describes. */
    fun loadWorshipper(prefs: SharedPreferences): Worshipper =
        Worshipper.entries.firstOrNull { it.name == prefs.getString(KEY_WORSHIPPER, null) } ?: Worshipper.MAN

    fun saveWorshipper(prefs: SharedPreferences, worshipper: Worshipper) {
        prefs.edit().putString(KEY_WORSHIPPER, worshipper.name).apply()
    }
}
