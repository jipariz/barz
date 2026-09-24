import BarzSample
import SwiftUI
import UIKit

/// Wraps the shared Compose Multiplatform screen (`dev.parez.barz.sample.ui
/// .DestinationScreen`) — hosted via `ComposeUIViewController` in `MainViewController.kt` — as a
/// native `UIViewController`, so it can be dropped straight into SwiftUI content.
///
/// The nav chrome hosting this view (`RootView`'s `TabView`/`NavigationStack`) stays 100% native
/// SwiftUI; only the screen content is Compose Multiplatform, mirroring how the Android app
/// composes the very same `DestinationScreen` inside its native `NavigationSuiteScaffold`.
private struct ComposeDestinationView: UIViewControllerRepresentable {
    let destination: AppDestination

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.destinationViewController(destination: destination)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct DestinationDetailView: View {
    let destination: AppDestination

    var body: some View {
        ComposeDestinationView(destination: destination)
            .ignoresSafeArea()
    }
}
