package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.PlayerState
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.ui.components.CosmicBackground
import com.example.ui.components.SongItemRow
import com.example.ui.theme.*

@Composable
fun SearchScreen(
    searchQuery: String,
    selectedCategory: String,
    filteredSongs: List<Song>,
    filteredPlaylists: List<Playlist>,
    playerState: PlayerState,
    searchHistory: List<String>,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onSongClick: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onOpenSongMenu: (Song) -> Unit,
    onClearSearchHistory: () -> Unit,
    onRemoveSearchTerm: (String) -> Unit,
    onShowNotice: (String) -> Unit
) {
    val categories = listOf("All", "Songs", "Albums", "Artists", "Playlists")

    CosmicBackground(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("search_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "SEARCH",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Search Bar Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search songs, albums, artists, playlists...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentCyan) },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                        IconButton(onClick = { onShowNotice("Listening... speak now") }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = AccentCyan)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = GlassBorder,
                    focusedContainerColor = AmoledCardBackground,
                    unfocusedContainerColor = AmoledCardBackground,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSel = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSel) AccentPurple else DarkSurfaceVariant)
                            .border(1.dp, if (isSel) AccentPurple else GlassBorder, RoundedCornerShape(16.dp))
                            .clickable { onCategorySelected(cat) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (searchQuery.isEmpty()) {
                if (searchHistory.isNotEmpty()) {
                    // Recent Searches Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recent Searches", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "Clear All",
                            color = AccentCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onClearSearchHistory() }
                        )
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(searchHistory) { term ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AmoledCardBackground)
                                    .clickable { onSearchQueryChange(term) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                                    Text(term, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(start = 12.dp))
                                }
                                IconButton(
                                    onClick = { onRemoveSearchTerm(term) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent search history.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {

                // Search Results
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    if (selectedCategory == "All" || selectedCategory == "Songs" || selectedCategory == "Artists" || selectedCategory == "Albums") {
                        item {
                            Text(
                                text = "Songs (${filteredSongs.size})",
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(filteredSongs) { song ->
                            SongItemRow(
                                song = song,
                                isPlayingCurrent = playerState.currentSong?.id == song.id,
                                onClickSong = { onSongClick(song) },
                                onToggleFavorite = { onToggleFavorite(song) },
                                onOpenOptions = { onOpenSongMenu(song) }
                            )
                        }
                    }

                    if (selectedCategory == "All" || selectedCategory == "Playlists") {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Playlists (${filteredPlaylists.size})",
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(filteredPlaylists) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AmoledCardBackground)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = null,
                                    tint = AccentPurple,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(playlist.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("${playlist.songCount} Tracks", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
