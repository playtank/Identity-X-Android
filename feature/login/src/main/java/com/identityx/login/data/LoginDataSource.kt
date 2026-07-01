package com.identityx.login.data

/**
 * Contract for the login data source.
 * Swap implementations via Hilt binding — see LoginDataSourceModule.
 */
interface LoginDataSource {
    suspend fun login(email: String, password: String): Result<Unit>
}
