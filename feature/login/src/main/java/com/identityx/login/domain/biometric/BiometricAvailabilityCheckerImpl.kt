package com.identityx.login.domain.biometric

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Android implementation of [BiometricAvailabilityChecker].
 *
 * Calls [BiometricManager.from] which requires a real Android context.
 * By isolating this call here, the ViewModel and its unit tests are
 * fully decoupled from Android framework statics.
 */
class BiometricAvailabilityCheckerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BiometricAvailabilityChecker {

    override fun isAvailable(): Boolean =
        BiometricManager.from(context)
            .canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
}
