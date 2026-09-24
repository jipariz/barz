package dev.parez.barz.sample

/**
 * One row in a destination's list pane.
 *
 * [artSeed] is the only "visual" field, and it isn't a color or an asset — it seeds the
 * deterministic generator in `ui/ShapeArt.kt`. Keeping it an `Int` means the shared module still
 * holds no rendering knowledge: the same seed produces the same composition on both platforms,
 * but what it actually looks like is decided by the theme at draw time.
 */
data class DemoItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val body: String,
    val artSeed: Int,
)

/**
 * Mock content for the demo. Deliberately hand-written rather than generated: the list needs
 * enough variation in title length to show how the panes behave at different widths, which
 * `"Item 1".."Item 6"` would not.
 */
object DemoCatalog {

    private val home = listOf(
        item("home-1", "Morning Ridge", "Trail · 4.2 km"),
        item("home-2", "Harbour Light", "Coast · 1.8 km"),
        item("home-3", "Fern Hollow and the Long Way Round", "Forest · 6.0 km"),
        item("home-4", "Quarry Steps", "Climb · 0.9 km"),
        item("home-5", "Saltmarsh Loop", "Wetland · 3.4 km"),
        item("home-6", "Beacon Hill", "Summit · 5.1 km"),
    )

    private val favorites = listOf(
        item("fav-1", "Slate and Ochre", "Saved in March"),
        item("fav-2", "Low Tide", "Saved in April"),
        item("fav-3", "Paper Lanterns", "Saved in April"),
        item("fav-4", "The Winter Sequence, Parts I–IV", "Saved in May"),
        item("fav-5", "Copper Wire", "Saved in June"),
    )

    private val shopping = listOf(
        item("shop-1", "Enamel Mug", "2 × · in stock"),
        item("shop-2", "Canvas Field Bag", "1 × · ships Friday"),
        item("shop-3", "Graphite Set", "1 × · in stock"),
        item("shop-4", "Folding Stool", "1 × · backordered"),
        item("shop-5", "Linen Cloth, Undyed", "3 × · in stock"),
        item("shop-6", "Brass Compass", "1 × · in stock"),
    )

    private val profile = listOf(
        item("prof-1", "Account", "Name, email, password"),
        item("prof-2", "Appearance", "Theme and text size"),
        item("prof-3", "Notifications", "Quiet hours on"),
        item("prof-4", "Downloads", "Wi-Fi only"),
        item("prof-5", "Privacy and Data Sharing", "Review your choices"),
    )

    private val byDestination: Map<AppDestination, List<DemoItem>> = mapOf(
        AppDestination.HOME to home,
        AppDestination.FAVORITES to favorites,
        AppDestination.SHOPPING to shopping,
        AppDestination.PROFILE to profile,
    )

    private val byId: Map<String, DemoItem> =
        byDestination.values.flatten().associateBy { it.id }

    fun items(destination: AppDestination): List<DemoItem> =
        byDestination.getValue(destination)

    fun item(id: String): DemoItem? = byId[id]

    /**
     * Derives the seed and body copy from the id so the call sites above stay readable. The seed
     * is the id's `hashCode`, which is stable for a given string within a run and identical
     * across platforms for ASCII — good enough for decorative art, and it means adding an item
     * never requires picking a magic number by hand.
     */
    private fun item(id: String, title: String, subtitle: String) = DemoItem(
        id = id,
        title = title,
        subtitle = subtitle,
        body = "Placeholder detail copy for \"$title\". This pane exists to show how the " +
            "list-detail layout behaves as the window changes size — on a phone it replaces " +
            "the list, on a tablet or unfolded foldable it sits beside it. The artwork above " +
            "is generated from this item's seed and colored entirely from the active " +
            "MaterialTheme, so it follows light/dark and Material You without any fixed colors.",
        artSeed = id.hashCode(),
    )
}
