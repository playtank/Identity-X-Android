//
//  KeychainTokenStore.swift
//  iosApp
//
//  Persists and retrieves the access/refresh token pair in the iOS Keychain.
//  Equivalent of Android's EncryptedTokenProvider / TokenProvider.
//

import Foundation
import Security

final class KeychainTokenStore {

    // MARK: - Keys

    private enum Key {
        static let accessToken  = "com.identityx.auth.accessToken"
        static let refreshToken = "com.identityx.auth.refreshToken"
    }

    // MARK: - Singleton

    static let shared = KeychainTokenStore()
    private init() {}

    // MARK: - Public API

    var accessToken: String? {
        get { read(key: Key.accessToken) }
        set { newValue == nil ? delete(key: Key.accessToken)  : write(newValue!, key: Key.accessToken) }
    }

    var refreshToken: String? {
        get { read(key: Key.refreshToken) }
        set { newValue == nil ? delete(key: Key.refreshToken) : write(newValue!, key: Key.refreshToken) }
    }

    /// Saves both tokens atomically (best-effort — writes are independent).
    func saveTokens(accessToken: String, refreshToken: String) {
        self.accessToken  = accessToken
        self.refreshToken = refreshToken
    }

    /// Removes both tokens — call on logout.
    func clearTokens() {
        accessToken  = nil
        refreshToken = nil
    }

    var hasTokens: Bool {
        accessToken != nil && refreshToken != nil
    }

    // MARK: - Private Keychain helpers

    private func write(_ value: String, key: String) {
        guard let data = value.data(using: .utf8) else { return }

        // Delete any existing entry first so SecItemAdd doesn't fail with errSecDuplicateItem.
        delete(key: key)

        let query: [CFString: Any] = [
            kSecClass:             kSecClassGenericPassword,
            kSecAttrAccount:       key,
            kSecValueData:         data,
            kSecAttrAccessible:    kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
        ]
        SecItemAdd(query as CFDictionary, nil)
    }

    private func read(key: String) -> String? {
        let query: [CFString: Any] = [
            kSecClass:             kSecClassGenericPassword,
            kSecAttrAccount:       key,
            kSecReturnData:        true,
            kSecMatchLimit:        kSecMatchLimitOne
        ]
        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)
        guard status == errSecSuccess,
              let data = result as? Data,
              let string = String(data: data, encoding: .utf8)
        else { return nil }
        return string
    }

    private func delete(key: String) {
        let query: [CFString: Any] = [
            kSecClass:       kSecClassGenericPassword,
            kSecAttrAccount: key
        ]
        SecItemDelete(query as CFDictionary)
    }
}
