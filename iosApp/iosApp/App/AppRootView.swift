//
//  AppRootView.swift
//  iosApp
//
//  Root router — switches between LoginView and DashboardView based on
//  AuthSession.isLoggedIn. Equivalent of Android's AppNavHost.
//

import SwiftUI
import Combine

struct AppRootView: View {

    @EnvironmentObject private var authSession: AuthSession

    @ObservedObject var loginViewModel:     LoginViewModel
    @ObservedObject var dashboardViewModel: DashboardViewModel

    var body: some View {
        Group {
            if authSession.isLoggedIn {
                DashboardView(viewModel: dashboardViewModel)
            } else {
                NavigationStack {
                    LoginView(viewModel: loginViewModel)
                        .navigationBarHidden(true)
                }
                .onChange(of: loginViewModel.shouldNavigateToDashboard) { navigate in
                    if navigate, !authSession.isLoggedIn {
                        authSession.onLoginSuccess()
                    }
                }
            }
        }
        .animation(.easeInOut(duration: 0.3), value: authSession.isLoggedIn)
    }
}
