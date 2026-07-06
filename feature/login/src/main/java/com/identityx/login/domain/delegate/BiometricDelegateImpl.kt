package com.identityx.login.domain.delegate

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CoroutineScope

/**
 * Implements biometric authentication using AndroidX BiometricPrompt.
 * Requires a [FragmentActivity] context — pass from the Activity, not a Service.
 */
class BiometricDelegateImpl(
    private val activity: FragmentActivity
) : BiometricDelegate {

    private var viewModelScope: CoroutineScope? = null

    override fun registerLifecycle(viewModelScope: CoroutineScope) {
        this.viewModelScope = viewModelScope
    }

    override fun launchBiometricPrompt(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // User cancelled (ERROR_USER_CANCELED, ERROR_NEGATIVE_BUTTON) or hardware error
                // Treat all as dismissal — don't show an error message for cancel
                when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_CANCELED -> onError("dismissed")
                    else -> onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                // Finger not recognised — prompt stays open, no action needed here
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verify your identity")
            .setSubtitle("Use biometrics to sign in to Identity X")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .build()

        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }
}
