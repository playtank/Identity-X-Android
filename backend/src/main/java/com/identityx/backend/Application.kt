package com.identityx.backend

import com.identityx.backend.auth.authRoutes
import com.identityx.backend.auth.JwtConfig
import com.identityx.backend.auth.TokenManager
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.sql.DriverManager
import java.util.Properties

// Loaded once at startup from backend.properties (generated from local.properties at build time)
private val backendProps: Properties by lazy {
    Properties().apply {
        val stream = object {}.javaClass.classLoader
            .getResourceAsStream("backend.properties")
            ?: error("backend.properties not found. Run a Gradle build first.")
        load(stream)
    }
}

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
    val jwtConfig = JwtConfig(
        secret   = backendProps.getProperty("jwt.secret"),
        issuer   = backendProps.getProperty("jwt.issuer"),
        audience = backendProps.getProperty("jwt.audience")
    )
    val tokenManager = TokenManager(jwtConfig)

    routing {
        // Health check
        get("/") {
            call.respondText("Identity-X Backend is running.")
        }

        // Auth routes: /api/v1/auth/login, /refresh, /logout
        authRoutes(tokenManager)

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
