//
//  LoginViewModel.swift
//  iosApp
//
//  SwiftUI ObservableObject — drives the login screen.
//  Uses KMP LoginUseCase for the network call and
//  KMP LocalKeychainTokenProvider (via UserPreferencesProvider) for persistence.
//

import Foundation
import Combine
import LocalAuthentication
import SharedAuthDomain

@MainActor
final class LoginViewModel: ObservableObject {

    // MARK: - UI State

    struct UiState {
        var email:                String = ""
        var password:             String = ""
        var rememberUsername:     Bool   = false
        var biometricEnabled:     Bool   = false
        var isBiometricAvailable: Bool   = false
        var status:               Status = .idle

        enum Status: Equatable {
            case idle
            case loading
            case biometricPrompting
            case error(String)
        }
    }

    @Published private(set) var uiState = UiState()
    @Published private(set) var shouldNavigateToDashboard = false

    // MARK: - Dependencies

    private let loginUseCase: LoginUseCase
    private let userPrefs: any LocalUserPreferencesProvider

    // MARK: - Init

    init(loginUseCase: LoginUseCase, userPrefs: any LocalUserPreferencesProvider) {
        self.loginUseCase = loginUseCase
        self.userPrefs    = userPrefs
        restorePreferences()
    }

    // MARK: - Intents

    func onEmailChanged(_ email: String) {
        uiState.email  = email
        uiState.status = .idle
    }

    func onPasswordChanged(_ password: String) {
        uiState.password = password
        uiState.status   = .idle
    }

    func onRememberUsernameChanged(_ checked: Bool) { uiState.rememberUsername = checked }
    func onBiometricEnabledChanged(_ checked: Bool) { uiState.biometricEnabled = checked }

    func onBiometricSuccess() {
        uiState.status            = .idle
        shouldNavigateToDashboard = true
    }

    func onBiometricDismissed() { uiState.status = .idle }

    func submit() {
        guard uiState.status != .loading else { return }
        let email    = uiState.email.trimmingCharacters(in: .whitespaces)
        let password = uiState.password
        guard !email.isEmpty, !password.isEmpty else {
            uiState.status = .error("Email and password are required")
            return
        }
        uiState.status = .loading
        loginUseCase.invoke(email: email, password: password) { [weak self] _, error in
            guard let self else { return }
            // Kotlin completion handler fires on a background thread — hop to main
            DispatchQueue.main.async {
                if let error {
                    self.uiState.status = .error(error.localizedDescription)
                } else {
                    Task { @MainActor in
                        await self.handleLoginSuccess(email: email)
                    }
                }
            }
        }
    }

    // MARK: - Private

    private func restorePreferences() {
        uiState.isBiometricAvailable = biometricAvailable()
        if let email = userPrefs.getRememberedEmail() {
            uiState.email            = email
            uiState.rememberUsername = true
            uiState.biometricEnabled = userPrefs.isBiometricEnabled()
        }
    }

    private func handleLoginSuccess(email: String) async {
        let state = uiState
        if state.rememberUsername {
            userPrefs.saveUserPreferences(email: email, biometricEnabled: state.biometricEnabled)
        } else {
            userPrefs.clearUserPreferences()
        }
        if state.rememberUsername && state.biometricEnabled {
            uiState.status = .biometricPrompting
        } else {
            uiState.status            = .idle
            shouldNavigateToDashboard = true
        }
    }

    private func biometricAvailable() -> Bool {
        let context = LAContext()
        var error: NSError?
        return context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error)
    }
}
