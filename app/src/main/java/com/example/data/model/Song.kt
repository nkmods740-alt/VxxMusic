package com.example.data.model

import androidx.annotation.DrawableRes

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String = "Pop",
    val durationMs: Long = 204000L, // 3:24
    @DrawableRes val albumArtResId: Int = 0,
    val albumArtUri: String = "",
    val filePath: String = "/storage/emulated/0/Music/song.mp3",
    val fileSizeMb: Float = 4.2f,
    val isFavorite: Boolean = false,
    val isOnline: Boolean = false,
    val lyrics: String = "I was broken from a young age\nTaking my sulking to the masses\nWriting my poems for the few\nThat look at me, took to me, shook to me, feeling me\nSinging from heartache from the pain\nTaking my message from the veins\nSpeaking my lesson from the brain\nSeeing the beauty through the...",
    val playCount: Int = 0,
    val addedTime: Long = System.currentTimeMillis()
) {
    val durationFormatted: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }
}

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int,
    @DrawableRes val coverResId: Int = 0
)

data class AudioFolder(
    val name: String,
    val path: String,
    val songCount: Int
)

data class EqualizerPreset(
    val name: String,
    val bandGains: List<Float> // e.g. 10 bands gains in dB (-12.0 to +12.0)
)
