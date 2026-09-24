package dev.parez.barz.sample.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items as columnItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import dev.parez.barz.sample.AppDestination
import dev.parez.barz.sample.DemoCatalog
import dev.parez.barz.sample.DemoItem
import dev.parez.barz.sample.DestinationContent
import dev.parez.barz.sample.ui.theme.LocalAppSpacing

/**
 * The screen content, written once in Compose Multiplatform and reused verbatim on both platforms:
 * - On Android, composed directly inside `NavigationSuiteScaffold`'s content slot.
 * - On iOS, hosted via `ComposeUIViewController` (see `MainViewController.kt`) and embedded as the
 *   content of a native `NavigationStack` tab.
 *
 * Every label here is a shape rather than a string. That is deliberate: the subject of the sample
 * is the navigation chrome and the way panes reflow, so the content reads as *structure* without
 * ever asking to be read. The real strings still live in `DemoCatalog` and are attached as
 * semantics, so a screen reader gets the full picture.
 *
 * Three of the four destinations use [ListDetailPaneScaffold], each with a visibly different list
 * treatment; [AppDestination.PROFILE] deliberately does not.
 */
@Composable
fun DestinationScreen(destination: AppDestination, modifier: Modifier = Modifier) {
    // Edge-to-edge: the Surface fills the entire window so its background runs under the status
    // and navigation bars. Only the *horizontal* insets are taken as layout padding here — doing
    // that once at the root avoids double-applying them to both panes of a two-pane layout.
    // Vertical insets are handed to each scroll container as content padding instead, so content
    // scrolls under the system bars rather than being boxed in by them.
    Surface(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
            ),
        ) {
            when (destination) {
                AppDestination.PROFILE -> ProfilePane()
                else -> ListDetailContent(destination)
            }
        }
    }
}

/** Per-destination presentation, so the four screens do not read as one screen four times. */
private enum class ListStyle { Hero, Grid, Compact }

private val AppDestination.listStyle: ListStyle
    get() = when (this) {
        AppDestination.HOME -> ListStyle.Hero
        AppDestination.FAVORITES -> ListStyle.Grid
        AppDestination.SHOPPING -> ListStyle.Compact
        AppDestination.PROFILE -> ListStyle.Compact // unused: PROFILE never reaches here
    }

private val AppDestination.artVariant: ArtVariant
    get() = when (this) {
        AppDestination.HOME -> ArtVariant.Rounded
        AppDestination.FAVORITES -> ArtVariant.Angular
        AppDestination.SHOPPING -> ArtVariant.Banded
        AppDestination.PROFILE -> ArtVariant.Mixed
    }

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun ListDetailContent(destination: AppDestination) {
    val items = remember(destination) { DemoCatalog.items(destination) }

    // Selection is plain state rather than a ThreePaneScaffoldNavigator on purpose. The navigator
    // keeps a destination *history*, so tapping a second row pushes another entry and back has to
    // be pressed once per row visited. Here there is only ever one open detail: picking a
    // different row reassigns, and back clears. Keyed on the destination so each tab starts closed.
    var selectedId by rememberSaveable(destination) { mutableStateOf<String?>(null) }

    val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
    val twoPane = directive.maxHorizontalPartitions > 1

    // The detail pane stays collapsed until a row is picked, at every width. Left to itself the
    // scaffold keeps it expanded-but-empty in a wide window, so the value is stated explicitly.
    val scaffoldValue = ThreePaneScaffoldValue(
        primary = if (selectedId != null) PaneAdaptedValue.Expanded else PaneAdaptedValue.Hidden,
        secondary = if (selectedId == null || twoPane) {
            PaneAdaptedValue.Expanded
        } else {
            PaneAdaptedValue.Hidden
        },
        tertiary = PaneAdaptedValue.Hidden,
    )

    // BackHandler is deprecated in favour of NavigationEventHandler, which we cannot adopt yet:
    // its artifact (org.jetbrains.androidx.navigationevent:navigationevent-compose) publishes no
    // iosX64 / uikitX64 variant, and shared/build.gradle.kts still declares iosX64(). Swap this
    // the day that target is dropped.
    @Suppress("DEPRECATION")
    BackHandler(enabled = selectedId != null) { selectedId = null }

    ListDetailPaneScaffold(
        directive = directive,
        value = scaffoldValue,
        listPane = {
            AnimatedPane {
                ItemListPane(
                    destination = destination,
                    items = items,
                    selectedId = selectedId,
                    onSelect = { item -> selectedId = item.id },
                )
            }
        },
        detailPane = {
            AnimatedPane {
                val item = selectedId?.let(DemoCatalog::item)
                if (item != null) {
                    ItemDetailPane(
                        item = item,
                        showBack = true,
                        onBack = { selectedId = null },
                        variant = destination.artVariant,
                    )
                }
            }
        },
    )
}

@Composable
private fun ItemListPane(
    destination: AppDestination,
    items: List<DemoItem>,
    selectedId: String?,
    onSelect: (DemoItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    val variant = destination.artVariant
    // The screen shows no heading, so the pane carries one for assistive tech instead.
    val paneLabel = DestinationContent.headline(destination)
    val paneModifier = modifier
        .fillMaxSize()
        .semantics { contentDescription = paneLabel }

    when (destination.listStyle) {
        ListStyle.Hero -> LazyColumn(
            modifier = paneModifier,
            contentPadding = scrollInsets(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            columnItems(items, key = { it.id }) { item ->
                HeroCard(item, variant, item.id == selectedId) { onSelect(item) }
            }
        }

        ListStyle.Grid -> LazyVerticalGrid(
            // 120dp gives two columns in the list pane of a two-pane window and three at phone
            // width, instead of collapsing to a single column.
            columns = GridCells.Adaptive(minSize = 120.dp),
            modifier = paneModifier,
            contentPadding = scrollInsets(spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            gridItems(items, key = { it.id }) { item ->
                GridTile(item, variant, item.id == selectedId) { onSelect(item) }
            }
        }

        ListStyle.Compact -> LazyColumn(
            modifier = paneModifier,
            contentPadding = scrollInsets(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            columnItems(items, key = { it.id }) { item ->
                CompactRow(item, variant, item.id == selectedId) { onSelect(item) }
            }
        }
    }
}

/**
 * Uniform [all] padding plus whatever the system bars need vertically. Used as `contentPadding`
 * so the first and last items clear the bars while the list still scrolls underneath them.
 */
@Composable
private fun scrollInsets(all: Dp): PaddingValues {
    val bars = WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical).asPaddingValues()
    return PaddingValues(
        start = all,
        end = all,
        top = all + bars.calculateTopPadding(),
        bottom = all + bars.calculateBottomPadding(),
    )
}

@Composable
private fun selectedCardColors(selected: Boolean) = if (selected) {
    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
} else {
    CardDefaults.cardColors()
}

/** HOME — a tall card with a wide art block above two placeholder lines. */
@Composable
private fun HeroCard(item: DemoItem, variant: ArtVariant, selected: Boolean, onClick: () -> Unit) {
    val spacing = LocalAppSpacing.current
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = item.title },
        colors = selectedCardColors(selected),
    ) {
        ShapeArt(
            seed = item.artSeed,
            variant = variant,
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 7f),
        )
        Column(
            modifier = Modifier.padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            SkeletonBar(widthFraction = 0.7f, height = 14.dp)
            SkeletonBar(widthFraction = 0.4f, height = 10.dp)
        }
    }
}

/** FAVORITES — a square art tile with a single caption line. */
@Composable
private fun GridTile(item: DemoItem, variant: ArtVariant, selected: Boolean, onClick: () -> Unit) {
    val spacing = LocalAppSpacing.current
    Card(
        onClick = onClick,
        modifier = Modifier.semantics { contentDescription = item.title },
        colors = selectedCardColors(selected),
    ) {
        ShapeArt(
            seed = item.artSeed,
            variant = variant,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        )
        Box(modifier = Modifier.padding(spacing.sm)) {
            SkeletonBar(widthFraction = 0.6f, height = 10.dp)
        }
    }
}

/** SHOPPING — a dense row: small art, two lines, and a trailing control. */
@Composable
private fun CompactRow(
    item: DemoItem,
    variant: ArtVariant,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val spacing = LocalAppSpacing.current
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = item.title },
        colors = selectedCardColors(selected),
    ) {
        Row(
            modifier = Modifier.padding(spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShapeArt(
                seed = item.artSeed,
                variant = variant,
                modifier = Modifier.size(52.dp).clip(MaterialTheme.shapes.small),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                SkeletonBar(widthFraction = 0.8f, height = 12.dp)
                SkeletonBar(widthFraction = 0.45f, height = 9.dp)
            }
            SkeletonPill()
        }
    }
}

/**
 * The detail pane. Public so the Android module can preview it in isolation — it is the best place
 * to check the generated artwork in light and dark.
 */
@Composable
fun ItemDetailPane(
    item: DemoItem,
    showBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ArtVariant = ArtVariant.Mixed,
) {
    val spacing = LocalAppSpacing.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(scrollInsets(spacing.md))
            .semantics { contentDescription = item.body },
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        if (showBack) {
            // A bare pill rather than a labelled button: nothing on this screen is text, and the
            // tap target still announces itself through the semantics below.
            TextButton(
                onClick = onBack,
                modifier = Modifier.semantics { contentDescription = "Back" },
            ) {
                SkeletonPill(width = 56.dp, height = 20.dp)
            }
        }
        ShapeArt(
            seed = item.artSeed,
            variant = variant,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(MaterialTheme.shapes.large),
        )
        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            SkeletonBar(widthFraction = 0.55f, height = 20.dp)
            SkeletonBar(widthFraction = 0.3f, height = 12.dp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            listOf(1f, 0.95f, 0.98f, 0.9f, 0.6f).forEach { fraction ->
                SkeletonBar(widthFraction = fraction, height = 10.dp)
            }
        }
    }
}

/**
 * PROFILE — deliberately not a list-detail screen. A settings page has no list-detail relationship
 * to express, and having one destination opt out makes the point that the navigation chrome adapts
 * independently of whatever layout a given screen picks.
 */
@Composable
private fun ProfilePane(modifier: Modifier = Modifier) {
    val spacing = LocalAppSpacing.current
    val rows = remember { DemoCatalog.items(AppDestination.PROFILE) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(scrollInsets(spacing.lg))
            .semantics {
                contentDescription = DestinationContent.headline(AppDestination.PROFILE)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        ShapeArt(
            seed = AppDestination.PROFILE.id.hashCode(),
            variant = ArtVariant.Rounded,
            modifier = Modifier.size(96.dp).clip(CircleShape),
        )
        SkeletonBar(widthFraction = 0.45f, height = 16.dp)
        SkeletonBar(widthFraction = 0.3f, height = 10.dp)

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            repeat(3) { index ->
                Card(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacing.xs),
                    ) {
                        ShapeArt(
                            seed = index * 7717,
                            variant = ArtVariant.Angular,
                            modifier = Modifier.size(28.dp).clip(CircleShape),
                        )
                        SkeletonBar(widthFraction = 0.7f, height = 8.dp)
                    }
                }
            }
        }

        rows.forEach { row ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = row.title },
            ) {
                Row(
                    modifier = Modifier.padding(spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SkeletonCircle(size = 28.dp)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(spacing.xs),
                    ) {
                        SkeletonBar(widthFraction = 0.6f, height = 11.dp)
                        SkeletonBar(widthFraction = 0.35f, height = 8.dp)
                    }
                    SkeletonPill()
                }
            }
        }
        Spacer(modifier = Modifier.height(spacing.lg))
    }
}
