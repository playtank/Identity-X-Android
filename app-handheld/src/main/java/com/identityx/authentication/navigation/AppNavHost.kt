package com.identityx.authentication.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.identityx.android.core.navigation.LoginActions
import com.identityx.android.core.navigation.NavigationDestinations
import com.identityx.android.core.network.session.SessionManager
import com.identityx.authentication.ui.DashboardScreen
import com.identityx.industrial_capture.IndustrialCaptureScreen
import com.identityx.industrial_capture.IndustrialCaptureViewModel
import com.identityx.login.navigation.loginNavGraph

/**
 * Root NavHost for the handheld app.
 *
 * Owns all NavController calls — feature modules never import NavController.
 * Observes [SessionManager.sessionState] to react to forced logout when both tokens expire.
 */
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavigationDestinations.LOGIN,
    sessionManager: SessionManager
) {
    LaunchedEffect(Unit) {
        sessionManager.sessionState.collect { state ->
            if (state is SessionManager.SessionState.Unauthenticated) {
                backToLogin(navController)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
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

        composable(route = NavigationDestinations.HOME) {
            DashboardScreen(
                onLogout = {
                    sessionManager.onLogout()
                    backToLogin(navController)
                },
                onScan = {
                    navController.navigate(NavigationDestinations.SCAN)
                }
            )
        }

        // Scan / barcode capture screen
        composable(route = NavigationDestinations.SCAN) {
            val viewModel: IndustrialCaptureViewModel = hiltViewModel()
            IndustrialCaptureScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

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

/**
 * Navigates to Login and clears the entire back stack.
 * Shared by manual logout and forced session expiry.
 */
private fun backToLogin(navController: NavHostController) {
    navController.navigate(NavigationDestinations.LOGIN) {
        popUpTo(0) { inclusive = true }
    }
}
