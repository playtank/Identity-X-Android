package com.identityx.login.data

import com.identityx.android.core.network.model.AuthRequest
import com.identityx.android.core.network.restful.IdentityXApiClient
import com.identityx.local.domain.TokenProvider
import javax.inject.Inject

/**
 * Real login data source — calls the Ktor backend via IdentityXApiClient.
 * On success, persists the returned tokens via TokenProvider.
 *
 * Base URL is set from BuildConfig.BASE_URL, which reads DEBUG_BASE_URL
 * from local.properties at build time (never committed to source control).
 */
class RealLoginDataSource @Inject constructor(
    private val apiClient: IdentityXApiClient,
    private val tokenProvider: TokenProvider
) : LoginDataSource {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = apiClient.login(AuthRequest(email = email, password = password))
            tokenProvider.saveTokens(response.accessToken, response.refreshToken)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
