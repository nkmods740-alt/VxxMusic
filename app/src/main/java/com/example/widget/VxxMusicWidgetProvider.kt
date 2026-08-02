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

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val minWidth = newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 0
        val minHeight = newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 0
        
        if (minWidth > 0) {
            val autoSize = when {
                minWidth < 140 -> "Compact (Small 2x1)"
                minWidth in 140..250 -> "Standard (Medium 3x2)"
                minWidth in 251..350 -> "Expanded (Large 4x2)"
                else -> "Ultra (Full 4x3)"
            }
            setWidgetSize(context, autoSize)
        }
        updateWidgetUI(context, appWidgetManager, appWidgetId, "Vxx Music", "Select a song to play", false)
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

        const val PREFS_NAME = "vxx_widget_prefs"
        const val KEY_WIDGET_SIZE = "widget_size_mode"

        fun getWidgetSize(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_WIDGET_SIZE, "Standard (Medium)") ?: "Standard (Medium)"
        }

        fun setWidgetSize(context: Context, size: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_WIDGET_SIZE, size).apply()
        }

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

            val widgetSize = getWidgetSize(context)
            when {
                widgetSize.contains("Compact") || widgetSize.contains("Small") -> {
                    views.setTextViewTextSize(R.id.widget_title, android.util.TypedValue.COMPLEX_UNIT_SP, 12f)
                    views.setTextViewTextSize(R.id.widget_artist, android.util.TypedValue.COMPLEX_UNIT_SP, 10f)
                    views.setViewPadding(R.id.widget_root, 6, 6, 6, 6)
                    views.setViewVisibility(R.id.widget_btn_prev, android.view.View.GONE)
                    views.setViewVisibility(R.id.widget_btn_next, android.view.View.GONE)
                }
                widgetSize.contains("Expanded") || widgetSize.contains("Large") -> {
                    views.setTextViewTextSize(R.id.widget_title, android.util.TypedValue.COMPLEX_UNIT_SP, 16f)
                    views.setTextViewTextSize(R.id.widget_artist, android.util.TypedValue.COMPLEX_UNIT_SP, 13f)
                    views.setViewPadding(R.id.widget_root, 16, 16, 16, 16)
                    views.setViewVisibility(R.id.widget_btn_prev, android.view.View.VISIBLE)
                    views.setViewVisibility(R.id.widget_btn_next, android.view.View.VISIBLE)
                }
                widgetSize.contains("Ultra") || widgetSize.contains("Full") -> {
                    views.setTextViewTextSize(R.id.widget_title, android.util.TypedValue.COMPLEX_UNIT_SP, 18f)
                    views.setTextViewTextSize(R.id.widget_artist, android.util.TypedValue.COMPLEX_UNIT_SP, 14f)
                    views.setViewPadding(R.id.widget_root, 22, 22, 22, 22)
                    views.setViewVisibility(R.id.widget_btn_prev, android.view.View.VISIBLE)
                    views.setViewVisibility(R.id.widget_btn_next, android.view.View.VISIBLE)
                }
                else -> { // Standard (Medium)
                    views.setTextViewTextSize(R.id.widget_title, android.util.TypedValue.COMPLEX_UNIT_SP, 14f)
                    views.setTextViewTextSize(R.id.widget_artist, android.util.TypedValue.COMPLEX_UNIT_SP, 12f)
                    views.setViewPadding(R.id.widget_root, 12, 12, 12, 12)
                    views.setViewVisibility(R.id.widget_btn_prev, android.view.View.VISIBLE)
                    views.setViewVisibility(R.id.widget_btn_next, android.view.View.VISIBLE)
                }
            }

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
