package com.identityx.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.identityx.local.domain.TokenProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

/**
 * Stores access and refresh tokens in EncryptedSharedPreferences,
 * backed by AES-256-GCM via Android Keystore (hardware-backed sandbox where available).
 */
@Singleton
class EncryptedTokenProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : TokenProvider {

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

    override fun getAccessToken(): String? = sharedPrefs.getString(KEY_ACCESS, null)

    override fun getRefreshToken(): String? = sharedPrefs.getString(KEY_REFRESH, null)

    override fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPrefs.edit {
            putString(KEY_ACCESS, accessToken)
                .putString(KEY_REFRESH, refreshToken)
        }
    }

    override fun clearTokens() {
        sharedPrefs.edit {
            remove(KEY_ACCESS)
                .remove(KEY_REFRESH)
        }
    }

    companion object {
        private const val KEY_ACCESS  = "encrypted_access_token"
        private const val KEY_REFRESH = "encrypted_refresh_token"
    }
}
