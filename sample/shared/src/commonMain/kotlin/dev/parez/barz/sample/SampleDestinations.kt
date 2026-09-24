package dev.parez.barz.sample

import dev.parez.barz.NavigationItem

/**
 * The sample's destinations, expressed with the SDK's own type.
 *
 * No `icon` is set here. The Compose resources this module ships are not packaged into the
 * consuming Android app — AGP's `com.android.kotlin.multiplatform.library` plugin does not merge a
 * KMP library's compose resources into an app's assets — so the sample supplies its icons through
 * the composable `icon` slot instead. `systemIcon` is still set, because the iOS Swift layer reads
 * it directly and never touches the Compose side.
 */
val sampleDestinations: List<NavigationItem> = listOf(
    NavigationItem(title = "Home", systemIcon = "house", selectedSystemIcon = "house.fill"),
    NavigationItem(
        title = "Favorites",
        systemIcon = "heart",
        selectedSystemIcon = "heart.fill",
        badge = "3",
    ),
    NavigationItem(title = "Shopping", systemIcon = "cart", selectedSystemIcon = "cart.fill"),
    NavigationItem(
        title = "Profile",
        systemIcon = "person.crop.circle",
        selectedSystemIcon = "person.crop.circle.fill",
    ),
)

/** Maps a tab index back to the demo content enum. */
fun destinationAt(index: Int): AppDestination = AppDestination.entries[index]
