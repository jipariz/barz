package dev.jparizek.adaptivenavsuite.ui

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.min
import kotlin.random.Random

/**
 * Biases which shapes a composition is built from, so each destination reads as a different
 * visual family rather than four screens of the same noise.
 */
enum class ArtVariant { Rounded, Angular, Banded, Mixed }

/**
 * Deterministic generated artwork, used as the stand-in for real imagery throughout the demo.
 *
 * Two deliberate constraints:
 *  - **Every color comes from [MaterialTheme.colorScheme]**, never a literal. That is what keeps
 *    "playful" from turning into "louder than the navigation chrome", and it means the art is
 *    correct in dark mode and reacts to Material You on Android for free.
 *  - **The composition is a pure function of [seed]**, so the same item looks the same on Android
 *    and iOS, across recompositions, and in previews.
 */
@Composable
fun ShapeArt(
    seed: Int,
    modifier: Modifier = Modifier,
    variant: ArtVariant = ArtVariant.Mixed,
) {
    val scheme = MaterialTheme.colorScheme
    val palette = remember(scheme) {
        listOf(
            scheme.primaryContainer,
            scheme.secondaryContainer,
            scheme.tertiaryContainer,
            scheme.surfaceVariant,
        )
    }
    // Built once per seed rather than per frame: the recipe is normalized (0..1), so it survives
    // any resize and costs nothing to redraw.
    val recipe = remember(seed, variant) { artRecipe(seed, variant) }

    Canvas(modifier = modifier) {
        drawRect(color = palette[recipe.backgroundIndex % palette.size])
        recipe.shapes.forEach { shape ->
            drawArtShape(shape, palette[shape.colorIndex % palette.size])
        }
    }
}

@Immutable
private data class ArtShape(
    val kind: Kind,
    val centerX: Float,
    val centerY: Float,
    val scale: Float,
    val rotation: Float,
    val colorIndex: Int,
) {
    enum class Kind { Circle, Wedge, Triangle, Band }
}

@Immutable
private data class ArtRecipe(
    val backgroundIndex: Int,
    val shapes: List<ArtShape>,
)

private fun artRecipe(seed: Int, variant: ArtVariant): ArtRecipe {
    val random = Random(seed)
    val backgroundIndex = random.nextInt(4)
    val count = 2 + random.nextInt(2) // 2 or 3 shapes — enough variety, never busy
    val kinds = when (variant) {
        ArtVariant.Rounded -> listOf(ArtShape.Kind.Circle, ArtShape.Kind.Wedge)
        ArtVariant.Angular -> listOf(ArtShape.Kind.Triangle, ArtShape.Kind.Wedge)
        ArtVariant.Banded -> listOf(ArtShape.Kind.Band, ArtShape.Kind.Circle)
        ArtVariant.Mixed -> ArtShape.Kind.entries
    }
    val shapes = List(count) { index ->
        ArtShape(
            kind = kinds[random.nextInt(kinds.size)],
            centerX = 0.2f + random.nextFloat() * 0.6f,
            centerY = 0.2f + random.nextFloat() * 0.6f,
            scale = 0.35f + random.nextFloat() * 0.45f,
            rotation = random.nextFloat() * 360f,
            // Offset from the background so a shape never disappears into it.
            colorIndex = backgroundIndex + 1 + index,
        )
    }
    return ArtRecipe(backgroundIndex = backgroundIndex, shapes = shapes)
}

private fun DrawScope.drawArtShape(shape: ArtShape, color: Color) {
    val minSide = min(size.width, size.height)
    val extent = minSide * shape.scale
    val center = Offset(size.width * shape.centerX, size.height * shape.centerY)

    rotate(degrees = shape.rotation, pivot = center) {
        when (shape.kind) {
            ArtShape.Kind.Circle -> drawCircle(
                color = color,
                radius = extent / 2f,
                center = center,
            )

            ArtShape.Kind.Wedge -> drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = 120f,
                useCenter = true,
                topLeft = Offset(center.x - extent / 2f, center.y - extent / 2f),
                size = Size(extent, extent),
            )

            ArtShape.Kind.Triangle -> drawPath(
                path = Path().apply {
                    moveTo(center.x, center.y - extent / 2f)
                    lineTo(center.x + extent / 2f, center.y + extent / 2f)
                    lineTo(center.x - extent / 2f, center.y + extent / 2f)
                    close()
                },
                color = color,
            )

            ArtShape.Kind.Band -> drawRect(
                color = color,
                topLeft = Offset(center.x - size.width, center.y - extent / 6f),
                size = Size(size.width * 2f, extent / 3f),
            )
        }
    }
}
