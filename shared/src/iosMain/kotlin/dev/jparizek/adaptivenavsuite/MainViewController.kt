package dev.jparizek.adaptivenavsuite

import androidx.compose.ui.window.ComposeUIViewController
import dev.jparizek.adaptivenavsuite.ui.DestinationScreen

/**
 * Hosts the shared [DestinationScreen] Compose Multiplatform content inside a native
 * `UIViewController`, so it can be embedded (via `UIViewControllerRepresentable`) as the content
 * of a tab in SwiftUI's native `TabView`/`NavigationStack` — see `DestinationDetailView.swift`.
 *
 * The nav chrome around this content stays 100% native SwiftUI; only the screen content itself is
 * Compose Multiplatform.
 */
fun destinationViewController(destination: AppDestination) = ComposeUIViewController {
    DestinationScreen(destination = destination)
}
