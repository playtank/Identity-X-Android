package com.identityx.local.domain

/**
 * Contract for reading, writing, and invalidating auth tokens.
 *
 * Lives in commonMain so any KMP target can depend on the interface without
 * pulling in Android-specific storage (EncryptedSharedPreferences / Keychain).
 * The Android implementation is [com.identityx.local.EncryptedTokenProvider].
 */
interface TokenProvider {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun saveTokens(accessToken: String, refreshToken: String)
    fun clearTokens()
}
