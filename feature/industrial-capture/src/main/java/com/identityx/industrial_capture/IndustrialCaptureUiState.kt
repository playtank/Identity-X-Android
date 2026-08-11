package com.identityx.industrial_capture

import com.identityx.industrial_capture.domain.Product

sealed interface IndustrialCaptureUiState {
    data object Initializing : IndustrialCaptureUiState

    data class Scanning(
        val lastScannedCode: String? = null,
        val isTorchEnabled: Boolean = false,
        val isBatchMode: Boolean = true
    ) : IndustrialCaptureUiState

    data class ProcessingProduct(
        val upc: String
    ) : IndustrialCaptureUiState

    data class ProductFound(
        val product: Product,
        val cartQuantity: Int
    ) : IndustrialCaptureUiState

    data class Error(
        val message: String,
        val upc: String? = null
    ) : IndustrialCaptureUiState
}
