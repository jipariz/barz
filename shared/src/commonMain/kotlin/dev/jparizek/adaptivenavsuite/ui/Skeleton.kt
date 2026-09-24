package dev.jparizek.adaptivenavsuite.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Placeholder bars and pills standing in for text and controls.
 *
 * The demo renders no copy at all — every label is a shape — so that nothing on screen competes
 * with the navigation chrome for attention. Real strings still exist in `DemoCatalog` and are
 * attached as semantics, so the screens stay legible to a screen reader even though they are
 * deliberately illegible to the eye.
 */
@Composable
fun SkeletonBar(
    widthFraction: Float,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f)),
    )
}

/** A small fixed-size pill, for things like a toggle or a badge. */
@Composable
fun SkeletonPill(
    modifier: Modifier = Modifier,
    width: Dp = 44.dp,
    height: Dp = 24.dp,
) {
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)),
    )
}

/** A circular placeholder, for avatars and icon slots. */
@Composable
fun SkeletonCircle(
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)),
    )
}
