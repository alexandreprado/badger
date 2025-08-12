package com.ducatti.badger.navigation

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/**
 * This function creates and remembers a [BadgerAppNavController] instance, which encapsulates
 * a standard [NavHostController] along with additional logic for handling back button presses
 * and exposing navigation state as a [StateFlow].
 *
 * @return An instance of [BadgerAppNavController] that can be used to control navigation.
 */
@Composable
fun rememberBadgerAppNavController(initialRoute: Any): BadgerAppNavController {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val backButtonStream = getBackButtonStream()

    return remember(navController, scope) {
        BadgerAppNavController(navController, initialRoute, scope, backButtonStream)
    }
}

class BadgerAppNavController(
    val navController: NavHostController,
    val initialRoute: Any,
    private val scope: CoroutineScope,
    private val backButtonStream: Flow<Unit>
) {

    private val isPoppingBack = mutableStateOf(false)

    val state: StateFlow<NavState> = navController.currentBackStackEntryFlow
        .filterNotNull()
        .map { backStackEntry ->
            NavState(
                destination = backStackEntry.destination.route,
                isPoppingBack = isPoppingBack.value,
                backStackEntry = backStackEntry,
                isInitialRoute = backStackEntry.destination.route == navController.graph.startDestinationRoute
            )
        }
        .onStart { handleBackButtonStream() }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NavState(destination = initialRoute::class.java.name)
        )

    fun <T : Any> navigateTo(route: T, navOptions: NavOptionsBuilder.() -> Unit) {
        isPoppingBack.value = false
        navController.navigate(route = route) {
            navOptions()
        }
    }

    fun navigateHome(navOptions: NavOptionsBuilder.() -> Unit) {
        isPoppingBack.value = true
        navController.navigate(route = initialRoute) {
            navOptions()
        }
    }

    fun popBackStack(): Boolean {
        isPoppingBack.value = true
        return navController.popBackStack()
    }

    private fun handleBackButtonStream() {
        backButtonStream
            .onEach { popBackStack() }
            .launchIn(scope)
    }

    data class NavState(
        val destination: String? = null,
        val isPoppingBack: Boolean = false,
        val backStackEntry: NavBackStackEntry? = null,
        val isInitialRoute: Boolean = true
    )
}

@Composable
private fun getBackButtonStream(): Flow<Unit> {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val lifecycleOwner = LocalLifecycleOwner.current
    val callbackFlow = remember {
        callbackFlow {
            val backCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    trySend(Unit)
                }
            }
            backDispatcher?.addCallback(lifecycleOwner, backCallback)
            awaitClose { backCallback.remove() }
        }
    }
    return callbackFlow
}
