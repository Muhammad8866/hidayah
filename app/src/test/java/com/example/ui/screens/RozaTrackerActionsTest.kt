package com.example.ui.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RozaTrackerActionsTest {
    private val today = LocalDate.of(2026, 10, 10)
    private lateinit var prefs: SharedPreferences
    private lateinit var store: RozaTrackerStore

    @Before fun setup() {
        prefs = ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("daily_tools", Context.MODE_PRIVATE)
        prefs.edit().clear().putInt("roza_qaza", 2).commit()
        store = RozaTrackerStore(prefs)
    }

    @Test fun `backup round trip restores balance logs and notes`() {
        store.setMark(today, RozaMark.NAFL, today)
        store.setMark(today.minusDays(1), RozaMark.MISSED, today)
        store.setNote(today, "Monday sunnah fast")
        val json = RozaBackup.export(store)
        val logs = store.logs()

        prefs.edit().clear().putInt("roza_qaza", 9).putString("roza_log_${today.minusDays(5)}", "QAZA").commit()
        assertTrue(RozaBackup.import(store, json, today))
        assertEquals(3, store.balance)
        assertEquals(logs, store.logs())
        assertEquals(mapOf(today to "Monday sunnah fast"), store.notes())
    }

    @Test fun `backup without notes is accepted`() {
        assertTrue(RozaBackup.import(store, """{"version":1,"balance":4,"logs":{"2026-10-09":"RAMADAN"}}""", today))
        assertEquals(4, store.balance)
        assertEquals(mapOf(today.minusDays(1) to RozaMark.RAMADAN), store.logs())
    }

    @Test fun `invalid backups are rejected without changing anything`() {
        store.setMark(today, RozaMark.NAFL, today)
        val bad = listOf(
            "", "not json", "[]", "{}",
            """{"version":1,"balance":1}""",
            """{"version":99,"balance":1,"logs":{}}""",
            """{"version":1,"balance":-1,"logs":{}}""",
            """{"version":1,"balance":1.5,"logs":{}}""",
            """{"version":1,"balance":"3","logs":{}}""",
            """{"version":1,"balance":1,"logs":{"2026-10-09":"FEAST"}}""",
            """{"version":1,"balance":1,"logs":{"09-10-2026":"NAFL"}}""",
            """{"version":1,"balance":1,"logs":{"2026-10-11":"NAFL"}}""",
            """{"version":1,"balance":1,"logs":{"2026-10-09":7}}""",
            """{"version":1,"balance":1,"logs":{},"notes":{"2026-10-11":"future"}}""",
            """{"version":1,"balance":1,"logs":{},"notes":{"bad":"note"}}"""
        )
        bad.forEach { assertFalse(it, RozaBackup.import(store, it, today)) }
        assertEquals(2, store.balance)
        assertEquals(mapOf(today to RozaMark.NAFL), store.logs())
    }

    @Test fun `summary lists balance and every stat`() {
        val text = rozaSummaryText(5, RozaStats(total = 40, ramadan = 30, nafl = 7, qazaDone = 3, missed = 4, currentStreak = 2, longestStreak = 30), today)
        assertEquals(
            listOf(
                "Roza tracker - 2026-10-10", "Qaza roza balance: 5", "Total fasts kept: 40", "Ramadan fasts: 30", "Nafl fasts: 7",
                "Qaza fasts completed: 3", "Missed fasts logged: 4", "Current streak: 2", "Longest streak: 30"
            ),
            text.lines()
        )
    }
}
