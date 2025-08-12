package com.ducatti.badger.ui.page.hello

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducatti.badger.data.model.User
import com.ducatti.badger.data.model.UserStatus
import com.ducatti.badger.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class HelloViewModel @Inject constructor(
    val userRepo: UserRepository
) : ViewModel() {

    private val usersState = MutableStateFlow<List<User>?>(null)
    private val searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    private val searchQueryState = MutableStateFlow("")

    val uiState: StateFlow<UiState> =
        combine(
            usersState.filterNotNull(),
            searchQueryState,
            searchState
        ) { users, query, search ->
            search.toUiState(
                baseState = users.toBaseState(),
                query = query,
                previousState = uiState.value
            )
        }
            .onStart {
                emit(UiState.Loading(BaseState()))
                loadUsers()
            }
            .catch { error -> emit(UiState.Failed(error)) }
            .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Idle)

    private fun loadUsers() {
        viewModelScope.launch {
            userRepo.getUsers().collect {
                usersState.value = it ?: emptyList()
            }
        }
    }

    @OptIn(FlowPreview::class)
    fun searchUser(searchQuery: String) {
        searchQueryState.value = searchQuery
        viewModelScope.launch {
            if (searchQuery.isEmpty()) {
                searchState.value = SearchState.Idle
            } else {
                searchState.value = SearchState.Loading
                userRepo.searchUsers(searchQuery)
                    .debounce(300.milliseconds)
                    .collectLatest { users ->
                        searchState.value = SearchState.Loaded(users ?: emptyList())
                    }
            }
        }
    }

//    fun onQueryChanged(searchQuery: String) {
//        searchQueryState.value = searchQuery
//    }

    private fun List<User>.countPresent(): Int =
        sumOf { if (it.status == UserStatus.PRESENT) 1 else 0 }

    private fun List<User>.countPending(): Int =
        sumOf { if (it.status == UserStatus.WAITING) 1 else 0 }

    private fun List<User>.toBaseState(): BaseState =
        BaseState(
            users = this,
            presentCount = countPresent(),
            pendingCount = countPending()
        )

    private fun SearchState.toUiState(
        baseState: BaseState,
        query: String,
        previousState: UiState? = null
    ): UiState = when (this) {
        is SearchState.Loaded -> UiState.Loaded(
            state = BaseState(
                users = users,
                searchQuery = query,
                presentCount = baseState.presentCount,
                pendingCount = baseState.pendingCount
            )
        )

        is SearchState.Loading -> UiState.Loading(
            state = BaseState(
                users = previousState?.state?.users.orEmpty(),
                searchQuery = query,
                presentCount = baseState.presentCount,
                pendingCount = baseState.pendingCount
            )
        )

        is SearchState.Failed -> UiState.Failed(error)
        SearchState.Idle -> UiState.Loaded(
            state = BaseState(
                users = baseState.users,
                searchQuery = query,
                presentCount = baseState.users.countPresent(),
                pendingCount = baseState.users.countPending()
            )
        )
    }

    sealed class UiState(open val state: BaseState) {
        data object Idle : UiState(BaseState())
        data class Loaded(override val state: BaseState) : UiState(state)
        data class Loading(override val state: BaseState) : UiState(state)
        data class Failed(val error: Throwable) : UiState(BaseState())
    }

    sealed interface SearchState {
        data class Loaded(val users: List<User>) : SearchState
        data object Idle : SearchState
        data object Loading : SearchState
        data class Failed(val error: Throwable) : SearchState
    }

    data class BaseState(
        val searchQuery: String = "",
        val users: List<User> = emptyList(),
        val presentCount: Int = 0,
        val pendingCount: Int = 0
    )
}
