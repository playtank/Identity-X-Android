package com.identityx.authentication.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.identityx.android.core.navigation.LoginActions
import com.identityx.android.core.navigation.NavigationDestinations
import com.identityx.authentication.ui.DashboardScreen
import com.identityx.login.navigation.loginNavGraph

/**
 * Root NavHost for the handheld app.
 *
 * All NavController calls live here — feature modules never touch NavController.
 * Each feature receives only an Actions object (plain lambdas) as its navigation contract.
 */
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavigationDestinations.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Feature: Login
        loginNavGraph(
            actions = LoginActions(
                onLoginSuccess = {
                    navController.navigate(NavigationDestinations.HOME) {
                        popUpTo(NavigationDestinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegistration = {
                    navController.navigate(NavigationDestinations.REGISTRATION)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(NavigationDestinations.FORGOT_PASSWORD)
                },
                onNavigateToSupport = {
                    navController.navigate(NavigationDestinations.SUPPORT)
                }
            )
        )

        // Feature: Dashboard / Home
        composable(route = NavigationDestinations.HOME) {
            DashboardScreen()
        }

        // Placeholder destinations — replace with feature nav graphs when built
        composable(route = NavigationDestinations.REGISTRATION) {
            Text("Registration — coming soon")
        }

        composable(route = NavigationDestinations.FORGOT_PASSWORD) {
            Text("Forgot Password — coming soon")
        }

        composable(route = NavigationDestinations.SUPPORT) {
            Text("Support — coming soon")
        }

        // Add more feature nav graphs here as the app grows:
        // profileNavGraph(profileActions)
        // settingsNavGraph(settingsActions)
    }
}
