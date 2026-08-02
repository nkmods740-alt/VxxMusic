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
        // No fake or demo data. App operates strictly on user scanned device audio.
    }

    suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            musicDao.deleteAllSongs()
            musicDao.deleteAllPlaylists()
            musicDao.deleteAllPlaylistSongs()
            musicDao.clearRecentHistory()
        }
    }


    fun getAudioFolders(songs: List<Song>): List<AudioFolder> {
        return songs.groupBy { song ->
            val file = java.io.File(song.filePath)
            if (file.parentFile != null) file.parentFile!!.absolutePath else "/storage/emulated/0/Music"
        }.map { (dirPath, songList) ->
            val folderName = java.io.File(dirPath).name.ifBlank { "Music" }
            AudioFolder(
                name = folderName,
                path = dirPath,
                songCount = songList.size
            )
        }.sortedBy { it.name }
    }

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
    albumArtResId = albumArtResId,
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
    coverResId = coverResId
)

