package com.ducatti.badger.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ducatti.badger.ui.page.camera.CameraScreen
import com.ducatti.badger.ui.page.camera.QRScannerRoute
import com.ducatti.badger.ui.page.confirmation.ConfirmationRoute
import com.ducatti.badger.ui.page.confirmation.ConfirmationScreen
import com.ducatti.badger.ui.page.confirmation.ConfirmationViewModel
import com.ducatti.badger.ui.page.details.UserDetailsRoute
import com.ducatti.badger.ui.page.details.UserDetailsScreen
import com.ducatti.badger.ui.page.details.UserDetailsViewModel
import com.ducatti.badger.ui.page.hello.HelloRoute
import com.ducatti.badger.ui.page.hello.HelloScreen
import com.ducatti.badger.ui.page.useractions.UserActionsRoute
import com.ducatti.badger.ui.page.useractions.UserActionsScreen
import com.ducatti.badger.ui.page.useractions.UserActionsViewModel

@Composable
fun BadgerAppNavHost(
    navController: BadgerAppNavController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController.navController,
        startDestination = navController.initialRoute,
        modifier = Modifier
            .padding(innerPadding)
            .background(MaterialTheme.colorScheme.background)
    ) {
        animatedCompose<HelloRoute> {
            HelloScreen(
                onNavigateToCamera = navController::navigateToCamera,
                onNavigateToUser = navController::navigateToDetails,
                onNavigateToUserActions = navController::navigateToUserActions
            )
        }
        animatedCompose<QRScannerRoute> {
            CameraScreen(
                onNavigateToConfirmation = navController::navigateToConfirmation,
                onNavigateBack = navController::popBackStack
            )
        }
        animatedCompose<ConfirmationRoute> { backStackEntry ->
            val code = backStackEntry.toRoute<ConfirmationRoute>().code
            ConfirmationScreen(
                viewModel =
                    hiltViewModel<ConfirmationViewModel, ConfirmationViewModel.Factory>(
                        key = code,
                    ) { factory -> factory.create(code) },
                onNavigateBack = navController::popBackStack
            )
        }
        animatedCompose<UserDetailsRoute> { backStackEntry ->
            val code = backStackEntry.toRoute<UserDetailsRoute>().code
            UserDetailsScreen(
                viewModel =
                    hiltViewModel<UserDetailsViewModel, UserDetailsViewModel.Factory>(
                        key = code,
                    ) { factory -> factory.create(code) },
                onNavigateBack = navController::popBackStack
            )
        }
        animatedCompose<UserActionsRoute> { backStackEntry ->
            val code = backStackEntry.toRoute<UserActionsRoute>().code
            UserActionsScreen(
                viewModel =
                    hiltViewModel<UserActionsViewModel, UserActionsViewModel.Factory>(
                        key = code,
                    ) { factory -> factory.create(code) },
                onNavigateBack = navController::popBackStack
            )
        }
    }
}

inline fun <reified T : Any> NavGraphBuilder.animatedCompose(
    crossinline content: @Composable (NavBackStackEntry) -> Unit
) {
    composable<T>(
        enterTransition = { slideInFromRightAnimation() },
        exitTransition = { slideOutToLeftAnimation() },
        popEnterTransition = { popSlideInFromLeftAnimation() },
        popExitTransition = { popSlideOutToRightAnimation() }
    ) { backStackEntry ->
        content(backStackEntry)
    }
}

fun BadgerAppNavController.goHome(
    navOptions: NavOptionsBuilder.() -> Unit = {}
) {
    navigateHome { navOptions() }
}

fun BadgerAppNavController.navigateToCamera(
    navOptions: NavOptionsBuilder.() -> Unit = {}
) {
    navigateTo(route = QRScannerRoute) {
        navOptions()
    }
}

fun BadgerAppNavController.navigateToDetails(
    code: String,
    navOptions: NavOptionsBuilder.() -> Unit = {}
) {
    navigateTo(route = UserDetailsRoute(code)) {
        navOptions()
    }
}

fun BadgerAppNavController.navigateToConfirmation(
    code: String,
    navOptions: NavOptionsBuilder.() -> Unit = {}
) {
    navigateTo(route = ConfirmationRoute(code)) {
        navOptions()
    }
}

fun BadgerAppNavController.navigateToUserActions(
    code: String?,
    navOptions: NavOptionsBuilder.() -> Unit = {}
) {
    navigateTo(route = UserActionsRoute(code)) {
        navOptions()
    }
}
