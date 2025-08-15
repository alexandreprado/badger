package com.ducatti.badger.ui.page.useractions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ducatti.badger.data.model.User
import com.ducatti.badger.ui.component.ErrorMessage
import kotlinx.serialization.Serializable

@Serializable
data class UserActionsRoute(val code: String?)

@Composable
fun UserActionsScreen(
    viewModel: UserActionsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .clickable(
                indication = null,
                interactionSource = null,
                onClick = { focusManager.clearFocus() }
            ),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Header(onNavigateBack)

        when (uiState) {
            is UserActionsViewModel.UiState.Ready -> UserForm(
                user = uiState.user,
                isSaving = uiState.isSaving,
                isValid = uiState.isValid,
                onNameChange = viewModel::updateName,
                onGuestsChange = viewModel::updateGuests,
                onSaveChanges = viewModel::save
            )

            UserActionsViewModel.UiState.Success -> SuccessMessage()
            is UserActionsViewModel.UiState.Error -> ErrorMessage(
                error = uiState.message ?: "Erro ao adicionar convidado"
            )
        }
    }
}

@Composable
private fun Header(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier.padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                painter = rememberVectorPainter(Icons.AutoMirrored.Default.ArrowBack),
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = "Voltar",
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = "Novo Convidado",
            fontSize = 24.sp,
        )
    }
}

@Composable
fun UserForm(
    user: User,
    isSaving: Boolean,
    isValid: Boolean,
    onNameChange: (String) -> Unit,
    onGuestsChange: (String) -> Unit,
    onSaveChanges: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(36.dp)) {
        TextField(
            value = user.name,
            label = { Text("Nome completo") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Guest name"
                )
            },
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
        )

        TextField(
            value = user.guestString,
            label = { Text("Nº de convidados") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Guests count"
                )
            },
            onValueChange = onGuestsChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )
        )

        Button(
            onClick = onSaveChanges,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving && isValid,
            contentPadding = PaddingValues(16.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Salvar convidado")
            }
        }
    }
}

@Composable
fun SuccessMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 36.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = rememberVectorPainter(Icons.Default.CheckCircle),
            tint = Color.Green,
            contentDescription = "Success",
            modifier = Modifier.size(128.dp)
        )
        Text("Convidado adicionado com sucesso!")
    }
}
