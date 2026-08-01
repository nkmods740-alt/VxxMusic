package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.R

@Composable
fun safePainterResource(
    @DrawableRes resId: Int,
    @DrawableRes fallbackResId: Int = R.drawable.anime_night_art_1785557649722
): Painter {
    val context = LocalContext.current
    val validResId = if (resId != 0) {
        try {
            context.resources.getResourceName(resId)
            resId
        } catch (e: Exception) {
            fallbackResId
        }
    } else {
        fallbackResId
    }
    return painterResource(id = validResId)
}
