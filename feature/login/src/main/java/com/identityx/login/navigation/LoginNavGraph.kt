package com.identityx.login.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.identityx.android.core.navigation.LoginActions
import com.identityx.android.core.navigation.NavigationDestinations
import com.identityx.login.presentation.LoginScreen

/**
 * Registers the login destination into the NavGraph.
 *
 * Accepts an optional [sessionExpiredMessage] argument — non-null when the user
 * was force-logged-out due to an expired refresh token. LoginScreen shows a
 * Snackbar with this message when it arrives.
 */
fun NavGraphBuilder.loginNavGraph(actions: LoginActions) {
    composable(
        route = NavigationDestinations.LOGIN_WITH_REASON,
        arguments = listOf(
            navArgument(NavigationDestinations.LOGIN_ARG_REASON) {
                type         = NavType.StringType
                nullable     = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val reason = backStackEntry.arguments
            ?.getString(NavigationDestinations.LOGIN_ARG_REASON)
        LoginScreen(
            actions               = actions,
            sessionExpiredMessage = reason
        )
    }
}
