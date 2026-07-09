package com.identityx.android.core.network.industrial

/**
 * Sealed type modelling all possible outcomes of an industrial network operation.
 * Exhausted in when() expressions — no boolean flags or nullable returns needed.
 */
sealed interface NetworkResponse {

    /** Server accepted the payload — 2xx response. */
    data object Success : NetworkResponse

    /** Server returned a non-2xx status code (e.g. 503 Service Unavailable). */
    data class ServerFailure(val statusCode: Int) : NetworkResponse

    /** No connectivity, DNS failure, socket timeout, or other transport-layer error. */
    data class NetworkError(val exception: Throwable) : NetworkResponse

    val isSuccessful: Boolean
        get() = this is Success
}
