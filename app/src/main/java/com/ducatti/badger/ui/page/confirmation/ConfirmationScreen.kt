package com.ducatti.badger.ui.page.confirmation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ducatti.badger.data.model.User
import com.ducatti.badger.ui.component.ErrorMessage
import com.ducatti.badger.ui.component.LoadingIndicator
import com.ducatti.badger.ui.component.Title
import com.ducatti.badger.ui.component.UserMetadata
import kotlinx.serialization.Serializable

@Serializable
data class ConfirmationRoute(val code: String)

@Composable
fun ConfirmationScreen(
    viewModel: ConfirmationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Title("Confirmação", onNavigateBack)
        when (uiState) {
            is ConfirmationViewModel.UiState.Idle -> Unit
            is ConfirmationViewModel.UiState.NotFound -> GuestNotFound()
            is ConfirmationViewModel.UiState.Loading -> LoadingIndicator()
            is ConfirmationViewModel.UiState.Success -> Confirmation(uiState.user)
            is ConfirmationViewModel.UiState.Error -> ErrorMessage(
                error = uiState.error ?: "Erro ao atualizar convidado."
            )

            is ConfirmationViewModel.UiState.AlreadyAdmitted -> Confirmation(
                user = uiState.user,
                wasAlreadyAdmitted = true
            )
        }
    }
}

@Composable
private fun Confirmation(user: User, wasAlreadyAdmitted: Boolean = false) {
    val scrollState = rememberScrollState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp)
            .scrollable(scrollState, orientation = Orientation.Vertical)
    ) {
        UserMetadata(user, shouldCenterItems = true, isExpanded = true)
        val admittanceStatus = if (wasAlreadyAdmitted) {
            AdmittanceStatus.AlreadyAdmitted
        } else {
            AdmittanceStatus.Success
        }
        StatusMessage(admittanceStatus, Modifier.weight(1f))
    }
}

@Composable
fun GuestNotFound() {
    val scrollState = rememberScrollState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp)
            .scrollable(scrollState, orientation = Orientation.Vertical)
    ) {
        StatusMessage(admittanceStatus = AdmittanceStatus.NotFound, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatusMessage(admittanceStatus: AdmittanceStatus, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (admittanceStatus) {
            is AdmittanceStatus.Success -> {
                Text("Convidado autorizado!")
                Icon(
                    painter = rememberVectorPainter(Icons.Default.CheckCircle),
                    tint = Color.Green,
                    contentDescription = "Sucesso!",
                    modifier = Modifier.size(128.dp)
                )
            }

            is AdmittanceStatus.NotFound -> {
                Text("Convidado não encontrado.")
                Icon(
                    painter = rememberVectorPainter(Icons.Default.Close),
                    tint = Color.Red,
                    contentDescription = "Erro!",
                    modifier = Modifier.size(128.dp)
                )
            }

            is AdmittanceStatus.AlreadyAdmitted -> {
                Text("Convidado já entrou!")
                Icon(
                    painter = rememberVectorPainter(Icons.Default.Warning),
                    tint = Color.Yellow,
                    contentDescription = "Atenção!",
                    modifier = Modifier.size(128.dp)
                )
            }
        }
    }
}

sealed interface AdmittanceStatus {
    data object Success : AdmittanceStatus
    data object AlreadyAdmitted : AdmittanceStatus
    data object NotFound : AdmittanceStatus
}
