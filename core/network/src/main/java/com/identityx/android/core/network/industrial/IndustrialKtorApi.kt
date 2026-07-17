package com.identityx.android.core.network.industrial

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import javax.inject.Inject
import javax.inject.Named

/**
 * Ktor-based API client for industrial edge telemetry uploads.
 *
 * Accepts raw byte payloads (e.g. captured images, sensor data) and transmits
 * them to the edge ingestion backend.
 *
 * No real backend exists yet — inject via [MockHttpClientProvider] until
 * the production endpoint is available.
 */
class IndustrialKtorApi @Inject constructor(
    @Named("industrialHttpClient") private val httpClient: HttpClient,
    @Named("industrialBaseUrl") private val baseUrl: String
) {
    /**
     * Uploads raw asset bytes to the edge ingestion endpoint.
     *
     * @param assetId  Unique identifier for the asset being uploaded.
     * @param bytes    Raw byte payload (image, telemetry blob, etc.).
     * @return [NetworkResponse] describing the outcome.
     */
    suspend fun uploadAssetBytes(assetId: String, bytes: ByteArray): NetworkResponse {
        return try {
            val response: HttpResponse = httpClient.post("$baseUrl/api/v1/assets/upload") {
                contentType(ContentType.Application.OctetStream)
                header("X-Asset-ID", assetId)
                setBody(bytes)
            }
            if (response.status.isSuccess()) {
                NetworkResponse.Success
            } else {
                NetworkResponse.ServerFailure(statusCode = response.status.value)
            }
        } catch (e: Exception) {
            NetworkResponse.NetworkError(e)
        }
    }
}
