import SwiftUI

/// One tab. Mirrors the Kotlin `NavigationItem`, minus the fields only Compose uses.
public struct BarzItem: Identifiable, Hashable, Sendable {
    public let id: String
    public let title: String
    public let systemIcon: String
    public let selectedSystemIcon: String?
    public let badge: String?
    public let isEnabled: Bool

    public init(
        id: String,
        title: String,
        systemIcon: String,
        selectedSystemIcon: String? = nil,
        badge: String? = nil,
        isEnabled: Bool = true
    ) {
        self.id = id
        self.title = title
        self.systemIcon = systemIcon
        self.selectedSystemIcon = selectedSystemIcon
        self.badge = badge
        self.isEnabled = isEnabled
    }
}

/// Mirrors Kotlin's `IosOptions`.
public struct BarzIosStyle: Sendable {
    /// Promote tabs to a sidebar on iPad via `.tabViewStyle(.sidebarAdaptable)`.
    public var sidebarAdaptable: Bool
    /// Let the tab bar shrink while scrolling. iOS 26+; ignored below.
    public var tabBarMinimizeBehavior: Bool
    /// **Not a true system opt-out.** The only real switch is the app-level
    /// `UIDesignRequiresCompatibility` Info.plist key, which a package cannot set. When false,
    /// Barz forces an opaque bar background instead of letting the system material show through.
    public var liquidGlass: Bool

    public init(
        sidebarAdaptable: Bool = true,
        tabBarMinimizeBehavior: Bool = true,
        liquidGlass: Bool = true
    ) {
        self.sidebarAdaptable = sidebarAdaptable
        self.tabBarMinimizeBehavior = tabBarMinimizeBehavior
        self.liquidGlass = liquidGlass
    }
}

/// The iOS half of Barz: a real SwiftUI `TabView`.
///
/// This exists instead of drawing a bar in Compose because only genuine system containers pick up
/// system behaviour — Liquid Glass, sidebar promotion on iPad, and the placement iPhone Duo
/// introduces. A hand-drawn bar gets none of it. Your Compose content goes inside each tab.
///
/// ```swift
/// BarzTabView(items: items, selection: $selection) { item in
///     NavigationStack { ComposeScreen(id: item.id) }
/// }
/// ```
public struct BarzTabView<Content: View>: View {
    private let items: [BarzItem]
    @Binding private var selection: String
    private let style: BarzIosStyle
    private let content: (BarzItem) -> Content

    public init(
        items: [BarzItem],
        selection: Binding<String>,
        style: BarzIosStyle = BarzIosStyle(),
        @ViewBuilder content: @escaping (BarzItem) -> Content
    ) {
        self.items = items
        self._selection = selection
        self.style = style
        self.content = content
    }

    public var body: some View {
        applyStyle(
            TabView(selection: $selection) {
                ForEach(items) { item in
                    Tab(item.title, systemImage: icon(for: item), value: item.id) {
                        content(item)
                            .toolbarBackground(
                                style.liquidGlass ? .automatic : .visible,
                                for: .tabBar
                            )
                    }
                    .badge(item.badge)
                }
            }
        )
    }

    private func icon(for item: BarzItem) -> String {
        item.id == selection ? (item.selectedSystemIcon ?? item.systemIcon) : item.systemIcon
    }

    @ViewBuilder
    private func applyStyle(_ view: some View) -> some View {
        let styled = style.sidebarAdaptable
            ? AnyView(view.tabViewStyle(.sidebarAdaptable))
            : AnyView(view)
        if #available(iOS 26.0, *), style.tabBarMinimizeBehavior {
            styled.tabBarMinimizeBehavior(.onScrollDown)
        } else {
            styled
        }
    }
}
