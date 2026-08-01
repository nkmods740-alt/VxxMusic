package com.example.data.repository

import com.example.data.local.MusicDao
import com.example.data.local.PlayHistoryEntity
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistSongCrossRef
import com.example.data.local.SongEntity
import com.example.data.model.AudioFolder
import com.example.data.model.EqualizerPreset
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MusicRepository(private val musicDao: MusicDao) {

    val allSongs: Flow<List<Song>> = musicDao.getAllSongs().map { entities ->
        entities.map { it.toSong() }
    }

    val favoriteSongs: Flow<List<Song>> = musicDao.getFavoriteSongs().map { entities ->
        entities.map { it.toSong() }
    }

    val mostPlayedSongs: Flow<List<Song>> = musicDao.getMostPlayedSongs().map { entities ->
        entities.map { it.toSong() }
    }

    val recentlyPlayedSongs: Flow<List<Song>> = musicDao.getRecentlyPlayedSongs().map { entities ->
        entities.map { it.toSong() }
    }

    val allPlaylists: Flow<List<Playlist>> = musicDao.getAllPlaylists().map { entities ->
        entities.map { Playlist(it.id, it.name, it.songCount, it.coverResId) }
    }

    suspend fun toggleFavorite(songId: Long, currentIsFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            musicDao.updateFavorite(songId, !currentIsFavorite)
        }
    }

    suspend fun recordPlayedSong(songId: Long) {
        withContext(Dispatchers.IO) {
            musicDao.incrementPlayCount(songId)
            musicDao.addPlayHistory(PlayHistoryEntity(songId = songId))
        }
    }

    suspend fun scanAndSyncDeviceAudioFiles(scanner: MediaScanner): Int = withContext(Dispatchers.IO) {
        val scanned = scanner.scanDeviceAudioFiles()
        if (scanned.isNotEmpty()) {
            musicDao.insertSongs(scanned)
        }
        scanned.size
    }

    suspend fun deleteSong(song: Song, scanner: MediaScanner): Boolean = withContext(Dispatchers.IO) {
        musicDao.deleteSong(song.id)
        musicDao.deleteSongFromAllPlaylists(song.id)
        musicDao.deleteSongFromHistory(song.id)
        scanner.deleteFileFromDevice(song.filePath)
    }

    suspend fun createPlaylist(name: String) {
        withContext(Dispatchers.IO) {
            musicDao.insertPlaylist(
                PlaylistEntity(
                    name = name,
                    songCount = 0,
                    coverResId = R.drawable.img_album_cosmic_1785497695301
                )
            )
        }
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String) {
        withContext(Dispatchers.IO) {
            musicDao.renamePlaylist(playlistId, newName)
        }
    }

    suspend fun deletePlaylist(playlistId: Long) {
        withContext(Dispatchers.IO) {
            musicDao.deletePlaylist(playlistId)
            musicDao.clearPlaylistSongs(playlistId)
        }
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        withContext(Dispatchers.IO) {
            musicDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId, songId))
        }
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        withContext(Dispatchers.IO) {
            musicDao.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> {
        return musicDao.getSongsForPlaylist(playlistId).map { list -> list.map { it.toSong() } }
    }

    suspend fun seedInitialDataIfEmpty() {
        withContext(Dispatchers.IO) {
            if (musicDao.getSongCount() > 0) return@withContext
            val initialSongs = listOf(
                SongEntity(
                    id = 1L,
                    title = "Believer",
                    artist = "Imagine Dragons",
                    album = "Evolve",
                    genre = "Alternative / Rock",
                    durationMs = 204000L,
                    albumArtResId = R.drawable.img_album_cosmic_1785497695301,
                    albumArtUri = "",
                    filePath = "/storage/emulated/0/Music/Believer.mp3",
                    fileSizeMb = 8.4f,
                    isFavorite = true,
                    isOnline = false,
                    lyrics = "First things first\nI'm sayin' all the words inside my head...",
                    playCount = 42,
                    addedTime = System.currentTimeMillis() - 86400000L * 2
                ),
                SongEntity(
                    id = 2L,
                    title = "Night Changes",
                    artist = "One Direction",
                    album = "Four",
                    genre = "Pop",
                    durationMs = 227000L,
                    albumArtResId = R.drawable.anime_night_art_1785557649722,
                    albumArtUri = "",
                    filePath = "/storage/emulated/0/Music/Night_Changes.mp3",
                    fileSizeMb = 9.1f,
                    isFavorite = true,
                    isOnline = false,
                    lyrics = "Going out tonight, changes into something red...",
                    playCount = 38,
                    addedTime = System.currentTimeMillis() - 86400000L * 5
                ),
                SongEntity(
                    id = 3L,
                    title = "Blinding Lights",
                    artist = "The Weeknd",
                    album = "After Hours",
                    genre = "Synthwave / Pop",
                    durationMs = 200000L,
                    albumArtResId = R.drawable.img_album_neon_drive_1785497728365,
                    albumArtUri = "",
                    filePath = "/storage/emulated/0/Music/Blinding_Lights.mp3",
                    fileSizeMb = 7.8f,
                    isFavorite = true,
                    isOnline = true,
                    lyrics = "I've been tryna call\nI've been on my own for long enough...",
                    playCount = 55,
                    addedTime = System.currentTimeMillis() - 86400000L * 1
                ),
                SongEntity(
                    id = 4L,
                    title = "Shape of You",
                    artist = "Ed Sheeran",
                    album = "÷ (Divide)",
                    genre = "Pop",
                    durationMs = 233000L,
                    albumArtResId = R.drawable.img_app_icon_1785497679870,
                    albumArtUri = "",
                    filePath = "/storage/emulated/0/Music/Shape_Of_You.mp3",
                    fileSizeMb = 9.5f,
                    isFavorite = true,
                    isOnline = false,
                    lyrics = "The club isn't the best place to find a lover...",
                    playCount = 29,
                    addedTime = System.currentTimeMillis() - 86400000L * 10
                ),
                SongEntity(
                    id = 5L,
                    title = "Someone You Loved",
                    artist = "Lewis Capaldi",
                    album = "Divinely Uninspired",
                    genre = "Acoustic / Pop",
                    durationMs = 182000L,
                    albumArtResId = R.drawable.anime_night_art_1785557649722,
                    albumArtUri = "",
                    filePath = "/storage/emulated/0/Music/Someone_You_Loved.mp3",
                    fileSizeMb = 7.2f,
                    isFavorite = false,
                    isOnline = true,
                    lyrics = "I'm going under and this time I fear there's no one to save me...",
                    playCount = 19,
                    addedTime = System.currentTimeMillis() - 86400000L * 12
                )
            )

            musicDao.insertSongs(initialSongs)

            val initialPlaylists = listOf(
                PlaylistEntity(1L, "Favorites", 4, R.drawable.img_album_cosmic_1785497695301),
                PlaylistEntity(2L, "Workout Beats", 2, R.drawable.img_album_neon_drive_1785497728365),
                PlaylistEntity(3L, "Night Vibes", 3, R.drawable.anime_night_art_1785557649722)
            )

            for (pl in initialPlaylists) {
                musicDao.insertPlaylist(pl)
            }

            musicDao.addPlayHistory(PlayHistoryEntity(songId = 3L))
            musicDao.addPlayHistory(PlayHistoryEntity(songId = 1L))
            musicDao.addPlayHistory(PlayHistoryEntity(songId = 2L))
        }
    }

    fun getAudioFolders(): List<AudioFolder> = listOf(
        AudioFolder("Music", "/storage/emulated/0/Music", 523),
        AudioFolder("Download", "/storage/emulated/0/Download", 120),
        AudioFolder("Records", "/storage/emulated/0/Records", 36),
        AudioFolder("Audiobooks", "/storage/emulated/0/Audiobooks", 19),
        AudioFolder("WhatsApp Audio", "/storage/emulated/0/WhatsApp/Media/WhatsApp Audio", 80),
        AudioFolder("Telegram Audio", "/storage/emulated/0/Telegram/Audio", 42)
    )

    fun getEqualizerPresets(): List<EqualizerPreset> = listOf(
        EqualizerPreset("Rock", listOf(5f, 3f, -1f, -3f, 1f, 4f, 6f, 7f, 5f, 3f)),
        EqualizerPreset("Pop", listOf(-1f, 2f, 4f, 5f, 3f, -1f, -2f, -1f, 2f, 4f)),
        EqualizerPreset("Hip Hop", listOf(6f, 5f, 2f, 0f, -2f, -1f, 2f, 1f, 4f, 5f)),
        EqualizerPreset("Jazz", listOf(4f, 3f, 1f, 2f, -2f, -2f, 0f, 2f, 3f, 4f)),
        EqualizerPreset("Flat", listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)),
        EqualizerPreset("Dance", listOf(6f, 7f, 4f, 0f, 2f, 4f, 6f, 5f, 3f, 0f)),
        EqualizerPreset("Acoustic", listOf(3f, 3f, 2f, 1f, 2f, 2f, 4f, 4f, 3f, 2f)),
        EqualizerPreset("Electronic", listOf(5f, 5f, 2f, 0f, -2f, 3f, 2f, 4f, 5f, 6f))
    )
}

fun SongEntity.toSong() = Song(
    id = id,
    title = title,
    artist = artist,
    album = album,
    genre = genre,
    durationMs = durationMs,
    albumArtResId = when (id) {
        1L -> R.drawable.img_album_cosmic_1785497695301
        2L -> R.drawable.anime_night_art_1785557649722
        3L -> R.drawable.img_album_neon_drive_1785497728365
        4L -> R.drawable.img_album_anime_night_1785497710314
        5L -> R.drawable.anime_night_art_1785557649722
        else -> if (albumArtResId != 0) albumArtResId else R.drawable.anime_night_art_1785557649722
    },
    albumArtUri = albumArtUri,
    filePath = filePath,
    fileSizeMb = fileSizeMb,
    isFavorite = isFavorite,
    isOnline = isOnline,
    lyrics = lyrics,
    playCount = playCount,
    addedTime = addedTime
)

fun PlaylistEntity.toPlaylist() = Playlist(
    id = id,
    name = name,
    songCount = songCount,
    coverResId = when (id) {
        1L -> R.drawable.img_album_cosmic_1785497695301
        2L -> R.drawable.img_album_neon_drive_1785497728365
        3L -> R.drawable.anime_night_art_1785557649722
        else -> if (coverResId != 0) coverResId else R.drawable.anime_night_art_1785557649722
    }
)
