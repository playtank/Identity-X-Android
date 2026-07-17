package com.identityx.login.data

import com.identityx.android.core.network.model.AuthRequest
import com.identityx.android.core.network.restful.IdentityXApiClient
import com.identityx.android.core.network.session.SessionManager
import com.identityx.local.domain.TokenProvider
import javax.inject.Inject

/**
 * Real login data source — calls the Ktor backend via IdentityXApiClient.
 * On success, persists tokens via [TokenProvider] and notifies [SessionManager].
 */
class RealLoginDataSource @Inject constructor(
    private val apiClient: IdentityXApiClient,
    private val tokenProvider: TokenProvider,
    private val sessionManager: SessionManager
) : LoginDataSource {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = apiClient.login(AuthRequest(email = email, password = password))
            tokenProvider.saveTokens(response.accessToken, response.refreshToken)
            sessionManager.onLoginSuccess()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
