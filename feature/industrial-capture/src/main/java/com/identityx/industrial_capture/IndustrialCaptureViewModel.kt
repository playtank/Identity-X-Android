package com.identityx.industrial_capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.identityx.industrial_capture.domain.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the industrial capture screen.
 *
 * Handles product lookup only — scanner engine and audio/haptic feedback
 * are owned by the Composable screen since they require View/Context
 * references that cannot be injected via Hilt.
 *
 * The screen calls [onBarcodeScanned] when CameraX detects a barcode.
 */
@HiltViewModel
class IndustrialCaptureViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<IndustrialCaptureUiState>(IndustrialCaptureUiState.Scanning())
    val uiState: StateFlow<IndustrialCaptureUiState> = _uiState.asStateFlow()

    private var lastScannedUpc: String? = null

    /** Called by the screen whenever CameraX / BarcodeAnalyzer detects a barcode. */
    fun onBarcodeScanned(upc: String) {
        if (upc == lastScannedUpc) return  // simple duplicate guard
        lastScannedUpc = upc
        lookupProduct(upc)
    }

    private fun lookupProduct(upc: String) {
        viewModelScope.launch {
            _uiState.update { IndustrialCaptureUiState.ProcessingProduct(upc) }

            productRepository.getProductByUpc(upc)
                .onSuccess { product ->
                    _uiState.update {
                        IndustrialCaptureUiState.ProductFound(product = product, cartQuantity = 1)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        IndustrialCaptureUiState.Error(
                            message = error.message ?: "Product not found in catalog",
                            upc = upc
                        )
                    }
                }
        }
    }

    fun toggleTorch(currentlyOn: Boolean, setTorch: (Boolean) -> Unit) {
        val next = !currentlyOn
        setTorch(next)
        _uiState.update { state ->
            if (state is IndustrialCaptureUiState.Scanning) state.copy(isTorchEnabled = next)
            else state
        }
    }

    fun resumeScanning() {
        _uiState.update { IndustrialCaptureUiState.Scanning(lastScannedCode = lastScannedUpc) }
    }
}
