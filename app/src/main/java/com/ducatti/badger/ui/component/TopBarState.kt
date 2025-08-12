package com.ducatti.badger.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ducatti.badger.navigation.BadgerAppNavController
import com.ducatti.badger.navigation.getTitle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * This composable is responsible for managing the state of the top app bar,
 * observing navigation changes to update the title, and providing a coroutine scope
 * for state management.
 *
 * @param navController The [BadgerAppNavController] used to observe navigation state changes.
 * @return An instance of [BadgerAppTopBarState] that reflects the current navigation and top bar configuration.
 */
@Composable
fun rememberBadgerAppTopBarState(
    navController: BadgerAppNavController,
): BadgerAppTopBarState {
    val scope = rememberCoroutineScope()
    val navState by navController.state.collectAsStateWithLifecycle()
    val title = navState?.backStackEntry.getTitle()
    return remember(navState) {
        BadgerAppTopBarState(
            title = title,
            isPoppingBack = navState?.isPoppingBack == true,
            isInitialRoute = navState?.isInitialRoute == true,
            scope = scope,
        )
    }
}

@Stable
class BadgerAppTopBarState(
    title: String,
    isPoppingBack: Boolean,
    isInitialRoute: Boolean,
    private val scope: CoroutineScope
) {
    private val _actionState = MutableStateFlow(false)

    val state: StateFlow<TopBarState> = _actionState
        .map { hasAction ->
            TopBarState(
                title = title,
                isPoppingBack = isPoppingBack,
                hasActionButton = hasAction,
                isInitialRoute = isInitialRoute
            )
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TopBarState()
        )

    fun setActionState(hasAction: Boolean) {
        _actionState.value = hasAction
    }

    data class TopBarState(
        val title: String = "",
        val isPoppingBack: Boolean = false,
        val hasActionButton: Boolean = false,
        val isInitialRoute: Boolean = true
    )
}

@Composable
fun TopBarActionsEffect(hasAction: Boolean) {
    val topBarState = LocalTopBarState.current
    LaunchedEffect(hasAction) {
        topBarState.setActionState(hasAction)
    }
}

val LocalTopBarState = compositionLocalOf<BadgerAppTopBarState> {
    error("No BadgerAppTopBarState provided")
}
