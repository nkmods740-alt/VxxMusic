package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.audio.AudioPlayerManager
import com.example.data.model.Song
import com.example.widget.VxxMusicWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AudioPlayerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaSession: MediaSessionCompat? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupMediaSession()
        observePlayerState()
    }

    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(this, "VxxMusicMediaSession").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    val manager = activeAudioPlayerManager
                    if (manager?.playerState?.value?.isPlaying == false) {
                        manager.playPause()
                    }
                }
                override fun onPause() { activeAudioPlayerManager?.pause() }
                override fun onSkipToNext() { activeAudioPlayerManager?.nextSong() }
                override fun onSkipToPrevious() { activeAudioPlayerManager?.previousSong() }
                override fun onStop() { activeAudioPlayerManager?.pause() }
                override fun onSeekTo(pos: Long) { activeAudioPlayerManager?.seekTo(pos) }
            })
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val playerManager = activeAudioPlayerManager

        when (action) {
            ACTION_PLAY_PAUSE -> playerManager?.playPause()
            ACTION_NEXT -> playerManager?.nextSong()
            ACTION_PREVIOUS -> playerManager?.previousSong()
            ACTION_STOP -> {
                playerManager?.pause()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(NOTIFICATION_ID)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val state = playerManager?.playerState?.value
        if (state?.currentSong != null) {
            updateMediaSessionState(state.currentSong, state.isPlaying, state.currentPositionMs)
            val notification = buildNotification(state.currentSong, state.isPlaying)
            startForeground(NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    private fun observePlayerState() {
        val playerManager = activeAudioPlayerManager ?: return
        playerManager.playerState.onEach { state ->
            val currentSong = state.currentSong
            updateMediaSessionState(currentSong, state.isPlaying, state.currentPositionMs)

            if (currentSong != null && state.isPlaying) {
                val notification = buildNotification(currentSong, state.isPlaying)
                startForeground(NOTIFICATION_ID, notification)
            } else if (currentSong != null) {
                val notification = buildNotification(currentSong, false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_DETACH)
                }
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)
            }

            // Update Home Screen Widget
            VxxMusicWidgetProvider.updateAllWidgets(
                context = applicationContext,
                title = currentSong?.title ?: "Vxx Music",
                artist = currentSong?.artist ?: "Select a song",
                isPlaying = state.isPlaying
            )
        }.launchIn(serviceScope)
    }

    private fun updateMediaSessionState(song: Song?, isPlaying: Boolean, currentPosMs: Long) {
        val session = mediaSession ?: return

        val stateInt = if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SEEK_TO or
                PlaybackStateCompat.ACTION_STOP
            )
            .setState(stateInt, currentPosMs, 1.0f)
            .build()
        session.setPlaybackState(playbackState)

        if (song != null) {
            val albumArtBitmap = loadAlbumArtBitmap(song)
            val displayArtist = if (song.artist.isBlank() || song.artist.contains("<unknown>", ignoreCase = true)) "<unknown>" else song.artist
            val metadataBuilder = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, displayArtist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.durationMs)

            if (albumArtBitmap != null) {
                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArtBitmap)
                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, albumArtBitmap)
            }

            session.setMetadata(metadataBuilder.build())
        }
    }

    private fun loadAlbumArtBitmap(song: Song?): Bitmap? {
        if (song == null) return null
        try {
            if (song.albumArtUri.isNotBlank()) {
                val uri = Uri.parse(song.albumArtUri)
                contentResolver.openInputStream(uri)?.use { stream ->
                    return BitmapFactory.decodeStream(stream)
                }
            }
            if (song.albumArtResId != 0) {
                return BitmapFactory.decodeResource(resources, song.albumArtResId)
            }
            if (song.filePath.isNotBlank()) {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(song.filePath)
                val artBytes = retriever.embeddedPicture
                retriever.release()
                if (artBytes != null) {
                    return BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return try {
            BitmapFactory.decodeResource(resources, R.drawable.vxx_app_logo_1785609096055)
        } catch (e: Exception) {
            null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Vxx Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active music playback notification with media controls"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(song: Song?, isPlaying: Boolean): Notification {
        val title = song?.title ?: "Vxx Music"
        val artist = if (song == null || song.artist.isBlank() || song.artist.contains("<unknown>", ignoreCase = true)) "<unknown>" else song.artist
        val albumArtBitmap = loadAlbumArtBitmap(song)

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = Intent(this, AudioPlayerService::class.java).apply { action = ACTION_PREVIOUS }
        val prevPendingIntent = PendingIntent.getService(
            this, 1, prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = Intent(this, AudioPlayerService::class.java).apply { action = ACTION_PLAY_PAUSE }
        val playPausePendingIntent = PendingIntent.getService(
            this, 2, playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = Intent(this, AudioPlayerService::class.java).apply { action = ACTION_NEXT }
        val nextPendingIntent = PendingIntent.getService(
            this, 3, nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AudioPlayerService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this, 4, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

        val mediaStyle = MediaNotificationCompat.MediaStyle()
            .setShowActionsInCompactView(0, 1, 2)
            .setShowCancelButton(true)
            .setCancelButtonIntent(stopPendingIntent)

        mediaSession?.sessionToken?.let { token ->
            mediaStyle.setMediaSession(token)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText(song?.album ?: "Vxx Music")
            .setLargeIcon(albumArtBitmap)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
            .addAction(playIcon, if (isPlaying) "Pause" else "Play", playPausePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
            .setStyle(mediaStyle)

        return builder.build()
    }

    override fun onDestroy() {
        mediaSession?.run {
            isActive = false
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "vxx_music_playback_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY_PAUSE = "com.example.service.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.service.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.service.ACTION_PREVIOUS"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"

        var activeAudioPlayerManager: AudioPlayerManager? = null

        fun startService(context: Context) {
            val intent = Intent(context, AudioPlayerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
