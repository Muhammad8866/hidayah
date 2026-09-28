package com.example.quran

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.persistedAppLanguage

/**
 * Keeps Quran recitation running while the app is in the background. The service owns no player of
 * its own; it only fronts [QuranAudioManager] with a foreground notification and transport
 * controls, so playback continues when the user leaves the screen or locks the device.
 */
class QuranAudioService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE -> QuranAudioManager.toggle()
            ACTION_NEXT -> QuranAudioManager.next()
            ACTION_PREVIOUS -> QuranAudioManager.previous()
            ACTION_STOP -> { QuranAudioManager.stop(); return START_NOT_STICKY }
        }
        startForegroundCompat()
        return START_STICKY
    }

    private fun startForegroundCompat() {
        val notification = buildNotification(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val CHANNEL_ID = "quran_audio_playback"
        private const val NOTIFICATION_ID = 4101
        private const val ACTION_TOGGLE = "com.example.quran.TOGGLE"
        private const val ACTION_NEXT = "com.example.quran.NEXT"
        private const val ACTION_PREVIOUS = "com.example.quran.PREVIOUS"
        private const val ACTION_STOP = "com.example.quran.STOP"

        fun start(context: Context) {
            val intent = Intent(context, QuranAudioService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        /** Refreshes the notification after a state change, without restarting playback. */
        fun update(context: Context?) {
            if (context == null) return
            val state = QuranAudioManager.state.value
            if (state.playingSurah == null) return
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, buildNotification(context))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, QuranAudioService::class.java))
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(NOTIFICATION_ID)
        }

        private fun buildNotification(context: Context): Notification {
            ensureChannel(context)
            val state = QuranAudioManager.state.value
            val language = persistedAppLanguage(context)
            val contentIntent = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(QuranAudioManager.nowPlayingTitle())
                .setContentText("Mishary Rashid Alafasy")
                .setContentIntent(contentIntent)
                .setOngoing(state.isPlaying)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .addAction(android.R.drawable.ic_media_previous, localizeUiText("Previous", language), command(context, ACTION_PREVIOUS))
                .addAction(
                    if (state.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                    localizeUiText(if (state.isPlaying) "Pause" else "Play", language),
                    command(context, ACTION_TOGGLE)
                )
                .addAction(android.R.drawable.ic_media_next, localizeUiText("Next", language), command(context, ACTION_NEXT))
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, localizeUiText("Stop", language), command(context, ACTION_STOP))
                .build()
        }

        private fun command(context: Context, action: String): PendingIntent {
            val intent = Intent(context, QuranAudioService::class.java).setAction(action)
            return PendingIntent.getService(
                context, action.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val language = persistedAppLanguage(context)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    localizeUiText("Quran playback", language),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = localizeUiText("Quran audio continues in the background", language)
                    setShowBadge(false)
                }
            )
        }
    }
}
