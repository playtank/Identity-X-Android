package com.identityx.backend.auth

import com.identityx.backend.auth.model.RefreshRequest
import com.identityx.backend.auth.model.TokenPairDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable

@Serializable
private data class LoginRequest(val email: String, val password: String)

@Serializable
private data class ErrorResponse(val message: String)


/**
 * Call from Application.configureRouting():
 *   routing { authRoutes(tokenManager) }
 */
fun Route.authRoutes(tokenManager: TokenManager) {

    // POST /api/v1/auth/login
    // Validates credentials and issues a fresh token pair.
    post("/api/v1/auth/login") {
        val request = call.receive<LoginRequest>()

        // TODO: replace with real DB credential lookup
        val userId = validateCredentials(request.email, request.password)
        if (userId != null) {
            call.respond(HttpStatusCode.OK, tokenManager.generatePair(userId))
        } else {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid email or password"))
        }
    }

    // POST /api/v1/auth/refresh
    // Accepts an unexpired refresh token and issues a new token pair.
    post("/api/v1/auth/refresh") {
        val request = call.receive<RefreshRequest>()
        val userId = tokenManager.verifyRefreshToken(request.refreshToken)

        if (userId != null) {
            call.respond(HttpStatusCode.OK, tokenManager.generatePair(userId))
        } else {
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse("Refresh token expired or invalid")
            )
        }
    }

    // POST /api/v1/auth/logout
    // Stateless logout — client discards tokens locally.
    // For server-side token revocation, add a token denylist in the DB.
    post("/api/v1/auth/logout") {
        call.respond(HttpStatusCode.OK, ErrorResponse("Logged out."))
    }
}

/**
 * Placeholder credential validator.
 * Returns a userId string on success, null on failure.
 * TODO: replace with a real DB query against the users table.
 */
private fun validateCredentials(email: String, password: String): String? {
    return if (email == "test@identityx.com" && password == "password123") {
        "user_001"
    } else {
        null
    }
}
