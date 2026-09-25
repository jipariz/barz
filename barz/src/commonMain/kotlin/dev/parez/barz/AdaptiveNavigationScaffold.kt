package dev.parez.barz

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Navigation chrome that changes shape with the window: a bottom bar when compact, a rail when
 * medium, a permanent drawer when genuinely wide.
 *
 * ```
 * AdaptiveNavigationScaffold(
 *     items = navItems,
 *     selectedIndex = selectedIndex,
 *     onItemSelected = { selectedIndex = it },
 * ) {
 *     CurrentScreen(selectedIndex)
 * }
 * ```
 *
 * Use [AdaptiveNavigationBar] instead if you want a plain bottom bar in a `Scaffold`, and
 * [rememberNavigationMode] if you want the decision without the container.
 *
 * On iOS this renders the Compose container, not a `UITabBarController` — the rail and drawer have
 * no UIKit equivalent, so there is nothing native to defer to above bottom-bar widths. If you want
 * the system to own the bar (and reposition it on a foldable), root the app in
 * [barzTabBarController] instead of using this scaffold.
 *
 * @param header optional content above the items in the rail and the drawer — a logo, a menu
 *   button, a title. Ignored in bottom-bar mode, where a horizontal bar has nowhere to put it.
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
    content: @Composable () -> Unit,
) {
    val mode = rememberNavigationMode(config)

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
                        NavigationRail(
                            header = header,
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                        ) {
                            // Material reserves only 4dp above the first item and expects a header
                            // to do the spacing. Without one the icons end up against the top
                            // edge, so stand in for it.
                            if (header == null) Spacer(Modifier.height(TopContentSpacing))
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
                        }

                    NavigationMode.Drawer ->
                        PermanentDrawerSheet {
                            Column(Modifier.verticalScroll(rememberScrollState())) {
                                // Same gutter the items below get. PermanentDrawerSheet lays its
                                // content out edge to edge, and unlike the rail it does not centre
                                // it, so an unpadded header is clipped by the sheet's edge.
                                Column(Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)) {
                                    header?.invoke(this)
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
                                        // NavigationDrawerItem has no `enabled`; approximate it so
                                        // a disabled item is at least inert and announced as such.
                                        onClick = { if (navItem.enabled) onItemSelected(index) },
                                        icon = {
                                            NavigationItemIcon(
                                                navItem,
                                                index,
                                                selected,
                                                icon,
                                                showBadge = false,
                                            )
                                        },
                                        // The drawer has a dedicated badge slot at the row's end;
                                        // routing badges through the icon would overlap them.
                                        badge = navItem.badgeLabel(),
                                        label = { if (navItem.showLabel) Text(navItem.title) },
                                    )
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
 * @Composable so the returned lambda is compiler-memoized. As a plain function it allocated a fresh
 *   lambda per call, changing the `label` parameter's identity every recomposition and stopping the
 *   Material item from ever skipping.
 */
@Composable
private fun NavigationItem.labelOrNull(): (@Composable () -> Unit)? =
    if (showLabel) ({ Text(title) }) else null

private fun NavigationMode.toSuiteType(): NavigationSuiteType =
    when (this) {
        NavigationMode.BottomBar -> NavigationSuiteType.NavigationBar
        NavigationMode.Rail -> NavigationSuiteType.NavigationRail
        NavigationMode.Drawer -> NavigationSuiteType.NavigationDrawer
    }

/** The drawer renders badges in its own end slot rather than over the icon. */
@Composable
private fun NavigationItem.badgeLabel(): (@Composable () -> Unit)? =
    when {
        badge != null -> ({ Text(badge) })
        showBadgeDot -> ({ Text("") })
        else -> null
    }
