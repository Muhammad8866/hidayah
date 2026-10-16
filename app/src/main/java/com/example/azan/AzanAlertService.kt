package com.example.azan

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.prayer.PrayerNotificationReceiver
import java.io.File

/**
 * Plays the chosen azan when a prayer alert fires. The prayer notification is this service's
 * foreground notification while the azan plays, with a Stop action, and stays behind afterwards.
 */
class AzanAlertService : Service() {
    private var player: MediaPlayer? = null
    private var alert: Intent? = null
    private var ringerRegistered = false

    private val audioManager by lazy { getSystemService(AudioManager::class.java) }
    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        // A call or another player took over: the azan gives way instead of resuming later.
        if (change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) finish()
    }
    private val ringerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Switching the phone to silent or vibrate is the quickest way to hush the azan.
            if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) finish()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val path = intent?.getStringExtra(EXTRA_FILE)
        if (intent == null || intent.action == ACTION_STOP || path == null) {
            // The Stop action and swiping the alert away both arrive here.
            if (intent?.action == ACTION_STOP) alert?.getStringExtra(PrayerNotificationReceiver.EXTRA_PRAYER_NAME)
                ?.let { com.example.core.analytics.AppAnalytics.prayerAlert("azan_stopped", it, "azan") }
            finish()
            return START_NOT_STICKY
        }
        releasePlayer()
        alert = intent
        val notification = PrayerNotificationReceiver.buildPrayerNotification(
            this, intent, withAzan = true, stopAzan = stopIntent(this)
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(notificationId(intent), notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else startForeground(notificationId(intent), notification)
        } catch (_: RuntimeException) {
            // The system refused a background start; the alert must still be shown.
            alert = null
            PrayerNotificationReceiver.showPrayerNotification(this, intent)
            stopSelf()
            return START_NOT_STICKY
        }
        AzanAudioManager.stop()
        play(path)
        return START_NOT_STICKY
    }

    private fun play(path: String) {
        try {
            player = MediaPlayer().apply {
                // Notification usage follows the ring/notification volume, like any alert tone.
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
                )
                setWakeMode(this@AzanAlertService, PowerManager.PARTIAL_WAKE_LOCK)
                setDataSource(path)
                setOnPreparedListener { mp ->
                    @Suppress("DEPRECATION")
                    audioManager.requestAudioFocus(
                        focusListener, AudioManager.STREAM_NOTIFICATION, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                    )
                    mp.start()
                }
                setOnCompletionListener { finish() }
                setOnErrorListener { _, _, _ -> finish(); true }
                prepareAsync()
            }
            if (!ringerRegistered) {
                ContextCompat.registerReceiver(
                    this, ringerReceiver, IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION),
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
                ringerRegistered = true
            }
        } catch (_: Exception) {
            finish()
        }
    }

    /** Stops the azan and leaves the prayer notification behind as an ordinary, dismissible one. */
    private fun finish() {
        releasePlayer()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH)
        alert?.let { shown ->
            try {
                NotificationManagerCompat.from(this).notify(
                    notificationId(shown), PrayerNotificationReceiver.buildPrayerNotification(this, shown, withAzan = true)
                )
            } catch (_: SecurityException) {
                // Notification permission was revoked while the azan played.
            }
        }
        alert = null
        stopSelf()
    }

    private fun releasePlayer() {
        player?.release()
        player = null
        @Suppress("DEPRECATION")
        audioManager.abandonAudioFocus(focusListener)
        if (ringerRegistered) runCatching { unregisterReceiver(ringerReceiver) }
        ringerRegistered = false
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }

    private fun notificationId(alert: Intent) = PrayerNotificationReceiver.notificationId(
        alert.getStringExtra(PrayerNotificationReceiver.EXTRA_PRAYER_NAME).orEmpty()
    )

    companion object {
        private const val ACTION_STOP = "com.example.azan.STOP"
        private const val EXTRA_FILE = "extra_azan_file"

        /**
         * Starts the azan for the prayer described by [alert]'s extras. Returns false when it must
         * not or cannot play, and the caller then posts the normal notification instead.
         */
        fun start(context: Context, azan: File, alert: Intent): Boolean {
            val audio = context.getSystemService(AudioManager::class.java)
            // On silent or vibrate (in the masjid, in a meeting) or during a call the phone stays quiet.
            if (audio.ringerMode != AudioManager.RINGER_MODE_NORMAL || audio.mode != AudioManager.MODE_NORMAL) return false
            val intent = Intent(context, AzanAlertService::class.java)
                .putExtras(alert)
                .putExtra(EXTRA_FILE, azan.absolutePath)
            return try {
                ContextCompat.startForegroundService(context, intent)
                true
            } catch (_: RuntimeException) {
                // Android 12+ blocks background starts when the alarm was not delivered as exact.
                false
            }
        }

        private fun stopIntent(context: Context): PendingIntent = PendingIntent.getService(
            context, 0, Intent(context, AzanAlertService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
