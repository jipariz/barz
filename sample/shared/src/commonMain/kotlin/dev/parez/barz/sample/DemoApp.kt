package dev.parez.barz.sample

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.CatchingPokemon
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.rememberNavBackStack
import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import dev.parez.barz.AdaptiveNavigationScaffold
import dev.parez.barz.FabPlacement
import dev.parez.barz.NavigationItem
import dev.parez.barz.NavigationMode
import dev.parez.barz.rememberNavigationMode
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.parez.barz.sample.di.LibraryKoinContext
import dev.parez.barz.sample.navigation.BrowserHistoryEffect
import dev.parez.barz.sample.navigation.DemoSavedStateConfiguration
import dev.parez.barz.sample.navigation.PokemonListKey
import dev.parez.barz.sample.theme.AppTypography
import dev.parez.barz.sample.theme.appColorScheme
import dev.parez.barz.sample.ui.PokeballMark
import dev.parez.barz.sample.ui.SettingsScreen
import dev.parez.barz.sample.ui.TeamFullDialog
import dev.parez.barz.sample.ui.TeamScreen
import dev.parez.barz.sample.ui.withoutTop
import org.koin.compose.KoinIsolatedContext
import org.koin.compose.koinInject

/**
 * Frosts the screens so the navigation chrome is the only thing in focus.
 *
 * `noiseFactor(0f)` because the grain Haze adds by default reads as dirt at this radius, and the
 * point here is a clean defocus rather than a frosted-glass texture.
 */
private val ContentBlur = HazeBlurStyle {
    blurRadius(18.dp)
    noiseFactor(0f)
}

/** The three top-level destinations, in the order [DemoNavItems] lists them. */
internal enum class Tab {
    POKEMON,
    TEAM,
    SETTINGS,
}

/**
 * The navigation items, shared by both hosts: the Compose [AdaptiveNavigationScaffold] used
 * everywhere, and the real `UITabBarController` the iOS shell roots itself in. Keeping one list
 * means the two can never drift.
 */
val DemoNavItems: List<NavigationItem> =
    listOf(
        NavigationItem(
            title = "Pokemon",
            systemIcon = "circle.circle",
            selectedSystemIcon = "circle.circle.fill",
        ),
        NavigationItem(title = "Team", systemIcon = "shield", selectedSystemIcon = "shield.fill"),
        NavigationItem(
            title = "Settings",
            systemIcon = "gearshape",
            selectedSystemIcon = "gearshape.fill",
        ),
    )

/**
 * The whole demo, navigation chrome included — Barz's Compose container picks a bottom bar, a rail
 * or a drawer from the window size.
 *
 * iOS roots itself in `barzTabBarController` and calls [DemoTab] per tab instead, so the system owns
 * the bar there. See `MainViewController.kt`.
 */
@Composable
fun DemoApp() {
    DemoRoot {
        var selectedTab by rememberSaveable { mutableIntStateOf(0) }
        // Bumping this re-runs the Pokemon tab's "random" pick. Kept out here rather than in
        // TabContent because the FAB that drives it lives in the chrome, not in the screen.
        var shuffle by remember { mutableIntStateOf(0) }

        AdaptiveNavigationScaffold(
            items = DemoNavItems,
            selectedIndex = selectedTab,
            onItemSelected = { selectedTab = it },
            icon = { index, selected -> TabIcon(Tab.entries[index], selected) },
            // Rail and drawer only — the bottom bar ignores it. Doubles as the top spacing the
            // rail would otherwise lack.
            header = { RailHeader() },
            // Honoured in all three modes: over the bottom-end corner above a bar, in the header
            // of a rail or drawer.
            fabPlacement = FabPlacement.Bottom,
            fab = {
                FloatingActionButton(
                    onClick = {
                        selectedTab = Tab.POKEMON.ordinal
                        shuffle++
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Filled.Casino, contentDescription = "Show a random Pokémon")
                }
            },
        ) {
            TabContent(Tab.entries[selectedTab], shuffle)
        }
    }
}

/**
 * One tab's content, themed and wired to the shared state but with no navigation chrome of its own.
 *
 * Each native tab is a separate `ComposeUIViewController`, so this runs as its own composition —
 * which is why all cross-tab state lives in Koin singletons rather than in `remember`.
 */
@Composable
fun DemoTab(index: Int) {
    DemoRoot { TabContent(Tab.entries[index]) }
}

/** Koin, theme and the page background — everything both hosts need underneath the content. */
@Composable
private fun DemoRoot(content: @Composable () -> Unit) {
    KoinIsolatedContext(context = LibraryKoinContext.koinApp) {
        remember { Logger.setLogWriters(platformLogWriter()) }
        val settings: SettingsState = koinInject()
        val mode by settings.mode.collectAsState()
        val dark =
            when (mode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
        MaterialTheme(colorScheme = appColorScheme(dark), typography = AppTypography) {
            Surface(color = MaterialTheme.colorScheme.background) { content() }
        }
    }
}

@Composable
private fun TabContent(tab: Tab, shuffle: Int = 0) {
    val settings: SettingsState = koinInject()
    val team: TeamState = koinInject()

    val members by team.members.collectAsState()
    val unit by settings.unit.collectAsState()
    val twentyFourHourTime by settings.twentyFourHourTime.collectAsState()
    val mode by settings.mode.collectAsState()

    var teamFull by remember { mutableStateOf(false) }

    // Both hosts draw edge to edge, so each screen insets its own scrolling content instead of the
    // container clipping it — that way sprites scroll under the bar rather than stopping at it.
    val insets = WindowInsets.safeDrawing.asPaddingValues()

    // Frosts the screens and nothing else. `HazeInput.Content` blurs this modifier's own subtree;
    // both hosts draw their chrome outside it — the Compose scaffold in [DemoApp], the native
    // UITabBarController on iOS — so the bar and the FAB stay sharp either way.
    Column(Modifier.fillMaxSize().hazeBlur(input = HazeInput.Content, style = ContentBlur)) {
        when (tab) {
            Tab.POKEMON -> {
                // Only this tab drills down, so it is the only one with a nav3 stack. On web
                // `BrowserHistoryEffect` binds it to the History API so the URL and browser
                // back/forward stay in sync.
                val backStack = rememberNavBackStack(DemoSavedStateConfiguration, PokemonListKey)
                BrowserHistoryEffect(backStack)
                PokemonCatalog(
                    backStack = backStack,
                    team = team,
                    unit = unit,
                    onTeamFull = { teamFull = true },
                    shuffle = shuffle,
                    contentPadding = insets,
                )
            }
            Tab.TEAM -> {
                ScreenHeader("Team", trailing = "${members.size}/$TEAM_CAPACITY", insets = insets)
                TeamScreen(
                    members = members,
                    twentyFourHourTime = twentyFourHourTime,
                    onRemove = team::remove,
                    contentPadding = insets.withoutTop(),
                )
            }
            Tab.SETTINGS -> {
                ScreenHeader("Settings", insets = insets)
                SettingsScreen(
                    unit = unit,
                    onUnitChange = settings::setUnit,
                    twentyFourHourTime = twentyFourHourTime,
                    onTwentyFourHourTimeChange = settings::setTwentyFourHourTime,
                    mode = mode,
                    onModeChange = settings::setMode,
                    contentPadding = insets.withoutTop(),
                )
            }
        }
    }

    if (teamFull) {
        TeamFullDialog(onDismiss = { teamFull = false })
    }
}

/** Brand mark at the top of the rail and the drawer, where a horizontal bar has no room for one. */
@Composable
private fun RailHeader() {
    Row(
        // No horizontal padding: the rail centres this, and in the drawer Barz applies the
        // same gutter the items get.
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PokeballMark(Modifier.size(28.dp))
        // The wordmark would clip the rail's ~80dp width, so it is drawer-only.
        if (rememberNavigationMode() == NavigationMode.Drawer) {
            Text("Pokédex", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun TabIcon(tab: Tab, selected: Boolean) {
    val icon =
        when (tab) {
            Tab.POKEMON ->
                if (selected) Icons.Filled.CatchingPokemon else Icons.Outlined.CatchingPokemon
            Tab.TEAM -> if (selected) Icons.Filled.Shield else Icons.Outlined.Shield
            Tab.SETTINGS -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
        }
    Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
}

/** The design's oversized page title — "Pokemon", "Team 4/6". */
@Composable
internal fun ScreenHeader(title: String, trailing: String? = null, insets: PaddingValues) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(top = insets.calculateTopPadding())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        if (trailing != null) {
            Text(
                text = "  $trailing",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
