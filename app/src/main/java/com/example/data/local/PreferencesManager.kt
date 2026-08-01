package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vxx_music_prefs", Context.MODE_PRIVATE)

    var lastTab: String
        get() = prefs.getString("KEY_LAST_TAB", "HOME") ?: "HOME"
        set(value) = prefs.edit().putString("KEY_LAST_TAB", value).apply()

    var lastSongId: Long
        get() = prefs.getLong("KEY_LAST_SONG_ID", -1L)
        set(value) = prefs.edit().putLong("KEY_LAST_SONG_ID", value).apply()

    var lastPositionMs: Long
        get() = prefs.getLong("KEY_LAST_POSITION_MS", 0L)
        set(value) = prefs.edit().putLong("KEY_LAST_POSITION_MS", value).apply()

    var accentColorHex: String
        get() = prefs.getString("KEY_ACCENT_COLOR", "#A855F7") ?: "#A855F7"
        set(value) = prefs.edit().putString("KEY_ACCENT_COLOR", value).apply()

    var isEqualizerEnabled: Boolean
        get() = prefs.getBoolean("KEY_EQ_ENABLED", true)
        set(value) = prefs.edit().putBoolean("KEY_EQ_ENABLED", value).apply()

    var equalizerPreset: String
        get() = prefs.getString("KEY_EQ_PRESET", "Rock") ?: "Rock"
        set(value) = prefs.edit().putString("KEY_EQ_PRESET", value).apply()

    var bassBoost: Float
        get() = prefs.getFloat("KEY_BASS_BOOST", 7f)
        set(value) = prefs.edit().putFloat("KEY_BASS_BOOST", value).apply()

    var surroundSound: Float
        get() = prefs.getFloat("KEY_SURROUND", 6f)
        set(value) = prefs.edit().putFloat("KEY_SURROUND", value).apply()

    var loudnessEnhancer: Boolean
        get() = prefs.getBoolean("KEY_LOUDNESS", true)
        set(value) = prefs.edit().putBoolean("KEY_LOUDNESS", value).apply()

    fun saveBandGains(gains: List<Float>) {
        val str = gains.joinToString(",") { it.toString() }
        prefs.edit().putString("KEY_BAND_GAINS", str).apply()
    }

    fun getBandGains(): List<Float>? {
        val str = prefs.getString("KEY_BAND_GAINS", null) ?: return null
        return try {
            str.split(",").map { it.toFloat() }
        } catch (e: Exception) {
            null
        }
    }
}
