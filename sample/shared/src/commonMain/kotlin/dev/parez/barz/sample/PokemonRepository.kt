package dev.parez.barz.sample

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Paged PokéAPI reader with an in-memory cache.
 *
 * The cache is deliberately not persisted — this is a navigation demo, and a disk-backed store
 * would add a dependency per platform for no visible benefit. Restarting the app refetches.
 */
class PokemonRepository(private val api: PokemonApi) {
    private val fetchMutex = Mutex()
    private var nextOffset = 0
    var hasMore = true
        private set

    // Sorted on read rather than on write: entries arrive page-ordered anyway, and a single sort
    // per emission is cheaper than keeping an ordered structure in sync.
    private val entries = MutableStateFlow<Map<Int, PokemonListEntry>>(emptyMap())
    private val details = MutableStateFlow<Map<Int, PokemonDetail>>(emptyMap())

    fun observePokemonList(): Flow<List<PokemonListEntry>> =
        entries.map { it.values.sortedBy { entry -> entry.id } }

    fun observeDetail(id: Int): Flow<PokemonDetail?> = details.map { it[id] }

    suspend fun fetchNextPage(limit: Int = 20) {
        fetchMutex.withLock {
            if (!hasMore) return
            Logger.d(tag = "Repository") { "fetchNextPage: offset=$nextOffset, limit=$limit" }
            val response = api.fetchList(nextOffset, limit)
            entries.value = entries.value + response.results.associateBy { it.id }
            Logger.i(tag = "Repository") { "fetchNextPage: cached ${response.results.size} entries" }
            nextOffset += limit
            hasMore = response.next != null
        }
    }

    suspend fun fetchDetail(id: Int) {
        Logger.d(tag = "Repository") { "fetchDetail: id=$id" }
        val detail = api.fetchDetail(id)
        details.value = details.value + (detail.id to detail)
        Logger.i(tag = "Repository") { "fetchDetail: cached ${detail.name}" }
    }
}
