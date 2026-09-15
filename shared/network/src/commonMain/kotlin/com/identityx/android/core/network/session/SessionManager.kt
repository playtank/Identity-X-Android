package com.identityx.android.core.network.session

import com.identityx.local.domain.TokenProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val tokenProvider: TokenProvider,
    private val sessionEventBus: SessionEventBus
) {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Authenticating)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    init {
        _sessionState.value = if (tokenProvider.getAccessToken() != null)
            SessionState.Authenticated
        else
            SessionState.Unauthenticated
    }

    val isLoggedIn: Boolean
        get() = tokenProvider.getAccessToken() != null

    fun onLoginSuccess() {
        _sessionState.value = SessionState.Authenticated
    }

    fun onLogout() {
        tokenProvider.clearTokens()
        _sessionState.value = SessionState.Unauthenticated
    }

    fun onSessionExpired() {
        tokenProvider.clearTokens()
        _sessionState.value = SessionState.Unauthenticated
        sessionEventBus.postSessionExpired()
    }

    sealed interface SessionState {
        data object Authenticating  : SessionState
        data object Authenticated   : SessionState
        data object Unauthenticated : SessionState
    }
}
