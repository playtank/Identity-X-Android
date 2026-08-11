package com.identityx.camera.controller

import androidx.camera.core.Camera
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the camera zoom ratio.
 *
 * Clamps requested zoom values between the hardware min/max reported by [Camera.cameraInfo].
 * Exposes [zoomRatio] as a [StateFlow] for the UI to observe and display a zoom indicator.
 */
class ZoomControl(private val camera: Camera) {

    private val zoomState get() = camera.cameraInfo.zoomState.value

    val minZoomRatio: Float get() = zoomState?.minZoomRatio ?: 1f
    val maxZoomRatio: Float get() = zoomState?.maxZoomRatio ?: 1f

    private val _zoomRatio = MutableStateFlow(1f)
    val zoomRatio: StateFlow<Float> = _zoomRatio.asStateFlow()

    /**
     * Sets zoom to [ratio], clamped to [minZoomRatio]..[maxZoomRatio].
     */
    fun setZoom(ratio: Float) {
        val clamped = ratio.coerceIn(minZoomRatio, maxZoomRatio)
        camera.cameraControl.setZoomRatio(clamped)
        _zoomRatio.value = clamped
    }

    /** Increments zoom by [step], clamped at max. */
    fun zoomIn(step: Float = 0.5f) = setZoom(_zoomRatio.value + step)

    /** Decrements zoom by [step], clamped at min. */
    fun zoomOut(step: Float = 0.5f) = setZoom(_zoomRatio.value - step)

    /** Resets zoom to 1× (no zoom). */
    fun reset() = setZoom(1f)
}
