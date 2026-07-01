package com.identityx.local.domain

/**
 * Contract for reading, writing, and invalidating auth tokens.
 * Implemented by EncryptedTokenProvider in :core:local.
 * Consumed by AuthInterceptor and OkHttpTokenAuthenticator in :core:network.
 */
interface TokenProvider {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun saveTokens(accessToken: String, refreshToken: String)
    fun clearTokens()
}
