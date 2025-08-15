package com.ducatti.badger.ui.page.details

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ducatti.badger.R
import com.ducatti.badger.data.model.User
import com.ducatti.badger.data.model.UserStatus
import com.ducatti.badger.ui.component.ErrorMessage
import com.ducatti.badger.ui.component.LoadingIndicator
import com.ducatti.badger.ui.component.QrCode
import com.ducatti.badger.ui.component.Title
import com.ducatti.badger.ui.component.UserMetadata
import com.ducatti.badger.ui.component.UserNotFound
import kotlinx.serialization.Serializable

@Serializable
data class UserDetailsRoute(val code: String)

@Composable
fun UserDetailsScreen(
    viewModel: UserDetailsViewModel = hiltViewModel(),
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
        Title("Dados do convidado", onNavigateBack)
        when (uiState) {
            is UserDetailsViewModel.UiState.Idle -> Unit
            is UserDetailsViewModel.UiState.Failed -> ErrorMessage(uiState.error.message.orEmpty())
            is UserDetailsViewModel.UiState.Loading -> LoadingIndicator()
            is UserDetailsViewModel.UiState.NotFound -> UserNotFound()
            is UserDetailsViewModel.UiState.Loaded -> UserDetails(uiState.user)
        }
    }
}

@Composable
private fun UserDetails(user: User) {
    val scrollState = rememberScrollState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp)
            .scrollable(scrollState, orientation = Orientation.Vertical)
    ) {
        UserMetadata(user, shouldCenterItems = true, isExpanded = true)
        StatusIcon(user.status, Modifier.weight(1f))
        QrCode(content = user.id, modifier = Modifier.padding(24.dp))
    }
}

@Composable
private fun StatusIcon(userStatus: UserStatus, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (userStatus == UserStatus.PRESENT) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                tint = Color.Unspecified,
                contentDescription = "Presente",
                modifier = Modifier.size(48.dp)
            )
            Text("Presente")
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_pending),
                tint = Color.Unspecified,
                contentDescription = "Pendente",
                modifier = Modifier.size(48.dp)
            )
            Text("Aguardando")
        }
    }
}
