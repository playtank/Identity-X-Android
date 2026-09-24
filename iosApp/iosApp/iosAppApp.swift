//
//  iosAppApp.swift
//  iosApp
//
//  Created by Yunwen Lei on 9/23/26.
//
//  Wires the full dependency graph:
//    KeychainTokenStore → RealLoginDataSource
//    AuthSession        → environment object for all views
//    LoginUseCase       → LoginViewModel → AppRootView → LoginView / DashboardView
//

import SwiftUI
import SharedAuthDomain

@main
struct iosAppApp: App {

    // MARK: - Shared singletons

    private let authSession   = AuthSession()
    private let tokenStore    = KeychainTokenStore.shared
    private let userPrefs     = UserPreferencesStore.shared

    // MARK: - KMP object graph

    /// Change to your real server URL (or drive from a build config / Info.plist).
    private static let baseURL = "https://api.identityx.com"

    // Lazy so `authSession` is fully initialised before we pass it in.
    private lazy var loginUseCase: SharedAuthDomainLoginUseCase = {
        let dataSource = RealLoginDataSource(
            baseURL:    Self.baseURL,
            tokenStore: tokenStore,
            session:    authSession
        )
        let repository = SharedAuthDomainLoginRepository(dataSource: dataSource)
        return SharedAuthDomainLoginUseCase(repository: repository)
    }()

    private lazy var loginViewModel: LoginViewModel = {
        LoginViewModel(loginUseCase: loginUseCase, userPrefs: userPrefs)
    }()

    // MARK: - Scene

    var body: some Scene {
        WindowGroup {
            AppRootView(loginViewModel: loginViewModel)
                .environmentObject(authSession)
        }
    }
}
