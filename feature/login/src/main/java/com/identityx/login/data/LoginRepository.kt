package com.identityx.login.data

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepository @Inject constructor() {

    /**
     * Mock login — simulates a 1-second network call.
     * Succeeds only for the test credential set.
     * Replace with real IdentityXApiClient call when backend is ready.
     */
    suspend fun login(email: String, password: String): Result<Unit> {
        delay(1_000)
        return if (email == "test@identityx.com" && password == "password123") {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Invalid email or password"))
        }
    }
}
