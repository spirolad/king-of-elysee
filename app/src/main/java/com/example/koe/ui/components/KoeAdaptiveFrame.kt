package com.example.koe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

// The Figma frames are drawn for a landscape phone.
private const val FRAME_WIDTH = 844f
private const val FRAME_HEIGHT = 390f

// Tablets are closer to 16:10 than the 844 x 390 frame: the layout may grow this much taller than Figma's
// frame before it is letterboxed, so a tablet uses its height without the screens stretching too far.
private const val MAX_HEIGHT_RATIO = 1.25f

/**
 * Draws [content] as Figma's 844 x 390 landscape phone frame, scaled up uniformly on larger screens such as
 * tablets and centred on [background].
 *
 * Phones keep their real density: they only scale when wider and taller than the frame. Inside, every dp and
 * sp is scaled, so the screens keep their phone layout and proportions.
 */
@Composable
fun KoeAdaptiveFrame(
    background: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        val scale = minOf(maxWidth.value / FRAME_WIDTH, maxHeight.value / FRAME_HEIGHT).coerceAtLeast(1f)
        if (scale == 1f) {
            content()
        } else {
            val outer = LocalDensity.current
            val width = maxWidth / scale
            val height = minOf(maxHeight / scale, (FRAME_HEIGHT * MAX_HEIGHT_RATIO).dp)
            CompositionLocalProvider(
                LocalDensity provides Density(outer.density * scale, outer.fontScale),
            ) {
                Box(Modifier.size(width, height)) { content() }
            }
        }
    }
}
