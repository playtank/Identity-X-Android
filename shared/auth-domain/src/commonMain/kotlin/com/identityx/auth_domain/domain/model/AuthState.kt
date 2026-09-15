package com.identityx.auth_domain.domain.model

/**
 * Represents the outcome of an authentication attempt.
 *
 * This is the shared domain model for login results — it is intentionally
 * decoupled from UI concerns. The Android presentation layer maps this into
 * its own [LoginUiState] / [LoginStatus] sealed hierarchy as needed.
 */
sealed interface AuthState {

    /** No authentication operation is in progress. */
    data object Idle : AuthState

    /** An authentication request is currently in flight. */
    data object Loading : AuthState

    /** Authentication completed successfully. */
    data object Success : AuthState

    /**
     * Authentication failed.
     *
     * @property message Human-readable reason; safe to display to the user.
     * @property cause   The underlying exception, if available, for logging.
     */
    data class Failure(
        val message: String,
        val cause: Throwable? = null
    ) : AuthState
}
