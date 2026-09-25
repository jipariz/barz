package dev.parez.navbarz

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
) {
    init {
        // Swapped, the resolver tests drawer first and Rail becomes unreachable — a config that
        // looks plausible and fails silently. AdaptiveNavigationConfig already guards its own
        // invariant; this is the other one.
        require(railFromWidthDp <= drawerFromWidthDp) {
            "railFromWidthDp ($railFromWidthDp) must be <= drawerFromWidthDp ($drawerFromWidthDp)"
        }
    }
}

/**
 * Which bar [AdaptiveNavigationBar] renders on iOS.
 *
 * There are three levels of "native" available, and only two of them are choices here:
 *
 * |                           | Liquid Glass | repositioned on a foldable |
 * |---------------------------|--------------|----------------------------|
 * | [ComposeGlass]            | imitated     | no                         |
 * | [NativeTabBar]            | **real**     | no                         |
 * | [navBarzTabBarController] | **real**     | **yes**                    |
 *
 * The third is not a value in this enum because it is not a rendering choice — it replaces your
 * root view controller. Liquid Glass is a property of the *view*, so an embedded `UITabBar` gets
 * it; foldable placement is a property of the *container*, so only a `UITabBarController` does.
 */
enum class IosChrome {
    /**
     * A real `UITabBar` embedded through `UIKitView`. Genuine system material, laid out by Compose.
     * The default: it looks native and costs nothing structurally.
     */
    NativeTabBar,

    /**
     * A bar drawn in Compose. Use it when you want full control of the rendering, or on a platform
     * mix where a uniform look matters more than a native one.
     */
    ComposeGlass,
}

/**
 * iOS-only knobs. Ignored on every other platform.
 *
 * @param chrome which bar [AdaptiveNavigationBar] renders; see [IosChrome].
 * @param liquidGlass **not a true system opt-out.** The only real switch is the app-level
 *   `UIDesignRequiresCompatibility` Info.plist key, which a library cannot set. This flag chooses
 *   between the system material and an explicitly opaque bar background.
 * @param sidebarAdaptable promote tabs to a sidebar on iPad. Only meaningful for
 *   [navBarzTabBarController] — an embedded bar has no sidebar mode.
 * @param nativeBarHeight height reserved for the embedded `UITabBar` ([IosChrome.NativeTabBar]). A
 *   UIKit view cannot report its size back through Compose interop, so the host has to reserve
 *   space for it. Raise this if your layout adds an offset and the bar ends up clipped.
 */
@Immutable
data class IosOptions(
    val chrome: IosChrome = IosChrome.NativeTabBar,
    val liquidGlass: Boolean = true,
    val sidebarAdaptable: Boolean = true,
    val nativeBarHeight: Dp = 56.dp,
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
        require(allowedModes.isNotEmpty()) {
            "allowedModes must contain at least one NavigationMode"
        }
    }
}
