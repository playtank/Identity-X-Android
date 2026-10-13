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
import com.identityx.dashboard.DashboardScreen
import com.identityx.industrial_capture.IndustrialCaptureScreen
import com.identityx.industrial_capture.IndustrialCaptureViewModel
import com.identityx.login.navigation.loginNavGraph

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavigationDestinations.LOGIN,
    sessionManager: SessionManager
) {
    LaunchedEffect(Unit) {
        var hasBeenAuthenticated = false
        sessionManager.sessionState.collect { state ->
            when (state) {
                is SessionManager.SessionState.Authenticated -> {
                    hasBeenAuthenticated = true
                }
                is SessionManager.SessionState.Unauthenticated -> {
                    if (hasBeenAuthenticated) {
                        val reason = state.reason
                        val destination = if (reason != null)
                            NavigationDestinations.loginWithReason(reason)
                        else
                            NavigationDestinations.LOGIN
                        navController.navigate(destination) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                else -> Unit
            }
        }
    }

    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {
        loginNavGraph(actions = loginActions(navController))

        composable(route = NavigationDestinations.HOME) {
            DashboardScreen(
                onSignOutClick = { sessionManager.onLogout() },
                onCardClick    = { }
            )
        }

        composable(route = NavigationDestinations.SCAN) {
            val viewModel: IndustrialCaptureViewModel = hiltViewModel()
            IndustrialCaptureScreen(
                viewModel      = viewModel,
                onNavigateBack = { navController.popBackStack() }
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

private fun loginActions(navController: NavHostController) = LoginActions(
    onLoginSuccess = {
        navController.navigate(NavigationDestinations.HOME) {
            popUpTo(0) { inclusive = true }
        }
    },
    onNavigateToRegistration   = { navController.navigate(NavigationDestinations.REGISTRATION) },
    onNavigateToForgotPassword = { navController.navigate(NavigationDestinations.FORGOT_PASSWORD) },
    onNavigateToSupport        = { navController.navigate(NavigationDestinations.SUPPORT) }
)
