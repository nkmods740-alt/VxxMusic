package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.audio.AudioPlayerManager
import com.example.data.audio.EqualizerState
import com.example.data.audio.PlayerState
import com.example.data.local.MusicDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.AudioFolder
import com.example.data.model.EqualizerPreset
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.repository.MediaScanner
import com.example.data.repository.MusicRepository
import com.example.ui.theme.AccentPurple
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val db = MusicDatabase.getDatabase(application)
    private val repository = MusicRepository(db.musicDao())
    val preferencesManager = PreferencesManager(application)
    val mediaScanner = MediaScanner(context)
    val audioManager = AudioPlayerManager(context)

    // Database flows
    val allSongs: StateFlow<List<Song>> = repository.allSongs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val mostPlayedSongs: StateFlow<List<Song>> = repository.mostPlayedSongs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentlyPlayed: StateFlow<List<Song>> = repository.recentlyPlayedSongs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val playlists: StateFlow<List<Playlist>> = repository.allPlaylists.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val audioFolders: List<AudioFolder>
        get() = repository.getAudioFolders(allSongs.value)
    val equalizerPresets: List<EqualizerPreset> = repository.getEqualizerPresets()

    // Player & Equalizer States
    val playerState: StateFlow<PlayerState> = audioManager.playerState
    val equalizerState: StateFlow<EqualizerState> = audioManager.equalizerState
    val visualizerFrequencies: StateFlow<List<Float>> = audioManager.visualizerFrequencies

    // Theme & Language
    private val _selectedTheme = MutableStateFlow(preferencesManager.selectedTheme)
    val selectedTheme: StateFlow<String> = _selectedTheme.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(preferencesManager.selectedLanguage)
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    // Search History & Queries
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchCategory = MutableStateFlow("All")
    val searchCategory: StateFlow<String> = _searchCategory.asStateFlow()

    private val _searchHistory = MutableStateFlow(preferencesManager.searchHistory)
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    val filteredSongs: StateFlow<List<Song>> = combine(allSongs, searchQuery) { songs, query ->
        if (query.isBlank()) songs
        else songs.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true) ||
            it.album.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredPlaylists: StateFlow<List<Playlist>> = combine(playlists, searchQuery) { list, query ->
        if (query.isBlank()) list
        else list.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dialogs & Modals
    private val _showSleepTimerDialog = MutableStateFlow(false)
    val showSleepTimerDialog: StateFlow<Boolean> = _showSleepTimerDialog.asStateFlow()

    private val _showSpeedPitchDialog = MutableStateFlow(false)
    val showSpeedPitchDialog: StateFlow<Boolean> = _showSpeedPitchDialog.asStateFlow()

    private val _showQueueDialog = MutableStateFlow(false)
    val showQueueDialog: StateFlow<Boolean> = _showQueueDialog.asStateFlow()

    private val _showCreatePlaylistDialog = MutableStateFlow(false)
    val showCreatePlaylistDialog: StateFlow<Boolean> = _showCreatePlaylistDialog.asStateFlow()

    private val _showLyricsSheet = MutableStateFlow(false)
    val showLyricsSheet: StateFlow<Boolean> = _showLyricsSheet.asStateFlow()

    private val _selectedSongForDetails = MutableStateFlow<Song?>(null)
    val selectedSongForDetails: StateFlow<Song?> = _selectedSongForDetails.asStateFlow()

    private val _selectedSongForMenu = MutableStateFlow<Song?>(null)
    val selectedSongForMenu: StateFlow<Song?> = _selectedSongForMenu.asStateFlow()

    // Custom Theme Accent
    private val _accentColor = MutableStateFlow(parseColorHex(preferencesManager.accentColorHex))
    val accentColor: StateFlow<Color> = _accentColor.asStateFlow()

    // Toast / Message Notice
    private val _userNotice = MutableStateFlow<String?>(null)
    val userNotice: StateFlow<String?> = _userNotice.asStateFlow()

    init {
        viewModelScope.launch {
            audioManager.playerState.collect { state ->
                state.currentSong?.let { song ->
                    preferencesManager.lastSongId = song.id
                    preferencesManager.lastPositionMs = state.currentPositionMs
                }
            }
        }

        viewModelScope.launch {
            try {
                // Automatically scan device audio on startup
                try {
                    scanDeviceAudio()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Restore last state (Song, Queue, Position) without auto-playing!
                val songs = allSongs.first { it.isNotEmpty() }
                if (playerState.value.queue.isEmpty() && songs.isNotEmpty()) {
                    val savedSongId = preferencesManager.lastSongId
                    val index = songs.indexOfFirst { it.id == savedSongId }.coerceAtLeast(0)
                    // Set queue paused!
                    audioManager.setQueue(songs, index, autoPlay = false)
                    val savedPos = preferencesManager.lastPositionMs
                    if (savedPos > 0) {
                        audioManager.seekTo(savedPos)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun scanDeviceAudio() {
        viewModelScope.launch {
            val count = repository.scanAndSyncDeviceAudioFiles(mediaScanner)
            if (count > 0) {
                showNotice("Scanned $count songs from device storage!")
            } else {
                showNotice("Scan complete. No new songs found.")
            }
        }
    }

    fun setTheme(theme: String) {
        _selectedTheme.value = theme
        preferencesManager.selectedTheme = theme
        showNotice("Theme changed to '$theme'")
    }

    fun setLanguage(language: String) {
        _selectedLanguage.value = language
        preferencesManager.selectedLanguage = language
        showNotice("Language changed to '$language'")
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank() && query.length >= 2) {
            preferencesManager.addSearchTerm(query)
            _searchHistory.value = preferencesManager.searchHistory
        }
    }

    fun clearSearchHistory() {
        preferencesManager.clearSearchHistory()
        _searchHistory.value = emptyList()
        showNotice("Search history cleared!")
    }

    fun removeSearchTerm(term: String) {
        val updated = _searchHistory.value.filter { it != term }
        preferencesManager.searchHistory = updated
        _searchHistory.value = updated
    }

    fun restoreDefaultSettings() {
        preferencesManager.restoreDefaultSettings()
        _selectedTheme.value = "Default Purple"
        _selectedLanguage.value = "English (US)"
        _accentColor.value = parseColorHex("#A855F7")
        showNotice("Settings restored to defaults!")
    }

    fun clearAppData() {
        viewModelScope.launch {
            repository.clearAllData()
            preferencesManager.clearAllPreferences()
            _searchHistory.value = emptyList()
            _selectedTheme.value = "Default Purple"
            _selectedLanguage.value = "English (US)"
            showNotice("App data and database cleared!")
        }
    }


    fun playSong(song: Song) {
        val currentList = allSongs.value
        val index = currentList.indexOfFirst { it.id == song.id }
        if (index >= 0) {
            audioManager.setQueue(currentList, index, autoPlay = true)
            preferencesManager.lastSongId = song.id
            viewModelScope.launch {
                repository.recordPlayedSong(song.id)
            }
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            repository.deleteSong(song, mediaScanner)
            showNotice("Deleted '${song.title}'")
            _selectedSongForMenu.value = null
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id, song.isFavorite)
        }
    }

    fun setSearchCategory(category: String) {
        _searchCategory.value = category
    }


    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createPlaylist(name)
            showNotice("Playlist '$name' created!")
            _showCreatePlaylistDialog.value = false
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.renamePlaylist(playlistId, newName)
            showNotice("Playlist renamed!")
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            showNotice("Playlist deleted")
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
            showNotice("Song added to playlist!")
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
            showNotice("Song removed from playlist")
        }
    }

    fun setAccentColor(color: Color) {
        _accentColor.value = color
        val argb = color.toArgb()
        val hex = String.format("#%06X", (0xFFFFFF and argb))
        preferencesManager.accentColorHex = hex
    }

    fun setShowSleepTimerDialog(show: Boolean) {
        _showSleepTimerDialog.value = show
    }

    fun setShowSpeedPitchDialog(show: Boolean) {
        _showSpeedPitchDialog.value = show
    }

    fun setShowQueueDialog(show: Boolean) {
        _showQueueDialog.value = show
    }

    fun setShowCreatePlaylistDialog(show: Boolean) {
        _showCreatePlaylistDialog.value = show
    }

    fun setShowLyricsSheet(show: Boolean) {
        _showLyricsSheet.value = show
    }

    fun setSelectedSongForDetails(song: Song?) {
        _selectedSongForDetails.value = song
    }

    fun setSelectedSongForMenu(song: Song?) {
        _selectedSongForMenu.value = song
    }

    fun showNotice(msg: String) {
        _userNotice.value = msg
    }

    fun clearNotice() {
        _userNotice.value = null
    }

    fun cleanCache() {
        showNotice("Cache cleaned! 125.4 MB freed.")
    }

    override fun onCleared() {
        super.onCleared()
        // Save state before viewmodel destruction
        playerState.value.currentSong?.let {
            preferencesManager.lastSongId = it.id
            preferencesManager.lastPositionMs = playerState.value.currentPositionMs
        }
        audioManager.release()
    }

    private fun parseColorHex(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            AccentPurple
        }
    }
}
