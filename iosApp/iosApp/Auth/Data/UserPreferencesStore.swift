//
//  UserPreferencesStore.swift
//  iosApp
//
//  Lightweight UserDefaults wrapper for non-sensitive login preferences:
//  remembered email and biometric-enabled flag.
//
//  Equivalent of Android's UserPreferencesProvider / EncryptedTokenProvider
//  (the email/biometric portion — tokens go to KeychainTokenStore instead).
//

import Foundation

final class UserPreferencesStore {

    // MARK: - Keys

    private enum Key {
        static let rememberedEmail   = "com.identityx.prefs.rememberedEmail"
        static let biometricEnabled  = "com.identityx.prefs.biometricEnabled"
    }

    // MARK: - Singleton

    static let shared = UserPreferencesStore()
    private init() {}

    private let defaults = UserDefaults.standard

    // MARK: - Public API

    var rememberedEmail: String? {
        get { defaults.string(forKey: Key.rememberedEmail) }
        set { defaults.set(newValue, forKey: Key.rememberedEmail) }
    }

    var isBiometricEnabled: Bool {
        get { defaults.bool(forKey: Key.biometricEnabled) }
        set { defaults.set(newValue, forKey: Key.biometricEnabled) }
    }

    /// Persists email and biometric preference together — matches
    /// Android's `UserPreferencesProvider.saveUserPreferences(email, biometricEnabled)`.
    func save(email: String, biometricEnabled: Bool) {
        rememberedEmail       = email
        self.isBiometricEnabled = biometricEnabled
    }

    /// Clears both preferences — matches Android's `clearUserPreferences()`.
    func clear() {
        defaults.removeObject(forKey: Key.rememberedEmail)
        defaults.removeObject(forKey: Key.biometricEnabled)
    }
}
