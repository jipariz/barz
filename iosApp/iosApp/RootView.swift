import BarzSample
import SwiftUI

/// The entire iOS UI comes from Kotlin.
///
/// `barzSampleRootViewController()` returns a real `UITabBarController` built by Barz out of UIKit,
/// with the shared Compose screens inside each tab. Swift hosts it and nothing else — there is no
/// SwiftUI navigation chrome here, and no Swift package to add: the Gradle dependency carries the
/// whole thing.
struct RootView: View {
    var body: some View {
        BarzRoot()
            .ignoresSafeArea()
    }
}

private struct BarzRoot: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.barzSampleRootViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

#Preview {
    RootView()
}
