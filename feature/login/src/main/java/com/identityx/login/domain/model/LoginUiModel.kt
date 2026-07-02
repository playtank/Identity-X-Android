package com.identityx.login.domain.model

/**
 * Centralized UI state for the Login screen.
 *
 * [status] is a sealed class — exhausted in when() expressions,
 * no boolean flags needed.
 * [email] and [password] are always available regardless of status.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val status: LoginStatus = LoginStatus.Idle
) {
    sealed interface LoginStatus {
        /** Default — no operation in progress, no error. */
        data object Idle : LoginStatus

        /** Login request in flight. */
        data object Loading : LoginStatus

        /** Login failed — [message] describes why. */
        data class Error(val message: String) : LoginStatus
    }
}
