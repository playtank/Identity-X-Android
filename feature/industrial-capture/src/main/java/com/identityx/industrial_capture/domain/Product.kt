package com.identityx.industrial_capture.domain

data class Product(
    val id: String,
    val upc: String,
    val name: String,
    val price: Double,
    val imageUrl: String?
)
