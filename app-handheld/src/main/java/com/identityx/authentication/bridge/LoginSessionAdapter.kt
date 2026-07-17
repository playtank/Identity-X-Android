package com.identityx.authentication.bridge

import com.identityx.android.core.network.restful.IdentityXApiClient
import com.identityx.android.core.network.session.SessionManager
import com.identityx.local.AppDatabase
import com.identityx.login.bridge.LoginSessionPort
import javax.inject.Inject

class LoginSessionAdapter @Inject constructor(
    private val database: AppDatabase,
    private val apiClient: IdentityXApiClient,
    private val sessionManager: SessionManager
) : LoginSessionPort {

    override suspend fun clearSessionOnGate() {
        try {
            apiClient.logout()
        } finally {
            // Clear local DB data, tokens, and update session state atomically
            database.clearAllData()
            sessionManager.onLogout()
        }
    }
}
