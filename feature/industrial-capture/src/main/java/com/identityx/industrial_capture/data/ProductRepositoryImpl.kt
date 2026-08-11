package com.identityx.industrial_capture.data

import com.identityx.android.core.network.restful.IdentityXApiClient
import com.identityx.industrial_capture.domain.Product
import com.identityx.industrial_capture.domain.ProductRepository
import javax.inject.Inject

/**
 * Real implementation of [ProductRepository].
 * Calls GET /api/v1/products/{upc} via [IdentityXApiClient].
 * The Bearer token is attached automatically by the Ktor Auth plugin.
 */
class ProductRepositoryImpl @Inject constructor(
    private val apiClient: IdentityXApiClient
) : ProductRepository {

    override suspend fun getProductByUpc(upc: String): Result<Product> {
        return try {
            val dto = apiClient.getProductByUpc(upc)
            Result.success(
                Product(
                    id       = dto.id,
                    upc      = dto.upc,
                    name     = dto.name,
                    price    = dto.price,
                    imageUrl = dto.imageUrl
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
