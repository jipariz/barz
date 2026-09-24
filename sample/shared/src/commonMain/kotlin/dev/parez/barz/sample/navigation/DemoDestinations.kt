package dev.parez.barz.sample.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/** The list pane — the catalog of Pokémon. */
@Serializable data object PokemonListKey : NavKey

/** The detail pane — a single Pokémon. */
@Serializable data class PokemonDetailKey(val id: Int, val name: String) : NavKey

/** The team roster — reachable only from its own tab. */
@Serializable data object TeamKey : NavKey

/** Settings — reachable only from its own tab. */
@Serializable data object SettingsKey : NavKey

/**
 * Required by `rememberNavBackStack` on non-Android targets: registers each concrete [NavKey]
 * subtype so the polymorphic serializer can round-trip the backstack across process death.
 */
val DemoSavedStateConfiguration: SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(PokemonListKey::class, PokemonListKey.serializer())
            subclass(PokemonDetailKey::class, PokemonDetailKey.serializer())
            subclass(TeamKey::class, TeamKey.serializer())
            subclass(SettingsKey::class, SettingsKey.serializer())
        }
    }
}
