package dev.parez.barz.sample.android.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import dev.parez.barz.AdaptiveNavigationScaffold
import dev.parez.barz.sample.DemoCatalog
import dev.parez.barz.sample.android.ui.theme.AdaptiveNavSuiteTheme
import dev.parez.barz.sample.destinationAt
import dev.parez.barz.sample.sampleDestinations
import dev.parez.barz.sample.ui.DestinationScreen
import dev.parez.barz.sample.ui.ItemDetailPane
import dev.parez.barz.sample.AppDestination

/**
 * The sample app, now a consumer of the Barz SDK rather than an implementation of it.
 *
 * Everything that used to live here — the window-size measurement, the breakpoint guards, the
 * NavigationSuiteScaffold wiring, the string-key icon lookup — is inside
 * [AdaptiveNavigationScaffold]. What remains is what an app should actually own: which
 * destinations exist, which one is selected, and what to draw for it.
 */
@Composable
fun AdaptiveNavApp() {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    AdaptiveNavigationScaffold(
        items = sampleDestinations,
        selectedIndex = selectedIndex,
        onItemSelected = { selectedIndex = it },
        icon = { index, selected ->
            Icon(
                imageVector = sampleIcons[index][if (selected) 1 else 0],
                contentDescription = sampleDestinations[index].title,
            )
        },
    ) {
        DestinationScreen(destination = destinationAt(selectedIndex))
    }
}

/**
 * Renders every navigation state the SDK can produce, without launching the app.
 *
 * `@PreviewScreenSizes` spans phone (compact) / foldable (medium) / tablet + desktop (expanded),
 * which exercises Barz's bottom bar, rail and — at the widest spec — drawer. It also covers the
 * shared list-detail content collapsing to one pane and splitting into two.
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
 * The detail pane alone, to check the generated artwork in both colour schemes — the art is built
 * from `MaterialTheme.colorScheme`, so light and dark are genuinely different renderings rather
 * than the same image on a different background.
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

/** Outline/filled pair per destination, in the same order as [sampleDestinations]. */
private val sampleIcons = listOf(
    listOf(Icons.Outlined.Home, Icons.Filled.Home),
    listOf(Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
    listOf(Icons.Outlined.ShoppingCart, Icons.Filled.ShoppingCart),
    listOf(Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle),
)
