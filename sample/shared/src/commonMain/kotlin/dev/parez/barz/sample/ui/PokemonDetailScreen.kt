package dev.parez.barz.sample.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.parez.barz.sample.PokemonDetail
import dev.parez.barz.sample.UnitSystem
import dev.parez.barz.sample.artworkUrlFor
import dev.parez.barz.sample.theme.MonoTagStyle
import dev.parez.barz.sample.theme.isDarkScheme
import dev.parez.barz.sample.toDisplayName
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PokemonDetailScreen(
    id: Int,
    name: String,
    onBack: () -> Unit,
    onTeam: Boolean,
    onToggleTeam: (PokemonDetail) -> Unit,
    unit: UnitSystem,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: PokemonDetailViewModel =
        koinViewModel(key = "pokemon-detail-$id") { parametersOf(id) },
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is DetailUiState.Loading ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    PokeballLoader(modifier = Modifier.size(96.dp))
                }
            is DetailUiState.Error ->
                TeamRocketError(
                    message = state.message,
                    onRetry = viewModel::onRetry,
                    modifier = Modifier.padding(contentPadding),
                )
            is DetailUiState.Content ->
                DetailContent(
                    detail = state.detail,
                    onTeam = onTeam,
                    onToggleTeam = { onToggleTeam(state.detail) },
                    unit = unit,
                    contentPadding = contentPadding,
                )
        }

        // Floats over the type-coloured header rather than sitting in a top bar — the design has
        // the artwork running full-bleed behind it.
        IconButton(
            onClick = onBack,
            colors =
                IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.12f),
                    contentColor = Color.White,
                ),
            modifier =
                Modifier.padding(contentPadding)
                    .padding(start = 16.dp, top = 8.dp)
                    .minimumInteractiveComponentSize()
                    .size(40.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        // Placeholder so the name is announced even before the detail arrives.
        if (uiState is DetailUiState.Loading) {
            Text(
                text = name.toDisplayName(),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    detail: PokemonDetail,
    onTeam: Boolean,
    onToggleTeam: () -> Unit,
    unit: UnitSystem,
    contentPadding: PaddingValues,
) {
    val layoutDirection = LocalLayoutDirection.current
    val types = remember(detail) { detail.types.sortedBy { it.slot }.map { it.type.name } }
    val accent = typeColor(types.firstOrNull().orEmpty())

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // ── Type-coloured hero ────────────────────────────────────────────────
        Box(
            Modifier.fillMaxWidth().background(accent).padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = artworkUrlFor(detail.id),
                contentDescription = detail.name,
                modifier = Modifier.size(240.dp).padding(top = 48.dp, bottom = 16.dp),
            )
        }

        // ── Sheet ─────────────────────────────────────────────────────────────
        // Pulled up over the hero so its rounded top corners cut into the colour.
        Column(
            Modifier.offset(y = (-24).dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    end = contentPadding.calculateEndPadding(layoutDirection),
                    bottom = contentPadding.calculateBottomPadding(),
                )
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    types.forEach { TypeChip(it) }
                }
                Text(
                    text = "#${detail.id.toString().padStart(4, '0')}",
                    style = MonoTagStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = detail.name.toDisplayName(),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    onClick = onToggleTeam,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                    PokeballGlyph(
                        color =
                            if (onTeam) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp).size(28.dp),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FactCard(
                    label = "Weight",
                    value = formatWeight(detail.weight, unit),
                    icon = Icons.Filled.FitnessCenter,
                    accent = accent,
                    modifier = Modifier.weight(1f),
                )
                FactCard(
                    label = "Height",
                    value = formatHeight(detail.height, unit),
                    icon = Icons.Filled.Straighten,
                    accent = accent,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(12.dp))

            FactCard(
                label = "Moves",
                value =
                    detail.abilities
                        .firstOrNull()
                        ?.ability
                        ?.name
                        ?.toDisplayName()
                        .orEmpty()
                        .ifEmpty { "—" },
                icon = Icons.Filled.Whatshot,
                accent = accent,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            Surface(shape = CardShape, color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Base Stats",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    detail.stats.forEach { stat ->
                        StatBar(
                            name = statAbbreviation(stat.stat.name),
                            value = stat.baseStat,
                            accent = accent,
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Sub-components ────────────────────────────────────────────────────────────

@Composable
private fun FactCard(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, shape = CardShape, color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    Modifier.size(28.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.25f))
                        .padding(6.dp)
                ) {
                    Icon(icon, contentDescription = null, tint = accent)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun StatBar(name: String, value: Int, accent: Color) {
    // 255 is the highest base stat any Pokémon has, so the bars are comparable across the dex
    // rather than each being normalised to its own row.
    val fraction = (value / 255f).coerceIn(0f, 1f)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.width(48.dp),
        )
        Box(
            Modifier.weight(1f)
                .height(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Box(Modifier.fillMaxWidth(fraction).fillMaxSize().clip(CircleShape).background(accent))
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.width(40.dp),
        )
    }
}

@Composable
internal fun TypeChip(typeName: String) {
    val dark = isDarkScheme()
    Surface(color = typeChipBackground(typeName, dark), shape = CircleShape) {
        Text(
            text = typeName.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MonoTagStyle,
            color = typeChipContent(typeName, dark),
        )
    }
}

// ── Formatting ────────────────────────────────────────────────────────────────

/** API weights are hectograms. */
private fun formatWeight(hectograms: Int, unit: UnitSystem): String =
    when (unit) {
        UnitSystem.METRIC -> "${hectograms / 10}.${hectograms % 10} kg"
        UnitSystem.IMPERIAL -> {
            val tenthsOfPound = (hectograms * 2205) / 1000
            "${tenthsOfPound / 10}.${tenthsOfPound % 10} lb"
        }
    }

/** API heights are decimetres. */
private fun formatHeight(decimetres: Int, unit: UnitSystem): String =
    when (unit) {
        UnitSystem.METRIC -> "${decimetres / 10}.${decimetres % 10} m"
        UnitSystem.IMPERIAL -> {
            val totalInches = (decimetres * 3937) / 1000
            "${totalInches / 12}'${totalInches % 12}\""
        }
    }

/** The design labels stats HP / ATK / DEF / SATK / SDEF / SPD. */
private fun statAbbreviation(apiName: String): String =
    when (apiName) {
        "hp" -> "HP"
        "attack" -> "ATK"
        "defense" -> "DEF"
        "special-attack" -> "SATK"
        "special-defense" -> "SDEF"
        "speed" -> "SPD"
        else -> apiName.toDisplayName()
    }
