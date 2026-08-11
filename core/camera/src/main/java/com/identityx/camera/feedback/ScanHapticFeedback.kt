package com.identityx.camera.feedback

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService

/**
 * Triggers a short haptic pulse when a barcode is successfully scanned.
 *
 * Uses the modern [VibrationEffect] API on API 26+ and falls back to the
 * legacy [Vibrator.vibrate] on older devices.
 */
class ScanHapticFeedback(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService<VibratorManager>()?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService<Vibrator>()
    }

    /**
     * Plays a short confirmation vibration (~80ms).
     * Requires `android.permission.VIBRATE` in the manifest.
     */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrate() {
        vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    VIBRATION_DURATION_MS,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(VIBRATION_DURATION_MS)
        }
    }

    companion object {
        private const val VIBRATION_DURATION_MS = 80L
    }
}
