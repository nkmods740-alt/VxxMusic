package com.example.ui.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.MusicPlayerTheme
import com.example.ui.viewmodel.MusicViewModel

@Composable
fun AppNavigation(viewModel: MusicViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    val accentColor by viewModel.accentColor.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()

    MusicPlayerTheme(themeName = selectedTheme, accentColor = accentColor) {
        val playerState by viewModel.playerState.collectAsState()
        val equalizerState by viewModel.equalizerState.collectAsState()
        val visualizerFrequencies by viewModel.visualizerFrequencies.collectAsState()

        val allSongs by viewModel.allSongs.collectAsState()
        val favoriteSongs by viewModel.favoriteSongs.collectAsState()
        val mostPlayedSongs by viewModel.mostPlayedSongs.collectAsState()
        val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
        val playlists by viewModel.playlists.collectAsState()
        val filteredSongs by viewModel.filteredSongs.collectAsState()
        val filteredPlaylists by viewModel.filteredPlaylists.collectAsState()
        val searchQuery by viewModel.searchQuery.collectAsState()
        val searchCategory by viewModel.searchCategory.collectAsState()


        val showSleepTimerDialog by viewModel.showSleepTimerDialog.collectAsState()
        val showSpeedPitchDialog by viewModel.showSpeedPitchDialog.collectAsState()
        val showQueueDialog by viewModel.showQueueDialog.collectAsState()
        val showCreatePlaylistDialog by viewModel.showCreatePlaylistDialog.collectAsState()
        val showLyricsSheet by viewModel.showLyricsSheet.collectAsState()
        val selectedSongForDetails by viewModel.selectedSongForDetails.collectAsState()
        val selectedSongForMenu by viewModel.selectedSongForMenu.collectAsState()
        val userNotice by viewModel.userNotice.collectAsState()

        var showSplash by remember { mutableStateOf(true) }
        var selectedTab by remember { mutableStateOf(NavTab.HOME) }
        var isNowPlayingExpanded by remember { mutableStateOf(false) }
        var isEqualizerExpanded by remember { mutableStateOf(false) }
        var isAboutScreenVisible by remember { mutableStateOf(false) }
        var isAccentPickerOpen by remember { mutableStateOf(false) }

        val snackbarHostState = remember { SnackbarHostState() }

        // Back Handler Strategy (Bug Fix #1)
        BackHandler(enabled = !showSplash) {
            when {
                isNowPlayingExpanded -> isNowPlayingExpanded = false
                isEqualizerExpanded -> isEqualizerExpanded = false
                isAboutScreenVisible -> isAboutScreenVisible = false
                selectedTab != NavTab.HOME -> selectedTab = NavTab.HOME
                else -> activity?.moveTaskToBack(true) // Minimize app instead of closing!
            }
        }

        LaunchedEffect(userNotice) {
            userNotice?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearNotice()
            }
        }

        if (showSplash) {
            SplashScreen(onSplashFinished = { showSplash = false })
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = AmoledBackground,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (!isNowPlayingExpanded && !isEqualizerExpanded && !isAboutScreenVisible) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            MiniPlayerCard(
                                playerState = playerState,
                                onPlayPause = { viewModel.audioManager.playPause() },
                                onNext = { viewModel.audioManager.nextSong() },
                                onOpenQueue = { viewModel.setShowQueueDialog(true) },
                                onClickCard = { isNowPlayingExpanded = true }
                            )
                            BottomNavBar(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top Feature Pills
                        if (!isNowPlayingExpanded && !isEqualizerExpanded && !isAboutScreenVisible && selectedTab != NavTab.SETTINGS) {
                            TopFeaturePills(
                                isOnline = playerState.isOnlineMode,
                                onToggleOnline = { viewModel.audioManager.toggleOnlineMode() },
                                onOpenEqualizer = { isEqualizerExpanded = true },
                                onOpenLyrics = { viewModel.setShowLyricsSheet(true) }
                            )
                        }

                        // Main Content Tabs
                        Box(modifier = Modifier.weight(1f)) {
                            when (selectedTab) {
                                NavTab.HOME -> HomeScreen(
                                    playerState = playerState,
                                    allSongs = allSongs,
                                    recentlyPlayedSongs = recentlyPlayed,
                                    mostPlayedSongs = mostPlayedSongs,
                                    favoriteSongs = favoriteSongs,
                                    playlists = playlists,
                                    onSongClick = {
                                        viewModel.playSong(it)
                                        isNowPlayingExpanded = true
                                    },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onOpenSongMenu = { viewModel.setSelectedSongForMenu(it) },
                                    onNavigateTab = { tabName ->
                                        val target = NavTab.values().firstOrNull { it.title.equals(tabName, ignoreCase = true) }
                                        if (target != null) selectedTab = target
                                    },
                                    onScanDevice = { viewModel.scanDeviceAudio() },
                                    onCreatePlaylistClick = { viewModel.setShowCreatePlaylistDialog(true) }
                                )

                                NavTab.APPS -> AppsGridScreen(
                                    playerState = playerState,
                                    onNavigateTab = { tabName ->
                                        val target = NavTab.values().firstOrNull { it.title.equals(tabName, ignoreCase = true) }
                                        if (target != null) selectedTab = target
                                    },
                                    onOpenEqualizer = { isEqualizerExpanded = true },
                                    onOpenSleepTimer = { viewModel.setShowSleepTimerDialog(true) },
                                    onShowNotice = { viewModel.showNotice(it) }
                                )

                                NavTab.LIBRARY -> LibraryScreen(
                                    playerState = playerState,
                                    allSongs = allSongs,
                                    favoriteSongs = favoriteSongs,
                                    recentlyPlayed = recentlyPlayed,
                                    onSongClick = {
                                        viewModel.playSong(it)
                                        isNowPlayingExpanded = true
                                    },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onOpenSongDetails = { viewModel.setSelectedSongForMenu(it) }
                                )

                                NavTab.SEARCH -> SearchScreen(
                                    searchQuery = searchQuery,
                                    selectedCategory = searchCategory,
                                    filteredSongs = filteredSongs,
                                    filteredPlaylists = filteredPlaylists,
                                    playerState = playerState,
                                    searchHistory = searchHistory,
                                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                    onCategorySelected = { viewModel.setSearchCategory(it) },
                                    onSongClick = {
                                        viewModel.playSong(it)
                                        isNowPlayingExpanded = true
                                    },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onOpenSongMenu = { viewModel.setSelectedSongForMenu(it) },
                                    onClearSearchHistory = { viewModel.clearSearchHistory() },
                                    onRemoveSearchTerm = { viewModel.removeSearchTerm(it) },
                                    onShowNotice = { viewModel.showNotice(it) }
                                )

                                NavTab.PLAYLIST -> PlaylistScreen(
                                    playlists = playlists,
                                    allSongs = allSongs,
                                    playerState = playerState,
                                    onOpenCreateDialog = { viewModel.setShowCreatePlaylistDialog(true) },
                                    onRenamePlaylist = { id, name -> viewModel.renamePlaylist(id, name) },
                                    onDeletePlaylist = { viewModel.deletePlaylist(it) },
                                    onPlayPlaylist = { pl ->
                                        if (allSongs.isNotEmpty()) {
                                            viewModel.playSong(allSongs.first())
                                            isNowPlayingExpanded = true
                                        }
                                    },
                                    onShowNotice = { viewModel.showNotice(it) }
                                )

                                NavTab.SETTINGS -> SettingsScreen(
                                    currentTheme = selectedTheme,
                                    currentLanguage = selectedLanguage,
                                    onSelectTheme = { viewModel.setTheme(it) },
                                    onSelectLanguage = { viewModel.setLanguage(it) },
                                    onRescanMusic = { viewModel.scanDeviceAudio() },
                                    onOpenAccentPicker = { isAccentPickerOpen = true },
                                    onOpenSleepTimer = { viewModel.setShowSleepTimerDialog(true) },
                                    onRestoreSettings = { viewModel.restoreDefaultSettings() },
                                    onCleanCache = { viewModel.cleanCache() },
                                    onClearAppData = { viewModel.clearAppData() },
                                    onNavigateToAbout = { isAboutScreenVisible = true },
                                    onShowNotice = { viewModel.showNotice(it) }
                                )

                            }
                        }
                    }

                    // Full Screen Overlays: Now Playing, Equalizer, About
                    AnimatedVisibility(
                        visible = isAboutScreenVisible,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        AboutScreen(onBackClick = { isAboutScreenVisible = false })
                    }

                    AnimatedVisibility(
                        visible = isNowPlayingExpanded,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        NowPlayingScreen(
                            playerState = playerState,
                            visualizerFrequencies = visualizerFrequencies,
                            onPlayPause = { viewModel.audioManager.playPause() },
                            onNext = { viewModel.audioManager.nextSong() },
                            onPrevious = { viewModel.audioManager.previousSong() },
                            onSeek = { viewModel.audioManager.seekTo(it) },
                            onToggleShuffle = { viewModel.audioManager.toggleShuffle() },
                            onCycleRepeat = { viewModel.audioManager.cycleRepeatMode() },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onOpenLyrics = { viewModel.setShowLyricsSheet(true) },
                            onOpenQueue = { viewModel.setShowQueueDialog(true) },
                            onOpenSleepTimer = { viewModel.setShowSleepTimerDialog(true) },
                            onOpenSpeedPitch = { viewModel.setShowSpeedPitchDialog(true) },
                            onOpenEqualizer = { isEqualizerExpanded = true },
                            onBackClick = { isNowPlayingExpanded = false }
                        )
                    }

                    AnimatedVisibility(
                        visible = isEqualizerExpanded,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        EqualizerScreen(
                            equalizerState = equalizerState,
                            presets = viewModel.equalizerPresets,
                            onToggleEnabled = { viewModel.audioManager.toggleEqualizer(it) },
                            onSelectPreset = { viewModel.audioManager.selectEqualizerPreset(it) },
                            onUpdateBandGain = { idx, gain -> viewModel.audioManager.updateBandGain(idx, gain) },
                            onSetBassBoost = { viewModel.audioManager.setBassBoost(it) },
                            onSetSurroundSound = { viewModel.audioManager.setSurroundSound(it) },
                            onSetReverbPreset = { viewModel.audioManager.setReverbPreset(it) },
                            onToggleLoudness = { viewModel.audioManager.toggleLoudnessEnhancer(it) },
                            onReset = { viewModel.audioManager.resetEqualizer() },
                            onBackClick = { isEqualizerExpanded = false }
                        )
                    }

                    // Dialogs & Modals
                    if (selectedSongForMenu != null) {
                        SongOptionsMenu(
                            song = selectedSongForMenu!!,
                            playlists = playlists,
                            onPlay = {
                                viewModel.playSong(selectedSongForMenu!!)
                                isNowPlayingExpanded = true
                                viewModel.setSelectedSongForMenu(null)
                            },
                            onAddToPlaylist = { plId ->
                                viewModel.addSongToPlaylist(plId, selectedSongForMenu!!.id)
                                viewModel.setSelectedSongForMenu(null)
                            },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(selectedSongForMenu!!)
                                viewModel.setSelectedSongForMenu(null)
                            },
                            onShowDetails = {
                                viewModel.setSelectedSongForDetails(selectedSongForMenu!!)
                                viewModel.setSelectedSongForMenu(null)
                            },
                            onDeleteSong = {
                                viewModel.deleteSong(selectedSongForMenu!!)
                            },
                            onDismiss = { viewModel.setSelectedSongForMenu(null) }
                        )
                    }

                    if (showSleepTimerDialog) {
                        SleepTimerDialog(
                            remainingSeconds = playerState.sleepTimerRemainingSeconds,
                            onSetTimer = { viewModel.audioManager.startSleepTimer(it) },
                            onCancelTimer = { viewModel.audioManager.cancelSleepTimer() },
                            onDismiss = { viewModel.setShowSleepTimerDialog(false) }
                        )
                    }

                    if (showSpeedPitchDialog) {
                        SpeedPitchDialog(
                            currentSpeed = playerState.playbackSpeed,
                            currentPitch = playerState.pitchSemitones,
                            currentVolumeBoost = playerState.volumeBoost,
                            onSpeedChanged = { viewModel.audioManager.setSpeed(it) },
                            onPitchChanged = { viewModel.audioManager.setPitch(it) },
                            onVolumeBoostChanged = { viewModel.audioManager.setVolumeBoost(it) },
                            onDismiss = { viewModel.setShowSpeedPitchDialog(false) }
                        )
                    }

                    if (showQueueDialog) {
                        QueueSheet(
                            playerState = playerState,
                            onSongSelected = { idx ->
                                val song = playerState.queue.getOrNull(idx)
                                if (song != null) viewModel.playSong(song)
                            },
                            onDismiss = { viewModel.setShowQueueDialog(false) }
                        )
                    }

                    if (showCreatePlaylistDialog) {
                        CreatePlaylistDialog(
                            onCreate = { viewModel.createPlaylist(it) },
                            onDismiss = { viewModel.setShowCreatePlaylistDialog(false) }
                        )
                    }

                    if (showLyricsSheet) {
                        LyricsSheet(
                            song = playerState.currentSong,
                            onDismiss = { viewModel.setShowLyricsSheet(false) }
                        )
                    }

                    if (selectedSongForDetails != null) {
                        FileInfoModal(
                            song = selectedSongForDetails!!,
                            onDismiss = { viewModel.setSelectedSongForDetails(null) }
                        )
                    }

                    if (isAccentPickerOpen) {
                        AccentColorPickerDialog(
                            currentColor = accentColor,
                            onSelectColor = { viewModel.setAccentColor(it) },
                            onDismiss = { isAccentPickerOpen = false }
                        )
                    }

                    if (playerState.showSmartVolumeDialog) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { viewModel.audioManager.dismissSmartVolumeDialog() },
                            containerColor = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(20.dp),
                            title = {
                                Text(
                                    text = "Vxx Music Volume Control",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            },
                            text = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = playerState.externalAudioReason.ifEmpty { "External audio or mic active" },
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("0%", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                        Slider(
                                            value = playerState.appVolume,
                                            onValueChange = { viewModel.audioManager.setAppVolume(it) },
                                            valueRange = 0f..1f,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text("100%", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "App Volume: ${(playerState.appVolume * 100).toInt()}%",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = { viewModel.audioManager.dismissSmartVolumeDialog() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Done", color = Color.White)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
