//
//  AppRootView.swift
//  iosApp
//
//  Root router — switches between LoginView and DashboardView based on
//  AuthSession.isLoggedIn. Equivalent of Android's AppNavHost routing
//  between NavigationDestinations.LOGIN and the main graph.
//

import Foundation
import Combine
import SwiftUI

struct AppRootView: View {

    @EnvironmentObject private var authSession: AuthSession

    // AppContainer owns the lifetime — we just observe it here.
    @ObservedObject var loginViewModel: LoginViewModel

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
