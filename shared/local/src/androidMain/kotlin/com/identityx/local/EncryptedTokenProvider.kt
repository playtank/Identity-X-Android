package com.identityx.local

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.identityx.local.domain.TokenProvider
import com.identityx.local.domain.UserPreferencesProvider

/**
 * Stores auth tokens and user preferences in EncryptedSharedPreferences,
 * backed by AES-256-GCM via Android Keystore.
 *
 * No Hilt annotations here — this class lives in :shared:local which has no
 * Hilt plugin (KSP + Hilt are incompatible with the KMP library plugin).
 * Hilt constructs this via @Provides in :core:local's LocalDataModule,
 * which does have the Hilt plugin and supplies the Context.
 */
class EncryptedTokenProvider(
    context: Context
) : TokenProvider, UserPreferencesProvider {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "identityx_secure_tokens",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

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
        private const val KEY_ACCESS    = "encrypted_access_token"
        private const val KEY_REFRESH   = "encrypted_refresh_token"
        private const val KEY_EMAIL     = "remembered_email"
        private const val KEY_BIOMETRIC = "biometric_enabled"
    }
}
