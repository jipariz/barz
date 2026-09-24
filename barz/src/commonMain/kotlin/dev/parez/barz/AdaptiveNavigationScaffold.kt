package dev.parez.barz

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource

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
 * On iOS with [IosChrome.NativeTabView] this still renders the Compose container — the native
 * `TabView` lives in the companion Swift package and wraps your Compose *content*, not the other
 * way round. See the README's iOS section.
 */
@Composable
fun AdaptiveNavigationScaffold(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    config: AdaptiveNavigationConfig = AdaptiveNavigationConfig(),
    icon: (@Composable (index: Int, selected: Boolean) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    NavigationSuiteScaffold(
        modifier = modifier,
        layoutType = rememberNavigationMode(config).toSuiteType(),
        navigationSuiteItems = {
            items.forEachIndexed { index, navItem ->
                val selected = index == selectedIndex
                item(
                    selected = selected,
                    onClick = { onItemSelected(index) },
                    enabled = navItem.enabled,
                    icon = { NavigationItemIcon(navItem, index, selected, icon) },
                    label = if (navItem.showLabel) ({ Text(navItem.title) }) else null,
                )
            }
        },
        content = content,
    )
}

private fun NavigationMode.toSuiteType(): NavigationSuiteType = when (this) {
    NavigationMode.BottomBar -> NavigationSuiteType.NavigationBar
    NavigationMode.Rail -> NavigationSuiteType.NavigationRail
    NavigationMode.Drawer -> NavigationSuiteType.NavigationDrawer
}
