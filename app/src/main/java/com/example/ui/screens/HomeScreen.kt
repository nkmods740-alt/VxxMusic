package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.audio.PlayerState
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.ui.components.CosmicBackground
import com.example.ui.components.SongItemRow
import com.example.ui.components.safePainterResource
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    playerState: PlayerState,
    allSongs: List<Song>,
    recentlyPlayedSongs: List<Song>,
    mostPlayedSongs: List<Song>,
    favoriteSongs: List<Song>,
    playlists: List<Playlist>,
    onSongClick: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onOpenSongMenu: (Song) -> Unit,
    onNavigateTab: (String) -> Unit,
    onScanDevice: () -> Unit,
    onCreatePlaylistClick: () -> Unit
) {
    CosmicBackground(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // Top Header Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Vxx Music",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp
                        )
                        Text(
                            text = "Futuristic Audio Player",
                            color = AccentCyan,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onScanDevice,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AmoledCardBackground)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Scan Storage",
                                tint = AccentCyan
                            )
                        }

                        IconButton(
                            onClick = { onNavigateTab("Search") },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AmoledCardBackground)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Quick Category Pills
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CategoryPill("Songs", Icons.Default.MusicNote) { onNavigateTab("Library") }
                    CategoryPill("Albums", Icons.Default.Album) { onNavigateTab("Library") }
                    CategoryPill("Artists", Icons.Default.Person) { onNavigateTab("Library") }
                    CategoryPill("Playlists", Icons.Default.QueueMusic) { onNavigateTab("Playlist") }
                }
            }

            // Empty State Banner if no songs scanned
            if (allSongs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = AmoledCardBackground),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentPurple.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderSpecial,
                                contentDescription = "Scan Storage",
                                tint = AccentCyan,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Local Music Found",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap below to scan your internal storage, downloads, SD card, WhatsApp & Telegram audio folders.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onScanDevice,
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan Device Music", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // SECTION 1: Recently Played Header
            item {
                SectionHeader("Recently Played", "View All") { onNavigateTab("Library") }
            }

            // Recently Played Cards Row
            item {
                if (recentlyPlayedSongs.isEmpty()) {
                    Text(
                        text = "No recently played songs yet.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(recentlyPlayedSongs) { song ->
                            RecentSongCard(
                                song = song,
                                isPlaying = playerState.currentSong?.id == song.id,
                                onClick = { onSongClick(song) }
                            )
                        }
                    }
                }
            }

            // SECTION 2: All Songs
            item {
                SectionHeader("All Songs", "View All (${allSongs.size})") { onNavigateTab("Library") }
            }

            if (allSongs.isEmpty()) {
                item {
                    Text(
                        text = "No songs found. Please click 'Scan Device Music' above.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(allSongs.take(15)) { song ->
                    SongItemRow(
                        song = song,
                        isPlayingCurrent = playerState.currentSong?.id == song.id,
                        onClickSong = { onSongClick(song) },
                        onToggleFavorite = { onToggleFavorite(song) },
                        onOpenOptions = { onOpenSongMenu(song) }
                    )
                }
            }


            // SECTION 3: Favorites
            item {
                SectionHeader("Favorites", "See All (${favoriteSongs.size})") { onNavigateTab("Library") }
            }

            if (favoriteSongs.isEmpty()) {
                item {
                    Text(
                        text = "No favorite songs added yet.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(favoriteSongs.take(4)) { song ->
                    SongItemRow(
                        song = song,
                        isPlayingCurrent = playerState.currentSong?.id == song.id,
                        onClickSong = { onSongClick(song) },
                        onToggleFavorite = { onToggleFavorite(song) },
                        onOpenOptions = { onOpenSongMenu(song) }
                    )
                }
            }

            // SECTION 4: New Playlists
            item {
                SectionHeader("New Playlists", "+ Create") { onCreatePlaylistClick() }
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        // Create Playlist Card
                        Box(
                            modifier = Modifier
                                .size(width = 130.dp, height = 150.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(AmoledCardBackground)
                                .border(1.dp, AccentPurple.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .clickable { onCreatePlaylistClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = "New Playlist",
                                    tint = AccentPurple,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("New Playlist", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    items(playlists) { playlist ->
                        PlaylistCard(playlist = playlist) { onNavigateTab("Playlist") }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionText: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = actionText,
            color = AccentCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.clickable { onActionClick() }
        )
    }
}

@Composable
private fun RecentSongCard(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .height(170.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, GlassBorder, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
    ) {
        if (song.albumArtUri.isNotBlank()) {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
                error = safePainterResource(resId = R.drawable.anime_night_art_1785557649722)
            )
        } else {
            Image(
                painter = safePainterResource(resId = song.albumArtResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, AmoledBackground.copy(alpha = 0.9f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = song.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1
            )
            Text(
                text = song.artist,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PlaylistCard(playlist: Playlist, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 130.dp, height = 150.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = safePainterResource(resId = playlist.coverResId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, AmoledBackground.copy(alpha = 0.85f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = playlist.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1
            )
            Text(
                text = "${playlist.songCount} Tracks",
                color = AccentCyan,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun CategoryPill(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = AccentPurple,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
