package com.identityx.backend.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.identityx.backend.auth.model.TokenPairDto
import java.util.Date

/**
 * Issues and verifies JWT access/refresh token pairs.
 *
 * Access token:  15 minutes — short-lived, sent on every API request
 * Refresh token: 7 days    — long-lived, used only to issue a new pair
 *
 * Both tokens carry a "type" claim to prevent a refresh token from being
 * used as an access token and vice versa.
 */
class TokenManager(private val config: JwtConfig) {

    private val algorithm = Algorithm.HMAC256(config.secret)

    fun generatePair(userId: String): TokenPairDto {
        val now = System.currentTimeMillis()

        val accessToken = JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withClaim("userId", userId)
            .withClaim("type", "access")
            .withExpiresAt(Date(now + ACCESS_TOKEN_TTL_MS))
            .sign(algorithm)

        val refreshToken = JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withClaim("userId", userId)
            .withClaim("type", "refresh")
            .withExpiresAt(Date(now + REFRESH_TOKEN_TTL_MS))
            .sign(algorithm)

        return TokenPairDto(accessToken, refreshToken)
    }

    /**
     * Verifies a refresh token and returns the userId it was issued for.
     * Returns null if the token is expired, invalid, or is not a refresh token.
     */
    fun verifyRefreshToken(token: String): String? {
        return try {
            val verifier = JWT.require(algorithm)
                .withAudience(config.audience)
                .withIssuer(config.issuer)
                .withClaim("type", "refresh")
                .build()
            val decoded = verifier.verify(token)
            decoded.getClaim("userId").asString()
        } catch (e: JWTVerificationException) {
            null
        }
    }

    /**
     * Verifies an access token and returns the userId.
     * Returns null if expired, invalid, or not an access token.
     */
    fun verifyAccessToken(token: String): String? {
        return try {
            val verifier = JWT.require(algorithm)
                .withAudience(config.audience)
                .withIssuer(config.issuer)
                .withClaim("type", "access")
                .build()
            val decoded = verifier.verify(token)
            decoded.getClaim("userId").asString()
        } catch (e: JWTVerificationException) {
            null
        }
    }

    companion object {
        private const val ACCESS_TOKEN_TTL_MS  = 15 * 60 * 1000L           // 15 minutes
        private const val REFRESH_TOKEN_TTL_MS = 30 * 60 * 1000L //7 * 24 * 60 * 60 * 1000L  // 7 days
    }
}
