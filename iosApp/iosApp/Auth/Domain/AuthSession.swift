//
//  AuthSession.swift
//  iosApp
//
//  Observable session state — the single source of truth for whether the user
//  is logged in. Equivalent of Android's SessionManager.
//
//  Any view or ViewModel that needs to react to login/logout observes this object.
//

import Foundation

@MainActor
final class AuthSession: ObservableObject {

    // MARK: - Published state

    /// `true` when valid tokens are present in the Keychain.
    @Published private(set) var isLoggedIn: Bool

    // MARK: - Dependencies

    private let tokenStore: KeychainTokenStore
    private let userPrefs: UserPreferencesStore

    // MARK: - Init

    init(
        tokenStore: KeychainTokenStore = .shared,
        userPrefs: UserPreferencesStore = .shared
    ) {
        self.tokenStore = tokenStore
        self.userPrefs  = userPrefs
        // Restore session from Keychain on cold start.
        self.isLoggedIn = tokenStore.hasTokens
    }

    // MARK: - Session lifecycle

    /// Called by the login data source after tokens are persisted.
    func onLoginSuccess() {
        isLoggedIn = true
    }

    /// Clears tokens and user preferences, then routes back to login.
    func logout() {
        tokenStore.clearTokens()
        isLoggedIn = false
    }
}
