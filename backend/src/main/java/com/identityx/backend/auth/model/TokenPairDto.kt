package com.identityx.backend.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class TokenPairDto(
    val accessToken: String,
    val refreshToken: String
)
