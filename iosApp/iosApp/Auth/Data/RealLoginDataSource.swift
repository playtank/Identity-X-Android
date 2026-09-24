//
//  RealLoginDataSource.swift
//  iosApp
//
//  Implements the KMP LoginDataSource protocol using URLSession.
//  Mirrors Android's RealLoginDataSource (Ktor → URLSession, Hilt → manual DI).
//
//  Flow:
//    1. POST /api/v1/auth/login  →  { accessToken, refreshToken }
//    2. Persist tokens in KeychainTokenStore
//    3. Notify AuthSession so the app routes to dashboard
//

import Foundation
import SharedAuthDomain

final class RealLoginDataSource: NSObject, SharedAuthDomainLoginDataSource {

    // MARK: - Config

    /// Injected at app startup from Info.plist / build config.
    private let baseURL: String

    // MARK: - Dependencies

    private let tokenStore: KeychainTokenStore
    private let session: AuthSession

    // MARK: - Init

    init(
        baseURL: String,
        tokenStore: KeychainTokenStore = .shared,
        session: AuthSession
    ) {
        self.baseURL    = baseURL
        self.tokenStore = tokenStore
        self.session    = session
    }

    // MARK: - LoginDataSource

    /// Called by the KMP `LoginUseCase` via `LoginRepository`.
    /// Kotlin `suspend` → Swift `async throws`.
    func login(email: String, password: String) async throws -> SharedAuthDomainKotlinUnit {
        let url = URL(string: "\(baseURL)/api/v1/auth/login")!

        var request         = URLRequest(url: url)
        request.httpMethod  = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody    = try JSONEncoder().encode(LoginRequest(email: email, password: password))

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let http = response as? HTTPURLResponse else {
            throw LoginError.invalidResponse
        }
        guard (200..<300).contains(http.statusCode) else {
            // Try to surface a message from the server body.
            let message = (try? JSONDecoder().decode(ErrorBody.self, from: data))?.message
                ?? "Login failed (\(http.statusCode))"
            throw LoginError.serverError(message)
        }

        let authResponse = try JSONDecoder().decode(AuthResponse.self, from: data)

        // Persist tokens — matches Android's TokenProvider.saveTokens()
        tokenStore.saveTokens(
            accessToken:  authResponse.accessToken,
            refreshToken: authResponse.refreshToken
        )

        // Notify session — matches Android's SessionManager.onLoginSuccess()
        await session.onLoginSuccess()

        return SharedAuthDomainKotlinUnit.shared
    }
}

// MARK: - Request / Response models

private struct LoginRequest: Encodable {
    let email: String
    let password: String
}

private struct AuthResponse: Decodable {
    let accessToken: String
    let refreshToken: String
}

/// Generic server error envelope — adapt to your actual API error shape.
private struct ErrorBody: Decodable {
    let message: String
}

// MARK: - Errors

enum LoginError: LocalizedError {
    case invalidResponse
    case serverError(String)

    var errorDescription: String? {
        switch self {
        case .invalidResponse:        return "Invalid server response."
        case .serverError(let msg):   return msg
        }
    }
}
