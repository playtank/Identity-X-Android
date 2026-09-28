package com.identityx.auth_domain

import com.identityx.auth_domain.data.RealLoginDataSource
import com.identityx.auth_domain.data.SessionCallback
import com.identityx.auth_domain.domain.usecase.LoginUseCase
import com.identityx.auth_domain.data.LoginRepository
import com.identityx.android.core.network.restful.IdentityXApiClient
import com.identityx.local.KeychainTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * iOS-only factory that wires the complete login object graph in Kotlin.
 *
 * Swift only needs to:
 *   1. Implement [SessionCallback] (AuthSession.swift already does this)
 *   2. Call [LoginServiceFactory.create] once at startup
 *   3. Pass the returned [LoginServiceResult] into the SwiftUI layer
 *
 * This keeps iosAppApp.swift free of any Ktor or Keychain construction details.
 */
object LoginServiceFactory {

    /**
     * Builds and returns the complete login service graph.
     *
     * @param baseUrl        API base URL, e.g. "https://api.identityx.com"
     * @param sessionCallback The Swift [AuthSession] that conforms to [SessionCallback]
     */
    fun create(baseUrl: String, sessionCallback: SessionCallback): LoginServiceResult {
        // 1. iOS Keychain + NSUserDefaults storage
        val tokenProvider = KeychainTokenProvider()

        // 2. Ktor HttpClient with Darwin (NSURLSession) engine
        val httpClient = HttpClient(Darwin) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        // 3. Network client
        val apiClient = IdentityXApiClient(
            client  = httpClient,
            baseUrl = baseUrl
        )

        // 4. Full domain graph
        val dataSource = RealLoginDataSource(
            apiClient       = apiClient,
            tokenProvider   = tokenProvider,
            sessionCallback = sessionCallback
        )
        val repository = LoginRepository(dataSource)
        val useCase    = LoginUseCase(repository)

        return LoginServiceResult(
            loginUseCase  = useCase,
            tokenProvider = tokenProvider
        )
    }
}

/**
 * Carries the two objects Swift actually needs after the graph is built:
 *  - [loginUseCase]  → drives [LoginViewModel]
 *  - [tokenProvider] → drives [AuthSession] (token check on cold start)
 *                      and [LoginViewModel] (UserPreferencesProvider for remembered email)
 */
class LoginServiceResult(
    val loginUseCase:  LoginUseCase,
    val tokenProvider: KeychainTokenProvider
)
