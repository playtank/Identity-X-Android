package com.identityx.backend.auth

/**
 * Holds JWT signing configuration loaded from backend.properties.
 * Never hardcode these values — they come from local.properties at build time.
 */
data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String
)
