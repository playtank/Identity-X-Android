package com.identityx.backend

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.DriverManager
import java.util.Properties

// Load backend.properties from the classpath (generated at build time from local.properties)
private val backendProps: Properties by lazy {
    Properties().apply {
        val stream = object {}.javaClass.classLoader
            .getResourceAsStream("backend.properties")
            ?: error("backend.properties not found on classpath. Run a Gradle build first.")
        load(stream)
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
}

fun Application.configureRouting() {
    routing {
        // Health check endpoint
        get("/") {
            call.respondText("Identity-X Backend is running.")
        }

        // Database connectivity test endpoint
        get("/test-db") {
            val jdbcUrl  = backendProps.getProperty("neon.url")
            val user     = backendProps.getProperty("neon.user")
            val password = backendProps.getProperty("neon.password")

            try {
                // Explicitly load the PostgreSQL driver class
                Class.forName("org.postgresql.Driver")

                DriverManager.getConnection(jdbcUrl, user, password).use { connection ->
                    val sql = "SELECT client_name FROM oauth_clients WHERE client_id = ?"
                    connection.prepareStatement(sql).use { statement ->
                        statement.setString(1, "identity-x-android")
                        val resultSet = statement.executeQuery()

                        if (resultSet.next()) {
                            val clientName = resultSet.getString("client_name")
                            call.respondText("DB connection successful. Client name: $clientName")
                        } else {
                            call.respondText("DB connection successful, but no matching client record found.")
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
