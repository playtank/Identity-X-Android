package com.identityx.login.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.identityx.android.core.navigation.NavigationDestinations
import com.identityx.login.ui.LoginScreen

/**
 * Registers the login destination into the NavGraph.
 * The app-level NavHost calls this extension — feature module stays
 * fully self-contained and the app module never imports LoginScreen directly.
 */
fun NavGraphBuilder.loginNavGraph(navController: NavHostController) {
    composable(route = NavigationDestinations.LOGIN) {
        LoginScreen(
            onLoginSuccess = {
                navController.navigate(NavigationDestinations.HOME) {
                    // Clear login from the back stack so back button doesn't return to it
                    popUpTo(NavigationDestinations.LOGIN) { inclusive = true }
                }
            }
        )
    }
}
