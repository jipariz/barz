package dev.parez.barz

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

/**
 * A navigation bar that renders in the platform's idiom.
 *
 * Drop-in replacement for a Material 3 `NavigationBar`:
 *
 * ```
 * Scaffold(
 *     bottomBar = {
 *         AdaptiveNavigationBar(
 *             items = navItems,
 *             selectedIndex = selectedIndex,
 *             onItemSelected = { selectedIndex = it },
 *         )
 *     },
 * ) { … }
 * ```
 *
 * This always draws a bottom bar. For chrome that becomes a rail or a drawer as the window grows,
 * use [AdaptiveNavigationScaffold] instead.
 */
@Composable
fun AdaptiveNavigationBar(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    config: AdaptiveNavigationConfig = AdaptiveNavigationConfig(),
    icon: (@Composable (index: Int, selected: Boolean) -> Unit)? = null,
    colors: AdaptiveNavigationBarColors = AdaptiveNavigationBarDefaults.colors(),
) {
    val glass = currentPlatform == BarzPlatform.Ios &&
        config.ios.chrome == IosChrome.ComposeGlass &&
        config.ios.liquidGlass
    if (glass) {
        // An approximation of the system material, not the real thing — see IosOptions.liquidGlass.
        Surface(
            modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = colors.containerColor.copy(alpha = 0.72f),
            tonalElevation = 3.dp,
        ) {
            BarContent(items, selectedIndex, onItemSelected, icon, colors, Color.Transparent)
        }
    } else {
        BarContent(items, selectedIndex, onItemSelected, icon, colors, colors.containerColor, modifier)
    }
}

/**
 * Overload for apps whose icons are [ImageVector]s (`Icons.Default.*`) rather than Compose
 * resources. Same behaviour; [NavigationItem.icon] is ignored in favour of [icon].
 */
@Composable
fun AdaptiveNavigationBar(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    icon: (index: Int, selected: Boolean) -> ImageVector,
    modifier: Modifier = Modifier,
    colors: AdaptiveNavigationBarColors = AdaptiveNavigationBarDefaults.colors(),
) {
    NavigationBar(modifier = modifier, containerColor = colors.containerColor) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(index) },
                enabled = item.enabled,
                icon = {
                    ItemBadge(item) {
                        Icon(
                            imageVector = icon(index, selected),
                            contentDescription = item.contentDescription ?: item.title,
                        )
                    }
                },
                label = if (item.showLabel) ({ Text(item.title) }) else null,
                colors = colors.itemColors(),
            )
        }
    }
}

@Composable
private fun BarContent(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    icon: (@Composable (index: Int, selected: Boolean) -> Unit)?,
    colors: AdaptiveNavigationBarColors,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier, containerColor = containerColor) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(index) },
                enabled = item.enabled,
                icon = { ItemBadge(item) { NavigationItemIcon(item, index, selected, icon) } },
                label = if (item.showLabel) ({ Text(item.title) }) else null,
                colors = colors.itemColors(),
            )
        }
    }
}

/** Wraps [content] in a badge when the item asks for one. Text badge wins over the dot. */
@Composable
private fun ItemBadge(item: NavigationItem, content: @Composable () -> Unit) {
    when {
        item.badge != null -> BadgedBox(badge = { Badge { Text(item.badge) } }) { content() }
        item.showBadgeDot -> BadgedBox(badge = { Badge() }) { content() }
        else -> Box { content() }
    }
}

internal fun NavigationItem.iconFor(selected: Boolean) =
    if (selected) selectedIcon ?: icon else icon

@Composable
private fun AdaptiveNavigationBarColors.itemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = selectedIconColor,
    selectedTextColor = selectedTextColor,
    unselectedIconColor = unselectedIconColor,
    unselectedTextColor = unselectedTextColor,
    indicatorColor = indicatorColor,
)
