package com.identityx.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.identityx.login.bridge.LoginSessionPort
import com.identityx.login.domain.model.LoginUiState
import com.identityx.login.domain.usecase.LoginAndFetchProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginAndFetchProfileUseCase,
    private val sessionBridge: LoginSessionPort
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    // One-shot navigation event — navigates to dashboard on successful login
    private val _navigationEvent = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun onIntent(intent: LoginUiIntent) {
        when (intent) {
            is LoginUiIntent.EmailChanged    -> _email.update { intent.email }
            is LoginUiIntent.PasswordChanged -> _password.update { intent.password }
            is LoginUiIntent.Submit          -> submitLogin()
        }
    }

    private fun submitLogin() {
        if (_uiState.value is LoginUiState.Loading) return

        val email = _email.value.trim()
        val password = _password.value

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { LoginUiState.Error("Email and password are required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { LoginUiState.Loading }
            loginUseCase(email, password)
                .onSuccess {
                    _uiState.update { LoginUiState.Idle }
                    _navigationEvent.send(NavigationEvent.ToDashboard)
                }
                .onFailure { error ->
                    _uiState.update { LoginUiState.Error(error.message ?: "Login failed") }
                }
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            sessionBridge.clearSessionOnGate()
        }
    }

    sealed interface NavigationEvent {
        data object ToDashboard : NavigationEvent
    }
}
