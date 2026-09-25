package dev.parez.barz

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * A navigation bar that renders in the platform's idiom.
 *
 * Drop-in replacement for a Material 3 `NavigationBar`:
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
    // Prefer the platform's own bar view: a real UITabBar renders with the genuine system
    // material, which nothing drawn in Compose can reproduce. Falls through to the Compose bar
    // everywhere else, and whenever the caller asks for it explicitly.
    if (supportsNativeBar && config.ios.chrome == IosChrome.NativeTabBar) {
        NativeIosBar(
            items = items,
            selectedIndex = selectedIndex,
            onItemSelected = onItemSelected,
            colors = colors,
            options = config.ios,
            modifier = modifier.fillMaxWidth().height(config.ios.nativeBarHeight),
        )
        return
    }

    val glass = currentPlatform == BarzPlatform.Ios && config.ios.liquidGlass
    if (glass) {
        // An approximation of the system material, not the real thing — see IosOptions.liquidGlass.
        Surface(
            modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = colors.containerColor.copy(alpha = 0.72f),
            // Explicit, because `copy(alpha = …)` produces a colour that matches no colorScheme
            // entry, so Surface's own contentColorFor lookup returns Unspecified — and an Icon with
            // an Unspecified tint applies no ColorFilter at all, drawing caller icons in their
            // source colours instead of the theme's.
            contentColor = colors.unselectedIconColor,
            tonalElevation = 3.dp,
        ) {
            // The pill already sits inside the caller's insets; letting the inner NavigationBar
            // apply the system bottom inset again inflates it by the gesture-bar height.
            BarContent(
                items,
                selectedIndex,
                onItemSelected,
                icon,
                colors,
                Color.Transparent,
                windowInsets = WindowInsets(0),
            )
        }
    } else {
        BarContent(
            items,
            selectedIndex,
            onItemSelected,
            icon,
            colors,
            colors.containerColor,
            modifier,
        )
    }
}

/**
 * Overload for apps whose icons are [ImageVector]s (`Icons.Default.*`) rather than Compose
 * resources. [NavigationItem.icon] is ignored in favour of [icon].
 *
 * Delegates rather than duplicating: as its own implementation it bypassed the native-bar and glass
 * branches entirely, so an iOS app that happened to use ImageVector icons silently got a plain
 * Compose bar with no way to tell.
 */
@Composable
fun AdaptiveNavigationBar(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    icon: (index: Int, selected: Boolean) -> ImageVector,
    modifier: Modifier = Modifier,
    config: AdaptiveNavigationConfig = AdaptiveNavigationConfig(),
    colors: AdaptiveNavigationBarColors = AdaptiveNavigationBarDefaults.colors(),
) {
    // Typed explicitly: both overloads take an `icon` in the same position, so an un-annotated
    // lambda resolves back to this one.
    val slot: @Composable (index: Int, selected: Boolean) -> Unit = { index, selected ->
        val item = items[index]
        Icon(
            imageVector = icon(index, selected),
            contentDescription =
                if (item.showLabel) null else item.contentDescription ?: item.title,
        )
    }
    AdaptiveNavigationBar(
        items = items,
        selectedIndex = selectedIndex,
        onItemSelected = onItemSelected,
        modifier = modifier,
        config = config,
        icon = slot,
        colors = colors,
    )
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
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = containerColor,
        windowInsets = windowInsets,
    ) {
        // Hoisted: this was being rebuilt once per item, per recomposition.
        val itemColors = colors.itemColors()
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(index) },
                enabled = item.enabled,
                icon = {
                    NavigationItemIcon(
                        item,
                        index,
                        selected,
                        icon,
                        badgeContainerColor = colors.badgeContainerColor,
                        badgeContentColor = colors.badgeContentColor,
                    )
                },
                label = if (item.showLabel) ({ Text(item.title) }) else null,
                colors = colors.itemColors(),
            )
        }
    }
}

internal fun NavigationItem.iconFor(selected: Boolean) =
    if (selected) selectedIcon ?: icon else icon

@Composable
private fun AdaptiveNavigationBarColors.itemColors() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = selectedIconColor,
        selectedTextColor = selectedTextColor,
        unselectedIconColor = unselectedIconColor,
        unselectedTextColor = unselectedTextColor,
        indicatorColor = indicatorColor,
    )
