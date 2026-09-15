package com.identityx.auth_domain.data

/**
 * Platform-agnostic contract for the login data source.
 *
 * Lives in commonMain so any target (Android, JVM server, future iOS) can
 * provide its own implementation while the domain layer stays portable.
 *
 * Swap implementations via Hilt binding in androidMain — see AuthDomainModule.
 */
interface LoginDataSource {
    suspend fun login(email: String, password: String): Result<Unit>
}
