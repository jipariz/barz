package dev.jparizek.adaptivenavsuite.android.ui

import androidx.compose.material3.Icon
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
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import dev.jparizek.adaptivenavsuite.AppDestination
import dev.jparizek.adaptivenavsuite.android.ui.icons.icon
import dev.jparizek.adaptivenavsuite.android.ui.theme.AdaptiveNavSuiteTheme
import dev.jparizek.adaptivenavsuite.ui.DestinationScreen

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
        // Width alone isn't enough: a phone in landscape is wide (~891dp) but short (~411dp).
        // NavigationSuiteScaffoldDefaults would give it a bottom bar for exactly that reason, so
        // the drawer override has to respect the same compact-height guard or it hands a 411dp-tall
        // window a permanent drawer.
        if (
            windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND) &&
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
 * `currentWindowAdaptiveInfo()` derives the size class from `LocalWindowInfo.containerSize`, which
 * the preview canvas populates from the device spec — no fake window plumbing needed.
 */
@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun AdaptiveNavAppPreview() {
    AdaptiveNavSuiteTheme(dynamicColor = false) {
        AdaptiveNavApp()
    }
}
