package com.identityx.login.domain.usecase

import com.identityx.login.data.LoginRepository
import javax.inject.Inject

class LoginAndFetchProfileUseCase @Inject constructor(
    private val repository: LoginRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> =
        repository.login(email, password)
}
