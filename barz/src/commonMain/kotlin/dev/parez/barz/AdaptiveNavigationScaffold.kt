package dev.parez.barz

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Where [AdaptiveNavigationScaffold]'s `fab` sits in the rail and the drawer.
 *
 * Does not apply to the bottom bar, which always floats it over the bottom-end corner.
 */
enum class FabPlacement {
    /**
     * Above the destinations, in the component's header. Material's convention — it is what
     * `NavigationRail`'s `header` slot is for, and what Google's own apps do.
     */
    Top,

    /**
     * Below the destinations, at the foot of the component. Keeps the FAB at roughly the height it
     * occupied over the bottom bar, so it does not leap across the window when the chrome changes.
     */
    Bottom,
}

/**
 * Navigation chrome that changes shape with the window: a bottom bar when compact, a rail when
 * medium, a permanent drawer when genuinely wide.
 *
 * ```
 * AdaptiveNavigationScaffold(
 *     items = navItems,
 *     selectedIndex = selectedIndex,
 *     onItemSelected = { selectedIndex = it },
 *     fab = { FloatingActionButton(onClick = ::compose) { Icon(Icons.Filled.Add, null) } },
 * ) {
 *     CurrentScreen(selectedIndex)
 * }
 * ```
 *
 * Use [AdaptiveNavigationBar] instead if you want a plain bottom bar in a `Scaffold`, and
 * [rememberNavigationMode] if you want the decision without the container.
 *
 * On iOS this renders the Compose container, not a `UITabBarController` — the rail and drawer
 * have no UIKit equivalent, so there is nothing native to defer to above bottom-bar widths. If you
 * want the system to own the bar (and reposition it on a foldable), root the app in
 * [barzTabBarController] instead of using this scaffold.
 *
 * @param header optional content above the items in the rail and the drawer — a logo, a menu
 *   button, a title. Ignored in bottom-bar mode, where a horizontal bar has nowhere to put it.
 * @param fab optional primary action, typically a `FloatingActionButton`. It moves with the
 *   chrome: floating over the bottom-end corner above a bar, and carried into a rail or drawer.
 *   Unlike [header] it is honoured in all three modes.
 * @param fabPlacement where [fab] sits in the rail and the drawer. Ignored in bottom-bar mode.
 */
@Composable
fun AdaptiveNavigationScaffold(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    config: AdaptiveNavigationConfig = AdaptiveNavigationConfig(),
    icon: (@Composable (index: Int, selected: Boolean) -> Unit)? = null,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    fab: (@Composable () -> Unit)? = null,
    fabPlacement: FabPlacement = FabPlacement.Top,
    content: @Composable () -> Unit,
) {
    val mode = rememberNavigationMode(config)

    // The vertical modes have to host the FAB themselves: `NavigationSuiteScaffoldLayout` measures
    // `primaryActionContent` but only *places* it in its bottom-bar branch, so passing it through
    // would make it silently vanish above bottom-bar widths.
    val fabInHeader = fab != null && fabPlacement == FabPlacement.Top
    val verticalHeader: (@Composable ColumnScope.() -> Unit)? =
        if (header == null && !fabInHeader) {
            null
        } else {
            {
                header?.invoke(this)
                if (fabInHeader) {
                    if (header != null) Spacer(Modifier.height(TopContentSpacing))
                    fab!!()
                }
            }
        }
    val fabAtFoot = fab != null && fabPlacement == FabPlacement.Bottom

    // Barz builds the three components itself rather than handing items to `NavigationSuite`.
    // That wrapper drops two things Material's own components offer: `NavigationRail`'s `header`
    // slot is hardcoded to null, and the drawer's items never get
    // `NavigationDrawerItemDefaults.ItemPadding`, so they sit flush against the sheet's edges.
    // `NavigationSuiteScaffoldLayout` is the public seam that keeps the adaptive *placement* while
    // letting the caller own the component, so only the placement is delegated.
    Surface(
        modifier = modifier,
        color = NavigationSuiteScaffoldDefaults.containerColor,
        contentColor = NavigationSuiteScaffoldDefaults.contentColor,
    ) {
        NavigationSuiteScaffoldLayout(
            navigationSuiteType = mode.toSuiteType(),
            primaryActionContent = {
                if (mode == NavigationMode.BottomBar && fab != null) fab()
            },
            navigationSuite = {
                when (mode) {
                    NavigationMode.BottomBar ->
                        NavigationBar {
                            items.forEachIndexed { index, navItem ->
                                val selected = index == selectedIndex
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { onItemSelected(index) },
                                    enabled = navItem.enabled,
                                    icon = { NavigationItemIcon(navItem, index, selected, icon) },
                                    label = navItem.labelOrNull(),
                                )
                            }
                        }

                    NavigationMode.Rail ->
                        NavigationRail(header = verticalHeader) {
                            // Material reserves only 4dp above the first item and expects a header
                            // to do the spacing. With neither header nor FAB the icons end up
                            // against the top edge, so stand in for it.
                            if (verticalHeader == null) Spacer(Modifier.height(TopContentSpacing))
                            items.forEachIndexed { index, navItem ->
                                val selected = index == selectedIndex
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { onItemSelected(index) },
                                    enabled = navItem.enabled,
                                    icon = { NavigationItemIcon(navItem, index, selected, icon) },
                                    label = navItem.labelOrNull(),
                                )
                            }
                            if (fabAtFoot) {
                                // A weighted spacer rather than Arrangement.SpaceBetween: the items
                                // keep their own spacing at the top, and only the slack below them
                                // goes to the FAB.
                                Spacer(Modifier.weight(1f))
                                // Padded rather than followed by a spacer: the rail's column uses
                                // `Arrangement.spacedBy`, so a trailing sibling would also collect
                                // an arrangement gap and overshoot.
                                Box(Modifier.padding(bottom = FabEdgeInset - RailVerticalPadding)) {
                                    fab!!()
                                }
                            }
                        }

                    NavigationMode.Drawer ->
                        PermanentDrawerSheet {
                            Column(Modifier.fillMaxHeight()) {
                                // Same gutter the items below get. PermanentDrawerSheet lays its
                                // content out edge to edge, and unlike the rail it does not centre
                                // it, so an unpadded header or FAB is clipped by the sheet's edge.
                                Column(
                                    Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                ) {
                                    verticalHeader?.invoke(this)
                                }
                                Spacer(Modifier.height(TopContentSpacing))
                                items.forEachIndexed { index, navItem ->
                                    val selected = index == selectedIndex
                                    NavigationDrawerItem(
                                        modifier =
                                            Modifier.padding(
                                                NavigationDrawerItemDefaults.ItemPadding
                                            ),
                                        selected = selected,
                                        onClick = { onItemSelected(index) },
                                        icon = {
                                            NavigationItemIcon(navItem, index, selected, icon)
                                        },
                                        label = { Text(navItem.title) },
                                    )
                                }
                                if (fabAtFoot) {
                                    Spacer(Modifier.weight(1f))
                                    Box(
                                        // ItemPadding is horizontal only, so the bottom inset has
                                        // to be added; the drawer sheet contributes none of its own.
                                        Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                            .padding(bottom = FabEdgeInset)
                                    ) {
                                        fab!!()
                                    }
                                }
                            }
                        }
                }
            },
            content = content,
        )
    }
}

/** Matches Material's own header-to-items gap in `NavigationRail`. */
private val TopContentSpacing = 8.dp

/**
 * How far a [FabPlacement.Bottom] FAB sits from the window edge.
 *
 * Material specifies a rail's FAB in the *header* and nowhere else, so this position has no spec to
 * follow. 16dp is the inset `NavigationSuiteScaffoldLayout` gives the FAB above a bottom bar, which
 * is the point of `Bottom` in the first place: the FAB stays the same distance from the edge as the
 * chrome changes shape.
 */
private val FabEdgeInset = 16.dp

/**
 * Mirrors `NavigationRailVerticalPadding`, which the rail applies below its last child and which is
 * `internal` to material3 so it cannot be referenced. Subtracted rather than added, so the FAB
 * lands on [FabEdgeInset] in total rather than 4dp past it.
 */
private val RailVerticalPadding = 4.dp

private fun NavigationItem.labelOrNull(): (@Composable () -> Unit)? =
    if (showLabel) ({ Text(title) }) else null

private fun NavigationMode.toSuiteType(): NavigationSuiteType = when (this) {
    NavigationMode.BottomBar -> NavigationSuiteType.NavigationBar
    NavigationMode.Rail -> NavigationSuiteType.NavigationRail
    NavigationMode.Drawer -> NavigationSuiteType.NavigationDrawer
}
