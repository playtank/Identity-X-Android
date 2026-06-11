package com.identityx.authentication.bridge

import com.identityx.android.core.network.restful.IdentityXApiService
import com.identityx.database.AppDatabase
import com.identityx.login.bridge.LoginSessionPort
import javax.inject.Inject

class LoginSessionAdapter @Inject constructor(
    private val database: AppDatabase,
    private val apiService: IdentityXApiService
) : LoginSessionPort {

    override suspend fun clearSessionOnGate() {
        try {
            apiService.logout()
        } finally {
            database.clearAllData()
        }
    }
}
