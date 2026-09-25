package dev.parez.navbarz.sample

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** How many Pokémon fit on a team. Exceeding it is what raises the "Team is full!" dialog. */
const val TEAM_CAPACITY = 6

data class TeamMember(
    val id: Int,
    val name: String,
    /** Types, in slot order. Empty until the detail endpoint for this Pokémon has been fetched. */
    val types: List<String>,
    val addedAt: Instant,
)

/**
 * The team roster: an ordered list of at most [TEAM_CAPACITY] members.
 *
 * [add] returns `false` when the team is full rather than throwing or silently dropping — the
 * caller turns that into the design's "Team is full!" dialog.
 */
class TeamState {
    private val _members = MutableStateFlow<List<TeamMember>>(emptyList())
    val members: StateFlow<List<TeamMember>> = _members.asStateFlow()

    val isFull: Boolean
        get() = _members.value.size >= TEAM_CAPACITY

    fun contains(id: Int): Boolean = _members.value.any { it.id == id }

    fun add(id: Int, name: String, types: List<String> = emptyList()): Boolean {
        if (isFull || contains(id)) return false
        _members.value = _members.value + TeamMember(id, name, types, Clock.System.now())
        return true
    }

    fun remove(id: Int) {
        _members.value = _members.value.filterNot { it.id == id }
    }

    /** Adds when absent, removes when present. Returns false only when a add hit the cap. */
    fun toggle(id: Int, name: String, types: List<String> = emptyList()): Boolean {
        if (contains(id)) {
            remove(id)
            return true
        }
        return add(id, name, types)
    }
}
