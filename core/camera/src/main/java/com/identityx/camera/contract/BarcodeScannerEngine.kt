package com.identityx.camera.contract

import androidx.camera.core.Camera
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.Flow

/**
 * Public contract for the barcode scanner engine.
 * Consumers depend on this interface — not the CameraX implementation.
 */
interface BarcodeScannerEngine {

    /** Cold flow that emits each unique scanned barcode value. */
    val scannedBarcodes: Flow<String>

    /** Bind camera to the given [lifecycleOwner] and [previewView] and start scanning. */
    fun start(lifecycleOwner: LifecycleOwner, previewView: PreviewView)

    /** Unbind all camera use cases and release resources. */
    fun stop()

    /** Enable or disable the camera torch. */
    fun setTorchEnabled(enabled: Boolean)

    /** Expose the underlying [Camera] for additional control after [start]. */
    val camera: Camera?
}
