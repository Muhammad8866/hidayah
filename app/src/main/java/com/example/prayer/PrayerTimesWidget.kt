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
                Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_DATE_CHANGED,
                Intent.ACTION_MY_PACKAGE_REPLACED)) refresh(context)
    }

    override fun onDisabled(context: Context) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        alarm?.cancel(refreshIntent(context))
    }

    companion object {
        private const val ACTION_REFRESH = "com.example.prayer.WIDGET_REFRESH"

        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, PrayerTimesWidget::class.java))
            if (ids.isNotEmpty()) render(context, manager, ids)
        }

        private fun render(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val prefs = PrayerSettingsStore.prefs(context)
            val city = PrayerSettingsStore.loadCity(prefs)
            val zone = TimeZone.getTimeZone(city.timeZoneId)
            val today = Calendar.getInstance(zone)
            val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
            val config = PrayerSettingsStore.loadConfig(prefs)
            val result = runCatching {
                AstronomicalPrayerCalculator.calculateTimes(today, city.latitude, city.longitude, config) to
                    AstronomicalPrayerCalculator.calculateTimes(tomorrow, city.latitude, city.longitude, config)
            }.getOrNull()
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
            val launch = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            ids.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.prayer_widget)
                views.setTextViewText(R.id.widget_location, city.name)
                views.setTextViewText(R.id.widget_next, next?.first ?: "Prayer times unavailable")
                views.setTextViewText(R.id.widget_next_time, next?.second?.let {
                    java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).apply {
                        timeZone = zone
                    }.format(java.util.Date(it))
                } ?: "Open app to check your location")
                val rowIds = intArrayOf(R.id.widget_fajr, R.id.widget_dhuhr, R.id.widget_asr,
                    R.id.widget_maghrib, R.id.widget_isha)
                rowIds.forEachIndexed { index, viewId ->
                    views.setTextViewText(viewId, rows?.get(index)?.let { (name, hours) ->
                        "$name\n${AstronomicalPrayerCalculator.formatHoursToAmPm(hours)}"
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
            next?.let { (_, time) ->
                context.getSystemService(AlarmManager::class.java)?.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, time + 1000L, refreshIntent(context))
            }
        }

        private fun refreshIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context, 0, Intent(context, PrayerTimesWidget::class.java).setAction(ACTION_REFRESH),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
