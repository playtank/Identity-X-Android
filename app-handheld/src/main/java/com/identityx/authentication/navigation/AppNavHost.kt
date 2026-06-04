package com.identityx.authentication.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.identityx.android.core.navigation.NavigationDestinations
import com.identityx.login.navigation.loginNavGraph

/**
 * Root NavHost for the handheld app.
 * Each feature registers its own graph via extension functions —
 * AppNavHost only orchestrates, it never contains screen UI itself.
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
        loginNavGraph(navController)

        // Feature: Home (placeholder — replace with homeNavGraph(navController) when ready)
        composable(route = NavigationDestinations.HOME) {
            Text("Home Screen — coming soon")
        }

        // Add more feature nav graphs here as the app grows:
        // profileNavGraph(navController)
        // settingsNavGraph(navController)
    }
}
