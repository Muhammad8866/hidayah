package com.example.prayer

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.azan.AzanAlertService
import com.example.azan.AzanAlertStore
import com.example.domain.model.*
import com.example.ui.localization.localizeUiTemplate
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.persistedAppLanguage
import com.example.ui.localization.appLocale
import com.example.ui.localization.formatHijriDate
import com.example.ui.localization.isRtlAppLanguage
import java.util.Calendar
import java.util.TimeZone

/**
 * Handles prayer time notification channels, scheduling alarms,
 * broadcasting alerts, and immediate test notifications.
 */
class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = PrayerSettingsStore.prefs(context)
        if (!PrayerSettingsStore.notificationsEnabled(prefs) || !PrayerSettingsStore.hasSavedCity(prefs)) {
            cancelPrayerAlerts(context)
            return
        }
        deliverPrayerAlert(context, intent)
        // Only today and tomorrow are ever scheduled, so each alert extends the chain.
        rescheduleFromPreferences(context)
    }

    companion object {
        const val CHANNEL_ID = "prayer_times_channel"
        const val CHANNEL_NAME = "Prayer Times Alerts"
        const val CHANNEL_DESC = "Peaceful notifications when each prayer time enters"
        const val AZAN_CHANNEL_ID = "prayer_azan_channel"
        const val AZAN_CHANNEL_NAME = "Prayer Alerts with Azan"

        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_PRAYER_ARABIC = "extra_prayer_arabic"
        const val EXTRA_PRAYER_TIME = "extra_prayer_time"
        const val EXTRA_LOCATION = "extra_location"
        const val EXTRA_HIJRI_DATE = "extra_hijri_date"
        /** Tells MainActivity it was opened from a prayer alert or the widget, for usage reports. */
        const val EXTRA_LAUNCH_SOURCE = "extra_launch_source"
        const val SOURCE_PRAYER_ALERT = "prayer_notification"
        const val SOURCE_WIDGET = "home_widget"
        private const val OPEN_APP_REQUEST = 1

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val language = persistedAppLanguage(context)
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, localizeUiText(CHANNEL_NAME, language), importance).apply {
                    description = localizeUiText(CHANNEL_DESC, language)
                    enableVibration(true)
                    setShowBadge(true)
                }
                // The azan is the sound of these alerts, so the channel itself stays silent.
                val azanChannel = NotificationChannel(AZAN_CHANNEL_ID, localizeUiText(AZAN_CHANNEL_NAME, language), importance).apply {
                    description = localizeUiText(CHANNEL_DESC, language)
                    setSound(null, null)
                    enableVibration(true)
                    setShowBadge(true)
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                notificationManager.createNotificationChannel(azanChannel)
            }
        }

        /** The details of one prayer alert, carried by alarms and handed to [AzanAlertService]. */
        fun alertDetails(
            prayerName: String,
            prayerArabic: String,
            prayerTime: String,
            location: String,
            hijriDate: String
        ): Intent = Intent()
            .putExtra(EXTRA_PRAYER_NAME, prayerName)
            .putExtra(EXTRA_PRAYER_ARABIC, prayerArabic)
            .putExtra(EXTRA_PRAYER_TIME, prayerTime)
            .putExtra(EXTRA_LOCATION, location)
            .putExtra(EXTRA_HIJRI_DATE, hijriDate)

        fun notificationId(prayerName: String): Int = prayerName.hashCode().takeIf { it != 0 } ?: 1

        /**
         * Announces the prayer with the azan the user chose for it. Without a downloaded azan, on
         * a silent phone or when the system refuses to start playback, it is a normal notification.
         */
        fun deliverPrayerAlert(context: Context, alert: Intent, test: Boolean = false) {
            val prayerName = alert.getStringExtra(EXTRA_PRAYER_NAME) ?: "Prayer"
            val action = if (test) "test_delivered" else "delivered"
            val azan = AzanAlertStore.fileFor(context, prayerName)
            if (azan != null && AzanAlertService.start(context, azan, alert)) {
                com.example.core.analytics.AppAnalytics.prayerAlert(action, prayerName, "azan")
                return
            }
            // An azan is chosen but could not play: silent phone, a call, or a blocked background start.
            com.example.core.analytics.AppAnalytics.prayerAlert(action, prayerName, if (azan != null) "azan_skipped" else "notification")
            showPrayerNotification(context, alert)
        }

        fun showPrayerNotification(context: Context, alert: Intent) {
            val prayerName = alert.getStringExtra(EXTRA_PRAYER_NAME) ?: "Prayer"
            try {
                NotificationManagerCompat.from(context).notify(notificationId(prayerName), buildPrayerNotification(context, alert))
            } catch (e: SecurityException) {
                // Runtime permission may not be granted yet
                com.example.core.analytics.AppAnalytics.prayerAlert("blocked", prayerName, "permission_missing")
            }
        }

        /** [withAzan] uses the silent azan channel; [stopAzan] adds the Stop action while it plays. */
        fun buildPrayerNotification(
            context: Context,
            alert: Intent,
            withAzan: Boolean = false,
            stopAzan: PendingIntent? = null
        ): Notification {
            val prayerName = alert.getStringExtra(EXTRA_PRAYER_NAME) ?: "Prayer"
            val prayerArabic = alert.getStringExtra(EXTRA_PRAYER_ARABIC) ?: ""
            val prayerTime = alert.getStringExtra(EXTRA_PRAYER_TIME) ?: ""
            val location = alert.getStringExtra(EXTRA_LOCATION) ?: ""
            val hijriDate = alert.getStringExtra(EXTRA_HIJRI_DATE) ?: ""
            createNotificationChannel(context)
            val language = persistedAppLanguage(context)

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(EXTRA_LAUNCH_SOURCE, SOURCE_PRAYER_ALERT)
                putExtra(EXTRA_PRAYER_NAME, prayerName)
            }
            // Its own request code: the widget opens the same Activity and must keep its own extras.
            val pendingIntent = PendingIntent.getActivity(
                context,
                OPEN_APP_REQUEST,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val displayPrayer = if (isRtlAppLanguage(language) && prayerArabic.isNotBlank()) prayerArabic else prayerName
            fun tr(text: String) = localizeUiText(text, language)
            // Only fixed copy is translated; city names like "New Delhi" must never be rewritten.
            val prayerLabel = if (displayPrayer == prayerArabic) displayPrayer else tr(displayPrayer)
            val title = if (prayerArabic.isNotBlank() && !isRtlAppLanguage(language)) {
                "${tr("Time for")} $prayerLabel ($prayerArabic)"
            } else "${tr("Time for")} $prayerLabel"

            val content = if (location.isNotBlank()) {
                localizeUiTemplate("Salah has commenced for {location} at {time}", language,
                    mapOf("location" to location, "time" to prayerTime)) + " • $hijriDate"
            } else {
                "${tr("Salah time has commenced at")} $prayerTime • $hijriDate"
            }

            val verse = localizeUiText(
                "Indeed, prayer has been decreed upon the believers a decree of specified times. [Quran 4:103]",
                language
            )

            val builder = NotificationCompat.Builder(context, if (withAzan) AZAN_CHANNEL_ID else CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(NotificationCompat.BigTextStyle().bigText("$content\n\n$verse"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
            if (withAzan) builder.setSilent(true)
            if (stopAzan != null) {
                // Swiping the notification away silences the azan too.
                builder.setDeleteIntent(stopAzan)
                    .addAction(android.R.drawable.ic_lock_silent_mode, tr("Stop Azan"), stopAzan)
            }
            return builder.build()
        }

        /**
         * Recomputes and schedules alerts from the saved city and settings. Runs without the app
         * open: after each alert (so the chain continues daily), after a reboot or app update, and
         * when the clock, timezone or exact-alarm permission changes.
         */
        fun rescheduleFromPreferences(context: Context) {
            val prefs = PrayerSettingsStore.prefs(context)
            if (!PrayerSettingsStore.notificationsEnabled(prefs)) {
                cancelPrayerAlerts(context)
                return
            }
            if (!PrayerSettingsStore.hasSavedCity(prefs)) {
                cancelPrayerAlerts(context)
                return
            }
            PrayerSettingsStore.restoreHijriPreference(prefs)
            val city = PrayerSettingsStore.loadCity(prefs)
            val config = PrayerSettingsStore.loadConfig(prefs)
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(city.timeZoneId))
            val today = runCatching {
                AstronomicalPrayerCalculator.calculateTimes(calendar, city.latitude, city.longitude, config)
            }.getOrNull() ?: return
            schedulePrayerAlerts(context, today, config, city, calendar)
        }

        /** Schedules fresh alarms for today and tomorrow, removing stale city/method alarms first. */
        fun schedulePrayerAlerts(
            context: Context,
            rawTimes: AstronomicalPrayerCalculator.RawPrayerTimes,
            config: PrayerTimeConfig,
            city: CityLocation,
            calendar: Calendar
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val now = System.currentTimeMillis()
            val tomorrow = (calendar.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
            val tomorrowTimes = runCatching {
                AstronomicalPrayerCalculator.calculateTimes(
                    tomorrow, city.latitude, city.longitude, config
                )
            }.getOrNull()

            fun scheduleDay(
                day: Calendar,
                times: AstronomicalPrayerCalculator.RawPrayerTimes,
                requestCodeBase: Int
            ) {
                val language = persistedAppLanguage(context)
                val hijriToday = formatHijriDate(HijriCalendarHelper.getHijriDate(day), language)
                val hijriTonight = formatHijriDate(HijriCalendarHelper.getHijriDate(day, dayOffset = 1), language)
                val prayers = listOf(
                    PrayerType.FAJR to times.fajrHours,
                    PrayerType.DHUHR to times.dhuhrHours,
                    PrayerType.ASR to times.asrHours,
                    PrayerType.MAGHRIB to times.maghribHours,
                    PrayerType.ISHA to times.ishaHours
                )

                AstronomicalPrayerCalculator.use24HourClock = android.text.format.DateFormat.is24HourFormat(context)
                prayers.forEachIndexed { index, (type, hours) ->
                    val intent = alertIntent(context).putExtras(
                        alertDetails(
                            prayerName = type.displayName,
                            prayerArabic = type.arabicName,
                            prayerTime = AstronomicalPrayerCalculator.formatHoursToAmPm(
                                hours, appLocale(persistedAppLanguage(context))
                            ),
                            location = city.name,
                            hijriDate = if (type == PrayerType.MAGHRIB || type == PrayerType.ISHA) {
                                hijriTonight
                            } else {
                                hijriToday
                            }
                        )
                    )
                    val pendingIntent = alertPendingIntent(context, requestCodeBase + index, intent)
                    // A location or method change may make an old future alarm obsolete.
                    alarmManager.cancel(pendingIntent)

                    val prayerMillis = AstronomicalPrayerCalculator.formatHoursToTimeInMillis(
                        day, hours
                    )
                    if (prayerMillis <= now) return@forEachIndexed
                    setAlarm(alarmManager, prayerMillis, pendingIntent)
                }
            }

            scheduleDay(calendar, rawTimes, requestCodeBase = TODAY_REQUEST_BASE)
            if (tomorrowTimes != null) scheduleDay(tomorrow, tomorrowTimes, requestCodeBase = TOMORROW_REQUEST_BASE)
        }

        /** Removes every scheduled prayer alert, e.g. when the user turns alerts off. */
        fun cancelPrayerAlerts(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            for (base in listOf(TODAY_REQUEST_BASE, TOMORROW_REQUEST_BASE)) {
                repeat(PRAYERS_PER_DAY) { index ->
                    val pendingIntent = alertPendingIntent(context, base + index, alertIntent(context))
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
            }
        }

        /** False on Android 12+ until the user allows "Alarms & reminders"; alerts may then arrive late. */
        fun canScheduleExactAlarms(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false
            return alarmManager.canScheduleExactAlarms()
        }

        private fun setAlarm(alarmManager: AlarmManager, atMillis: Long, pendingIntent: PendingIntent) {
            val exactAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
            try {
                if (exactAllowed) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pendingIntent)
                    return
                }
            } catch (e: SecurityException) {
                // Permission revoked between the check and the call; fall through to inexact.
            }
            // Still fires in Doze, though the system may delay it by a few minutes.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pendingIntent)
        }

        private fun alertIntent(context: Context) = Intent(context, PrayerNotificationReceiver::class.java)

        private fun alertPendingIntent(context: Context, requestCode: Int, intent: Intent): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        private const val TODAY_REQUEST_BASE = 100
        private const val TOMORROW_REQUEST_BASE = 200
        private const val PRAYERS_PER_DAY = 5
    }
}
