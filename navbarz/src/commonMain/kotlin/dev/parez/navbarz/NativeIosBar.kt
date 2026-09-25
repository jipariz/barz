package dev.parez.navbarz

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Renders the platform's own navigation bar view, where one exists.
 *
 * Only iOS implements this — it produces a real `UITabBar`, which is what makes Liquid Glass the
 * genuine system material rather than an approximation. Every other target falls back to the
 * Compose bar, which is the right answer there anyway.
 *
 * Note this gives native *rendering*, not native *placement*: an embedded `UITabBar` is laid out by
 * Compose, so the system cannot relocate it the way it relocates a `UITabBarController`'s bar on a
 * foldable. For that, root the app in [navBarzTabBarController] instead.
 */
internal expect val supportsNativeBar: Boolean

@Composable
internal expect fun NativeIosBar(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    colors: AdaptiveNavigationBarColors,
    options: IosBarOptions,
    modifier: Modifier,
)
