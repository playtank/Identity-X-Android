package com.identityx.local.domain

/**
 * Persists user-facing preferences that survive app restarts.
 * Stored in EncryptedSharedPreferences alongside auth tokens.
 *
 * - [rememberedEmail] is saved only when the user checks "Remember username".
 *   It is cleared on logout or when the user unchecks "Remember username" after a successful login.
 * - [biometricEnabled] is only meaningful when [rememberedEmail] is non-null.
 *   It is cleared alongside the email.
 */
interface UserPreferencesProvider {
    fun getRememberedEmail(): String?
    fun isBiometricEnabled(): Boolean
    fun saveUserPreferences(email: String, biometricEnabled: Boolean)
    fun clearUserPreferences()
}
