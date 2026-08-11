package com.identityx.android.core.network.model

import kotlinx.serialization.Serializable

/**
 * Network DTO matching the backend's ProductResponse JSON shape:
 * GET /api/v1/products/{upc}
 */
@Serializable
data class ProductDto(
    val id: String,
    val upc: String,
    val name: String,
    val price: Double,
    val imageUrl: String? = null
)
