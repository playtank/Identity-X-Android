package com.identityx.android.core.network.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Application-wide session event bus.
 *
 * Used by [OkHttpTokenAuthenticator] to signal that both the access token
 * and refresh token have expired — the app must log the user out.
 *
 * Collected in AppNavHost to trigger navigation back to the Login screen.
 */
@Singleton
class SessionEventBus @Inject constructor() {

    private val _events = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SessionEvent> = _events.asSharedFlow()

    fun postSessionExpired() {
        _events.tryEmit(SessionEvent.SessionExpired)
    }

    sealed interface SessionEvent {
        /** Both access and refresh tokens are expired — user must re-authenticate. */
        data object SessionExpired : SessionEvent
    }
}
