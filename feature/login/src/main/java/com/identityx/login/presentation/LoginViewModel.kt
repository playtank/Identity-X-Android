package com.identityx.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.identityx.login.bridge.LoginSessionPort
import com.identityx.login.domain.model.LoginUiState
import com.identityx.login.domain.model.LoginUiState.LoginStatus
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

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // One-shot navigation event
    private val _navigationEvent = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun onIntent(intent: LoginUiIntent) {
        when (intent) {
            is LoginUiIntent.EmailChanged    -> _uiState.update { it.copy(email = intent.email, status = LoginStatus.Idle) }
            is LoginUiIntent.PasswordChanged -> _uiState.update { it.copy(password = intent.password, status = LoginStatus.Idle) }
            is LoginUiIntent.Submit          -> submitLogin()
        }
    }

    private fun submitLogin() {
        if (_uiState.value.status is LoginStatus.Loading) return

        val email    = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(status = LoginStatus.Error("Email and password are required")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(status = LoginStatus.Loading) }
            loginUseCase(email, password)
                .onSuccess {
                    _uiState.update { it.copy(status = LoginStatus.Idle) }
                    _navigationEvent.send(NavigationEvent.ToDashboard)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(status = LoginStatus.Error(error.message ?: "Login failed")) }
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
