package com.ducatti.badger.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ducatti.badger.navigation.BadgerAppNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A Composable that allows setting the content of a FloatingActionButton
 * from within a screen or another Composable. This is useful for screens that need
 * to dynamically change the FAB's appearance or behavior.
 *
 * @param content A Composable lambda that defines the content to be displayed
 *                within the FloatingActionButton.
 */
@Composable
fun FloatingActionButtonEffect(content: FloatingActionContent) {
    val floatingActionButtonState = LocalFloatingActionButton.current
    LaunchedEffect(content) {
        floatingActionButtonState.setContent {
            content()
        }
    }
}

@Composable
fun rememberFloatingActionButtonState(
    navController: BadgerAppNavController
): FloatingActionButtonState {
    val navState by navController.state.collectAsStateWithLifecycle()
    return remember(navState.destination) { FloatingActionButtonState() }
}

@Stable
class FloatingActionButtonState() {
    private val state = MutableStateFlow<FloatingActionContent>({})
    val content = state.asStateFlow()

    fun setContent(newContent: FloatingActionContent) {
        state.value = newContent
    }
}

val LocalFloatingActionButton = compositionLocalOf<FloatingActionButtonState> {
    error("No FloatingActionButtonState provided")
}

typealias FloatingActionContent = @Composable () -> Unit
