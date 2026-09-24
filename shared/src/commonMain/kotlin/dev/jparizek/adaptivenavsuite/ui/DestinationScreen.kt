package dev.jparizek.adaptivenavsuite.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.jparizek.adaptivenavsuite.AppDestination
import dev.jparizek.adaptivenavsuite.DestinationContent

/**
 * The actual screen content, written once in Compose Multiplatform and reused verbatim on both
 * platforms:
 * - On Android, it is composed directly inside `NavigationSuiteScaffold`'s content slot.
 * - On iOS, it is hosted via `ComposeUIViewController` (see `MainViewController.kt` in
 *   `iosMain`) and wrapped in a `UIViewControllerRepresentable`, embedded as the content of a
 *   native `NavigationStack` tab.
 *
 * Only the navigation chrome around this screen differs per platform (native
 * `NavigationSuiteScaffold` on Android, native `TabView`/`NavigationStack` on iOS) — the screen
 * itself is fully shared.
 */
@Composable
fun DestinationScreen(destination: AppDestination, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = DestinationContent.headline(destination),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = DestinationContent.body(destination),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
