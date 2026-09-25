package dev.parez.barz

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo

/**
 * The navigation mode the current window calls for, clamped to
 * [AdaptiveNavigationConfig.allowedModes].
 *
 * Exposed so you can drive your own chrome instead of using [AdaptiveNavigationScaffold].
 *
 * Reads `LocalWindowInfo.containerSize` directly rather than going through
 * `currentWindowAdaptiveInfo()`. Two reasons: the window size class quantises to fixed buckets that
 * would defeat [NavigationBreakpoints], and reading the size keeps the resolution identical on
 * every platform. The size is state-backed, so desktop window drags and browser resizes recompose
 * live.
 */
@Composable
fun rememberNavigationMode(
    config: AdaptiveNavigationConfig = AdaptiveNavigationConfig()
): NavigationMode {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    // `containerSize` changes on every frame of a window drag or a browser resize, but this
    // function's output is a three-valued enum that changes at exactly two thresholds. Reading it
    // directly would invalidate every caller — and AdaptiveNavigationScaffold passes the consuming
    // app through as `content` — once per frame for the whole gesture. derivedStateOf collapses
    // that to the two frames where the answer actually changes.
    val mode =
        remember(windowInfo, density, config) {
            derivedStateOf {
                val size = windowInfo.containerSize
                resolveNavigationMode(
                    widthDp = with(density) { size.width.toDp().value },
                    heightDp = with(density) { size.height.toDp().value },
                    config = config,
                )
            }
        }
    return mode.value
}

/**
 * Pure resolution, separated from composition so it can be unit-tested without a Compose runtime.
 */
internal fun resolveNavigationMode(
    widthDp: Float,
    heightDp: Float,
    config: AdaptiveNavigationConfig,
): NavigationMode {
    val bp = config.breakpoints
    // A short window gets a bottom bar no matter how wide it is — a landscape phone is ~891dp
    // wide and ~411dp tall, and a rail or drawer there wastes the little height that remains.
    val tallEnough = heightDp >= bp.minHeightDp
    val wanted =
        when {
            !tallEnough -> NavigationMode.BottomBar
            widthDp >= bp.drawerFromWidthDp -> NavigationMode.Drawer
            widthDp >= bp.railFromWidthDp -> NavigationMode.Rail
            else -> NavigationMode.BottomBar
        }
    return wanted.clampTo(config.allowedModes)
}

/**
 * Falls back to the widest permitted mode no wider than this one, and if there is none, the
 * narrowest permitted mode. Never throws — an app that allows only [NavigationMode.Drawer] still
 * gets a drawer on a phone rather than a crash.
 */
private fun NavigationMode.clampTo(allowed: Set<NavigationMode>): NavigationMode =
    // `entries` is already ordered narrowest-to-widest, so `ordinal` is the width rank. Taking
    // ordinal + 1 subsumes the "already allowed" case.
    NavigationMode.entries.take(ordinal + 1).lastOrNull { it in allowed }
        ?: NavigationMode.entries.first { it in allowed }
