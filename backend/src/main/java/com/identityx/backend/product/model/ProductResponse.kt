package com.identityx.backend.product.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductResponse(
    val id: String,
    val upc: String,
    val name: String,
    val price: Double,
    val imageUrl: String?
)
