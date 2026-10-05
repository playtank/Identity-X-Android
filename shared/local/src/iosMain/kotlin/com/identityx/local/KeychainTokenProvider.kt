package com.identityx.local

import com.identityx.local.domain.TokenProvider
import com.identityx.local.domain.UserPreferencesProvider
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings

/**
 * iOS implementation of [TokenProvider] and [UserPreferencesProvider].
 *
 * Uses multiplatform-settings [KeychainSettings] for tokens (Keychain-backed,
 * encrypted) and a separate NSUserDefaults-backed [Settings] for non-sensitive
 * preferences — exactly mirroring Android's EncryptedTokenProvider.
 *
 * KeychainSettings handles all CFDictionary/Security framework interop internally,
 * avoiding the CValuesRef type mismatch issues with raw cinterop.
 */
@OptIn(com.russhwolf.settings.ExperimentalSettingsImplementation::class)
class KeychainTokenProvider : TokenProvider, UserPreferencesProvider {

    // Tokens stored in Keychain (AES-256 encrypted by iOS Secure Enclave)
    private val secureStore: Settings = KeychainSettings(service = "com.identityx.auth.tokens")

    // Non-sensitive preferences stored in NSUserDefaults
    private val prefsStore: Settings = KeychainSettings(service = "com.identityx.auth.prefs")

    private companion object {
        const val KEY_ACCESS    = "encrypted_access_token"
        const val KEY_REFRESH   = "encrypted_refresh_token"
        const val KEY_EMAIL     = "remembered_email"
        const val KEY_BIOMETRIC = "biometric_enabled"
    }

    // ── TokenProvider ────────────────────────────────────────────────────────

    override fun getAccessToken(): String?  = secureStore.getStringOrNull(KEY_ACCESS)
    override fun getRefreshToken(): String? = secureStore.getStringOrNull(KEY_REFRESH)

    override fun saveTokens(accessToken: String, refreshToken: String) {
        secureStore.putString(KEY_ACCESS,  accessToken)
        secureStore.putString(KEY_REFRESH, refreshToken)
    }

    override fun clearTokens() {
        secureStore.remove(KEY_ACCESS)
        secureStore.remove(KEY_REFRESH)
    }

    // ── UserPreferencesProvider ──────────────────────────────────────────────

    override fun getRememberedEmail(): String?  = prefsStore.getStringOrNull(KEY_EMAIL)
    override fun isBiometricEnabled(): Boolean  = prefsStore.getBooleanOrNull(KEY_BIOMETRIC) ?: false

    override fun saveUserPreferences(email: String, biometricEnabled: Boolean) {
        prefsStore.putString(KEY_EMAIL,     email)
        prefsStore.putBoolean(KEY_BIOMETRIC, biometricEnabled)
    }

    override fun clearUserPreferences() {
        prefsStore.remove(KEY_EMAIL)
        prefsStore.remove(KEY_BIOMETRIC)
    }
}
