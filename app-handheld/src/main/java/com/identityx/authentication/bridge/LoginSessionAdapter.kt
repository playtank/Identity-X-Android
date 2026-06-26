package com.identityx.authentication.bridge

import com.identityx.android.core.network.restful.IdentityXApiClient
import com.identityx.database.AppDatabase
import com.identityx.login.bridge.LoginSessionPort
import javax.inject.Inject

class LoginSessionAdapter @Inject constructor(
    private val database: AppDatabase,
    private val apiClient: IdentityXApiClient
) : LoginSessionPort {

    override suspend fun clearSessionOnGate() {
        try {
            apiClient.logout()
        } finally {
            database.clearAllData()
        }
    }
}
