package dev.parez.barz

import androidx.compose.runtime.Immutable

/** The shapes the navigation chrome can take. */
enum class NavigationMode {
    /** Bar pinned to the bottom edge. The right answer on phones and short windows. */
    BottomBar,

    /** Narrow vertical rail on the leading edge. */
    Rail,

    /** Permanent expanded drawer with labels. Only comfortable on genuinely wide windows. */
    Drawer,
}

/**
 * Width and height thresholds at which the chrome changes shape, in dp.
 *
 * The defaults are not arbitrary:
 * - [drawerFromWidthDp] is **1200**, not the 840dp "expanded" breakpoint. A permanent drawer takes
 *   roughly 40% of an unfolded foldable's width; that is affordable when the content is a single
 *   pane, and starves it when the content has its own multi-pane layout.
 * - [minHeightDp] guards the landscape-phone case: ~891dp wide but only ~411dp tall. Width alone
 *   would promote it to a rail or drawer, which is wrong for a window that short.
 */
@Immutable
data class NavigationBreakpoints(
    val railFromWidthDp: Int = 600,
    val drawerFromWidthDp: Int = 1200,
    val minHeightDp: Int = 480,
)

/** Which implementation supplies the iOS navigation chrome. */
enum class IosChrome {
    /**
     * Real SwiftUI `TabView` from the companion Swift package. Liquid Glass, `sidebarAdaptable`
     * and future iPhone Duo placement are genuine system behaviour, because they come from a real
     * system container. Costs an SPM dependency alongside the Gradle one.
     */
    NativeTabView,

    /**
     * Bar drawn in Compose. One Gradle dependency and no Swift, at the cost of only *imitating*
     * the system material — an app using this does not get Duo placement or sidebar promotion.
     */
    ComposeGlass,
}

/**
 * iOS-only knobs. Ignored on every other platform.
 *
 * @param chrome see [IosChrome].
 * @param liquidGlass **not a true system opt-out.** The only real switch is the app-level
 *   `UIDesignRequiresCompatibility` Info.plist key, which a library cannot set. This flag chooses
 *   between the system material and an explicitly opaque bar background.
 * @param sidebarAdaptable promote tabs to a sidebar on iPad (`.tabViewStyle(.sidebarAdaptable)`).
 *   [IosChrome.NativeTabView] only.
 * @param tabBarMinimizeBehavior let the tab bar shrink on scroll. iOS 26+, ignored below.
 */
@Immutable
data class IosOptions(
    val chrome: IosChrome = IosChrome.NativeTabView,
    val liquidGlass: Boolean = true,
    val sidebarAdaptable: Boolean = true,
    val tabBarMinimizeBehavior: Boolean = true,
)

/**
 * How the chrome should adapt.
 *
 * @param allowedModes the modes this app permits. Use it to opt out — `setOf(BottomBar)` pins a
 *   bottom bar at every width; omitting [NavigationMode.Drawer] caps the widest windows at a rail.
 *   The resolver clamps down to the widest permitted mode rather than failing.
 * @param breakpoints where the transitions happen.
 * @param ios iOS-only options.
 */
@Immutable
data class AdaptiveNavigationConfig(
    val allowedModes: Set<NavigationMode> = NavigationMode.entries.toSet(),
    val breakpoints: NavigationBreakpoints = NavigationBreakpoints(),
    val ios: IosOptions = IosOptions(),
) {
    init {
        require(allowedModes.isNotEmpty()) { "allowedModes must contain at least one NavigationMode" }
    }
}
