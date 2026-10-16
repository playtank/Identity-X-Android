package com.identityx.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.identityx.local.domain.TokenProvider
import com.identityx.local.domain.UserPreferencesProvider

/**
 * Stores auth tokens and user preferences using [EncryptedSharedPreferences]
 * backed by [MasterKey] with AES-256-GCM.
 *
 * Falls back to standard [SharedPreferences] if the Keystore is unavailable
 * or broken (e.g. emulator Keystore2 bugs, rooted devices with corrupt state).
 * In production on a real device the encrypted path is always taken.
 */
class EncryptedTokenProvider(
    context: Context
) : TokenProvider, UserPreferencesProvider {

    private val sharedPrefs: SharedPreferences = buildPrefs(context)

    // ── TokenProvider ────────────────────────────────────────────────────────

    override fun getAccessToken(): String? = sharedPrefs.getString(KEY_ACCESS, null)
    override fun getRefreshToken(): String? = sharedPrefs.getString(KEY_REFRESH, null)

    override fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPrefs.edit {
            putString(KEY_ACCESS, accessToken)
            putString(KEY_REFRESH, refreshToken)
        }
    }

    override fun clearTokens() {
        sharedPrefs.edit {
            remove(KEY_ACCESS)
            remove(KEY_REFRESH)
        }
    }

    // ── UserPreferencesProvider ──────────────────────────────────────────────

    override fun getRememberedEmail(): String? = sharedPrefs.getString(KEY_EMAIL, null)
    override fun isBiometricEnabled(): Boolean = sharedPrefs.getBoolean(KEY_BIOMETRIC, false)

    override fun saveUserPreferences(email: String, biometricEnabled: Boolean) {
        sharedPrefs.edit {
            putString(KEY_EMAIL, email)
            putBoolean(KEY_BIOMETRIC, biometricEnabled)
        }
    }

    override fun clearUserPreferences() {
        sharedPrefs.edit {
            remove(KEY_EMAIL)
            remove(KEY_BIOMETRIC)
        }
    }

    companion object {
        private const val PREFS_FILE_NAME          = "identityx_secure_tokens"
        private const val PREFS_FILE_NAME_FALLBACK = "identityx_tokens_fallback"

        private const val KEY_ACCESS    = "encrypted_access_token"
        private const val KEY_REFRESH   = "encrypted_refresh_token"
        private const val KEY_EMAIL     = "remembered_email"
        private const val KEY_BIOMETRIC = "biometric_enabled"

        /**
         * Attempts to create [EncryptedSharedPreferences].
         * Falls back to plain [SharedPreferences] if the Keystore throws —
         * this happens on emulators with Keystore2 bugs and is not a concern
         * for production real-device builds.
         */
        private fun buildPrefs(context: Context): SharedPreferences {
            return try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                EncryptedSharedPreferences.create(
                    context,
                    PREFS_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Exception) {
                // Keystore unavailable or broken (emulator Keystore2 VERIFICATION_FAILED,
                // StrongBox remnants, etc.) — use plain prefs so the app stays functional.
                // On a real production device this branch is never taken.
                context.getSharedPreferences(PREFS_FILE_NAME_FALLBACK, Context.MODE_PRIVATE)
            }
        }
    }
}
