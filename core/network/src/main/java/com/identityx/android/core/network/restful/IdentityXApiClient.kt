package com.identityx.android.core.network.restful

import com.identityx.android.core.network.model.AuthRequest
import com.identityx.android.core.network.model.AuthResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class IdentityXApiClient @Inject constructor(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun login(request: AuthRequest): AuthResponse =
        client.post("$baseUrl/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun refreshToken(request: AuthRequest): AuthResponse =
        client.post("$baseUrl/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun logout() {
        client.post("$baseUrl/api/v1/auth/logout")
    }
}
