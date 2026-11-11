package com.example.prayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PrayerAlertExtrasTest {

    private fun plan(prayer: String, day: Int = 0, pages: Int = 0, goal: Int = 2, streak: Int = 0) =
        PrayerAlertExtras.plan(prayer, day, morningCount = 18, eveningCount = 17,
            quranPagesToday = pages, quranGoal = goal, prayerStreak = streak)

    @Test fun fajrShowsMorningDhikrAndOpensAdhkar() {
        assertEquals(PrayerAlertExtra.Dhikr(evening = false, index = 5), plan("Fajr", day = 5).extra)
        assertEquals(PrayerAlertAction.ADHKAR, plan("Fajr").action)
    }

    @Test fun dhikrRotatesByDayAndWrapsAround() {
        assertEquals(PrayerAlertExtra.Dhikr(evening = false, index = 2), plan("Fajr", day = 20).extra)
        assertEquals(PrayerAlertExtra.Dhikr(evening = true, index = 3), plan("Asr", day = 20).extra)
    }

    @Test fun asrShowsEveningDhikrAndOpensAdhkar() {
        assertEquals(PrayerAlertAction.ADHKAR, plan("Asr").action)
    }

    @Test fun dhuhrShowsDhikrWithoutAction() {
        val p = plan("Dhuhr", day = 1)
        assertEquals(PrayerAlertExtra.Dhikr(evening = false, index = 1), p.extra)
        assertNull(p.action)
    }

    @Test fun maghribShowsPagesLeftOnlyWhileGoalOpen() {
        assertEquals(PrayerAlertExtra.PagesLeft(3), plan("Maghrib", pages = 1, goal = 4).extra)
        assertEquals(PrayerAlertAction.QURAN, plan("Maghrib", pages = 1, goal = 4).action)
        val done = plan("Maghrib", pages = 4, goal = 4)
        assertNull(done.extra)
        assertEquals(PrayerAlertAction.QURAN, done.action)
    }

    @Test fun ishaShowsStreakOnlyWhenPositive() {
        assertEquals(PrayerAlertExtra.Streak(7), plan("Isha", streak = 7).extra)
        assertNull(plan("Isha", streak = 0).extra)
        assertNull(plan("Isha", streak = 7).action)
    }

    @Test fun badInputIsSafe() {
        assertNull(plan("Unknown").extra)
        assertNull(plan("Unknown").action)
        assertEquals(PrayerAlertExtra.Dhikr(evening = false, index = 4), plan("Fajr", day = -14).extra)
        assertNull(PrayerAlertExtras.plan("Fajr", 3, 0, 0, 0, 2, 0).extra)
        assertNull(plan("Maghrib", pages = -5, goal = 0).extra)
    }
}
