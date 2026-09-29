package com.example.prayer

import android.app.backup.BackupAgent
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.os.ParcelFileDescriptor

/**
 * Full (auto) backup still does the work; this agent only exists for [onRestoreFinished], so a
 * restored city and alert setting schedule prayer alarms right away instead of on the next launch.
 */
class PrayerBackupAgent : BackupAgent() {
    override fun onBackup(oldState: ParcelFileDescriptor?, data: BackupDataOutput?, newState: ParcelFileDescriptor?) = Unit
    override fun onRestore(data: BackupDataInput?, appVersionCode: Int, newState: ParcelFileDescriptor?) = Unit

    override fun onRestoreFinished() {
        runCatching { PrayerNotificationReceiver.rescheduleFromPreferences(applicationContext) }
    }
}
