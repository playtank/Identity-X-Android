package com.identityx.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.identityx.login.bridge.LoginSessionPort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sessionBridge: LoginSessionPort,
): ViewModel() {

    fun onLogoutClicked() {
        viewModelScope.launch {
            // 直接调用，不管谁实现的
            sessionBridge.clearSessionOnGate()
        }
    }
}