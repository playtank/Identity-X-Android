package com.identityx.android.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val userId: String,
    val email: String,
    val displayName: String,
    val plan: String
)
