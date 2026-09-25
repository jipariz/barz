package dev.parez.barz.sample

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CatchingPokemon
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.PaneExpansionAnchor
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.PaddingValues
import dev.parez.barz.sample.TeamState
import dev.parez.barz.sample.UnitSystem
import dev.parez.barz.sample.navigation.PokemonDetailKey
import dev.parez.barz.sample.navigation.PokemonListKey
import dev.parez.barz.sample.ui.PokemonDetailScreen
import dev.parez.barz.sample.ui.PokemonListScreen
import dev.parez.barz.sample.ui.withoutTop

/**
 * Adaptive list-detail catalog driven by AndroidX Navigation 3.
 *
 * [backStack] is the single source of truth — pushing a [PokemonDetailKey] opens the detail pane,
 * popping returns to the list. On wide windows the [ListDetailSceneStrategy] keeps both panes
 * visible; on narrow windows it collapses to one, with `PaneMotionDefaults` animating the resize.
 *
 * Two fanciness knobs on top of the basic recipe:
 * - `shouldHandleSinglePaneLayout = true` keeps the scaffold in charge of every window size so pane
 *   transitions stay smooth.
 * - `paneExpansionDragHandle` exposes a Material 3 [VerticalDragHandle] so users can drag the
 *   divider between list and detail on wide windows.
 *
 * On web, the same backstack is bound to browser history via
 * [dev.parez.barz.sample.navigation.BrowserHistoryEffect].
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun PokemonCatalog(
    backStack: NavBackStack<NavKey>,
    team: TeamState,
    unit: UnitSystem,
    onTeamFull: () -> Unit,
    contentPadding: PaddingValues,
) {
    // Deliberately not read here with `by`. PokemonCatalog owns the NavDisplay, so reading the
    // team at this level made every add/remove rebuild the whole nav graph. Holding the State and
    // reading it inside the callbacks confines the snapshot read to the card that renders it.
    val members = team.members.collectAsState()

    // Drop the default horizontal gutter between the two panes — same tweak as
    // the official Material recipe (b/418201867).
    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val directive =
        remember(windowAdaptiveInfo) {
            calculatePaneScaffoldDirective(windowAdaptiveInfo)
                .copy(horizontalPartitionSpacerSize = 0.dp)
        }
    // Proportion-based anchors so the divider position scales with window
    // width instead of staying at a fixed dp offset — without this the
    // divider visibly jumps as the user resizes the host window.
    val paneExpansionState =
        rememberPaneExpansionState(
            anchors =
                listOf(
                    PaneExpansionAnchor.Proportion(0.35f),
                    PaneExpansionAnchor.Proportion(0.5f),
                    PaneExpansionAnchor.Proportion(0.65f),
                ),
            initialAnchoredIndex = 1,
        )
    val sceneStrategy =
        rememberListDetailSceneStrategy<NavKey>(
            // Keep the adaptive scaffold in charge of every window size so the
            // `PaneMotionDefaults` animations play on one-pane ↔ two-pane resizes.
            // Default `false` would hand single-pane mode off to NavDisplay's
            // plain `SinglePaneScene` crossfade.
            shouldHandleSinglePaneLayout = true,
            directive = directive,
            paneExpansionState = paneExpansionState,
            paneExpansionDragHandle = { state ->
                val interactionSource = remember { MutableInteractionSource() }
                VerticalDragHandle(
                    modifier =
                        Modifier.paneExpansionDraggable(
                            state = state,
                            minTouchTargetSize = 48.dp,
                            interactionSource = interactionSource,
                            semanticsProperties = null,
                        ),
                    interactionSource = interactionSource,
                )
            },
        )

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        sceneStrategies = listOf(sceneStrategy),
        entryProvider =
            entryProvider {
                entry<PokemonListKey>(
                    metadata =
                        ListDetailSceneStrategy.listPane(
                            detailPlaceholder = { DetailPlaceholder() }
                        )
                ) {
                    Column(Modifier.fillMaxSize()) {
                        ScreenHeader("Pokemon", insets = contentPadding)
                        PokemonListScreen(
                            contentPadding = contentPadding.withoutTop(),
                            isOnTeam = { id -> members.value.any { it.id == id } },
                            onToggleTeam = { entry ->
                                // The list only knows a name and an id — types arrive with the
                                // detail fetch, so a Pokémon added from here gets its tags filled
                                // in later if the user opens it.
                                if (!team.toggle(entry.id, entry.name)) onTeamFull()
                            },
                            onSelect = { entry ->
                                val key = PokemonDetailKey(entry.id, entry.name)
                                // In two-pane mode the list stays visible alongside the
                                // detail, so picking another Pokémon should swap the
                                // detail in place rather than stack on top — that way
                                // back always returns to the list, not to the previous
                                // detail.
                                if (backStack.lastOrNull() is PokemonDetailKey) {
                                    backStack[backStack.lastIndex] = key
                                } else {
                                    backStack.add(key)
                                }
                            },
                        )
                    }
                }
                entry<PokemonDetailKey>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
                    PokemonDetailScreen(
                        id = key.id,
                        name = key.name,
                        onBack = { backStack.removeLastOrNull() },
                        onTeam = members.value.any { it.id == key.id },
                        onToggleTeam = { detail ->
                            val types = detail.types.sortedBy { it.slot }.map { it.type.name }
                            if (!team.toggle(detail.id, detail.name, types)) onTeamFull()
                        },
                        unit = unit,
                        contentPadding = contentPadding,
                    )
                }
            },
    )
}

@Composable
private fun DetailPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.CatchingPokemon,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Select a Pokémon",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
