package com.identityx.login.presentation

sealed interface LoginUiIntent {
    data class EmailChanged(val email: String) : LoginUiIntent
    data class PasswordChanged(val password: String) : LoginUiIntent
    data class RememberUsernameChanged(val checked: Boolean) : LoginUiIntent
    data class BiometricEnabledChanged(val checked: Boolean) : LoginUiIntent
    data object Submit : LoginUiIntent
    data object BiometricSuccess : LoginUiIntent
    data object BiometricDismissed : LoginUiIntent  // cancel or failure — just dismiss
}
