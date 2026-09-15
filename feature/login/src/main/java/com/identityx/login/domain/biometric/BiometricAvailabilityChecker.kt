package com.identityx.login.domain.biometric

/**
 * Abstracts the platform check for whether the device can perform biometric
 * authentication.
 *
 * Keeping this as an interface means:
 * - The ViewModel never calls Android framework statics directly.
 * - Unit tests can inject a fake without Robolectric.
 * - A future KMP target can provide its own actual implementation.
 */
interface BiometricAvailabilityChecker {
    /**
     * Returns true when the device has biometric hardware AND the user
     * has enrolled at least one credential (fingerprint / face).
     */
    fun isAvailable(): Boolean
}
