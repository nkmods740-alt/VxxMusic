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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.PlayerState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GlassBorder

import com.example.ui.components.CosmicBackground

data class AppGridItem(
    val title: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val targetTab: String? = null,
    val action: String? = null
)

@Composable
fun AppsGridScreen(
    playerState: PlayerState,
    onNavigateTab: (String) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onShowNotice: (String) -> Unit
) {
    val items = listOf(
        AppGridItem("Songs", Icons.Default.MusicNote, Color(0xFFFF758F), "Library"),     // Pastel Coral
        AppGridItem("Albums", Icons.Default.Album, Color(0xFFFFB088), "Library"),       // Pastel Peach
        AppGridItem("Artists", Icons.Default.Person, Color(0xFFA28BFE), "Library"),      // Pastel Purple
        AppGridItem("Genres", Icons.Default.MusicNote, Color(0xFFB8A1FF), "Library"),     // Pastel Lavender
        AppGridItem("Playlists", Icons.Default.QueueMusic, Color(0xFF70D6FF), "Playlist"),// Pastel Cyan
        AppGridItem("Folders", Icons.Default.Folder, Color(0xFF38BDF8), "FileManager"),  // Sky Blue
        AppGridItem("Recently", Icons.Default.History, Color(0xFFF472B6), "Library"),    // Soft Pink
        AppGridItem("Favorites", Icons.Default.Favorite, Color(0xFFFF6B81), "Library"),   // Rose Pink
        AppGridItem("Downloads", Icons.Default.Download, Color(0xFFFB923C), "FileManager"),// Soft Amber
        AppGridItem("Radio", Icons.Default.Radio, Color(0xFFFF758F), action = "Radio"),
        AppGridItem("Equalizer", Icons.Default.Equalizer, Color(0xFFA28BFE), action = "Equalizer"),
        AppGridItem("Sleep Timer", Icons.Default.Timer, Color(0xFF818CF8), action = "Timer"),
        AppGridItem("Settings", Icons.Default.Settings, Color(0xFF64748B), "Settings"),
        AppGridItem("Backup", Icons.Default.Backup, Color(0xFF72EFDD), action = "Backup"),
        AppGridItem("Rate Us", Icons.Default.Star, Color(0xFF4ADE80), action = "Rate"),
        AppGridItem("Share", Icons.Default.Share, Color(0xFF2DD4BF), action = "Share")
    )

    CosmicBackground(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("apps_grid_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "APPS",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                IconButton(onClick = { }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Grid Container Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(DarkSurface)
                    .border(1.dp, GlassBorder, RoundedCornerShape(32.dp))
                    .padding(16.dp)
            ) {
                Column {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items) { item ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    if (item.targetTab != null) {
                                        onNavigateTab(item.targetTab)
                                    } else if (item.action == "Equalizer") {
                                        onOpenEqualizer()
                                    } else if (item.action == "Timer") {
                                        onOpenSleepTimer()
                                    } else {
                                        onShowNotice("${item.title} opened")
                                    }
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(item.iconBgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Text(
                                    text = item.title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }

                    // Current Playlist Pill Card at Bottom of Grid
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Current Playlist",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "My Favorite Playlist",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "50 Songs",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp)) // Clearance for Mini Player
        }
    }
}
