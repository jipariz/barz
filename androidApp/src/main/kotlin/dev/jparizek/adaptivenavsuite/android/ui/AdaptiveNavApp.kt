package dev.jparizek.adaptivenavsuite.android.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_LARGE_LOWER_BOUND
import dev.jparizek.adaptivenavsuite.AppDestination
import dev.jparizek.adaptivenavsuite.DemoCatalog
import dev.jparizek.adaptivenavsuite.android.ui.icons.icon
import dev.jparizek.adaptivenavsuite.android.ui.theme.AdaptiveNavSuiteTheme
import dev.jparizek.adaptivenavsuite.ui.DestinationScreen
import dev.jparizek.adaptivenavsuite.ui.ItemDetailPane

/**
 * Root composable — the Android half of the "adaptive navigation" story.
 *
 * `NavigationSuiteScaffold` (from `material3-adaptive-navigation-suite`) automatically renders a
 * bottom [androidx.compose.material3.NavigationBar] in compact windows and on tabletop-postured
 * foldables, and a [androidx.compose.material3.NavigationRail] in medium/expanded windows — this
 * is exactly the behavior described in
 * https://developer.android.com/develop/adaptive-apps/guides/build-adaptive-navigation
 *
 * On top of the default behavior, this sample also shows how to *customize* the navigation type:
 * once the window is wide enough to be considered "expanded" (e.g. a tablet in landscape, or a
 * desktop-sized window), we upgrade the rail to a permanent [NavigationSuiteType.NavigationDrawer]
 * with visible labels, which is more comfortable to use at that width.
 */
@Composable
fun AdaptiveNavApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.startDestination) }

    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val navSuiteType = with(adaptiveInfo) {
        // Two guards on the drawer override.
        //
        // Width: LARGE (1200dp), not EXPANDED (840dp). A permanent drawer is ~40% of an unfolded
        // foldable's width, which leaves the content's own list-detail layout too little room —
        // the Favorites grid collapses to a single column. Below 1200dp the rail is the better
        // trade, and the defaults already pick it.
        //
        // Height: a phone in landscape is wide (~891dp) but short (~411dp), and
        // NavigationSuiteScaffoldDefaults sends exactly that case to a bottom bar. Without this
        // the override would hand a 411dp-tall window a permanent drawer.
        if (
            windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_LARGE_LOWER_BOUND) &&
            windowSizeClass.isHeightAtLeastBreakpoint(HEIGHT_DP_MEDIUM_LOWER_BOUND)
        ) {
            NavigationSuiteType.NavigationDrawer
        } else {
            NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestination.entries.forEach { destination ->
                val selected = destination == currentDestination
                item(
                    icon = {
                        Icon(
                            imageVector = destination.icon(selected = selected),
                            contentDescription = destination.contentDescription,
                        )
                    },
                    label = { Text(destination.title) },
                    selected = selected,
                    onClick = { currentDestination = destination },
                )
            }
        },
        layoutType = navSuiteType,
    ) {
        // The screen content itself is the shared Compose Multiplatform `DestinationScreen` —
        // only the surrounding `NavigationSuiteScaffold` chrome is Android-native.
        DestinationScreen(destination = currentDestination)
    }
}

/**
 * Renders every navigation state the scaffold can produce, without launching the app.
 *
 * `@PreviewScreenSizes` spans phone (compact width) / foldable (medium) / tablet + desktop
 * (expanded), which is exactly the three nav treatments: bottom `NavigationBar`, `NavigationRail`,
 * and the permanent `NavigationDrawer` forced above the expanded breakpoint. This works because
 * `currentWindowAdaptiveInfoV2()` derives the size class from `LocalWindowInfo.containerSize`,
 * which the preview canvas populates from the device spec — no fake window plumbing needed.
 *
 * It now also covers the second adaptive axis for free: the shared `ListDetailPaneScaffold` inside
 * the content slot collapses to one pane on the phone specs and splits into two on the wide ones.
 */
@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun AdaptiveNavAppPreview() {
    AdaptiveNavSuiteTheme(dynamicColor = false) {
        AdaptiveNavApp()
    }
}

/**
 * The detail pane on its own, to check the generated artwork in both color schemes — the art is
 * built entirely from `MaterialTheme.colorScheme`, so light and dark are genuinely different
 * renderings rather than the same image on a different background.
 */
@PreviewLightDark
@Composable
private fun ItemDetailPanePreview() {
    AdaptiveNavSuiteTheme(dynamicColor = false) {
        Surface {
            ItemDetailPane(
                item = DemoCatalog.items(AppDestination.HOME).first(),
                showBack = true,
                onBack = {},
            )
        }
    }
}
