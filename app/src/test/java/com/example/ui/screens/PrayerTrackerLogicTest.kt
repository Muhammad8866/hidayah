package com.example.ui.screens

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PrayerTrackerLogicTest {
    private val today = LocalDate.of(2026, 9, 27)
    private lateinit var store: PrayerTrackerStore
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val prefs = context.getSharedPreferences("daily_tools", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        store = PrayerTrackerStore(prefs)
    }

    private fun completeDay(date: LocalDate) = TRACKED_PRAYERS.forEach { store.setMark(date, it, PrayerMark.ON_TIME) }

    @Test
    fun `old prayed and missed logs are still read`() {
        context.getSharedPreferences("daily_tools", Context.MODE_PRIVATE).edit()
            .putString("prayer_${today}_Fajr", "Prayed").putString("prayer_${today}_Isha", "Missed").commit()
        val day = store.day(today)
        assertEquals(PrayerMark.ON_TIME, day.marks["Fajr"])
        assertEquals(PrayerMark.MISSED, day.marks["Isha"])
        assertEquals(1, day.prayed)
    }

    @Test
    fun `missed prayer is added to qaza once and removed when corrected`() {
        store.setMark(today, "Fajr", PrayerMark.MISSED)
        store.setMark(today, "Fajr", PrayerMark.MISSED)
        assertEquals(1, store.qazaCount("Fajr"))
        store.setMark(today, "Fajr", PrayerMark.LATE)
        assertEquals(0, store.qazaCount("Fajr"))
    }

    @Test
    fun `auto qaza can be turned off`() {
        store.autoQaza = false
        store.setMark(today, "Asr", PrayerMark.MISSED)
        assertEquals(0, store.qazaCount("Asr"))
    }

    @Test
    fun `streak skips excused days and waits for today`() {
        context.getSharedPreferences("daily_tools", Context.MODE_PRIVATE).edit()
            .putBoolean("muslimah_mode", true).putBoolean("cycle_${today.minusDays(2)}", true).commit()
        completeDay(today.minusDays(1))
        completeDay(today.minusDays(3))
        store.setMark(today, "Fajr", PrayerMark.JAMAAT)
        assertEquals(2, currentStreak(today, store::day))
        completeDay(today)
        assertEquals(3, currentStreak(today, store::day))
        assertEquals(3, bestStreak(today, store::day))
    }

    @Test
    fun `stats find the weakest prayer`() {
        (0L..3L).forEach { offset ->
            val date = today.minusDays(offset)
            completeDay(date)
            store.setMark(date, "Fajr", PrayerMark.MISSED)
        }
        val stats = prayerStats(today, store::day)
        assertEquals(4, stats.trackedDays)
        assertEquals("Fajr", stats.weakest)
        assertEquals(0.8f, stats.completion, 0.001f)
    }

    @Test
    fun `bulk add, make up and plan`() {
        store.addQazaDays(10)
        assertEquals(10, store.qazaCount("Witr"))
        store.makeUpQaza("Witr")
        assertEquals(9, store.qazaCount("Witr"))
        assertEquals(1, store.qazaMadeUp)
        assertEquals(5, qazaDaysToFinish(store.qazaBalance(), 2))
    }

    @Test
    fun `mark remaining only fills unlogged prayers`() {
        store.setMark(today, "Dhuhr", PrayerMark.MISSED)
        val changed = store.markRemainingPrayed(today, listOf("Fajr", "Dhuhr", "Asr"))
        assertEquals(listOf("Fajr", "Asr"), changed)
        assertEquals(PrayerMark.MISSED, store.day(today).marks["Dhuhr"])
        assertNull(store.day(today).marks["Isha"])
    }
}
