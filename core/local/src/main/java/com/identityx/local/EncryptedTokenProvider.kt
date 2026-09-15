package com.identityx.local

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.identityx.local.domain.TokenProvider
import com.identityx.local.domain.UserPreferencesProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores auth tokens and user preferences in EncryptedSharedPreferences,
 * backed by AES-256-GCM via Android Keystore.
 *
 * Implements both [TokenProvider] and [UserPreferencesProvider] in one place
 * so both share the same encrypted storage file.
 */
@Singleton
class EncryptedTokenProvider @Inject constructor(
    @ApplicationContext private val context: Context
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
