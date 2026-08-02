package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AmoledBlackScheme = darkColorScheme(
  primary = AccentPurple,
  onPrimary = TextPrimary,
  primaryContainer = DarkSurfaceVariant,
  onPrimaryContainer = AccentPurpleGlow,
  secondary = AccentViolet,
  onSecondary = TextPrimary,
  tertiary = AccentPink,
  background = Color(0xFF000000),
  onBackground = TextPrimary,
  surface = Color(0xFF121212),
  onSurface = TextPrimary,
  surfaceVariant = Color(0xFF1E1E1E),
  onSurfaceVariant = TextSecondary,
  outline = GlassBorder
)

private val DarkGrayScheme = darkColorScheme(
  primary = AccentPurple,
  onPrimary = TextPrimary,
  primaryContainer = Color(0xFF2B2B2B),
  onPrimaryContainer = AccentPurpleGlow,
  secondary = AccentCyan,
  onSecondary = TextPrimary,
  tertiary = AccentPink,
  background = Color(0xFF121212),
  onBackground = TextPrimary,
  surface = Color(0xFF1E1E1E),
  onSurface = TextPrimary,
  surfaceVariant = Color(0xFF2B2B2B),
  onSurfaceVariant = TextSecondary,
  outline = GlassBorder
)

private val MidnightBlueScheme = darkColorScheme(
  primary = Color(0xFF38BDF8),
  onPrimary = TextPrimary,
  primaryContainer = Color(0xFF1E293B),
  onPrimaryContainer = Color(0xFFBAE6FD),
  secondary = Color(0xFF818CF8),
  onSecondary = TextPrimary,
  tertiary = AccentPink,
  background = Color(0xFF0A0E1A),
  onBackground = TextPrimary,
  surface = Color(0xFF131B2E),
  onSurface = TextPrimary,
  surfaceVariant = Color(0xFF1E293B),
  onSurfaceVariant = TextSecondary,
  outline = Color(0xFF334155)
)

private val PurpleDarkScheme = darkColorScheme(
  primary = Color(0xFF9D4EDD),
  onPrimary = TextPrimary,
  primaryContainer = Color(0xFF3C096C),
  onPrimaryContainer = Color(0xFFE0AAFF),
  secondary = Color(0xFF7B2CBF),
  onSecondary = TextPrimary,
  tertiary = AccentPink,
  background = Color(0xFF10002B),
  onBackground = TextPrimary,
  surface = Color(0xFF240046),
  onSurface = TextPrimary,
  surfaceVariant = Color(0xFF3C096C),
  onSurfaceVariant = TextSecondary,
  outline = Color(0xFF5A189A)
)

private val MaterialYouDarkScheme = darkColorScheme(
  primary = Color(0xFFD0BCFF),
  onPrimary = Color(0xFF381E72),
  primaryContainer = Color(0xFF4F378B),
  onPrimaryContainer = Color(0xFFEADDFF),
  secondary = Color(0xFFCCC2DC),
  onSecondary = Color(0xFF332D41),
  tertiary = Color(0xFFEFB8C8),
  background = Color(0xFF1C1B1F),
  onBackground = Color(0xFFE6E1E5),
  surface = Color(0xFF25232A),
  onSurface = Color(0xFFE6E1E5),
  surfaceVariant = Color(0xFF36343B),
  onSurfaceVariant = Color(0xFFCAC4D0),
  outline = Color(0xFF49454F)
)

@Composable
fun MusicPlayerTheme(
  themeName: String = "AMOLED Black",
  accentColor: Color = AccentPurple,
  content: @Composable () -> Unit
) {
  val baseScheme = when (themeName) {
    "Dark Gray" -> DarkGrayScheme
    "Midnight Blue" -> MidnightBlueScheme
    "Purple Dark" -> PurpleDarkScheme
    "Material You Dark" -> MaterialYouDarkScheme
    else -> AmoledBlackScheme
  }

  val customColorScheme = baseScheme.copy(
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


