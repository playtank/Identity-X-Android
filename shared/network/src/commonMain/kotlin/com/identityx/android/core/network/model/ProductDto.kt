package com.identityx.android.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: String,
    val upc: String,
    val name: String,
    val price: Double,
    val imageUrl: String? = null
)
