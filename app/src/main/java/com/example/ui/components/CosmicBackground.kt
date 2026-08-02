package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AmoledBackground
import kotlin.random.Random

import androidx.compose.foundation.layout.BoxScope

private data class StarPoint(val xRatio: Float, val yRatio: Float, val size: Float, val alpha: Float, val phaseOffset: Float)

@Composable
fun CosmicBackground(
    modifier: Modifier = Modifier,
    showMoon: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "starTwinkle")
    val twinklePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI.toFloat()),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinklePhase"
    )

    // Generate static star coordinates ratio once
    val stars = remember {
        val rand = Random(42)
        List(40) {
            StarPoint(
                xRatio = rand.nextFloat(),
                yRatio = rand.nextFloat(),
                size = rand.nextFloat() * 2.5f + 1f,
                alpha = rand.nextFloat() * 0.5f + 0.3f,
                phaseOffset = rand.nextFloat() * (2f * Math.PI.toFloat())
            )
        }
    }

    val bg = androidx.compose.material3.MaterialTheme.colorScheme.background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 0 || h <= 0) return@Canvas

            // 1. Subtle Radial Cosmic Ambient Glow at Top Center (from reference image)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x20A28BFE),
                        Color(0x10FF758F),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.15f),
                    radius = w * 0.7f
                ),
                radius = w * 0.7f,
                center = Offset(w * 0.5f, h * 0.15f)
            )

            // 2. Crescent Moon icon glow at top center (like in screenshot)
            if (showMoon) {
                val moonCx = w * 0.5f
                val moonCy = h * 0.08f
                val moonRadius = 10f

                // Outer moon soft glow
                drawCircle(
                    color = Color(0x60FFEAA7),
                    radius = moonRadius * 2.2f,
                    center = Offset(moonCx, moonCy)
                )

                // Moon main body
                drawCircle(
                    color = Color(0xFFFFF0A0),
                    radius = moonRadius,
                    center = Offset(moonCx, moonCy)
                )

                // Moon shadow cutout for crescent shape
                drawCircle(
                    color = bg,
                    radius = moonRadius * 0.85f,
                    center = Offset(moonCx - 4f, moonCy - 3f)
                )
            }

            // 3. Twinkling Star Particles scattered across canvas
            stars.forEach { star ->
                val starX = star.xRatio * w
                val starY = star.yRatio * h
                val twinkle = (kotlin.math.sin(twinklePhase + star.phaseOffset) + 1f) / 2f
                val currentAlpha = (star.alpha * (0.4f + 0.6f * twinkle)).coerceIn(0.1f, 0.9f)

                drawCircle(
                    color = Color.White.copy(alpha = currentAlpha),
                    radius = star.size,
                    center = Offset(starX, starY)
                )
            }
        }

        content()
    }
}
