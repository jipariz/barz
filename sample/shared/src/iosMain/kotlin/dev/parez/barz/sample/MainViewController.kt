package dev.parez.barz.sample

import androidx.compose.ui.window.ComposeUIViewController
import dev.parez.barz.sample.ui.DestinationScreen
import dev.parez.barz.sample.ui.theme.AppTheme

/**
 * Hosts the shared [DestinationScreen] Compose Multiplatform content inside a native
 * `UIViewController`, so it can be embedded (via `UIViewControllerRepresentable`) as the content
 * of a tab in SwiftUI's native `TabView`/`NavigationStack` — see `DestinationDetailView.swift`.
 *
 * The nav chrome around this content stays 100% native SwiftUI; only the screen content itself is
 * Compose Multiplatform.
 *
 * [AppTheme] is not optional here: `ComposeUIViewController` installs no Material composition
 * locals, so without it `MaterialTheme.colorScheme` resolves to `lightColorScheme()` no matter
 * what the system appearance is. It is also what gives the iOS side dark mode.
 */
fun destinationViewController(destination: AppDestination) = ComposeUIViewController {
    AppTheme {
        DestinationScreen(destination = destination)
    }
}
