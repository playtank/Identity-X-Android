package com.identityx.android.core.navigation

object NavigationDestinations {
    const val HOME            = "home"
    const val SCAN            = "scan"
    const val REGISTRATION    = "registration"
    const val FORGOT_PASSWORD = "forgot_password"
    const val SUPPORT         = "support"

    // Login route pattern — registered in the NavGraph to handle both
    // plain login and login-with-reason (forced logout notification).
    const val LOGIN_ARG_REASON  = "reason"
    const val LOGIN             = "login"
    // Pattern used when registering the composable — includes the optional arg
    const val LOGIN_WITH_REASON = "login?$LOGIN_ARG_REASON={$LOGIN_ARG_REASON}"

    /** Navigable route carrying a URL-encoded reason string. */
    fun loginWithReason(reason: String): String =
        "login?$LOGIN_ARG_REASON=${java.net.URLEncoder.encode(reason, "UTF-8")}"
}
