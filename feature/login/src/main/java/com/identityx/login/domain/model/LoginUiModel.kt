package com.identityx.login.domain.model

/**
 * Centralized UI state for the Login screen.
 *
 * [status] is exhausted — compiler enforces all branches are handled.
 * Biometric prompting is a first-class status, distinct from Loading and Error.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberUsername: Boolean = false,
    val biometricEnabled: Boolean = false,
    val status: LoginStatus = LoginStatus.Idle
) {
    sealed interface LoginStatus {
        /** No operation in progress. */
        data object Idle : LoginStatus

        /** Network login request in flight. */
        data object Loading : LoginStatus

        /**
         * Login succeeded and both checkboxes are checked —
         * biometric prompt is active. Cancel/failure returns to Idle.
         */
        data object BiometricPrompting : LoginStatus

        /** Login or biometric failed — [message] describes why. */
        data class Error(val message: String) : LoginStatus
    }
}
