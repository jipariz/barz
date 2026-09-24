import BarzSample
import SwiftUI

/// `AppDestination` comes from the shared Kotlin Multiplatform module. Its `id: String` property
/// (already shared with Android) is reused to satisfy `Identifiable` so it can drive `TabView`'s
/// selection binding and `ForEach` directly, with no duplicated Swift-side model. `Hashable` is
/// already provided by Kotlin/Native's bridging of `enum class` (via its `KotlinEnum` superclass),
/// so no extension is needed for that one — adding one conflicts with the inherited conformance.
extension AppDestination: @retroactive Identifiable {}

/// The iOS half of the "adaptive navigation" story.
///
/// `TabView` + `.tabViewStyle(.sidebarAdaptable)` is Apple's supported system container for
/// exactly this spread of idioms: a bottom tab bar on iPhone, with the same tabs promotable to a
/// sidebar on iPad — see
/// https://developer.apple.com/documentation/human-interface-guidelines/tab-bars
///
/// This is also why it's the right starting point for the iPhone Duo. Per Apple's "Preparing your
/// app for iPhone Duo" guidance, the vertical/sidebar placement Duo introduces is adopted
/// automatically by system containers (`TabView`, `NavigationSplitView`, standard toolbars) built
/// like this one — hand-rolled bars do not get it for free. There is no Duo-specific code to write
/// here today; see the README for the forward-looking `.axisBehavior(_:)` toolbar modifier to
/// adopt once building against the SDK that ships it.
struct RootView: View {
    @State private var selection = AppDestination.companion.startDestination

    var body: some View {
        TabView(selection: $selection) {
            ForEach(AppDestinationKt.allAppDestinations()) { destination in
                Tab(destination.title, systemImage: destination.systemImageName, value: destination) {
                    NavigationStack {
                        DestinationDetailView(destination: destination)
                            .navigationTitle(destination.title)
                    }
                }
            }
        }
        .tabViewStyle(.sidebarAdaptable)
    }
}

#Preview {
    RootView()
}
