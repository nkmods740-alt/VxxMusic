package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.service.AudioPlayerService

class VxxMusicWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidgetUI(context, appWidgetManager, appWidgetId, "Vxx Music", "Select a song to play", false)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_PLAY_PAUSE -> sendServiceCommand(context, AudioPlayerService.ACTION_PLAY_PAUSE)
            ACTION_NEXT -> sendServiceCommand(context, AudioPlayerService.ACTION_NEXT)
            ACTION_PREVIOUS -> sendServiceCommand(context, AudioPlayerService.ACTION_PREVIOUS)
        }
    }

    private fun sendServiceCommand(context: Context, serviceAction: String) {
        val serviceIntent = Intent(context, AudioPlayerService::class.java).apply {
            action = serviceAction
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "com.example.vxxmusic.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.vxxmusic.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.vxxmusic.ACTION_PREVIOUS"

        fun updateAllWidgets(
            context: Context,
            title: String,
            artist: String,
            isPlaying: Boolean
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, VxxMusicWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            for (appWidgetId in appWidgetIds) {
                updateWidgetUI(context, appWidgetManager, appWidgetId, title, artist, isPlaying)
            }
        }

        private fun updateWidgetUI(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            title: String,
            artist: String,
            isPlaying: Boolean
        ) {
            val views = RemoteViews(context.packageName, R.layout.vxx_music_widget)

            views.setTextViewText(R.id.widget_title, if (title.isBlank()) "Vxx Music" else title)
            views.setTextViewText(R.id.widget_artist, if (artist.isBlank()) "Select a song" else artist)

            val playIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            views.setImageViewResource(R.id.widget_btn_play_pause, playIcon)

            // Open main activity when tapping layout
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(
                context, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

            // Media Controls PendingIntents
            val playPauseIntent = Intent(context, VxxMusicWidgetProvider::class.java).apply { action = ACTION_PLAY_PAUSE }
            val playPausePendingIntent = PendingIntent.getBroadcast(
                context, 1, playPauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_play_pause, playPausePendingIntent)

            val nextIntent = Intent(context, VxxMusicWidgetProvider::class.java).apply { action = ACTION_NEXT }
            val nextPendingIntent = PendingIntent.getBroadcast(
                context, 2, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_next, nextPendingIntent)

            val prevIntent = Intent(context, VxxMusicWidgetProvider::class.java).apply { action = ACTION_PREVIOUS }
            val prevPendingIntent = PendingIntent.getBroadcast(
                context, 3, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_prev, prevPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
