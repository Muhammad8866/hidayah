package com.example.naat

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.persistedAppLanguage

/** Keeps Naat playback alive in the background, like [com.example.quran.QuranAudioService]. */
class NaatAudioService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        session = createPlaybackSession(
            this, "naat",
            isPlaying = { NaatAudioManager.state.value.playing },
            onToggle = NaatAudioManager::togglePlayPause,
            onNext = NaatAudioManager::next,
            onPrevious = NaatAudioManager::previous,
            onStop = NaatAudioManager::stop
        )
    }

    override fun onDestroy() {
        session?.release()
        session = null
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE -> NaatAudioManager.togglePlayPause()
            ACTION_NEXT -> NaatAudioManager.next()
            ACTION_PREVIOUS -> NaatAudioManager.previous()
            ACTION_STOP -> { NaatAudioManager.stop(); return START_NOT_STICKY }
        }
        val notification = buildNotification(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else startForeground(ID, notification)
        if (NaatAudioManager.state.value.activeId == null) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    companion object {
        private const val CHANNEL = "naat_audio_playback"
        private const val ID = 4103
        private const val ACTION_TOGGLE = "com.example.naat.TOGGLE"
        private const val ACTION_NEXT = "com.example.naat.NEXT"
        private const val ACTION_PREVIOUS = "com.example.naat.PREVIOUS"
        private const val ACTION_STOP = "com.example.naat.STOP"
        @Volatile private var session: MediaSession? = null

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, NaatAudioService::class.java))
        }

        fun update(context: Context) {
            if (NaatAudioManager.state.value.activeId == null) return
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(ID, buildNotification(context))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, NaatAudioService::class.java))
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(ID)
        }

        private fun command(context: Context, action: String) = PendingIntent.getService(
            context, action.hashCode(), Intent(context, NaatAudioService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        private fun buildNotification(context: Context): Notification {
            val language = persistedAppLanguage(context)
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) manager.createNotificationChannel(
                NotificationChannel(CHANNEL, localizeUiText("Naat playback", language), NotificationManager.IMPORTANCE_LOW)
                    .apply { setShowBadge(false) }
            )
            val playing = NaatAudioManager.state.value.playing
            val title = NaatAudioManager.activeTitle() ?: localizeUiText("Naat", language)
            session?.publish(playing, title, localizeUiText("Naat", language))
            val open = PendingIntent.getActivity(context, 0,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            return NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(localizeUiText("Naat", language))
                .setContentIntent(open)
                .setOngoing(playing)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .addAction(android.R.drawable.ic_media_previous, localizeUiText("Previous", language), command(context, ACTION_PREVIOUS))
                .addAction(if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                    localizeUiText(if (playing) "Pause" else "Play", language), command(context, ACTION_TOGGLE))
                .addAction(android.R.drawable.ic_media_next, localizeUiText("Next", language), command(context, ACTION_NEXT))
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, localizeUiText("Stop", language), command(context, ACTION_STOP))
                .build()
        }
    }
}

/**
 * Framework media session so headset buttons, Bluetooth, car and lock-screen controls reach the
 * player. Shared by the Quran, Tafseer and Naat playback services.
 */
internal fun createPlaybackSession(
    context: Context,
    tag: String,
    isPlaying: () -> Boolean,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onStop: () -> Unit
): MediaSession = MediaSession(context, tag).apply {
    setCallback(object : MediaSession.Callback() {
        override fun onPlay() { if (!isPlaying()) onToggle() }
        override fun onPause() { if (isPlaying()) onToggle() }
        override fun onSkipToNext() = onNext()
        override fun onSkipToPrevious() = onPrevious()
        override fun onStop() = onStop()
    })
    isActive = true
}

internal fun MediaSession.publish(playing: Boolean, title: String, subtitle: String) {
    setPlaybackState(
        PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_PLAY_PAUSE or
                    PlaybackState.ACTION_SKIP_TO_NEXT or PlaybackState.ACTION_SKIP_TO_PREVIOUS or PlaybackState.ACTION_STOP
            )
            .setState(
                if (playing) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED,
                PlaybackState.PLAYBACK_POSITION_UNKNOWN, if (playing) 1f else 0f
            )
            .build()
    )
    setMetadata(
        MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, subtitle)
            .build()
    )
}
