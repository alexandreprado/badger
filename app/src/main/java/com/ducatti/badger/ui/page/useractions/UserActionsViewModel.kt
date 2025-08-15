package com.ducatti.badger.ui.page.useractions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducatti.badger.data.model.User
import com.ducatti.badger.domain.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = UserActionsViewModel.Factory::class)
class UserActionsViewModel @AssistedInject constructor(
    @Assisted val code: String?,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Ready())
    val uiState = _uiState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Ready()
        )

    @AssistedFactory
    interface Factory {
        fun create(code: String?): UserActionsViewModel
    }

    fun updateName(name: String) {
        val state = _uiState.value
        if (state is UiState.Ready) {
            val newUser = state.user.copy(name = name)
            _uiState.value = UiState.Ready(user = newUser, isValid = isValid(newUser))
        }
    }

    fun updateGuests(guests: String) {
        val state = _uiState.value
        if (state is UiState.Ready) {
            val newUser = state.user.copy(guests = guests.toIntOrNull() ?: 0)
            newUser.guestString = guests
            _uiState.value = UiState.Ready(user = newUser, isValid = isValid(newUser))
        }
    }

    fun updateTable(table: String) {
        val state = _uiState.value
        if (state is UiState.Ready) {
            val newUser = state.user.copy(table = table.toIntOrNull() ?: 0)
            newUser.tableString = table
            _uiState.value = UiState.Ready(user = newUser, isValid = isValid(newUser))
        }
    }

    private fun isValid(user: User): Boolean {
        return user.name.length >= 3 && user.guests >= 0 && user.table > 0
    }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state is UiState.Ready) {
                _uiState.value = state.copy(isSaving = true)
                userRepository.addUser(state.user).fold(
                    onSuccess = {
                        _uiState.value = UiState.Success
                        delay(2_000)
                        _uiState.value = UiState.Ready()
                    },
                    onFailure = { error ->
                        _uiState.value = UiState.Error(error.message)
                    }
                )
            }
        }
    }

    sealed interface UiState {
        data class Ready(
            val user: User = User(),
            val isSaving: Boolean = false,
            val isValid: Boolean = false
        ) : UiState

        data object Success : UiState
        data class Error(val message: String?) : UiState
    }
}
