package com.ducatti.badger.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ducatti.badger.navigation.BadgerAppNavHost
import com.ducatti.badger.navigation.rememberBadgerAppNavController
import com.ducatti.badger.ui.component.LocalFloatingActionButton
import com.ducatti.badger.ui.component.LocalSnackbarState
import com.ducatti.badger.ui.component.rememberFloatingActionButtonState
import com.ducatti.badger.ui.component.rememberSnackbarState
import com.ducatti.badger.ui.page.hello.HelloRoute

@Composable
fun BadgerApp() {
    val navController = rememberBadgerAppNavController(HelloRoute)
    val floatingActionButtonState = rememberFloatingActionButtonState(navController)
    val floatingActionButton by floatingActionButtonState.content.collectAsStateWithLifecycle()
    val snackbarState = rememberSnackbarState()

    CompositionLocalProvider(
        LocalFloatingActionButton provides floatingActionButtonState,
        LocalSnackbarState provides snackbarState,
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = floatingActionButton,
            snackbarHost = { SnackbarHost(snackbarState.snackbarHostState) }
        ) { innerPadding ->
            BadgerAppNavHost(navController, innerPadding)
        }
    }
}
