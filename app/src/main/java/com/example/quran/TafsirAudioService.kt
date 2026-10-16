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
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import android.media.session.MediaSession
import com.example.naat.createPlaybackSession
import com.example.naat.publish
import com.example.MainActivity
import com.example.R
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.persistedAppLanguage

class TafsirAudioService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        session = createPlaybackSession(
            this, "tafsir",
            isPlaying = { TafsirAudioManager.state.value.isPlaying },
            onToggle = TafsirAudioManager::toggle,
            onNext = TafsirAudioManager::next,
            onPrevious = TafsirAudioManager::previous,
            onStop = TafsirAudioManager::stop
        )
    }

    override fun onDestroy() {
        session?.release()
        session = null
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE -> TafsirAudioManager.toggle()
            ACTION_NEXT -> TafsirAudioManager.next()
            ACTION_PREVIOUS -> TafsirAudioManager.previous()
            ACTION_STOP -> { TafsirAudioManager.stop(); return START_NOT_STICKY }
        }
        val notification = buildNotification(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else startForeground(ID, notification)
        // Restarted after the process died, or a stale notification tap: nothing is loaded, so
        // leave instead of showing an empty player.
        if (TafsirAudioManager.state.value.playingId == null) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    companion object {
        @Volatile private var session: MediaSession? = null
        private const val CHANNEL = "tafsir_audio_playback"
        private const val ID = 4102
        private const val ACTION_TOGGLE = "com.example.quran.TAFSIR_TOGGLE"
        private const val ACTION_NEXT = "com.example.quran.TAFSIR_NEXT"
        private const val ACTION_PREVIOUS = "com.example.quran.TAFSIR_PREVIOUS"
        private const val ACTION_STOP = "com.example.quran.TAFSIR_STOP"

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, TafsirAudioService::class.java))
        }

        fun update(context: Context?) {
            if (context == null || TafsirAudioManager.state.value.playingId == null) return
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(ID, buildNotification(context))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TafsirAudioService::class.java))
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(ID)
        }

        private fun command(context: Context, action: String) = PendingIntent.getService(
            context, action.hashCode(), Intent(context, TafsirAudioService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        private fun buildNotification(context: Context): Notification {
            val language = persistedAppLanguage(context)
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) manager.createNotificationChannel(
                NotificationChannel(CHANNEL, localizeUiText("Tafseer", language), NotificationManager.IMPORTANCE_LOW)
            )
            val state = TafsirAudioManager.state.value
            val track = TafsirAudioManager.track(state.playingId)
            val open = PendingIntent.getActivity(context, 0,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val title = track?.let { if (it.surah > 0) "${it.surah}. ${QuranAudioManager.surahName(it.surah)}" else localizeUiText(it.title, language) }
                ?: localizeUiText("Tafseer", language)
            val collection = localizeUiText(track?.collection ?: "Tafseer", language)
            session?.publish(state.isPlaying, title, collection)
            return NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText("$collection • Dawat-e-Islami")
                .setContentIntent(open)
                .setOngoing(state.isPlaying)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .addAction(android.R.drawable.ic_media_previous, localizeUiText("Previous", language), command(context, ACTION_PREVIOUS))
                .addAction(if (state.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                    localizeUiText(if (state.isPlaying) "Pause" else "Play", language), command(context, ACTION_TOGGLE))
                .addAction(android.R.drawable.ic_media_next, localizeUiText("Next", language), command(context, ACTION_NEXT))
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, localizeUiText("Stop", language), command(context, ACTION_STOP))
                .build()
        }
    }
}
