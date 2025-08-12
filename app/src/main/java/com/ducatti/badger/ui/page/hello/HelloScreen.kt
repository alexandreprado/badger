package com.ducatti.badger.ui.page.hello

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ducatti.badger.R
import com.ducatti.badger.data.model.User
import com.ducatti.badger.data.model.UserStatus
import com.ducatti.badger.ui.component.CameraButton
import com.ducatti.badger.ui.component.ErrorMessage
import com.ducatti.badger.ui.component.FloatingActionButtonEffect
import com.ducatti.badger.ui.component.LoadingIndicator
import com.ducatti.badger.ui.component.SearchField
import com.ducatti.badger.ui.component.UserMetadata
import kotlinx.serialization.Serializable

@Serializable
data object HelloRoute

@Composable
fun HelloScreen(
    viewModel: HelloViewModel = hiltViewModel(),
    onNavigateToCamera: () -> Unit,
    onNavigateToUser: (String) -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val focusManager = LocalFocusManager.current
    val searchFocusRequester = remember { FocusRequester() }

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
        Header(uiState.state)

        SearchField(
            focusRequester = searchFocusRequester,
            searchQuery = uiState.state.searchQuery,
            onSearch = viewModel::searchUser
        )

        when (uiState) {
            HelloViewModel.UiState.Idle -> Unit
            is HelloViewModel.UiState.Failed -> ErrorMessage(uiState.error.message.orEmpty())
            is HelloViewModel.UiState.Loading,
            is HelloViewModel.UiState.Loaded -> {
                val isLoading = uiState is HelloViewModel.UiState.Loading
                UserList(users = uiState.state.users, isLoading = isLoading) {
                    onNavigateToUser(it)
                }
            }
        }

        FloatingActionButtonEffect {
            CameraButton(onNavigateToCamera)
        }
    }

    LaunchedEffect(Unit) {
        if (uiState.state.searchQuery.isNotEmpty()) {
            searchFocusRequester.requestFocus()
        }
    }
}

@Composable
private fun UserList(users: List<User>, isLoading: Boolean, onClick: (id: String) -> Unit) {
    Box {
        LazyColumn {
            items(users) { user ->
                UserCard(user, onClick)
            }
        }
        if (users.isEmpty() && !isLoading) {
            EmptyMessage()
        }
        if (isLoading) {
            LoadingIndicator()
        }
    }
}

@Composable
fun UserCard(user: User, onClick: (id: String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = { onClick(user.id) })
            .padding(vertical = 16.dp, horizontal = 8.dp)
    ) {
        UserMetadata(user, Modifier.weight(1f))
        StatusIcon(user.status)
    }
    HorizontalDivider()
}

@Composable
fun StatusIcon(userStatus: UserStatus) {
    if (userStatus == UserStatus.PRESENT) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            tint = Color.Unspecified,
            contentDescription = "Presente",
            modifier = Modifier.size(36.dp)
        )
    } else {
        Icon(
            painter = painterResource(R.drawable.ic_pending),
            tint = Color.Unspecified,
            contentDescription = "Pendente",
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun Header(state: HelloViewModel.BaseState) {
    Text(
        text = "Lista de Convidados",
        fontSize = 28.sp,
        modifier = Modifier.padding(top = 16.dp)
    )
    GuestCounter(state)
}

@Composable
fun GuestCounter(state: HelloViewModel.BaseState) {
    Row {
        Text(
            text = "Presentes: ${state.presentCount}",
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Pendentes: ${state.pendingCount}",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun EmptyMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Nenhum convidado encontrado.",
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}
