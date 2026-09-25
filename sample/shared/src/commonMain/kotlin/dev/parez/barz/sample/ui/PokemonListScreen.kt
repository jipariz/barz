package dev.parez.barz.sample.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.parez.barz.sample.PokemonListEntry
import dev.parez.barz.sample.spriteUrlFor
import dev.parez.barz.sample.theme.MonoTagStyle
import dev.parez.barz.sample.toDisplayName
import org.koin.compose.viewmodel.koinViewModel

/** Card corner radius, shared by the list grid, the team roster and the settings card. */
internal val CardShape = RoundedCornerShape(20.dp)

@Composable
fun PokemonListScreen(
    onSelect: (PokemonListEntry) -> Unit,
    isOnTeam: (Int) -> Boolean,
    onToggleTeam: (PokemonListEntry) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: PokemonListViewModel = koinViewModel(),
) {
    when (val state = viewModel.uiState.collectAsState().value) {
        is ListUiState.Loading -> LoadingGrid(contentPadding)
        is ListUiState.Error ->
            TeamRocketError(
                message = state.message,
                onRetry = viewModel::loadNextPage,
                modifier = Modifier.padding(contentPadding),
            )
        is ListUiState.Content ->
            PokemonGrid(
                state = state,
                onSelect = onSelect,
                isOnTeam = isOnTeam,
                onToggleTeam = onToggleTeam,
                onLoadMore = viewModel::loadNextPage,
                onRetry = viewModel::loadNextPage,
                contentPadding = contentPadding,
            )
    }
}

@Composable
private fun PokemonGrid(
    state: ListUiState.Content,
    onSelect: (PokemonListEntry) -> Unit,
    isOnTeam: (Int) -> Boolean,
    onToggleTeam: (PokemonListEntry) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    contentPadding: PaddingValues,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding + PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.items, key = { it.id }, contentType = { "pokemon" }) { entry ->
            PokemonCard(
                entry = entry,
                onTeam = isOnTeam(entry.id),
                onClick = { onSelect(entry) },
                onToggleTeam = { onToggleTeam(entry) },
            )
        }

        // Footer: load-more sentinel / spinner / end-of-list.
        item(span = { GridItemSpan(maxLineSpan) }, contentType = "footer") {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                when {
                    state.isLoadingMore -> PokeballLoader(modifier = Modifier.size(40.dp))
                    state.error != null ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                state.error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = onRetry) { Text("Retry") }
                        }
                    state.hasMore -> {
                        // Infinite-scroll sentinel: this item only composes once it scrolls into
                        // view, and the effect runs immediately after that first composition — so
                        // there is no recomposition window in which the lambda could go stale.
                        LaunchedEffect(Unit) { onLoadMore() }
                        PokeballLoader(modifier = Modifier.size(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PokemonCard(
    entry: PokemonListEntry,
    onTeam: Boolean,
    onClick: () -> Unit,
    onToggleTeam: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
    ) {
        Column(Modifier.padding(10.dp)) {
            Box(Modifier.fillMaxWidth()) {
                // Sprite sits on an inset tile; the pokéball toggle floats over its top-left
                // corner, exactly as in the design.
                Box(
                    Modifier.fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    AsyncImage(
                        model = spriteUrlFor(entry.id),
                        contentDescription = entry.name,
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                    )
                }
                PokeballGlyph(
                    color =
                        if (onTeam) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    background = MaterialTheme.colorScheme.surfaceContainer,
                    // Inset from the tile's corner rather than pinned to it — the design floats the
                    // toggle inside the sprite well. minimumInteractiveComponentSize lifts the hit
                    // area to the 48dp floor without moving a pixel, and the label/role make it an
                    // addressable control: the parent card merges its descendants, so without them
                    // the primary affordance on this screen is an anonymous custom action.
                    modifier =
                        Modifier.minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(20.dp))
                            .toggleable(
                                value = onTeam,
                                role = Role.Checkbox,
                                onValueChange = { onToggleTeam() },
                            )
                            .padding(8.dp)
                            .size(24.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = entry.name.toDisplayName(),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "#${entry.id.toString().padStart(3, '0')}",
                style = MonoTagStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** The design's loading state: the grid's silhouette in flat placeholder tiles. */
@Composable
private fun LoadingGrid(contentPadding: PaddingValues) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding + PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false,
    ) {
        items(8) {
            Surface(shape = CardShape, color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(10.dp)) {
                    Placeholder(Modifier.fillMaxWidth().aspectRatio(1f), RoundedCornerShape(16.dp))
                    Spacer(Modifier.height(10.dp))
                    Placeholder(Modifier.fillMaxWidth(0.7f).height(16.dp))
                    Spacer(Modifier.height(6.dp))
                    Placeholder(Modifier.fillMaxWidth(0.35f).height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun Placeholder(modifier: Modifier, shape: RoundedCornerShape = RoundedCornerShape(6.dp)) {
    Box(modifier.clip(shape).background(MaterialTheme.colorScheme.surfaceContainer))
}

