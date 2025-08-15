package com.ducatti.badger.ui.page.hello

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducatti.badger.data.model.User
import com.ducatti.badger.data.model.UserStatus
import com.ducatti.badger.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HelloViewModel @Inject constructor(
    val userRepo: UserRepository
) : ViewModel() {

    private val filterState = MutableStateFlow<FilterState>(FilterState.None)
    private val searchQueryState = MutableStateFlow("")

    val uiState: StateFlow<UiState> =
        combine(
            getUserStream(),
            searchQueryState,
            filterState
        ) { users, query, filter ->
            users.orEmpty().toUiState(
                query = query,
                filterState = filter
            )
        }
            .onStart { emit(UiState.Loading) }
            .catch { error -> emit(UiState.Failed(error)) }
            .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Idle)

    private fun getUserStream(): Flow<List<User>?> = userRepo.getUsers().getOrThrow()

    fun searchUser(searchQuery: String) {
        searchQueryState.value = searchQuery
    }

    fun onFilterChanged(filterState: FilterState) {
        this.filterState.value = filterState
    }

    private fun List<User>.countPresent(): Int =
        sumOf { if (it.status == UserStatus.PRESENT) 1 else 0 }

    private fun List<User>.countPending(): Int =
        sumOf { if (it.status == UserStatus.WAITING) 1 else 0 }

    private fun List<User>.toUiState(
        query: String,
        filterState: FilterState,
    ): UiState {
        val filtered = when (filterState) {
            FilterState.None -> this
            FilterState.Present -> filter { it.status == UserStatus.PRESENT }
            FilterState.Waiting -> filter { it.status == UserStatus.WAITING }
        }

        val users = if (query.isNotBlank()) {
            filtered.filter { it.nameLowercase.contains(query) }
        } else {
            filtered
        }

        return UiState.Loaded(
            users = users,
            searchQuery = query,
            filterState = filterState,
            presentCount = countPresent(),
            pendingCount = countPending()
        )
    }

    sealed class UiState(
        open val searchQuery: String = "",
        open val filterState: FilterState = FilterState.None,
        open val presentCount: Int = 0,
        open val pendingCount: Int = 0
    ) {
        data object Idle : UiState()
        data class Loaded(
            val users: List<User>,
            override val searchQuery: String,
            override val filterState: FilterState,
            override val presentCount: Int,
            override val pendingCount: Int
        ) : UiState(searchQuery, filterState, presentCount, pendingCount)

        data object Loading : UiState()
        data class Failed(val error: Throwable) : UiState()
    }

    sealed interface FilterState {
        data object None : FilterState
        data object Present : FilterState
        data object Waiting : FilterState
    }
}
