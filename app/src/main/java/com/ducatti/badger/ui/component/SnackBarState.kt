package com.ducatti.badger.ui.component

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * This function creates  a [SnackbarState] that can be used
 * to control the display of Snackbars in a Composable UI.
 *
 * @return A [SnackbarState] instance that is remembered across recompositions.
 */
@Composable
fun rememberSnackbarState(): SnackbarState {
    val snackbarHostState = remember { SnackbarHostState() }
    return remember { SnackbarState(snackbarHostState) }
}

@Stable
class SnackbarState(
    val snackbarHostState: SnackbarHostState,
) {
    fun show(
        scope: CoroutineScope,
        message: String,
        actionLabel: String? = null,
        snackbarDuration: SnackbarDuration = SnackbarDuration.Long,
        onDismiss: () -> Unit = {},
        onAction: () -> Unit = {},
    ) {
        scope.launch {
            val snackbarResult = snackbarHostState
                .showSnackbar(
                    message = message,
                    actionLabel = actionLabel,
                    duration = snackbarDuration
                )
            when (snackbarResult) {
                SnackbarResult.Dismissed -> onDismiss()
                SnackbarResult.ActionPerformed -> onAction()
            }
        }
    }
}

val LocalSnackbarState = compositionLocalOf<SnackbarState> {
    error("No SnackbarState provided")
}
