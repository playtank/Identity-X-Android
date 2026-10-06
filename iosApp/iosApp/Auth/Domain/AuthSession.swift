//
//  AuthSession.swift
//  iosApp
//
//  Observable session state — single source of truth for login status.
//  Conforms to SessionCallback so Kotlin's RealLoginDataSource can notify
//  it directly after a successful login.
//
//  THREADING: Kotlin calls onLoginSuccess() from a background coroutine thread.
//  We must dispatch back to the main thread before mutating @Published properties.
//  The class is NOT @MainActor so Kotlin can call it freely; individual
//  mutations are explicitly dispatched to main.
//

import Foundation
import Combine
import SharedAuthDomain

final class AuthSession: NSObject, ObservableObject, SessionCallback {

    // MARK: - Published state (mutate only on main thread)

    @Published private(set) var isLoggedIn: Bool = false

    // MARK: - Dependencies

    private var tokenProvider: (any LocalTokenProvider)?

    // MARK: - Init

    override init() {
        super.init()
    }

    /// Called by AppContainer after LoginServiceFactory returns.
    func configure(tokenProvider: any LocalTokenProvider) {
        self.tokenProvider = tokenProvider
        // Already on main thread at app startup
        self.isLoggedIn = tokenProvider.getAccessToken() != nil
    }

    // MARK: - SessionCallback  (called from Kotlin background thread)

    func onLoginSuccess() {
        DispatchQueue.main.async {
            self.isLoggedIn = true
        }
    }

    // MARK: - Session lifecycle

    func logout() {
        tokenProvider?.clearTokens()
        DispatchQueue.main.async {
            self.isLoggedIn = false
        }
    }
}
