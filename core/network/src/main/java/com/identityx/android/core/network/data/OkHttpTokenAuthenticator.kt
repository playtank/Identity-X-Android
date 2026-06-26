package com.identityx.android.core.network.data

import com.identityx.local.domain.TokenProvider
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class OkHttpTokenAuthenticator @Inject constructor(
    private val tokenProvider: TokenProvider,
    @Named("baseUrl") private val baseUrl: String
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Guard against infinite retry loops (max 2 attempts per request chain)
        if (response.responseCount() >= 2) return null

        synchronized(this) {
            val currentAccess = tokenProvider.getAccessToken()
            val requestHeaderToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")

            // Double-checked lock: if another thread already refreshed, reuse the new token
            if (currentAccess != null && currentAccess != requestHeaderToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccess")
                    .build()
            }

            val refreshToken = tokenProvider.getRefreshToken() ?: return null
            val newTokens = performHttpRefresh(refreshToken)

            return if (newTokens != null) {
                tokenProvider.saveTokens(newTokens.first, newTokens.second)
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${newTokens.first}")
                    .build()
            } else {
                tokenProvider.clearTokens()
                null
            }
        }
    }

    private fun performHttpRefresh(refreshToken: String): Pair<String, String>? {
        return try {
            val json = """{"refresh_token":"$refreshToken"}"""
            val body = json.toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$baseUrl/api/v1/auth/refresh")
                .post(body)
                .build()

            // Use a dedicated bare client for token refresh to avoid recursive interceptor calls
            OkHttpClient().newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    // TODO: parse actual tokens from response body using kotlinx.serialization
                    Pair("new_access_token", "new_refresh_token")
                } else {
                    null
                }
            }
        } catch (e: IOException) {
            null
        }
    }

    private fun Response.responseCount(): Int {
        var count = 1
        var prior = priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
