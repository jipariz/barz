package dev.parez.navbarz.sample.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.parez.navbarz.sample.PokemonListEntry
import dev.parez.navbarz.sample.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 20

sealed interface ListUiState {
    data object Loading : ListUiState

    data class Content(
        val items: List<PokemonListEntry>,
        val isLoadingMore: Boolean,
        val hasMore: Boolean,
        val error: String?,
    ) : ListUiState

    data class Error(val message: String) : ListUiState
}

class PokemonListViewModel(private val repository: PokemonRepository) : ViewModel() {

    private val isLoadingMore = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ListUiState> =
        combine(
                repository.observePokemonList(),
                isLoadingMore,
                error,
                repository.hasMore,
            ) { items, loading, err, hasMore ->
                Logger.d(tag = "ListVM") {
                    "combine: items=${items.size}, loading=$loading, err=$err"
                }
                if (items.isEmpty() && loading && err == null) {
                    ListUiState.Loading
                } else if (items.isEmpty() && err != null) {
                    ListUiState.Error(err)
                } else {
                    ListUiState.Content(
                        items = items,
                        isLoadingMore = loading,
                        hasMore = hasMore,
                        error = err,
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ListUiState.Loading,
            )

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        if (isLoadingMore.value) return
        viewModelScope.launch {
            Logger.i(tag = "ListVM") { "loadNextPage: starting" }
            isLoadingMore.value = true
            error.value = null
            runCatching { repository.fetchNextPage(PAGE_SIZE) }
                .onSuccess { Logger.d(tag = "ListVM") { "loadNextPage: success" } }
                .onFailure {
                    Logger.e(it, tag = "ListVM") { "loadNextPage failed" }
                    error.value = it.message ?: "Unknown error"
                }
            isLoadingMore.value = false
        }
    }
}
