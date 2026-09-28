package com.identityx.auth_domain.data

import com.identityx.android.core.network.model.AuthRequest
import com.identityx.android.core.network.restful.IdentityXApiClient
import com.identityx.local.domain.TokenProvider

/**
 * Shared KMP implementation of [LoginDataSource].
 *
 * Mirrors the logic in :feature:login's Android-only RealLoginDataSource, but lives
 * in commonMain so both Android and iOS consume the same data layer through
 * the SharedAuthDomain framework:
 *
 *   1. POST /api/v1/auth/login via [IdentityXApiClient] (Ktor — KMP-compatible)
 *   2. Persist tokens via [TokenProvider] (interface in :shared:local commonMain)
 *   3. Notify [SessionCallback] so each platform can update its session state
 *
 * DI wiring:
 *   - Android: constructed by Hilt in the consuming app module; [SessionCallback]
 *              is bound to [com.identityx.android.core.network.session.SessionManager]
 *   - iOS:     constructed manually in iosAppApp.swift; [SessionCallback] is
 *              implemented by the Swift AuthSession object
 */
class RealLoginDataSource(
    private val apiClient: IdentityXApiClient,
    private val tokenProvider: TokenProvider,
    private val sessionCallback: SessionCallback
) : LoginDataSource {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = apiClient.login(AuthRequest(email = email, password = password))
            tokenProvider.saveTokens(response.accessToken, response.refreshToken)
            sessionCallback.onLoginSuccess()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
