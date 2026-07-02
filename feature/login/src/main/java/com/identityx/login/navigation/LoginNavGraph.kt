package com.identityx.login.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.identityx.android.core.navigation.LoginActions
import com.identityx.android.core.navigation.NavigationDestinations
import com.identityx.login.ui.LoginScreen

/**
 * Registers the login destination into the NavGraph.
 *
 * Accepts [LoginActions] instead of NavHostController — the feature module
 * has no knowledge of routing or back-stack logic. All navigation decisions
 * live in app-handheld's AppNavHost, passed down as lambdas.
 */
fun NavGraphBuilder.loginNavGraph(actions: LoginActions) {
    composable(route = NavigationDestinations.LOGIN) {
        LoginScreen(actions = actions)
    }
}
