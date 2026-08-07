package com.identityx.backend.product

import com.identityx.backend.product.model.ProductResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.log
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.DriverManager

@Serializable
private data class ErrorResponse(val message: String)

/**
 * Registers product endpoints.
 * Must be called inside an authenticate("jwt-access") block.
 *
 * SQL setup (run once on your Neon DB):
 *
 *   CREATE TABLE products (
 *     id         VARCHAR(36)     PRIMARY KEY,
 *     upc        VARCHAR(32)     NOT NULL UNIQUE,
 *     name       VARCHAR(255)    NOT NULL,
 *     price      DECIMAL(10, 2)  NOT NULL,
 *     image_url  TEXT,
 *     created_at TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
 *   );
 *
 *   CREATE UNIQUE INDEX idx_products_upc ON products(upc);
 */
fun Route.productRoutes(
    jdbcUrl: String,
    dbUser: String,
    dbPassword: String
) {
    // GET /api/v1/products/{upc}
    // Returns the product matching the given UPC barcode.
    // Requires: Authorization: Bearer <access_token>
    get("/api/v1/products/{upc}") {
        val upc = call.parameters["upc"]
        if (upc.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("UPC parameter is required"))
            return@get
        }

        try {
            Class.forName("org.postgresql.Driver")
            DriverManager.getConnection(jdbcUrl, dbUser, dbPassword).use { connection ->
                val product = queryProductByUpc(connection, upc)
                if (product != null) {
                    call.respond(HttpStatusCode.OK, product)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Product not found for UPC: $upc"))
                }
            }
        } catch (e: Exception) {
            call.application.log.error("Product lookup failed for UPC=$upc", e)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Product lookup failed"))
        }
    }
}

private fun queryProductByUpc(connection: Connection, upc: String): ProductResponse? {
    val sql = "SELECT id, upc, name, price, image_url FROM products WHERE upc = ?"
    return connection.prepareStatement(sql).use { statement ->
        statement.setString(1, upc)
        val rs = statement.executeQuery()
        if (rs.next()) {
            ProductResponse(
                id       = rs.getString("id"),
                upc      = rs.getString("upc"),
                name     = rs.getString("name"),
                price    = rs.getDouble("price"),
                imageUrl = rs.getString("image_url")
            )
        } else null
    }
}
