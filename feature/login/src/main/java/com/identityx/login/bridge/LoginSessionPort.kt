package com.identityx.login.bridge

interface LoginSessionPort {
    suspend fun clearSessionOnGate()
}