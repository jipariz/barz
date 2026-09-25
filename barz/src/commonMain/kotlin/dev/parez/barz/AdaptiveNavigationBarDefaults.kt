package dev.parez.barz

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Colours for the navigation chrome. Build one with [AdaptiveNavigationBarDefaults.colors]. */
@Immutable
data class AdaptiveNavigationBarColors(
    val containerColor: Color,
    val indicatorColor: Color,
    val selectedIconColor: Color,
    val selectedTextColor: Color,
    val unselectedIconColor: Color,
    val unselectedTextColor: Color,
    val badgeContainerColor: Color,
    val badgeContentColor: Color,
)

object AdaptiveNavigationBarDefaults {

    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
        indicatorColor: Color = MaterialTheme.colorScheme.secondaryContainer,
        selectedIconColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
        selectedTextColor: Color = MaterialTheme.colorScheme.onSurface,
        unselectedIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        badgeContainerColor: Color = MaterialTheme.colorScheme.error,
        badgeContentColor: Color = MaterialTheme.colorScheme.onError,
    ): AdaptiveNavigationBarColors =
        AdaptiveNavigationBarColors(
            containerColor = containerColor,
            indicatorColor = indicatorColor,
            selectedIconColor = selectedIconColor,
            selectedTextColor = selectedTextColor,
            unselectedIconColor = unselectedIconColor,
            unselectedTextColor = unselectedTextColor,
            badgeContainerColor = badgeContainerColor,
            badgeContentColor = badgeContentColor,
        )

    /**
     * Builds a config, optionally overriding it per platform.
     *
     * This is how you say "desktop should always use a drawer, phones should never leave the bottom
     * bar" from shared code:
     * ```
     * val config = AdaptiveNavigationBarDefaults.config(
     *     android = AdaptiveNavigationConfig(allowedModes = setOf(NavigationMode.BottomBar)),
     *     desktop = AdaptiveNavigationConfig(allowedModes = setOf(NavigationMode.Drawer)),
     * )
     * ```
     *
     * Only the override matching [currentPlatform] is used; the rest are ignored, so this is safe
     * to call from `commonMain` without any expect/actual of your own.
     */
    fun config(
        default: AdaptiveNavigationConfig = AdaptiveNavigationConfig(),
        android: AdaptiveNavigationConfig? = null,
        desktop: AdaptiveNavigationConfig? = null,
        web: AdaptiveNavigationConfig? = null,
        ios: AdaptiveNavigationConfig? = null,
    ): AdaptiveNavigationConfig =
        when (currentPlatform) {
            BarzPlatform.Android -> android
            BarzPlatform.Desktop -> desktop
            BarzPlatform.Web -> web
            BarzPlatform.Ios -> ios
        } ?: default
}
