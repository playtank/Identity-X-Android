package com.identityx.local

import com.identityx.local.domain.TokenProvider
import com.identityx.local.domain.UserPreferencesProvider
import platform.Foundation.NSUserDefaults
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.darwin.OSStatus
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding

/**
 * iOS implementation of [TokenProvider] and [UserPreferencesProvider].
 *
 * Mirrors Android's [EncryptedTokenProvider]:
 *  - Tokens (sensitive)       → iOS Keychain (kSecClassGenericPassword)
 *  - Preferences (low-risk)   → NSUserDefaults (same as Android SharedPreferences)
 *
 * Constructed manually in iosAppApp (no DI framework on iOS).
 */
@OptIn(ExperimentalForeignApi::class)
class KeychainTokenProvider : TokenProvider, UserPreferencesProvider {

    // ── Keychain service tag ─────────────────────────────────────────────────

    private val service = "com.identityx.auth"

    // ── Key constants ────────────────────────────────────────────────────────

    private companion object {
        const val KEY_ACCESS    = "encrypted_access_token"
        const val KEY_REFRESH   = "encrypted_refresh_token"
        const val KEY_EMAIL     = "remembered_email"
        const val KEY_BIOMETRIC = "biometric_enabled"
    }

    // ── TokenProvider ────────────────────────────────────────────────────────

    override fun getAccessToken(): String?  = keychainRead(KEY_ACCESS)
    override fun getRefreshToken(): String? = keychainRead(KEY_REFRESH)

    override fun saveTokens(accessToken: String, refreshToken: String) {
        keychainWrite(KEY_ACCESS,  accessToken)
        keychainWrite(KEY_REFRESH, refreshToken)
    }

    override fun clearTokens() {
        keychainDelete(KEY_ACCESS)
        keychainDelete(KEY_REFRESH)
    }

    // ── UserPreferencesProvider ──────────────────────────────────────────────

    override fun getRememberedEmail(): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(KEY_EMAIL)

    override fun isBiometricEnabled(): Boolean =
        NSUserDefaults.standardUserDefaults.boolForKey(KEY_BIOMETRIC)

    override fun saveUserPreferences(email: String, biometricEnabled: Boolean) {
        NSUserDefaults.standardUserDefaults.setObject(email, KEY_EMAIL)
        NSUserDefaults.standardUserDefaults.setBool(biometricEnabled, KEY_BIOMETRIC)
    }

    override fun clearUserPreferences() {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(KEY_EMAIL)
        NSUserDefaults.standardUserDefaults.removeObjectForKey(KEY_BIOMETRIC)
    }

    // ── Keychain helpers ─────────────────────────────────────────────────────

    private fun keychainWrite(key: String, value: String) {
        val data = NSString.create(string = value)
            .dataUsingEncoding(NSUTF8StringEncoding) ?: return

        val query = mapOf(
            kSecClass        to kSecClassGenericPassword,
            kSecAttrService  to service,
            kSecAttrAccount  to key
        )

        val status: OSStatus = SecItemCopyMatching(query as CFDictionaryRef, null)

        if (status == 0) {
            // Item exists — update it
            val update = mapOf(
                kSecValueData       to data,
                kSecAttrAccessible  to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
            )
            SecItemUpdate(query as CFDictionaryRef, update as CFDictionaryRef)
        } else {
            // New item — add it
            val add = mapOf(
                kSecClass           to kSecClassGenericPassword,
                kSecAttrService     to service,
                kSecAttrAccount     to key,
                kSecValueData       to data,
                kSecAttrAccessible  to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
            )
            SecItemAdd(add as CFDictionaryRef, null)
        }
    }

    private fun keychainRead(key: String): String? {
        val query = mapOf(
            kSecClass       to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to key,
            kSecReturnData  to true,
            kSecMatchLimit  to kSecMatchLimitOne
        )
        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as CFDictionaryRef, result.ptr)
            if (status != 0) return null
            val data = result.value as? NSData ?: return null
            return NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
        }
    }

    private fun keychainDelete(key: String) {
        val query = mapOf(
            kSecClass       to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to key
        )
        SecItemDelete(query as CFDictionaryRef)
    }
}
