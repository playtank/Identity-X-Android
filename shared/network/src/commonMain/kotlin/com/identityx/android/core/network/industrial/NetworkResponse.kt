package com.identityx.android.core.network.industrial

sealed interface NetworkResponse {

    data object Success : NetworkResponse

    data class ServerFailure(val statusCode: Int) : NetworkResponse

    data class NetworkError(val exception: Throwable) : NetworkResponse

    val isSuccessful: Boolean
        get() = this is Success
}
