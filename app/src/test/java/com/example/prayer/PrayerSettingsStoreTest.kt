package com.example.prayer

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PrayerSettingsStoreTest {
    @Test
    fun alertsRequireAnExplicitChoiceEvenWhenAnOldInstallSavedEnabled() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("prayer_settings_consent_test", Context.MODE_PRIVATE)
        prefs.edit().clear().putBoolean(PrayerSettingsStore.NOTIFICATIONS_ENABLED_KEY, true).commit()

        assertFalse(PrayerSettingsStore.notificationsEnabled(prefs))
        PrayerSettingsStore.saveNotificationsEnabled(prefs, true)
        assertTrue(PrayerSettingsStore.notificationsEnabled(prefs))
        PrayerSettingsStore.saveNotificationsEnabled(prefs, false)
        assertFalse(PrayerSettingsStore.notificationsEnabled(prefs))
    }

    @Test
    fun manualCityModeSurvivesRestartUntilCurrentLocationIsChosen() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("prayer_settings_location_test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        assertTrue(PrayerSettingsStore.useLiveLocation(prefs))
        PrayerSettingsStore.saveUseLiveLocation(prefs, false)
        assertFalse(PrayerSettingsStore.useLiveLocation(prefs))
        PrayerSettingsStore.saveUseLiveLocation(prefs, true)
        assertTrue(PrayerSettingsStore.useLiveLocation(prefs))
    }
}
