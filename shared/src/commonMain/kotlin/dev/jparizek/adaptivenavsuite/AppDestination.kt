package dev.jparizek.adaptivenavsuite

/**
 * The app's top-level destinations, shared between the Android and iOS front ends.
 *
 * Only *data* lives here — no icon rendering, no navigation-chrome decisions. Each platform maps
 * [materialIconName]/[materialSelectedIconName] or [systemImageName] to its own native icon type
 * (Compose `ImageVector` on Android, SF Symbol on iOS) and renders it inside whichever native
 * adaptive container is appropriate for the current window size:
 *  - Android: `NavigationSuiteScaffold` (bar / rail / drawer)
 *  - iOS: `TabView` (tab bar / sidebar, and eventually the iPhone Duo vertical bar)
 *
 * Keeping the destination list here means both apps stay in sync on *what* the destinations are,
 * while each platform stays fully native on *how* they are presented.
 */
enum class AppDestination(
    val id: String,
    val title: String,
    val materialIconName: String,
    val materialSelectedIconName: String,
    val systemImageName: String,
    val contentDescription: String = title,
) {
    HOME(
        id = "home",
        title = "Home",
        materialIconName = "home_outline",
        materialSelectedIconName = "home_filled",
        systemImageName = "house",
    ),
    FAVORITES(
        id = "favorites",
        title = "Favorites",
        materialIconName = "favorite_outline",
        materialSelectedIconName = "favorite_filled",
        systemImageName = "heart",
    ),
    SHOPPING(
        id = "shopping",
        title = "Shopping",
        materialIconName = "shopping_cart_outline",
        materialSelectedIconName = "shopping_cart_filled",
        systemImageName = "cart",
    ),
    PROFILE(
        id = "profile",
        title = "Profile",
        materialIconName = "account_circle_outline",
        materialSelectedIconName = "account_circle_filled",
        systemImageName = "person.crop.circle",
    );

    companion object {
        val startDestination: AppDestination get() = HOME
    }
}

/**
 * `AppDestination.entries` (Kotlin's `EnumEntries`) doesn't bridge cleanly to Swift. Exposing a
 * plain `List<AppDestination>` here is Swift-friendly: Kotlin's `List` is automatically bridged to
 * a native Swift `[AppDestination]`, so `AppDestinationKt.allAppDestinations()` can be used
 * directly in a SwiftUI `ForEach`.
 */
fun allAppDestinations(): List<AppDestination> = AppDestination.entries

