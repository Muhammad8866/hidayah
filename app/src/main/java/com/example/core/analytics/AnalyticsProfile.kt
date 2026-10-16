package com.example.core.analytics

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.example.azan.AzanAlertStore
import com.example.prayer.PrayerNotificationReceiver
import com.example.prayer.PrayerSettingsStore
import com.example.prayer.PrayerTimesWidget
import com.example.salah.SalahSettingsStore
import com.example.ui.localization.persistedAppLanguage

/**
 * Mirrors the saved setting choices into user-scoped dimensions, so any report can be split by
 * language, calculation method or alert setup. Only fixed choices: never the city or coordinates.
 */
object AnalyticsProfile {
    fun sync(context: Context) {
        runCatching {
            val app = context.applicationContext
            val prefs = PrayerSettingsStore.prefs(app)
            val config = PrayerSettingsStore.loadConfig(prefs)
            fun set(name: String, value: String) = AppAnalytics.userProperty(name, value)
            fun flag(on: Boolean) = if (on) "on" else "off"

            set("app_language", persistedAppLanguage(app))
            set("prayer_method", config.method.name)
            set("asr_method", config.asrMethod.name)
            set("high_latitude_rule", config.highLatitudeRule.name)
            set("hijri_calendar", prefs.getString(PrayerSettingsStore.HIJRI_CALENDAR_KEY, null) ?: "automatic")
            set("madhhab", SalahSettingsStore.load(prefs).name)
            set("quran_reader_mode", prefs.getString("quran_reader_mode", null) ?: "read")
            set("prayer_alerts", flag(PrayerSettingsStore.notificationsEnabled(prefs)))
            set("azan_alert", flag(AzanAlertStore.selectedId(prefs, fajr = true) != null ||
                AzanAlertStore.selectedId(prefs, fajr = false) != null))
            set("location_mode", when {
                PrayerSettingsStore.useLiveLocation(prefs) -> "gps"
                PrayerSettingsStore.hasSavedCity(prefs) -> "manual"
                else -> "unset"
            })
            set("exact_alarms", if (PrayerNotificationReceiver.canScheduleExactAlarms(app)) "allowed" else "denied")
            val widgets = AppWidgetManager.getInstance(app)
                ?.getAppWidgetIds(ComponentName(app, PrayerTimesWidget::class.java))
            set("home_widget", flag(widgets?.isNotEmpty() == true))
        }
    }
}
