package com.identityx.android.core.network.industrial

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Provides a mock [HttpClient] that simulates industrial edge-server responses
 * without opening real network sockets.
 *
 * Switch [simulationMode] to drive different scenarios:
 * - [SimulationMode.SUCCESS]      → 200 OK (default)
 * - [SimulationMode.SERVER_ERROR] → 503 Service Unavailable (tests retry/backoff logic)
 * - [SimulationMode.TIMEOUT]      → throws IOException (tests offline/Room retention path)
 */
object MockIndustrialClientProvider {

    enum class SimulationMode { SUCCESS, SERVER_ERROR, TIMEOUT }

    var simulationMode: SimulationMode = SimulationMode.SUCCESS

    fun create(): HttpClient {
        return HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            engine {
                addHandler { request ->
                    val assetId = request.headers["X-Asset-ID"] ?: "UNKNOWN"
                    when (simulationMode) {
                        SimulationMode.SUCCESS -> respond(
                            content = """{"status":"acknowledged","assetId":"$assetId"}""",
                            status  = HttpStatusCode.OK,
                            headers = headersOf(
                                HttpHeaders.ContentType,
                                ContentType.Application.Json.toString()
                            )
                        )
                        SimulationMode.SERVER_ERROR -> respond(
                            content = """{"error":"Edge ingestion pipeline busy"}""",
                            status  = HttpStatusCode.ServiceUnavailable,
                            headers = headersOf(
                                HttpHeaders.ContentType,
                                ContentType.Application.Json.toString()
                            )
                        )
                        SimulationMode.TIMEOUT ->
                            throw IOException("Simulated network disconnection")
                    }
                }
            }
        }
    }
}
