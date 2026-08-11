package com.identityx.camera.controller

import androidx.camera.core.Camera
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the camera flashlight (torch) toggle.
 *
 * Requires a [Camera] instance from [CameraXScannerEngine.camera] after scanning starts.
 * Exposes [torchEnabled] as a [StateFlow] for the UI to observe.
 */
class TorchControl(private val camera: Camera) {

    private val _torchEnabled = MutableStateFlow(false)
    val torchEnabled: StateFlow<Boolean> = _torchEnabled.asStateFlow()

    /** True if the device hardware supports a torch. */
    val isTorchAvailable: Boolean
        get() = camera.cameraInfo.hasFlashUnit()

    /** Turns the torch on or off. No-op if hardware unavailable. */
    fun setTorch(enabled: Boolean) {
        if (!isTorchAvailable) return
        camera.cameraControl.enableTorch(enabled)
        _torchEnabled.value = enabled
    }

    /** Toggles the current torch state. */
    fun toggle() = setTorch(!_torchEnabled.value)
}
