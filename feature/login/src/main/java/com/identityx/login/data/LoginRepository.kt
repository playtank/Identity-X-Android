package com.identityx.login.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepository @Inject constructor(
    private val dataSource: LoginDataSource
) {
    suspend fun login(email: String, password: String): Result<Unit> =
        dataSource.login(email, password)
}
