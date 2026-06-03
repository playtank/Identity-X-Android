package com.identityx.login.domain.delegate

import kotlinx.coroutines.CoroutineScope

interface BiometricDelegate {
    fun registerLifecycle(viewModelScope: CoroutineScope)
    fun launchBiometricPrompt(onSuccess: () -> Unit, onError: (String) -> Unit)
}
