package com.identityx.android.core.navigation

/**
 * Single source of truth for all navigation route strings.
 * Feature modules and the app-level NavHost both reference these constants —
 * no magic strings scattered across the codebase.
 */
object NavigationDestinations {
    const val LOGIN = "login"
    const val HOME = "home"
    // Add more destinations here as features grow
}
