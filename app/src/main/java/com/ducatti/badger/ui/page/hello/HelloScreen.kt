package com.ducatti.badger.ui.page.hello

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
    onNavigateToUser: (String) -> Unit,
    onNavigateToUserActions: (String?) -> Unit
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Header(onNavigateToUserActions = onNavigateToUserActions)

        SearchField(
            focusRequester = searchFocusRequester,
            searchQuery = uiState.searchQuery,
            onSearch = viewModel::searchUser
        )

        Filters(
            presentCount = uiState.presentCount,
            pendingCount = uiState.pendingCount,
            filterState = uiState.filterState,
            onFilterChanged = viewModel::onFilterChanged,
        )

        when (uiState) {
            HelloViewModel.UiState.Idle -> Unit
            is HelloViewModel.UiState.Failed -> ErrorMessage(uiState.error.message.orEmpty())
            is HelloViewModel.UiState.Loading -> LoadingIndicator()
            is HelloViewModel.UiState.Loaded -> UserList(
                users = uiState.users,
                onClick = onNavigateToUser
            )
        }

        FloatingActionButtonEffect {
            CameraButton(onNavigateToCamera)
        }
    }

    LaunchedEffect(Unit) {
        if (uiState.searchQuery.isNotEmpty()) {
            searchFocusRequester.requestFocus()
        }
    }
}

@Composable
private fun UserList(users: List<User>, onClick: (id: String) -> Unit) {
    Box {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 64.dp)
        ) {
            items(users) { user ->
                UserCard(user, onClick)
            }
        }
        if (users.isEmpty()) {
            EmptyMessage()
        }
    }
}

@Composable
private fun UserCard(user: User, onClick: (id: String) -> Unit) {
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
private fun StatusIcon(userStatus: UserStatus) {
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
private fun Header(onNavigateToUserActions: (String?) -> Unit) {
    Row(
        modifier = Modifier.padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Lista de Convidados",
            fontSize = 24.sp,
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = { onNavigateToUserActions(null) }) {
            Icon(
                painter = rememberVectorPainter(Icons.Filled.Add),
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = "Adicionar convidado",
                modifier = Modifier.size(28.dp)
            )
        }
    }


}

@Composable
private fun Filters(
    presentCount: Int,
    pendingCount: Int,
    filterState: HelloViewModel.FilterState,
    onFilterChanged: (HelloViewModel.FilterState) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        val isPresentFilterActive = filterState == HelloViewModel.FilterState.Present
        val isWaitingFilterActive = filterState == HelloViewModel.FilterState.Waiting
        Filter(
            text = "Presentes: $presentCount",
            isActive = isPresentFilterActive
        ) {
            if (filterState is HelloViewModel.FilterState.Present) {
                onFilterChanged(HelloViewModel.FilterState.None)
            } else {
                onFilterChanged(HelloViewModel.FilterState.Present)
            }
        }
        Filter(
            text = "Pendentes: $pendingCount",
            isActive = isWaitingFilterActive
        ) {
            if (filterState is HelloViewModel.FilterState.Waiting) {
                onFilterChanged(HelloViewModel.FilterState.None)
            } else {
                onFilterChanged(HelloViewModel.FilterState.Waiting)
            }
        }
    }
}

@Composable
private fun Filter(text: String, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground,
                shape = MaterialTheme.shapes.small
            )
            .background(
                color = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.background
                },
                shape = MaterialTheme.shapes.small
            )
            .clickable { onClick() },
    ) {
        Text(
            text = text,
            color = if (isActive) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun EmptyMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Nenhum convidado encontrado.",
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}
