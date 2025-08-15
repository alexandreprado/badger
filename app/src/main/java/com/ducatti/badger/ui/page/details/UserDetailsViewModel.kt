package com.ducatti.badger.ui.page.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducatti.badger.data.model.User
import com.ducatti.badger.domain.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = UserDetailsViewModel.Factory::class)
class UserDetailsViewModel @AssistedInject constructor(
    @Assisted val code: String,
    private val userRepo: UserRepository
) : ViewModel() {

    private val userState = MutableStateFlow<User?>(null)
    val uiState = userState
        .map { user ->
            user?.let {
                UiState.Loaded(it)
            } ?: run {
                UiState.NotFound
            }
        }
        .onStart {
            emit(UiState.Loading)
            loadUser(code)
        }
        .catch { error -> emit(UiState.Failed(error)) }
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Idle)

    private fun loadUser(id: String) {
        viewModelScope.launch {
            userRepo.getUser(id).fold(
                onSuccess = { user ->
                    userState.value = user
                },
                onFailure = {}
            )
        }
    }

    sealed interface UiState {
        data class Loaded(val user: User) : UiState
        data object NotFound : UiState
        data object Idle : UiState
        data object Loading : UiState
        data class Failed(val error: Throwable) : UiState
    }

    @AssistedFactory
    interface Factory {
        fun create(
            code: String,
        ): UserDetailsViewModel
    }
}
