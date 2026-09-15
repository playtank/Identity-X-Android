package com.identityx.auth_domain.domain.usecase

import com.identityx.auth_domain.data.LoginRepository

/**
 * Executes the login flow and returns a [Result] indicating success or failure.
 *
 * This use case is intentionally thin — its value is as a stable API boundary
 * between the presentation layer and the data layer, not as a place to hold
 * orchestration logic. Richer flows (e.g. post-login profile fetch) are added
 * here as the domain grows.
 *
 * Constructed by Hilt on Android (via [com.identityx.auth_domain.di.AuthDomainModule]);
 * constructed directly in JVM unit tests or server-side consumers.
 */
class LoginUseCase(
    private val repository: LoginRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> =
        repository.login(email, password)
}
