package com.identityx.login.presentation

sealed interface LoginUiIntent {
    data class EmailChanged(val email: String) : LoginUiIntent
    data class PasswordChanged(val password: String) : LoginUiIntent
    data object Submit : LoginUiIntent
}
