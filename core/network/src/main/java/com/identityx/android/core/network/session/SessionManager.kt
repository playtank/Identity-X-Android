package com.identityx.android.core.network.session

import com.identityx.local.domain.TokenProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized session state manager.
 *
 * Single source of truth for whether the user is authenticated.
 * Coordinates [TokenProvider] (token persistence) and [SessionEventBus]
 * (one-shot forced-logout signal from the network layer).
 *
 * Consumers:
 * - AppNavHost: observes [sessionState] to decide the start destination and react to logout
 * - LoginViewModel / RealLoginDataSource: call [onLoginSuccess] after a successful login
 * - NetworkModule (Ktor Auth plugin): calls [onSessionExpired] when refresh fails
 *
 * ## Session state transitions
 *
 * ```
 *                  app cold start
 *                       │
 *              ┌────────▼────────┐
 *              │  Authenticating │  (checking stored tokens)
 *              └────────┬────────┘
 *               has tokens?
 *              ┌────┴─────┐
 *           yes│          │no
 *   ┌──────────▼──┐   ┌───▼──────────────┐
 *   │Authenticated│   │ Unauthenticated   │
 *   └──────┬──────┘   └───────┬──────────┘
 *          │ onLogout()       │ onLoginSuccess()
 *          └──────────────────┘
 *
 * onSessionExpired() → clears tokens → Unauthenticated
 * ```
 */
@Singleton
class SessionManager @Inject constructor(
    private val tokenProvider: TokenProvider,
    private val sessionEventBus: SessionEventBus
) {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Authenticating)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    init {
        // Resolve initial state from persisted tokens on startup
        _sessionState.value = if (tokenProvider.getAccessToken() != null) {
            SessionState.Authenticated
        } else {
            SessionState.Unauthenticated
        }

        // Forward forced-logout events from the network layer into session state
        // SessionEventBus.postSessionExpired() is called by the Ktor Auth refreshTokens block
        // We mirror it here so the UI only needs to observe sessionState — not the event bus
    }

    /**
     * Check whether the user currently has a stored session.
     * Does not validate token expiry — that is handled by the Ktor Auth plugin on request.
     */
    val isLoggedIn: Boolean
        get() = tokenProvider.getAccessToken() != null

    /**
     * Called after a successful login.
     * Tokens are already persisted by [RealLoginDataSource] before this is called.
     */
    fun onLoginSuccess() {
        _sessionState.value = SessionState.Authenticated
    }

    /**
     * Called on explicit user logout (Sign Out button).
     * Clears tokens and transitions to Unauthenticated.
     */
    fun onLogout() {
        tokenProvider.clearTokens()
        _sessionState.value = SessionState.Unauthenticated
    }

    /**
     * Called by the Ktor Auth plugin when the refresh token is expired or invalid.
     * Clears tokens and fires a one-shot event via [SessionEventBus] so that
     * any active collector (AppNavHost) can react immediately.
     */
    fun onSessionExpired() {
        tokenProvider.clearTokens()
        _sessionState.value = SessionState.Unauthenticated
        sessionEventBus.postSessionExpired()
    }

    sealed interface SessionState {
        /** Startup — checking whether stored tokens exist. */
        data object Authenticating : SessionState

        /** User has a valid stored session. */
        data object Authenticated : SessionState

        /** No session — user must log in. */
        data object Unauthenticated : SessionState
    }
}
