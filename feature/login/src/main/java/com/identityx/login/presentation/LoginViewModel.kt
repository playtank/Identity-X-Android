package com.identityx.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.identityx.local.domain.UserPreferencesProvider
import com.identityx.login.domain.biometric.BiometricAvailabilityChecker
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
    private val userPrefs: UserPreferencesProvider,
    private val biometricChecker: BiometricAvailabilityChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        // Check whether this device has biometric hardware ready to authenticate
        val biometricAvailable = biometricChecker.isAvailable()

        // Restore remembered email and biometric preference on cold start
        val rememberedEmail = userPrefs.getRememberedEmail()
        if (rememberedEmail != null) {
            _uiState.update {
                it.copy(
                    email                = rememberedEmail,
                    rememberUsername     = true,
                    biometricEnabled     = userPrefs.isBiometricEnabled(),
                    isBiometricAvailable = biometricAvailable
                )
            }
        } else {
            _uiState.update { it.copy(isBiometricAvailable = biometricAvailable) }
        }
    }

    fun onIntent(intent: LoginUiIntent) {
        when (intent) {
            is LoginUiIntent.EmailChanged ->
                _uiState.update { it.copy(email = intent.email, status = LoginStatus.Idle) }

            is LoginUiIntent.PasswordChanged ->
                _uiState.update { it.copy(password = intent.password, status = LoginStatus.Idle) }

            is LoginUiIntent.RememberUsernameChanged ->
                _uiState.update { it.copy(rememberUsername = intent.checked) }

            is LoginUiIntent.BiometricEnabledChanged ->
                _uiState.update { it.copy(biometricEnabled = intent.checked) }

            is LoginUiIntent.Submit -> submitLogin()

            is LoginUiIntent.BiometricSuccess -> onBiometricSuccess()

            is LoginUiIntent.BiometricDismissed ->
                _uiState.update { it.copy(status = LoginStatus.Idle) }
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
                    val state = _uiState.value
                    // Persist or clear username based on checkbox state
                    if (state.rememberUsername) {
                        userPrefs.saveUserPreferences(
                            email            = email,
                            biometricEnabled = state.biometricEnabled
                        )
                    } else {
                        // User logged in with rememberUsername unchecked — forget the email
                        userPrefs.clearUserPreferences()
                    }

                    if (state.rememberUsername && state.biometricEnabled) {
                        _uiState.update { it.copy(status = LoginStatus.BiometricPrompting) }
                    } else {
                        _uiState.update { it.copy(status = LoginStatus.Idle) }
                        _navigationEvent.send(NavigationEvent.ToDashboard)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(status = LoginStatus.Error(error.message ?: "Login failed"))
                    }
                }
        }
    }

    private fun onBiometricSuccess() {
        viewModelScope.launch {
            _uiState.update { it.copy(status = LoginStatus.Idle) }
            _navigationEvent.send(NavigationEvent.ToDashboard)
        }
    }

    sealed interface NavigationEvent {
        data object ToDashboard : NavigationEvent
    }
}
