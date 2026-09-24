//
//  AppRootView.swift
//  iosApp
//
//  Root router — switches between LoginView and DashboardView based on
//  AuthSession.isLoggedIn. Equivalent of Android's AppNavHost routing
//  between NavigationDestinations.LOGIN and the main graph.
//

import SwiftUI
import SharedAuthDomain

struct AppRootView: View {

    @EnvironmentObject private var authSession: AuthSession

    // Hold the ViewModel so it survives recomposition while logged out.
    @StateObject private var loginViewModel: LoginViewModel

    init(loginViewModel: LoginViewModel) {
        _loginViewModel = StateObject(wrappedValue: loginViewModel)
    }

    var body: some View {
        Group {
            if authSession.isLoggedIn {
                DashboardView()
            } else {
                NavigationStack {
                    LoginView(viewModel: loginViewModel)
                        .navigationBarHidden(true)
                }
                // Navigate to dashboard when ViewModel signals success.
                .onChange(of: loginViewModel.shouldNavigateToDashboard) { navigate in
                    if navigate {
                        // AuthSession.isLoggedIn is already true (set by
                        // RealLoginDataSource after token persistence), so
                        // this branch just ensures the flag is consumed.
                        // If using MockLoginDataSource (tests/preview) we
                        // explicitly flip the session here.
                        if !authSession.isLoggedIn {
                            authSession.onLoginSuccess()
                        }
                    }
                }
            }
        }
        .animation(.easeInOut(duration: 0.3), value: authSession.isLoggedIn)
    }
}
