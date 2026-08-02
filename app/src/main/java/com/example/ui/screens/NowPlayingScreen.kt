package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.audio.PlayerState
import com.example.data.audio.RepeatMode as PlayerRepeatMode
import com.example.data.model.Song
import com.example.ui.components.AudioVisualizer
import com.example.ui.components.CosmicBackground
import com.example.ui.components.VisualizerStyle
import com.example.ui.components.safePainterResource
import com.example.ui.theme.*

@Composable
fun NowPlayingScreen(
    playerState: PlayerState,
    visualizerFrequencies: List<Float>,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenSpeedPitch: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onBackClick: () -> Unit
) {
    val song = playerState.currentSong ?: return
    var vizStyle by remember { mutableStateOf(VisualizerStyle.HYBRID_SPECTRUM) }

    // Smooth Scrubbing State
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubProgress by remember { mutableFloatStateOf(0f) }

    val positionMs = playerState.currentPositionMs
    val durationMs = if (song.durationMs > 0) song.durationMs else 1L
    val currentProgress = if (isScrubbing) scrubProgress else (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)

    CosmicBackground(
        modifier = Modifier
            .fillMaxSize()
            .testTag("now_playing_screen")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Collapse",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "NOW PLAYING",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.5.sp
                    )

                    IconButton(onClick = onOpenEqualizer) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Equalizer",
                            tint = AccentCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Album Art Card with Glowing Background
                Box(
                    modifier = Modifier.size(280.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        AccentPink.copy(alpha = 0.45f),
                                        AccentPurple.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 14.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.5.dp, GlassBorder, RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (song.albumArtUri.isNotBlank()) {
                            AsyncImage(
                                model = song.albumArtUri,
                                contentDescription = "Album Art",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize(),
                                error = safePainterResource(resId = R.drawable.anime_night_art_1785557649722)
                            )
                        } else {
                            Image(
                                painter = safePainterResource(resId = song.albumArtResId),
                                contentDescription = "Album Art",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                    }

                    // Stylish Pill Badge
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DarkSurface)
                            .border(1.dp, AccentPink.copy(alpha = 0.7f), CircleShape)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = AccentPink,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Vxx Music",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Song Title & Favorite Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${song.artist} • ${song.album}",
                            color = TextSecondary,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = { onToggleFavorite(song) }) {
                        Icon(
                            imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (song.isFavorite) AccentPink else TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Canvas Realtime Visualizer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            vizStyle = when (vizStyle) {
                                VisualizerStyle.HYBRID_SPECTRUM -> VisualizerStyle.SPECTRUM_BARS
                                VisualizerStyle.SPECTRUM_BARS -> VisualizerStyle.SMOOTH_WAVEFORM
                                VisualizerStyle.SMOOTH_WAVEFORM -> VisualizerStyle.HYBRID_SPECTRUM
                                else -> VisualizerStyle.HYBRID_SPECTRUM
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AudioVisualizer(
                        frequencies = visualizerFrequencies,
                        isPlaying = playerState.isPlaying,
                        style = vizStyle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = when (vizStyle) {
                            VisualizerStyle.HYBRID_SPECTRUM -> "• Realtime Spectrum + Wave •"
                            VisualizerStyle.SPECTRUM_BARS -> "• Dynamic Equalizer Bars •"
                            VisualizerStyle.SMOOTH_WAVEFORM -> "• Smooth Neon Waveform •"
                            else -> "• Audio Visualizer •"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AccentCyan.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Smooth Progress Slider
                Slider(
                    value = currentProgress,
                    onValueChange = { percent ->
                        isScrubbing = true
                        scrubProgress = percent
                    },
                    onValueChangeFinished = {
                        val seekTargetMs = (scrubProgress * durationMs).toLong()
                        onSeek(seekTargetMs)
                        isScrubbing = false
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = AccentCyan,
                        activeTrackColor = AccentCyan,
                        inactiveTrackColor = DarkSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Time Duration Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentDisplayMs = if (isScrubbing) (scrubProgress * durationMs).toLong() else positionMs
                    Text(
                        text = formatMs(currentDisplayMs),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = formatMs(durationMs),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Playback Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleShuffle) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (playerState.isShuffle) AccentCyan else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(onClick = onPrevious) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(AccentPurple, AccentPink)
                                )
                            )
                            .clickable(onClick = onPlayPause),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(onClick = onNext) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    IconButton(onClick = onCycleRepeat) {
                        Icon(
                            imageVector = when (playerState.repeatMode) {
                                PlayerRepeatMode.ONE -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = "Repeat",
                            tint = if (playerState.repeatMode != PlayerRepeatMode.OFF) AccentCyan else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Action Tools Bar (Lyrics, Queue, Sleep Timer, Speed)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(AmoledCardBackground)
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onOpenLyrics) {
                        Icon(
                            imageVector = Icons.Default.Lyrics,
                            contentDescription = "Lyrics",
                            tint = TextSecondary
                        )
                    }

                    IconButton(onClick = onOpenQueue) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = "Queue",
                            tint = TextSecondary
                        )
                    }

                    IconButton(onClick = onOpenSleepTimer) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Sleep Timer",
                            tint = if (playerState.sleepTimerRemainingSeconds != null) AccentPink else TextSecondary
                        )
                    }

                    IconButton(onClick = onOpenSpeedPitch) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Speed & Pitch",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
