package dev.parez.barz.sample

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── List endpoint ─────────────────────────────────────────────────────────────

@Serializable
data class PokemonListResponse(
    val count: Int,
    val next: String?,
    val results: List<PokemonListEntry>,
)

@Serializable
data class PokemonListEntry(val name: String, val url: String) {
    /**
     * Extracted from the URL: "https://pokeapi.co/api/v2/pokemon/1/" → 1.
     *
     * Computed once, not per access. As a getter this ran three allocations every time it was read
     * — including from `key = { it.id }`, which a lazy grid calls for every visible item on every
     * measure pass — and `toInt()` would have thrown from inside that key lambda, during
     * measurement, where it cannot be caught.
     */
    val id: Int by
        lazy(LazyThreadSafetyMode.NONE) {
            url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: 0
        }
}

// ── Detail endpoint ───────────────────────────────────────────────────────────

@Serializable
data class PokemonDetail(
    val id: Int,
    val name: String,
    val height: Int, // decimetres
    val weight: Int, // hectograms
    val types: List<TypeSlot>,
    val stats: List<StatEntry>,
    val abilities: List<AbilitySlot>,
)

@Serializable data class TypeSlot(val slot: Int, val type: NamedResource)

@Serializable
data class StatEntry(@SerialName("base_stat") val baseStat: Int, val stat: NamedResource)

@Serializable
data class AbilitySlot(val ability: NamedResource, @SerialName("is_hidden") val isHidden: Boolean)

@Serializable data class NamedResource(val name: String)

// ── URL helpers ───────────────────────────────────────────────────────────────

private const val SPRITES = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites"

fun spriteUrlFor(id: Int): String = "$SPRITES/pokemon/$id.png"

fun artworkUrlFor(id: Int): String = "$SPRITES/pokemon/other/official-artwork/$id.png"

// ── Display helpers ───────────────────────────────────────────────────────────

fun String.toDisplayName(): String =
    replace('-', ' ').split(' ').joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
