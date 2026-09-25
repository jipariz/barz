package dev.parez.navbarz.sample.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

private val PokeballRed = Color(0xFFEF5350)
private val PokeballBlack = Color(0xFF1F1F1F)
private val PokeballWhite = Color(0xFFF5F5F5)

/**
 * Pokéball loading indicator. Wobbles side-to-side and bobs vertically, mimicking the classic
 * capture animation. Pure Compose [Canvas] — no native or web runtime dependencies, works on every
 * CMP target.
 *
 * Default size 64.dp; pass [Modifier.size] to override.
 */
@Composable
fun PokeballLoader(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "pokeball-loader")
    val tilt by
        transition.animateFloat(
            initialValue = -18f,
            targetValue = 18f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "tilt",
        )
    val bobPhase by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * PI).toFloat(),
            animationSpec =
                infiniteRepeatable(animation = tween(durationMillis = 1200, easing = LinearEasing)),
            label = "bob",
        )

    Canvas(modifier = modifier.size(64.dp)) {
        val side = minOf(size.width, size.height)
        val radius = side / 2f
        val cx = size.width / 2f
        val cy = size.height / 2f
        // Reserve ~6% of the height for vertical bob; pivot the whole ball
        // around its center so wobble + bob compose naturally.
        val bobAmplitude = side * 0.06f
        val dy = sin(bobPhase) * bobAmplitude

        translate(left = 0f, top = dy) {
            rotate(degrees = tilt, pivot = Offset(cx, cy)) {
                val ballTopLeft = Offset(cx - radius, cy - radius)
                val ballSize = Size(radius * 2, radius * 2)

                // Top red half.
                drawArc(
                    color = PokeballRed,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = ballTopLeft,
                    size = ballSize,
                )
                // Bottom white half.
                drawArc(
                    color = PokeballWhite,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = ballTopLeft,
                    size = ballSize,
                )
                // Equator band.
                val bandHeight = radius * 0.16f
                drawRect(
                    color = PokeballBlack,
                    topLeft = Offset(cx - radius, cy - bandHeight / 2f),
                    size = Size(radius * 2, bandHeight),
                )
                // Outer outline.
                drawCircle(
                    color = PokeballBlack,
                    radius = radius,
                    center = Offset(cx, cy),
                    style = Stroke(width = radius * 0.08f),
                )
                // Centre button — white disc with a thick black ring, and a
                // smaller hollow ring inside to catch the eye.
                val buttonOuter = radius * 0.28f
                drawCircle(color = PokeballWhite, radius = buttonOuter, center = Offset(cx, cy))
                drawCircle(
                    color = PokeballBlack,
                    radius = buttonOuter,
                    center = Offset(cx, cy),
                    style = Stroke(width = radius * 0.08f),
                )
                drawCircle(
                    color = PokeballWhite,
                    radius = radius * 0.11f,
                    center = Offset(cx, cy),
                    style = Stroke(width = radius * 0.045f),
                )
            }
        }
    }
}

/**
 * Flat, single-colour pokéball glyph — the design's add/remove-from-team affordance on list cards
 * and the detail header. Filled with [color] when the Pokémon is on the team, drawn in a muted grey
 * when it is not; the caller decides which.
 */
@Composable
fun PokeballGlyph(
    color: Color,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surface,
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val radius = minOf(size.width, size.height) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(color = color, radius = radius, center = center)
        // Knock the equator and the centre hole out in the surface colour behind it — a plain
        // two-tone glyph rather than a miniature of the full ball, which turns to mud at 24.dp.
        drawRect(
            color = background,
            topLeft = Offset(center.x - radius, center.y - radius * 0.14f),
            size = Size(radius * 2, radius * 0.28f),
        )
        drawCircle(color = background, radius = radius * 0.3f, center = center)
        drawCircle(
            color = color,
            radius = radius * 0.3f,
            center = center,
            style = Stroke(width = radius * 0.12f),
        )
    }
}

/** The full red-and-white pokéball, used as the app mark in the About dialog. */
@Composable
fun PokeballMark(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(56.dp)) {
        val radius = minOf(size.width, size.height) / 2f
        val cx = size.width / 2f
        val cy = size.height / 2f
        val topLeft = Offset(cx - radius, cy - radius)
        val ballSize = Size(radius * 2, radius * 2)
        drawArc(PokeballRed, 180f, 180f, true, topLeft, ballSize)
        drawArc(PokeballWhite, 0f, 180f, true, topLeft, ballSize)
        drawRect(
            color = PokeballBlack,
            topLeft = Offset(cx - radius, cy - radius * 0.08f),
            size = Size(radius * 2, radius * 0.16f),
        )
        drawCircle(PokeballBlack, radius, Offset(cx, cy), style = Stroke(radius * 0.07f))
        drawCircle(PokeballWhite, radius * 0.26f, Offset(cx, cy))
        drawCircle(PokeballBlack, radius * 0.26f, Offset(cx, cy), style = Stroke(radius * 0.07f))
    }
}
