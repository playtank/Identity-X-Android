package com.identityx.android.core.network.restful

import com.identityx.android.core.network.model.AuthRequest
import com.identityx.android.core.network.model.AuthResponse
import com.identityx.android.core.network.model.ProductDto
import com.identityx.android.core.network.model.UserProfile
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
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

    /**
     * Fetches the authenticated user's profile.
     * The Authorization header is attached automatically by [AuthInterceptor].
     * On 401, [OkHttpTokenAuthenticator] will attempt a token refresh transparently.
     */
    suspend fun getProfile(): UserProfile =
        client.get("$baseUrl/api/v1/profile").body()

    /**
     * Looks up a product by its UPC barcode.
     * Calls GET /api/v1/products/{upc} — requires a valid Bearer token.
     * Returns null body on 404 which is surfaced as a failure Result in [ProductRepositoryImpl].
     */
    suspend fun getProductByUpc(upc: String): ProductDto =
        client.get("$baseUrl/api/v1/products/$upc").body()
}
