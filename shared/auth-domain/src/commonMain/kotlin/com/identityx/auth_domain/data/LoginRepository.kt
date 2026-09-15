package com.identityx.auth_domain.data

/**
 * Single-responsibility thin wrapper around [LoginDataSource].
 *
 * Keeping this class in commonMain means the use-case layer never imports
 * Android or platform types directly. Hilt constructs the concrete instance
 * on Android via [com.identityx.auth_domain.di.AuthDomainModule].
 */
class LoginRepository(
    private val dataSource: LoginDataSource
) {
    suspend fun login(email: String, password: String): Result<Unit> =
        dataSource.login(email, password)
}
