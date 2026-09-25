package dev.parez.barz.sample.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Design tokens ─────────────────────────────────────────────────────────────
// Straight from the Figma variables. Named as they are there so a change in the file maps to an
// obvious line here.

/** The brand accent — the red of the pokéball. */
val Pokeball = Color(0xFFF06449)

private val LightSurfacePrimary = Color(0xFFFAFAFA) // cards
private val LightSurfaceSecondary = Color(0xFFF1F1F1) // page background, inset tiles
private val LightSurfaceShadow = Color(0xFFE4E4E4) // hairlines, stat-bar tracks
private val LightTextPrimary = Color(0xFF3E3E3E)
private val LightTextSecondary = Color(0xFFA1A1A1)

private val DarkSurfacePrimary = Color(0xFF181818)
private val DarkSurfaceSecondary = Color(0xFF202020)
private val DarkSurfaceTertiary = Color(0xFF353535)
private val DarkSurfaceShadow = Color(0xFF141414)
private val DarkTextPrimary = Color(0xFFF0F0F0)
private val DarkTextSecondary = Color(0xFF7A7777)

/**
 * The design has no accent-derived containers — it is a greyscale sheet with one brand colour and
 * per-type accents applied locally. So most M3 container roles collapse onto the same two surfaces:
 * `background` is the page, `surface` is a card, `surfaceContainer` is a tile inset into a card.
 */
fun appColorScheme(dark: Boolean): ColorScheme =
    if (dark) {
        darkColorScheme(
            primary = Pokeball,
            onPrimary = Color.White,
            secondary = Pokeball,
            onSecondary = Color.White,
            background = DarkSurfacePrimary,
            onBackground = DarkTextPrimary,
            surface = DarkSurfaceSecondary,
            onSurface = DarkTextPrimary,
            surfaceContainerLowest = DarkSurfaceShadow,
            surfaceContainerLow = DarkSurfacePrimary,
            surfaceContainer = DarkSurfaceTertiary,
            surfaceContainerHigh = DarkSurfaceTertiary,
            surfaceContainerHighest = DarkSurfaceTertiary,
            surfaceVariant = DarkSurfaceTertiary,
            onSurfaceVariant = DarkTextSecondary,
            // Not a design token. M3 draws a Switch's thumb and border in `outline` over a
            // `surfaceContainerHighest` track — leaving it at the tertiary surface made the two
            // identical and the control vanished. It has to contrast with the surfaces.
            outline = Color(0xFF6E6E6E),
            outlineVariant = DarkSurfaceShadow,
        )
    } else {
        lightColorScheme(
            primary = Pokeball,
            onPrimary = Color.White,
            secondary = Pokeball,
            onSecondary = Color.White,
            background = LightSurfaceSecondary,
            onBackground = LightTextPrimary,
            surface = LightSurfacePrimary,
            onSurface = LightTextPrimary,
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = LightSurfacePrimary,
            surfaceContainer = LightSurfaceSecondary,
            surfaceContainerHigh = LightSurfaceSecondary,
            surfaceContainerHighest = LightSurfaceShadow,
            surfaceVariant = LightSurfaceSecondary,
            onSurfaceVariant = LightTextSecondary,
            outline = LightTextSecondary,
            outlineVariant = LightSurfaceShadow,
        )
    }

// ── Typography ───────────────────────────────────────────────────────────────

private val defaultTypography = Typography()

/**
 * The design pairs Inter for UI text with Share Tech Mono for numerals and type tags. Neither ships
 * with the demo, so this maps the *weights and metrics* onto the platform defaults and leaves the
 * mono role to [FontFamily.Monospace] — the shapes differ, the hierarchy does not.
 */
val AppTypography =
    Typography(
        displaySmall = defaultTypography.displaySmall.copy(fontWeight = FontWeight.Bold),
        // "Pokemon" / "Team 4/6" / the detail name — the design's `subheading` token:
        // Inter Bold 30/32, -1.5 tracking.
        headlineMedium =
            defaultTypography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                lineHeight = 32.sp,
                letterSpacing = (-1.5).sp,
            ),
        headlineSmall = defaultTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = defaultTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = defaultTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
    )

/** The design's `tags small` token — Share Tech Mono 16/20, used for type tags and `#0001`. */
val MonoTagStyle =
    defaultTypography.labelLarge.copy(
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Normal,
    )

/**
 * Whether the active scheme is the dark one.
 *
 * Read off the background rather than threaded down from [DemoApp]: the type-tag shades and the
 * settings controls are the only things that need it, and both are deep in the tree.
 */
@Composable fun isDarkScheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f
