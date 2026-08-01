package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CosmicBackground
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    onOpenAccentPicker: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onCleanCache: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onShowNotice: (String) -> Unit
) {
    var isAmoledEnabled by remember { mutableStateOf(true) }
    var isAppLockEnabled by remember { mutableStateOf(false) }

    CosmicBackground(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "SETTINGS",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    SettingsRow(
                        title = "Theme",
                        subtitle = "Futuristic Dark Purple",
                        icon = Icons.Default.Nightlight,
                        onClick = { onShowNotice("Current Theme: Dark Purple AMOLED") }
                    )
                }

                item {
                    SettingsRow(
                        title = "Accent Color",
                        subtitle = "Customize App Accent Color",
                        icon = Icons.Default.Palette,
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(AccentPurple)
                            )
                        },
                        onClick = onOpenAccentPicker
                    )
                }

                item {
                    SettingsRow(
                        title = "AMOLED Mode",
                        subtitle = "Deep Black Backgrounds",
                        icon = Icons.Default.ColorLens,
                        trailingContent = {
                            Switch(
                                checked = isAmoledEnabled,
                                onCheckedChange = { isAmoledEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan)
                            )
                        },
                        onClick = { isAmoledEnabled = !isAmoledEnabled }
                    )
                }

                item {
                    SettingsRow(
                        title = "Language",
                        subtitle = "English",
                        icon = Icons.Default.Language,
                        onClick = { onShowNotice("Language: English (US)") }
                    )
                }

                item {
                    SettingsRow(
                        title = "Backup & Restore",
                        subtitle = "Playlists & Equalizer presets",
                        icon = Icons.Default.Backup,
                        onClick = { onShowNotice("Settings backed up to local DataStore") }
                    )
                }

                item {
                    SettingsRow(
                        title = "Cache Cleaner",
                        subtitle = "Clear temporary media cache",
                        icon = Icons.Default.CleaningServices,
                        onClick = onCleanCache
                    )
                }

                item {
                    SettingsRow(
                        title = "Sleep Timer",
                        subtitle = "Auto turn off playback",
                        icon = Icons.Default.Timer,
                        onClick = onOpenSleepTimer
                    )
                }

                item {
                    SettingsRow(
                        title = "App Lock",
                        subtitle = "Biometric or PIN Security",
                        icon = Icons.Default.Lock,
                        trailingContent = {
                            Switch(
                                checked = isAppLockEnabled,
                                onCheckedChange = {
                                    isAppLockEnabled = it
                                    onShowNotice(if (it) "App Lock enabled" else "App Lock disabled")
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan)
                            )
                        },
                        onClick = { isAppLockEnabled = !isAppLockEnabled }
                    )
                }

                item {
                    SettingsRow(
                        title = "About Vxx Music",
                        subtitle = "Developer: Nawab • Channel: @NawabKingMods",
                        icon = Icons.Default.Info,
                        onClick = onNavigateToAbout
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AmoledCardBackground)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.padding(start = 14.dp)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (trailingContent != null) {
                    trailingContent()
                } else {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open",
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}
