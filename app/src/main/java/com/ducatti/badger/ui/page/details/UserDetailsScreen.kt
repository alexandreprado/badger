package com.ducatti.badger.ui.page.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ducatti.badger.data.model.User
import com.ducatti.badger.ui.component.ErrorMessage
import com.ducatti.badger.ui.component.LoadingIndicator
import com.ducatti.badger.ui.component.QrCode
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
        Title(onNavigateBack)
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
fun Title(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back button"
            )
        }
        Text(
            text = "Dados do convidado",
            fontSize = 24.sp,
        )
    }
}

@Composable
fun UserDetails(user: User) {
    Column(
        verticalArrangement = Arrangement.spacedBy(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp)
    ) {
        UserMetadata(user, shouldCenterItems = true, isExpanded = true)
        QrCode(content = user.id, modifier = Modifier.padding(24.dp))
    }
}
