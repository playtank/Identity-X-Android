package com.identityx.backend

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.sql.DriverManager
import java.util.Properties

private val backendProps: Properties by lazy {
    Properties().apply {
        val stream = object {}.javaClass.classLoader
            .getResourceAsStream("backend.properties")
            ?: error("backend.properties not found. Run a Gradle build first.")
        load(stream)
    }
}

@Serializable data class LoginRequest(val email: String, val password: String)
@Serializable data class AuthResponse(val accessToken: String, val refreshToken: String)
@Serializable data class ErrorResponse(val message: String)

fun main() {
    val host = backendProps.getProperty("backend.host", "0.0.0.0")
    val port = backendProps.getProperty("backend.port", "8080").toInt()

    embeddedServer(Netty, port = port, host = host, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    configureRouting()
}

fun Application.configureRouting() {
    routing {
        // Health check
        get("/") {
            call.respondText("Identity-X Backend is running.")
        }

        // Auth: login
        // POST /api/v1/auth/login
        // Body: { "email": "...", "password": "..." }
        // Returns: { "accessToken": "...", "refreshToken": "..." }
        post("/api/v1/auth/login") {
            val request = call.receive<LoginRequest>()

            // TODO: replace with real DB credential validation
            if (request.email == "test@identityx.com" && request.password == "password123") {
                call.respond(
                    AuthResponse(
                        accessToken  = "mock_access_token_${System.currentTimeMillis()}",
                        refreshToken = "mock_refresh_token_${System.currentTimeMillis()}"
                    )
                )
            } else {
                call.respond(
                    io.ktor.http.HttpStatusCode.Unauthorized,
                    ErrorResponse("Invalid email or password")
                )
            }
        }

        // Auth: refresh token
        post("/api/v1/auth/refresh") {
            // TODO: validate refresh token and issue new tokens
            call.respond(
                AuthResponse(
                    accessToken  = "refreshed_access_token_${System.currentTimeMillis()}",
                    refreshToken = "refreshed_refresh_token_${System.currentTimeMillis()}"
                )
            )
        }

        // Auth: logout (stateless — client discards tokens)
        post("/api/v1/auth/logout") {
            call.respondText("Logged out.")
        }

        // Database connectivity test
        get("/test-db") {
            val jdbcUrl  = backendProps.getProperty("neon.url")
            val user     = backendProps.getProperty("neon.user")
            val password = backendProps.getProperty("neon.password")

            try {
                Class.forName("org.postgresql.Driver")
                DriverManager.getConnection(jdbcUrl, user, password).use { connection ->
                    val sql = "SELECT client_name FROM oauth_clients WHERE client_id = ?"
                    connection.prepareStatement(sql).use { statement ->
                        statement.setString(1, "identity-x-android")
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            call.respondText("DB OK. Client: ${resultSet.getString("client_name")}")
                        } else {
                            call.respondText("DB connected, no matching client record found.")
                        }
                    }
                }
            } catch (e: Exception) {
                call.application.log.error("DB connection failed", e)
                call.respondText("DB connection failed: ${e.localizedMessage}")
            }
        }
    }
}
