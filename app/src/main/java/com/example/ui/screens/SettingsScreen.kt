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

import androidx.compose.ui.platform.LocalContext
import com.example.widget.VxxMusicWidgetProvider

@Composable
fun SettingsScreen(
    currentTheme: String,
    currentLanguage: String,
    onSelectTheme: (String) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onRescanMusic: () -> Unit,
    onOpenAccentPicker: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onRestoreSettings: () -> Unit,
    onCleanCache: () -> Unit,
    onClearAppData: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onShowNotice: (String) -> Unit
) {
    val context = LocalContext.current
    var currentWidgetSize by remember { mutableStateOf(VxxMusicWidgetProvider.getWidgetSize(context)) }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showWidgetSizeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    val themesList = listOf(
        "AMOLED Black",
        "Dark Gray",
        "Midnight Blue",
        "Purple Dark",
        "Material You Dark"
    )

    val widgetSizesList = listOf(
        "Compact (Small 2x1)",
        "Standard (Medium 3x2)",
        "Expanded (Large 4x2)",
        "Ultra (Full 4x3)"
    )

    val languagesList = listOf(
        "English (US)",
        "English (UK)"
    )

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
                        title = "Scan New Songs",
                        subtitle = "Scan device storage for audio files",
                        icon = Icons.Default.Sync,
                        onClick = onRescanMusic
                    )
                }

                item {
                    SettingsRow(
                        title = "Theme",
                        subtitle = currentTheme,
                        icon = Icons.Default.Nightlight,
                        onClick = { showThemeDialog = true }
                    )
                }

                item {
                    SettingsRow(
                        title = "Widget Customization & Size",
                        subtitle = currentWidgetSize,
                        icon = Icons.Default.Widgets,
                        onClick = { showWidgetSizeDialog = true }
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
                        title = "Language",
                        subtitle = currentLanguage,
                        icon = Icons.Default.Language,
                        onClick = { showLanguageDialog = true }
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
                        title = "Restore Settings",
                        subtitle = "Reset all app settings to default",
                        icon = Icons.Default.Restore,
                        onClick = onRestoreSettings
                    )
                }

                item {
                    SettingsRow(
                        title = "Clear Cache",
                        subtitle = "Free up temporary cache space",
                        icon = Icons.Default.CleaningServices,
                        onClick = onCleanCache
                    )
                }

                item {
                    SettingsRow(
                        title = "Clear App Data",
                        subtitle = "Reset database and search history",
                        icon = Icons.Default.DeleteForever,
                        onClick = { showClearDataDialog = true }
                    )
                }

                item {
                    SettingsRow(
                        title = "About Vxx Music",
                        subtitle = "Developer: Nawab • @NawabKingMods",
                        icon = Icons.Default.Info,
                        onClick = onNavigateToAbout
                    )
                }
            }
        }
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            containerColor = AmoledCardBackground,
            title = { Text("Select Theme", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    themesList.forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectTheme(theme)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (theme == currentTheme),
                                onClick = {
                                    onSelectTheme(theme)
                                    showThemeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentPurple)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(theme, color = Color.White, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close", color = AccentCyan)
                }
            }
        )
    }

    // Widget Size Selection Dialog
    if (showWidgetSizeDialog) {
        AlertDialog(
            onDismissRequest = { showWidgetSizeDialog = false },
            containerColor = AmoledCardBackground,
            title = { Text("Widget Size & Layout", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Choose your preferred home screen widget size:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    widgetSizesList.forEach { size ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentWidgetSize = size
                                    VxxMusicWidgetProvider.setWidgetSize(context, size)
                                    VxxMusicWidgetProvider.updateAllWidgets(context, "Vxx Music", "Widget Size Updated", false)
                                    onShowNotice("Widget Size set to: $size")
                                    showWidgetSizeDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (size == currentWidgetSize),
                                onClick = {
                                    currentWidgetSize = size
                                    VxxMusicWidgetProvider.setWidgetSize(context, size)
                                    VxxMusicWidgetProvider.updateAllWidgets(context, "Vxx Music", "Widget Size Updated", false)
                                    onShowNotice("Widget Size set to: $size")
                                    showWidgetSizeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentPurple)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(size, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWidgetSizeDialog = false }) {
                    Text("Close", color = AccentCyan)
                }
            }
        )
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = AmoledCardBackground,
            title = { Text("Select Language", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    languagesList.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (lang == currentLanguage),
                                onClick = {
                                    onSelectLanguage(lang)
                                    showLanguageDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentPurple)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(lang, color = Color.White, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close", color = AccentCyan)
                }
            }
        )
    }

    // Clear App Data Confirmation
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            containerColor = AmoledCardBackground,
            title = { Text("Clear All App Data?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text("This will reset all playlists, search history, and scanned songs data. Are you sure?", color = TextSecondary)
            },
            confirmButton = {
                TextButton(onClick = {
                    showClearDataDialog = false
                    onClearAppData()
                }) {
                    Text("Clear Everything", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
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
