package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.audio.PlayerState
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.ui.components.CosmicBackground
import com.example.ui.components.safePainterResource
import com.example.ui.theme.*

@Composable
fun PlaylistScreen(
    playlists: List<Playlist>,
    allSongs: List<Song>,
    playerState: PlayerState,
    onOpenCreateDialog: () -> Unit,
    onRenamePlaylist: (Long, String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onPlayPlaylist: (Playlist) -> Unit,
    onShowNotice: (String) -> Unit
) {
    var selectedPlaylistForMenu by remember { mutableStateOf<Playlist?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showRenameDialog && selectedPlaylistForMenu != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("New Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRenamePlaylist(selectedPlaylistForMenu!!.id, renameInput)
                        showRenameDialog = false
                        selectedPlaylistForMenu = null
                    }
                ) {
                    Text("Rename", color = AccentCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = AmoledCardBackground
        )
    }

    if (showDeleteConfirm && selectedPlaylistForMenu != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${selectedPlaylistForMenu!!.name}'?", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePlaylist(selectedPlaylistForMenu!!.id)
                        showDeleteConfirm = false
                        selectedPlaylistForMenu = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF4B4B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = AmoledCardBackground
        )
    }

    if (selectedPlaylistForMenu != null && !showRenameDialog && !showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { selectedPlaylistForMenu = null },
            title = { Text(selectedPlaylistForMenu!!.name, color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                renameInput = selectedPlaylistForMenu!!.name
                                showRenameDialog = true
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = AccentCyan)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Rename Playlist", color = Color.White, fontSize = 15.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showDeleteConfirm = true
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF4B4B))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Delete Playlist", color = Color(0xFFFF4B4B), fontSize = 15.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPlaylistForMenu = null }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = AmoledCardBackground
        )
    }

    CosmicBackground(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("playlist_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PLAYLISTS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                IconButton(onClick = onOpenCreateDialog, modifier = Modifier.testTag("create_playlist_btn")) {
                    Icon(Icons.Default.Add, contentDescription = "Create Playlist", tint = AccentCyan)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(playlists) { pl ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(AmoledCardBackground)
                            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                            .clickable { onPlayPlaylist(pl) }
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(DarkSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = safePainterResource(resId = pl.coverResId),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.matchParentSize()
                                    )
                                }

                                Column(modifier = Modifier.padding(start = 14.dp)) {
                                    Text(
                                        text = pl.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "${pl.songCount} Tracks",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            IconButton(onClick = { selectedPlaylistForMenu = pl }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Add Button
        FloatingActionButton(
            onClick = onOpenCreateDialog,
            containerColor = AccentPurple,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Playlist", tint = Color.White)
        }
    }
}
