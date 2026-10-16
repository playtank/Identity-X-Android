package com.identityx.android.core.network.session

import com.identityx.local.domain.TokenProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(
    private val tokenProvider: TokenProvider,
    private val sessionEventBus: SessionEventBus
) {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Authenticating)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    init {
        _sessionState.value = if (tokenProvider.getAccessToken() != null)
            SessionState.Authenticated
        else
            SessionState.Unauthenticated()
    }

    val isLoggedIn: Boolean
        get() = tokenProvider.getAccessToken() != null

    fun onLoginSuccess() {
        _sessionState.value = SessionState.Authenticated
    }

    /** Explicit user-initiated sign out — no message needed. */
    fun onLogout() {
        tokenProvider.clearTokens()
        _sessionState.value = SessionState.Unauthenticated()
    }

    /**
     * Called by the Ktor Auth plugin when the refresh token is expired or invalid.
     * Carries a user-facing message so the login screen can display a Snackbar.
     */
    fun onSessionExpired() {
        tokenProvider.clearTokens()
        _sessionState.value = SessionState.Unauthenticated(
            reason = "Your session has expired. Please sign in again."
        )
        sessionEventBus.postSessionExpired()
    }

    sealed interface SessionState {
        data object Authenticating : SessionState
        data object Authenticated  : SessionState

        /**
         * @param reason Non-null only when the session was terminated
         *               involuntarily (e.g. expired refresh token).
         *               Null for normal logout — no message should be shown.
         */
        data class Unauthenticated(val reason: String? = null) : SessionState
    }
}
