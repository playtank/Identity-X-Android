//
//  LoginView.swift
//  iosApp
//
//  SwiftUI login screen — mirrors Android's LoginScreen / LoginScreenContent.
//
//  Layout:
//    • Logo / branding header
//    • Email field
//    • Password field (toggle visibility)
//    • Remember username toggle
//    • Enable biometric toggle (conditional — only when rememberUsername is on
//      and the device supports biometrics)
//    • Error / biometric-waiting message
//    • Sign In button (shows spinner while loading)
//    • Forgot password / Register / Support links
//

import SwiftUI
import LocalAuthentication

struct LoginView: View {

    @ObservedObject var viewModel: LoginViewModel

    @State private var passwordVisible = false

    // Derived convenience
    private var state: LoginViewModel.UiState { viewModel.uiState }
    private var isInteracting: Bool {
        state.status != .loading && state.status != .biometricPrompting
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {

                // ── Header ────────────────────────────────────────────────
                headerSection

                // ── Fields ────────────────────────────────────────────────
                VStack(spacing: 16) {
                    emailField
                    passwordField
                }
                .padding(.horizontal, 24)
                .padding(.top, 32)

                // ── Toggles ───────────────────────────────────────────────
                VStack(spacing: 4) {
                    rememberUsernameToggle
                    if state.rememberUsername && state.isBiometricAvailable {
                        biometricToggle
                    }
                }
                .padding(.horizontal, 24)
                .padding(.top, 8)

                // ── Status feedback ───────────────────────────────────────
                statusFeedback
                    .padding(.horizontal, 24)
                    .padding(.top, 12)

                // ── Sign In button ─────────────────────────────────────────
                signInButton
                    .padding(.horizontal, 24)
                    .padding(.top, 24)

                // ── Footer links ──────────────────────────────────────────
                footerLinks
                    .padding(.top, 32)
                    .padding(.bottom, 40)
            }
        }
        .background(Color(.systemBackground))
        // Trigger biometric prompt when status becomes .biometricPrompting
        .onChange(of: state.status) { status in
            if status == .biometricPrompting {
                launchBiometricPrompt()
            }
        }
    }

    // MARK: - Subviews

    private var headerSection: some View {
        VStack(spacing: 12) {
            Image(systemName: "person.badge.shield.checkmark.fill")
                .font(.system(size: 64))
                .foregroundStyle(.tint)
                .padding(.top, 60)

            Text("Identity X")
                .font(.largeTitle.bold())

            Text("Sign in to your account")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .padding(.bottom, 8)
    }

    private var emailField: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Email")
                .font(.footnote.weight(.medium))
                .foregroundStyle(.secondary)

            TextField("you@example.com", text: Binding(
                get: { state.email },
                set: { viewModel.onEmailChanged($0) }
            ))
            .keyboardType(.emailAddress)
            .textContentType(.emailAddress)
            .autocapitalization(.none)
            .autocorrectionDisabled()
            .disabled(!isInteracting)
            .padding(12)
            .background(Color(.secondarySystemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 10))
        }
    }

    private var passwordField: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Password")
                .font(.footnote.weight(.medium))
                .foregroundStyle(.secondary)

            HStack {
                Group {
                    if passwordVisible {
                        TextField("••••••••", text: Binding(
                            get: { state.password },
                            set: { viewModel.onPasswordChanged($0) }
                        ))
                    } else {
                        SecureField("••••••••", text: Binding(
                            get: { state.password },
                            set: { viewModel.onPasswordChanged($0) }
                        ))
                    }
                }
                .textContentType(.password)
                .disabled(!isInteracting)

                Button {
                    passwordVisible.toggle()
                } label: {
                    Image(systemName: passwordVisible ? "eye.slash" : "eye")
                        .foregroundStyle(.secondary)
                }
                .buttonStyle(.plain)
            }
            .padding(12)
            .background(Color(.secondarySystemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 10))
        }
    }

    private var rememberUsernameToggle: some View {
        Toggle(isOn: Binding(
            get: { state.rememberUsername },
            set: { viewModel.onRememberUsernameChanged($0) }
        )) {
            Text("Remember username")
                .font(.subheadline)
        }
        .disabled(!isInteracting)
        .padding(.vertical, 4)
    }

    private var biometricToggle: some View {
        Toggle(isOn: Binding(
            get: { state.biometricEnabled },
            set: { viewModel.onBiometricEnabledChanged($0) }
        )) {
            Text("Enable biometric login")
                .font(.subheadline)
        }
        .disabled(!isInteracting)
        .padding(.vertical, 4)
    }

    @ViewBuilder
    private var statusFeedback: some View {
        switch state.status {
        case .error(let message):
            Text(message)
                .font(.footnote)
                .foregroundStyle(.red)
                .frame(maxWidth: .infinity, alignment: .leading)

        case .biometricPrompting:
            Label("Waiting for biometric verification…", systemImage: "faceid")
                .font(.footnote)
                .foregroundStyle(.secondary)
                .frame(maxWidth: .infinity, alignment: .leading)

        default:
            EmptyView()
        }
    }

    private var signInButton: some View {
        Button(action: viewModel.submit) {
            ZStack {
                // Always reserve the same height so the button doesn't resize.
                Color.clear.frame(height: 20)

                if state.status == .loading {
                    ProgressView()
                        .tint(.white)
                } else {
                    Text("Sign In")
                        .font(.headline)
                        .foregroundStyle(.white)
                }
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(isInteracting ? Color.accentColor : Color.accentColor.opacity(0.5))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .disabled(!isInteracting)
    }

    private var footerLinks: some View {
        VStack(spacing: 12) {
            Button("Forgot Password?") {
                // TODO: hook up forgot-password navigation
            }
            .font(.subheadline)

            HStack(spacing: 4) {
                Text("Don't have an account?")
                    .foregroundStyle(.secondary)
                Button("Register") {
                    // TODO: hook up registration navigation
                }
            }
            .font(.subheadline)
        }
    }

    // MARK: - Biometric prompt

    private func launchBiometricPrompt() {
        let context = LAContext()
        context.evaluatePolicy(
            .deviceOwnerAuthenticationWithBiometrics,
            localizedReason: "Confirm your identity to continue"
        ) { success, _ in
            DispatchQueue.main.async {
                if success {
                    viewModel.onBiometricSuccess()
                } else {
                    viewModel.onBiometricDismissed()
                }
            }
        }
    }
}

// MARK: - Preview

#Preview {
    let dataSource = MockLoginDataSource()
    let repo       = SharedAuthDomainLoginRepository(dataSource: dataSource)
    let useCase    = SharedAuthDomainLoginUseCase(repository: repo)
    let vm         = LoginViewModel(loginUseCase: useCase)
    return LoginView(viewModel: vm)
}
