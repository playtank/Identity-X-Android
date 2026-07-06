package com.identityx.android.core.navigation

/**
 * All navigation actions available from the Login screen.
 *
 * The feature module (feature:login) declares these as its navigation contract.
 * The app module (app-handheld) implements them by wiring NavController calls.
 * This way the feature never imports NavController — full separation of concerns.
 */
data class LoginActions(
    /** Called when the user successfully authenticates. Navigates to the home/dashboard screen. */
    val onLoginSuccess: () -> Unit,

    /** Called when the user taps "Create account". Navigates to the registration screen. */
    val onNavigateToRegistration: () -> Unit,

    /** Called when the user taps "Forgot password?". Navigates to the password reset screen. */
    val onNavigateToForgotPassword: () -> Unit,

    /** Called when the user taps "Contact support". Navigates to the support screen. */
    val onNavigateToSupport: () -> Unit
)
