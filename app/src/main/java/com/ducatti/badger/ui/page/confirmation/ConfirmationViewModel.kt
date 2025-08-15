package com.ducatti.badger.ui.page.confirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducatti.badger.data.model.User
import com.ducatti.badger.data.model.UserStatus
import com.ducatti.badger.domain.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ConfirmationViewModel.Factory::class)
class ConfirmationViewModel @AssistedInject constructor(
    @Assisted val code: String,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState
        .onStart { getUser() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = UiState.Idle
        )

    @AssistedFactory
    interface Factory {
        fun create(code: String): ConfirmationViewModel
    }

    private fun getUser() {
        viewModelScope.launch {
            _uiState.emit(UiState.Loading)
            userRepo.getUser(code).fold(
                onSuccess = { user ->
                    if (user != null) {
                        if (user.status == UserStatus.PRESENT) {
                            _uiState.emit(UiState.AlreadyAdmitted(user))
                        } else {
                            userRepo.updateUser(user.copy(status = UserStatus.PRESENT)).fold(
                                onSuccess = {
                                    _uiState.emit(UiState.Success(user))
                                },
                                onFailure = { error ->
                                    _uiState.emit(UiState.Error(error.message))
                                }
                            )
                        }
                    } else {
                        _uiState.emit(UiState.NotFound)
                    }
                },
                onFailure = { error ->
                    _uiState.emit(UiState.Error(error.message))
                }
            )
        }
    }

    sealed interface UiState {
        data object Idle : UiState
        data object Loading : UiState
        data class Success(val user: User) : UiState
        data object NotFound : UiState
        data class AlreadyAdmitted(val user: User) : UiState
        data class Error(val error: String?) : UiState
    }
}
