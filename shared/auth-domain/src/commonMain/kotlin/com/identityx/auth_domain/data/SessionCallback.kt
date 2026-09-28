package com.identityx.auth_domain.data

/**
 * Platform-agnostic hook called by [RealLoginDataSource] after a successful login.
 *
 * Keeps [RealLoginDataSource] free of any platform-specific session management.
 * Each platform provides its own implementation:
 *   - Android: delegates to [com.identityx.android.core.network.session.SessionManager]
 *   - iOS:     updates AuthSession.isLoggedIn via the Swift AuthSession object
 *
 * Intentionally minimal — a single callback is easier to bridge across the
 * KMP/Swift boundary than a full SessionManager with StateFlow.
 */
interface SessionCallback {
    fun onLoginSuccess()
}
