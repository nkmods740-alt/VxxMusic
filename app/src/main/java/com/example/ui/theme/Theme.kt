package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AmoledDarkScheme = darkColorScheme(
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

private val DefaultPurpleScheme = darkColorScheme(
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

private val DarkPurpleScheme = darkColorScheme(
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

private val AuroraPurpleScheme = darkColorScheme(
  primary = Color(0xFFC77DFF),
  onPrimary = TextPrimary,
  primaryContainer = Color(0xFF5A189A),
  onPrimaryContainer = Color(0xFFE0AAFF),
  secondary = Color(0xFF70D6FF),
  onSecondary = TextPrimary,
  tertiary = Color(0xFFFF758F),
  background = Color(0xFF180A29),
  onBackground = TextPrimary,
  surface = Color(0xFF2A1545),
  onSurface = TextPrimary,
  surfaceVariant = Color(0xFF3D1F63),
  onSurfaceVariant = TextSecondary,
  outline = Color(0xFF7B2CBF)
)

private val PureWhiteScheme = lightColorScheme(
  primary = Color(0xFF7C3AED),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFF3E8FF),
  onPrimaryContainer = Color(0xFF5B21B6),
  secondary = Color(0xFF06B6D4),
  onSecondary = Color.White,
  tertiary = Color(0xFFEC4899),
  background = Color(0xFFFFFFFF),
  onBackground = Color(0xFF0F172A),
  surface = Color(0xFFF8FAFC),
  onSurface = Color(0xFF0F172A),
  surfaceVariant = Color(0xFFF1F5F9),
  onSurfaceVariant = Color(0xFF475569),
  outline = Color(0xFFE2E8F0)
)

private val GlassWhiteScheme = lightColorScheme(
  primary = Color(0xFF6D28D9),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFEDE9FE),
  onPrimaryContainer = Color(0xFF4C1D95),
  secondary = Color(0xFF0891B2),
  onSecondary = Color.White,
  tertiary = Color(0xFFDB2777),
  background = Color(0xFFF3F4F6),
  onBackground = Color(0xFF111827),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF111827),
  surfaceVariant = Color(0xFFE5E7EB),
  onSurfaceVariant = Color(0xFF4B5563),
  outline = Color(0xFFD1D5DB)
)

@Composable
fun MusicPlayerTheme(
  themeName: String = "Default Purple",
  accentColor: Color = AccentPurple,
  content: @Composable () -> Unit
) {
  val baseScheme = when (themeName) {
    "AMOLED Black" -> AmoledDarkScheme
    "Dark Purple" -> DarkPurpleScheme
    "Aurora Purple" -> AuroraPurpleScheme
    "Pure White" -> PureWhiteScheme
    "Glass White" -> GlassWhiteScheme
    else -> DefaultPurpleScheme
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


