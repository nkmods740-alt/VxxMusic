package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
  primary = AccentPurple,
  onPrimary = TextPrimary,
  primaryContainer = DarkSurfaceVariant,
  onPrimaryContainer = AccentPurpleGlow,
  secondary = AccentViolet,
  onSecondary = TextPrimary,
  tertiary = AccentPink,
  background = AmoledBackground,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = GlassSurface,
  onSurfaceVariant = TextSecondary,
  outline = GlassBorder
)

@Composable
fun MusicPlayerTheme(
  accentColor: androidx.compose.ui.graphics.Color = AccentPurple,
  content: @Composable () -> Unit
) {
  val customColorScheme = DarkColorScheme.copy(
    primary = accentColor,
    onPrimaryContainer = accentColor
  )

  MaterialTheme(
    colorScheme = customColorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  MusicPlayerTheme(content = content)
}

