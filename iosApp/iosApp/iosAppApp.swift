//
//  iosAppApp.swift
//  iosApp
//
//  The entire data/domain graph is built in Kotlin via LoginServiceFactory.
//  Swift owns only the session observable and the SwiftUI presentation layer.
//

import SwiftUI
import Combine
import SharedAuthDomain

// MARK: - App container

final class AppContainer: ObservableObject {

    static let baseURL = "https://api.identityx.com"

    let authSession:       AuthSession
    let loginViewModel:    LoginViewModel
    let dashboardViewModel: DashboardViewModel

    init() {
        // 1. Session — created first so it can be passed into the Kotlin factory
        let session = AuthSession()
        self.authSession = session

        // 2. Build the entire Kotlin graph in one call
        let services = LoginServiceFactory.shared.create(
            baseUrl:         Self.baseURL,
            sessionCallback: session
        )

        // 3. Restore persisted session from Keychain
        session.configure(tokenProvider: services.tokenProvider)

        // 4. SwiftUI ViewModels — both use Kotlin dependencies
        self.loginViewModel = LoginViewModel(
            loginUseCase: services.loginUseCase,
            userPrefs:    services.tokenProvider
        )

        self.dashboardViewModel = DashboardViewModel(
            apiClient: services.apiClient
        )
    }
}

// MARK: - App entry point

@main
struct iosAppApp: App {

    @StateObject private var container = AppContainer()

    var body: some Scene {
        WindowGroup {
            AppRootView(
                loginViewModel:    container.loginViewModel,
                dashboardViewModel: container.dashboardViewModel
            )
            .environmentObject(container.authSession)
        }
    }
}
