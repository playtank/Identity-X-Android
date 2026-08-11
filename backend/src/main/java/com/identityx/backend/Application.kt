package com.identityx.backend

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.identityx.backend.auth.JwtConfig
import com.identityx.backend.auth.TokenManager
import com.identityx.backend.auth.authRoutes
import com.identityx.backend.product.productRoutes
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
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

@Serializable
data class UserProfileResponse(
    val userId: String,
    val email: String,
    val displayName: String,
    val plan: String
)

fun main() {
    val host = backendProps.getProperty("backend.host", "0.0.0.0")
    val port = backendProps.getProperty("backend.port", "8080").toInt()
    embeddedServer(Netty, port = port, host = host, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val jwtConfig = JwtConfig(
        secret   = backendProps.getProperty("jwt.secret"),
        issuer   = backendProps.getProperty("jwt.issuer"),
        audience = backendProps.getProperty("jwt.audience")
    )

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    // Install JWT authentication
    install(Authentication) {
        jwt("jwt-access") {
            realm = "Identity X"
            verifier(
                JWT.require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .withClaim("type", "access")
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                if (userId != null) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token expired or invalid")
            }
        }
    }

    configureRouting(TokenManager(jwtConfig))
}

fun Application.configureRouting(tokenManager: TokenManager) {
    val jdbcUrl  = backendProps.getProperty("neon.url")
    val dbUser   = backendProps.getProperty("neon.user")
    val dbPass   = backendProps.getProperty("neon.password")

    routing {
        // Health check
        get("/") {
            call.respondText("Identity-X Backend is running.")
        }

        // Auth routes: login, refresh, logout
        authRoutes(tokenManager)

        // Protected routes — require valid access token
        authenticate("jwt-access") {
            // GET /api/v1/profile
            get("/api/v1/profile") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                // TODO: fetch real profile from DB using userId
                call.respond(
                    HttpStatusCode.OK,
                    UserProfileResponse(
                        userId      = userId,
                        email       = "test@identityx.com",
                        displayName = "Identity X User",
                        plan        = "Premium"
                    )
                )
            }

            // GET /api/v1/products/{upc}
            productRoutes(jdbcUrl, dbUser, dbPass)
        }

        // Database connectivity test
        get("/test-db") {
            try {
                Class.forName("org.postgresql.Driver")
                DriverManager.getConnection(jdbcUrl, dbUser, dbPass).use { connection ->
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
