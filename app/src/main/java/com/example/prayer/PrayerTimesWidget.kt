package com.example.prayer

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.ui.localization.appLocale
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.persistedAppLanguage
import java.util.Calendar
import java.util.TimeZone

/** Uses the same saved city, settings and calculator as the prayer dashboard. */
class PrayerTimesWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        render(context, manager, ids)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action in setOf(ACTION_REFRESH, Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED,
                Intent.ACTION_MY_PACKAGE_REPLACED)) refresh(context)
    }

    override fun onEnabled(context: Context) {
        com.example.core.analytics.AppAnalytics.widget("added")
        com.example.core.analytics.AnalyticsProfile.sync(context)
    }

    override fun onDisabled(context: Context) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        alarm?.cancel(refreshIntent(context))
        com.example.core.analytics.AppAnalytics.widget("removed")
        com.example.core.analytics.AnalyticsProfile.sync(context)
    }

    companion object {
        private const val ACTION_REFRESH = "com.example.prayer.WIDGET_REFRESH"
        private const val OPEN_APP_REQUEST = 2

        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, PrayerTimesWidget::class.java))
            if (ids.isNotEmpty()) render(context, manager, ids)
        }

        private fun render(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val prefs = PrayerSettingsStore.prefs(context)
            val hasCity = PrayerSettingsStore.hasSavedCity(prefs)
            val city = PrayerSettingsStore.loadCity(prefs)
            val zone = TimeZone.getTimeZone(city.timeZoneId)
            val today = Calendar.getInstance(zone)
            val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
            val config = PrayerSettingsStore.loadConfig(prefs)
            val language = persistedAppLanguage(context)
            val locale = appLocale(language)
            AstronomicalPrayerCalculator.use24HourClock = android.text.format.DateFormat.is24HourFormat(context)
            fun tr(text: String) = localizeUiText(text, language)
            val result = if (hasCity) runCatching {
                AstronomicalPrayerCalculator.calculateTimes(today, city.latitude, city.longitude, config) to
                    AstronomicalPrayerCalculator.calculateTimes(tomorrow, city.latitude, city.longitude, config)
            }.getOrNull() else null
            val rows = result?.first?.let { raw ->
                listOf("Fajr" to raw.fajrHours, "Dhuhr" to raw.dhuhrHours,
                    "Asr" to raw.asrHours, "Maghrib" to raw.maghribHours, "Isha" to raw.ishaHours)
            }
            val next = rows?.firstNotNullOfOrNull { (name, hours) ->
                val time = AstronomicalPrayerCalculator.formatHoursToTimeInMillis(today, hours)
                if (time > today.timeInMillis) name to time else null
            } ?: result?.second?.let {
                "Fajr" to AstronomicalPrayerCalculator.formatHoursToTimeInMillis(tomorrow, it.fajrHours)
            }
            val launch = PendingIntent.getActivity(context, OPEN_APP_REQUEST,
                Intent(context, MainActivity::class.java).putExtra(
                    PrayerNotificationReceiver.EXTRA_LAUNCH_SOURCE, PrayerNotificationReceiver.SOURCE_WIDGET),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            ids.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.prayer_widget)
                views.setTextViewText(R.id.widget_location, if (hasCity) city.name else tr("Choose city in app"))
                views.setTextViewText(R.id.widget_next, tr(next?.first ?: "Prayer times unavailable"))
                // Same clock style as the rows and the app, so 24-hour phones never see "a.m.".
                views.setTextViewText(R.id.widget_next_time, next?.second?.let {
                    val at = (today.clone() as Calendar).apply { timeInMillis = it }
                    val hours = at.get(Calendar.HOUR_OF_DAY) + at.get(Calendar.MINUTE) / 60.0
                    AstronomicalPrayerCalculator.formatHoursToAmPm(hours, locale)
                } ?: tr("Open app to check your location"))
                val rowIds = intArrayOf(R.id.widget_fajr, R.id.widget_dhuhr, R.id.widget_asr,
                    R.id.widget_maghrib, R.id.widget_isha)
                rowIds.forEachIndexed { index, viewId ->
                    views.setTextViewText(viewId, rows?.get(index)?.let { (name, hours) ->
                        "${tr(name)}\n${AstronomicalPrayerCalculator.formatHoursToAmPm(hours, locale)}"
                    } ?: "—")
                }
                if (next != null) {
                    views.setChronometer(R.id.widget_countdown,
                        SystemClock.elapsedRealtime() + (next.second - System.currentTimeMillis()), null, true)
                    views.setViewVisibility(R.id.widget_countdown, android.view.View.VISIBLE)
                } else views.setViewVisibility(R.id.widget_countdown, android.view.View.GONE)
                views.setOnClickPendingIntent(R.id.widget_root, launch)
                manager.updateAppWidget(id, views)
            }
            next?.let { (_, time) -> scheduleRefresh(context, time + 1000L) }
        }

        /** Exact when allowed, so the countdown never runs past zero while waiting for a late refresh. */
        private fun scheduleRefresh(context: Context, atMillis: Long) {
            val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
            val intent = refreshIntent(context)
            if (PrayerNotificationReceiver.canScheduleExactAlarms(context)) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, intent)
                    return
                } catch (e: SecurityException) {
                    // Permission revoked between the check and the call; fall through to inexact.
                }
            }
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, intent)
        }

        private fun refreshIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context, 0, Intent(context, PrayerTimesWidget::class.java).setAction(ACTION_REFRESH),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
