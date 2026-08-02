package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple

enum class VisualizerStyle {
    SPECTRUM_BARS,
    SMOOTH_WAVEFORM,
    HYBRID_SPECTRUM
}

@Composable
fun AudioVisualizer(
    frequencies: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 70.dp,
    isPlaying: Boolean = true,
    style: VisualizerStyle = VisualizerStyle.HYBRID_SPECTRUM
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizerGlow")
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI.toFloat()),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing)
        ),
        label = "phase"
    )

    // APK Theme Color Gradients
    val barGradient = Brush.verticalGradient(
        colors = listOf(AccentCyan, AccentPink, AccentPurple)
    )

    val waveformGradient = Brush.horizontalGradient(
        colors = listOf(AccentCyan, AccentPink, AccentPurple, AccentOrange, AccentMint)
    )

    val reflectionGradient = Brush.verticalGradient(
        colors = listOf(AccentPurple.copy(alpha = 0.35f), Color.Transparent)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val canvasHeight = size.height
            val activeMultiplier = if (isPlaying) 1.0f else 0.15f

            if (width <= 0 || canvasHeight <= 0) return@Canvas

            val barCount = 36
            val spacing = 3.dp.toPx()
            val totalSpacing = spacing * (barCount - 1)
            val barWidth = ((width - totalSpacing) / barCount).coerceAtLeast(2.5.dp.toPx())
            
            val baselineY = canvasHeight * 0.72f
            val maxBarHeight = canvasHeight * 0.65f

            val peakPoints = mutableListOf<Offset>()

            // 1. Calculate and Draw Spectrum Bars with Real-time Frequencies
            for (index in 0 until barCount) {
                val freqIndex = (index.toFloat() / barCount * (frequencies.size - 1))
                    .toInt()
                    .coerceIn(0, (frequencies.size - 1).coerceAtLeast(0))
                
                val rawAmplitude = if (frequencies.isNotEmpty()) frequencies[freqIndex] else 0.35f
                
                // Micro-rhythm tick for dynamic responsiveness when playing
                val microTick = if (isPlaying) (((index * 3 + (phase * 8).toInt()) % 7) / 45f) else 0f
                val amplitudeFactor = (rawAmplitude + microTick).coerceIn(0.12f, 1.0f)
                
                val currentBarHeight = (maxBarHeight * amplitudeFactor * activeMultiplier).coerceAtLeast(4.dp.toPx())
                val x = index * (barWidth + spacing) + barWidth / 2f
                val topY = baselineY - currentBarHeight

                peakPoints.add(Offset(x, topY))

                if (style == VisualizerStyle.SPECTRUM_BARS || style == VisualizerStyle.HYBRID_SPECTRUM) {
                    val barLeft = index * (barWidth + spacing)
                    
                    // Main Bar
                    drawRoundRect(
                        brush = barGradient,
                        topLeft = Offset(barLeft, topY),
                        size = Size(barWidth, currentBarHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                        alpha = if (isPlaying) 0.95f else 0.35f
                    )

                    // Peak Cap Dot
                    val capHeight = barWidth
                    val capY = (topY - capHeight - 2.5.dp.toPx()).coerceAtLeast(0f)
                    drawRoundRect(
                        color = if (index % 3 == 0) AccentCyan else AccentOrange,
                        topLeft = Offset(barLeft, capY),
                        size = Size(barWidth, capHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                        alpha = if (isPlaying) 0.9f else 0.25f
                    )

                    // Mirror Reflection
                    val reflectionHeight = currentBarHeight * 0.32f
                    drawRoundRect(
                        brush = reflectionGradient,
                        topLeft = Offset(barLeft, baselineY + 2.dp.toPx()),
                        size = Size(barWidth, reflectionHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                        alpha = if (isPlaying) 0.28f else 0.08f
                    )
                }
            }

            // 2. Waveform Spline Line Path Connecting Real-Time Amplitude Peaks
            if ((style == VisualizerStyle.SMOOTH_WAVEFORM || style == VisualizerStyle.HYBRID_SPECTRUM) && peakPoints.size > 1) {
                val wavePath = Path()
                wavePath.moveTo(peakPoints[0].x, peakPoints[0].y)

                for (i in 0 until peakPoints.size - 1) {
                    val p1 = peakPoints[i]
                    val p2 = peakPoints[i + 1]
                    val cx = (p1.x + p2.x) / 2f
                    val cy = (p1.y + p2.y) / 2f
                    wavePath.quadraticTo(p1.x, p1.y, cx, cy)
                }

                // Outer Glowing Neon Wave Line
                drawPath(
                    path = wavePath,
                    brush = waveformGradient,
                    style = Stroke(
                        width = if (style == VisualizerStyle.SMOOTH_WAVEFORM) 3.5.dp.toPx() else 2.dp.toPx(),
                        cap = StrokeCap.Round
                    ),
                    alpha = if (isPlaying) 0.95f else 0.4f
                )

                // Fill beneath smooth waveform if standalone waveform mode
                if (style == VisualizerStyle.SMOOTH_WAVEFORM) {
                    val fillPath = Path()
                    fillPath.addPath(wavePath)
                    fillPath.lineTo(width, baselineY)
                    fillPath.lineTo(0f, baselineY)
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            listOf(AccentPink.copy(alpha = 0.35f), Color.Transparent)
                        )
                    )
                }
            }
        }
    }
}
