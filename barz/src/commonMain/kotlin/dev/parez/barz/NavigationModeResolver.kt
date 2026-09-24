package dev.parez.barz

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo

/**
 * The navigation mode the current window calls for, clamped to [AdaptiveNavigationConfig.allowedModes].
 *
 * Exposed so you can drive your own chrome instead of using [AdaptiveNavigationScaffold].
 *
 * Reads `LocalWindowInfo.containerSize` directly rather than going through
 * `currentWindowAdaptiveInfo()`. Two reasons: the window size class quantises to fixed buckets that
 * would defeat [NavigationBreakpoints], and reading the size keeps the resolution identical on
 * every platform. The size is state-backed, so desktop window drags and browser resizes
 * recompose live.
 */
@Composable
fun rememberNavigationMode(
    config: AdaptiveNavigationConfig = AdaptiveNavigationConfig(),
): NavigationMode {
    val size = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    val widthDp = with(density) { size.width.toDp().value }
    val heightDp = with(density) { size.height.toDp().value }
    return resolveNavigationMode(widthDp, heightDp, config)
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
    val wanted = when {
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
private fun NavigationMode.clampTo(allowed: Set<NavigationMode>): NavigationMode {
    if (this in allowed) return this
    val order = listOf(NavigationMode.BottomBar, NavigationMode.Rail, NavigationMode.Drawer)
    val index = order.indexOf(this)
    return order.take(index).lastOrNull { it in allowed }
        ?: order.first { it in allowed }
}
