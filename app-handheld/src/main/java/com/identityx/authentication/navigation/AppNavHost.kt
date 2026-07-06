package com.identityx.authentication.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.identityx.android.core.navigation.LoginActions
import com.identityx.android.core.navigation.NavigationDestinations
import com.identityx.android.core.network.session.SessionEventBus
import com.identityx.authentication.ui.DashboardScreen
import com.identityx.login.navigation.loginNavGraph

/**
 * Root NavHost for the handheld app.
 *
 * Owns all NavController calls — feature modules never import NavController.
 * Collects [SessionEventBus] to force-navigate to Login when both tokens expire.
 */
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavigationDestinations.LOGIN,
    sessionEventBus: SessionEventBus
) {
    // Force logout when OkHttpTokenAuthenticator signals session expiry
    LaunchedEffect(Unit) {
        sessionEventBus.events.collect { event ->
            when (event) {
                is SessionEventBus.SessionEvent.SessionExpired -> {
                    navController.navigate(NavigationDestinations.LOGIN) {
                        popUpTo(0) { inclusive = true } // clear entire back stack
                    }
                }
            }
        }
    }

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
            DashboardScreen(
                onLogout = {
                    navController.navigate(NavigationDestinations.LOGIN) {
                        popUpTo(NavigationDestinations.HOME) { inclusive = true }
                    }
                }
            )
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
    }
}
