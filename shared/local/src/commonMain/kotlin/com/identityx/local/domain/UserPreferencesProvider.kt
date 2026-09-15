package com.identityx.local.domain

/**
 * Persists user-facing preferences that survive app restarts.
 *
 * Lives in commonMain — the interface is platform-agnostic.
 * Android stores these in EncryptedSharedPreferences; a future iOS target
 * would implement this using NSUserDefaults or the Keychain.
 */
interface UserPreferencesProvider {
    fun getRememberedEmail(): String?
    fun isBiometricEnabled(): Boolean
    fun saveUserPreferences(email: String, biometricEnabled: Boolean)
    fun clearUserPreferences()
}
