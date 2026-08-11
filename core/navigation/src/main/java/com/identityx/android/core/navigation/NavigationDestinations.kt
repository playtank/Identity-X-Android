package com.identityx.android.core.navigation

/**
 * Single source of truth for all navigation route strings.
 * Feature modules and the app-level NavHost both reference these constants —
 * no magic strings scattered across the codebase.
 */
object NavigationDestinations {
    const val LOGIN            = "login"
    const val HOME             = "home"
    const val SCAN             = "scan"
    const val REGISTRATION     = "registration"
    const val FORGOT_PASSWORD  = "forgot_password"
    const val SUPPORT          = "support"
    // Add more destinations here as features grow
}
