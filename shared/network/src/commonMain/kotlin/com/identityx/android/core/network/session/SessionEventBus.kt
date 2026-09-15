package com.identityx.android.core.network.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionEventBus @Inject constructor() {

    private val _events = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SessionEvent> = _events.asSharedFlow()

    fun postSessionExpired() {
        _events.tryEmit(SessionEvent.SessionExpired)
    }

    sealed interface SessionEvent {
        data object SessionExpired : SessionEvent
    }
}
