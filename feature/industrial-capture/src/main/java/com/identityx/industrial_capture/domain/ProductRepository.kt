package com.identityx.industrial_capture.domain

/**
 * Contract for product lookup by UPC barcode.
 * Implementation calls the Ktor backend at GET /api/v1/products/{upc}.
 */
interface ProductRepository {
    suspend fun getProductByUpc(upc: String): Result<Product>
}
