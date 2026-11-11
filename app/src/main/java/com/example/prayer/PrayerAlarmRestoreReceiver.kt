package com.example.prayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Alarms are wiped on reboot and app update, and go stale when the clock or timezone changes. */
class PrayerAlarmRestoreReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        com.example.core.analytics.AppAnalytics.wake(context, "alarm_restore", intent.action)
        if (intent.action !in HANDLED_ACTIONS) return
        val pending = goAsync()
        Thread {
            try {
                PrayerNotificationReceiver.rescheduleFromPreferences(context)
                // Only the fixed system trigger is reported, so reports show whether alarms survive reboots and updates.
                com.example.core.analytics.AppAnalytics.milestone("prayer_alerts", "alarms_restored",
                    intent.action.orEmpty().substringAfterLast('.'))
            } finally {
                pending.finish()
            }
        }.start()
    }

    private companion object {
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
        )
    }
}
