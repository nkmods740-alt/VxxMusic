package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.EqualizerState
import com.example.data.model.EqualizerPreset
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GlassBorder

@Composable
fun VerticalEqualizerSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = -12f..12f,
    enabled: Boolean = true,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    modifier: Modifier = Modifier
) {
    val rangeMin = valueRange.start
    val rangeMax = valueRange.endInclusive
    val totalRange = rangeMax - rangeMin

    BoxWithConstraints(
        modifier = modifier
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset ->
                    val heightPx = size.height
                    if (heightPx > 0) {
                        val fraction = 1f - (offset.y / heightPx).coerceIn(0f, 1f)
                        val newValue = rangeMin + fraction * totalRange
                        onValueChange(newValue)
                    }
                }
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    val heightPx = size.height
                    if (heightPx > 0) {
                        val deltaFraction = -dragAmount / heightPx
                        val currentFraction = (value - rangeMin) / totalRange
                        val newFraction = (currentFraction + deltaFraction).coerceIn(0f, 1f)
                        val newValue = rangeMin + newFraction * totalRange
                        onValueChange(newValue)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val fraction = ((value - rangeMin) / totalRange).coerceIn(0f, 1f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackWidth = 8.dp.toPx()
            val thumbRadius = 10.dp.toPx()
            val availableHeight = size.height - 2 * thumbRadius
            val thumbY = thumbRadius + (1f - fraction) * availableHeight
            val centerX = size.width / 2f
            val zeroY = thumbRadius + 0.5f * availableHeight

            // Track background capsule
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(centerX - trackWidth / 2f, thumbRadius),
                size = Size(trackWidth, availableHeight),
                cornerRadius = CornerRadius(trackWidth / 2f, trackWidth / 2f)
            )

            // Active track from 0dB level to thumb
            if (thumbY <= zeroY) {
                drawRoundRect(
                    color = if (enabled) activeColor else activeColor.copy(alpha = 0.4f),
                    topLeft = Offset(centerX - trackWidth / 2f, thumbY),
                    size = Size(trackWidth, zeroY - thumbY),
                    cornerRadius = CornerRadius(trackWidth / 2f, trackWidth / 2f)
                )
            } else {
                drawRoundRect(
                    color = if (enabled) activeColor else activeColor.copy(alpha = 0.4f),
                    topLeft = Offset(centerX - trackWidth / 2f, zeroY),
                    size = Size(trackWidth, thumbY - zeroY),
                    cornerRadius = CornerRadius(trackWidth / 2f, trackWidth / 2f)
                )
            }

            // Zero dB center notch line
            drawLine(
                color = if (enabled) Color.White.copy(alpha = 0.6f) else Color.Gray,
                start = Offset(centerX - trackWidth - 3.dp.toPx(), zeroY),
                end = Offset(centerX + trackWidth + 3.dp.toPx(), zeroY),
                strokeWidth = 2.dp.toPx()
            )

            // Thumb handle
            drawCircle(
                color = if (enabled) activeColor else Color.Gray,
                radius = thumbRadius,
                center = Offset(centerX, thumbY)
            )
            drawCircle(
                color = Color.White,
                radius = thumbRadius * 0.45f,
                center = Offset(centerX, thumbY)
            )
        }
    }
}

@Composable
fun EqualizerScreen(
    equalizerState: EqualizerState,
    presets: List<EqualizerPreset>,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectPreset: (EqualizerPreset) -> Unit,
    onUpdateBandGain: (Int, Float) -> Unit,
    onSetBassBoost: (Float) -> Unit,
    onSetSurroundSound: (Float) -> Unit,
    onSetReverbPreset: (String) -> Unit = {},
    onToggleLoudness: (Boolean) -> Unit,
    onReset: () -> Unit,
    onBackClick: () -> Unit
) {
    val bandLabels = listOf("60Hz", "230Hz", "910Hz", "3.6K", "14K")
    val reverbOptions = listOf("None", "Small Room", "Medium Hall", "Large Hall", "Plate")
    var showPresetMenu by remember { mutableStateOf(false) }

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 16.dp)
            .testTag("equalizer_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }

                // Preset Selector Pill
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(surfaceVariantColor)
                            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                            .clickable { showPresetMenu = true }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = equalizerState.currentPreset,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = MaterialTheme.colorScheme.onSurface)
                    }

                    DropdownMenu(
                        expanded = showPresetMenu,
                        onDismissRequest = { showPresetMenu = false },
                        modifier = Modifier.background(surfaceColor)
                    ) {
                        presets.forEach { preset ->
                            DropdownMenuItem(
                                text = { Text(preset.name, color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    onSelectPreset(preset)
                                    showPresetMenu = false
                                }
                            )
                        }
                    }
                }

                // Power Toggle Button
                IconButton(
                    onClick = { onToggleEnabled(!equalizerState.isEnabled) }
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Power",
                        tint = if (equalizerState.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5-Band Equalizer Sliders Card Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(surfaceColor)
                    .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("+12dB", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        Text("0dB", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        Text("-12dB", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5 Band Horizontal Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        equalizerState.bandGains.take(5).forEachIndexed { index, gain ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "%+.0fdB".format(gain),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                VerticalEqualizerSlider(
                                    value = gain,
                                    onValueChange = { onUpdateBandGain(index, it) },
                                    valueRange = -12f..12f,
                                    enabled = equalizerState.isEnabled,
                                    activeColor = MaterialTheme.colorScheme.primary,
                                    trackColor = surfaceVariantColor,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                )

                                Text(
                                    text = bandLabels.getOrElse(index) { "B${index + 1}" },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bass Boost & Surround Sound Knobs / Sliders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bass Boost Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(surfaceColor)
                        .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Bass Boost", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(surfaceVariantColor)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("%.0f".format(equalizerState.bassBoost), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Slider(
                            value = equalizerState.bassBoost,
                            onValueChange = onSetBassBoost,
                            valueRange = 0f..10f,
                            enabled = equalizerState.isEnabled
                        )
                    }
                }

                // Surround Sound Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(surfaceColor)
                        .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Surround", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(surfaceVariantColor)
                                .border(2.dp, AccentPink, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("%.0f".format(equalizerState.surroundSound), color = AccentPink, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Slider(
                            value = equalizerState.surroundSound,
                            onValueChange = onSetSurroundSound,
                            valueRange = 0f..10f,
                            enabled = equalizerState.isEnabled,
                            colors = SliderDefaults.colors(thumbColor = AccentPink, activeTrackColor = AccentPink)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Echo / Reverb System Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(surfaceColor)
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("Echo & Reverb Effect", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Spatial room & hall reverberation", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(reverbOptions) { option ->
                            val isSel = equalizerState.reverbPreset == option
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else surfaceVariantColor)
                                    .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else GlassBorder, RoundedCornerShape(14.dp))
                                    .clickable { onSetReverbPreset(option) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = option,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loudness & Virtualizer Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(surfaceColor)
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Loudness Enhancer", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Boost volume dynamically", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }

                    Switch(
                        checked = equalizerState.loudnessEnhancer,
                        onCheckedChange = onToggleLoudness,
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Preset Chips Bottom Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(presets) { preset ->
                    val isSel = equalizerState.currentPreset == preset.name
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primary else surfaceVariantColor)
                            .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else GlassBorder, RoundedCornerShape(16.dp))
                            .clickable { onSelectPreset(preset) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = preset.name,
                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
