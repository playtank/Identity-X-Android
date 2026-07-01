package com.identityx.login.data

import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Mock login data source — simulates a 1-second network call.
 * Succeeds only for the test credential set.
 *
 * Re-enable via LoginDataSourceModule by rebinding this as the active implementation.
 * Never delete this class — it is the fallback for offline development and UI testing.
 *
 * Test credentials: test@identityx.com / password123
 */
class MockLoginDataSource @Inject constructor() : LoginDataSource {

    override suspend fun login(email: String, password: String): Result<Unit> {
        delay(1_000)
        return if (email == MOCK_EMAIL && password == MOCK_PASSWORD) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Invalid email or password"))
        }
    }

    companion object {
        const val MOCK_EMAIL    = "test@identityx.com"
        const val MOCK_PASSWORD = "password123"
    }
}
