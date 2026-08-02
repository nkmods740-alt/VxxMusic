package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.PlayerState
import com.example.data.model.Song
import com.example.ui.components.CosmicBackground
import com.example.ui.components.SongItemRow
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassBorder

data class LibraryCategory(
    val title: String,
    val countLabel: String,
    val icon: ImageVector,
    val filterType: String
)

@Composable
fun LibraryScreen(
    playerState: PlayerState,
    allSongs: List<Song>,
    favoriteSongs: List<Song>,
    recentlyPlayed: List<Song>,
    onSongClick: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onOpenSongDetails: (Song) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val categories = listOf(
        LibraryCategory("All Songs", "${allSongs.size} Songs", Icons.Default.MusicNote, "all"),
        LibraryCategory("Albums", "${allSongs.distinctBy { it.album }.size} Albums", Icons.Default.Album, "albums"),
        LibraryCategory("Artists", "${allSongs.distinctBy { it.artist }.size} Artists", Icons.Default.Person, "artists"),
        LibraryCategory("Genres", "16 Genres", Icons.Default.QueueMusic, "genres"),
        LibraryCategory("Folders", "12 Folders", Icons.Default.Folder, "folders"),
        LibraryCategory("Recently Added", "${allSongs.size} Songs", Icons.Default.History, "recent_added"),
        LibraryCategory("Recently Played", "${recentlyPlayed.size} Songs", Icons.Default.History, "recently_played"),
        LibraryCategory("Most Played", "${allSongs.size} Songs", Icons.Default.Star, "most_played"),
        LibraryCategory("Favorites", "${favoriteSongs.size} Songs", Icons.Default.Favorite, "favorites"),
        LibraryCategory("Downloads", "8 Songs", Icons.Default.Download, "downloads")
    )

    CosmicBackground(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("library_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedCategory != null) {
                    IconButton(onClick = { selectedCategory = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Text(
                    text = selectedCategory ?: "LIBRARY",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(start = if (selectedCategory == null) 8.dp else 0.dp)
                )
            }

            if (selectedCategory == null) {
                // Category List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(DarkSurface)
                                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                                .clickable { selectedCategory = cat.title }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = cat.icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.padding(start = 14.dp)) {
                                        Text(
                                            text = cat.title,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = cat.countLabel,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Open",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                // Drill-down song list for selected category
                val songsToDisplay = when (selectedCategory) {
                    "Favorites" -> favoriteSongs
                    "Recently Played" -> recentlyPlayed
                    else -> allSongs
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(songsToDisplay) { song ->
                        SongItemRow(
                            song = song,
                            isPlayingCurrent = playerState.currentSong?.id == song.id,
                            onClickSong = { onSongClick(song) },
                            onToggleFavorite = { onToggleFavorite(song) },
                            onOpenOptions = { onOpenSongDetails(song) }
                        )
                    }
                }
            }
        }
    }
}
