//
//  LoginViewModel.swift
//  iosApp
//
//  SwiftUI ObservableObject mirror of Android's LoginViewModel.
//  Uses the KMP LoginUseCase from SharedAuthDomain for the actual auth call.
//
//  State machine:
//    Idle ──[submit]──▶ Loading ──[success]──▶ BiometricPrompting or navigate
//                                └──[failure]──▶ Error(message)
//

import Foundation
import LocalAuthentication
import SharedAuthDomain

@MainActor
final class LoginViewModel: ObservableObject {

    // MARK: - UI State

    struct UiState {
        var email:               String  = ""
        var password:            String  = ""
        var rememberUsername:    Bool    = false
        var biometricEnabled:    Bool    = false
        var isBiometricAvailable: Bool   = false
        var status:              Status  = .idle

        enum Status: Equatable {
            case idle
            case loading
            case biometricPrompting
            case error(String)
        }
    }

    @Published private(set) var uiState = UiState()

    /// Fires once when the user should be taken to the dashboard.
    @Published private(set) var shouldNavigateToDashboard = false

    // MARK: - Dependencies

    private let loginUseCase: SharedAuthDomainLoginUseCase
    private let userPrefs: UserPreferencesStore

    // MARK: - Init

    init(
        loginUseCase: SharedAuthDomainLoginUseCase,
        userPrefs: UserPreferencesStore = .shared
    ) {
        self.loginUseCase = loginUseCase
        self.userPrefs    = userPrefs
        restorePreferences()
    }

    // MARK: - Intent handlers  (mirrors LoginViewModel.onIntent)

    func onEmailChanged(_ email: String) {
        uiState.email  = email
        uiState.status = .idle
    }

    func onPasswordChanged(_ password: String) {
        uiState.password = password
        uiState.status   = .idle
    }

    func onRememberUsernameChanged(_ checked: Bool) {
        uiState.rememberUsername = checked
    }

    func onBiometricEnabledChanged(_ checked: Bool) {
        uiState.biometricEnabled = checked
    }

    func onBiometricSuccess() {
        uiState.status             = .idle
        shouldNavigateToDashboard  = true
    }

    func onBiometricDismissed() {
        uiState.status = .idle
    }

    func submit() {
        guard uiState.status != .loading else { return }

        let email    = uiState.email.trimmingCharacters(in: .whitespaces)
        let password = uiState.password

        guard !email.isEmpty, !password.isEmpty else {
            uiState.status = .error("Email and password are required")
            return
        }

        uiState.status = .loading

        Task {
            do {
                // KMP suspend fun bridged as async throws
                _ = try await loginUseCase.invoke(email: email, password: password)
                await handleLoginSuccess(email: email)
            } catch {
                uiState.status = .error(error.localizedDescription)
            }
        }
    }

    // MARK: - Private

    private func restorePreferences() {
        uiState.isBiometricAvailable = biometricAvailable()

        if let email = userPrefs.rememberedEmail {
            uiState.email            = email
            uiState.rememberUsername = true
            uiState.biometricEnabled = userPrefs.isBiometricEnabled
        }
    }

    private func handleLoginSuccess(email: String) async {
        let state = uiState

        // Persist or clear preferences — mirrors Android ViewModel post-login block
        if state.rememberUsername {
            userPrefs.save(email: email, biometricEnabled: state.biometricEnabled)
        } else {
            userPrefs.clear()
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
