package com.identityx.camera.ui

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.identityx.camera.controller.CameraXScannerEngine
import com.identityx.camera.controller.TorchControl
import com.identityx.camera.feedback.ScanAudioFeedback
import com.identityx.camera.feedback.ScanHapticFeedback

/**
 * Full-screen camera preview composable with built-in barcode scanning.
 *
 * Wires [CameraXScannerEngine] for camera lifecycle management,
 * [ScanAudioFeedback] + [ScanHapticFeedback] for scan confirmation,
 * and exposes a torch toggle button in the top-right corner.
 *
 * @param onBarcodeScanned Called once per unique barcode (debounced by [FrameQualityFilter]).
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val audioFeedback = remember { ScanAudioFeedback(context) }
    val hapticFeedback = remember { ScanHapticFeedback(context) }
    val engine = remember { mutableStateOf<CameraXScannerEngine?>(null) }

    var torchControl by remember { mutableStateOf<TorchControl?>(null) }
    var torchOn by remember { mutableStateOf(false) }

    // Release audio and stop camera when composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            audioFeedback.release()
            engine.value?.stop()
        }
    }

    // Collect scanned barcodes from the engine's Flow
    LaunchedEffect(engine.value) {
        engine.value?.scannedBarcodes?.collect { raw ->
            audioFeedback.playBeep()
            hapticFeedback.vibrate()
            onBarcodeScanned(raw)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            update = { previewView ->
                val scanEngine = CameraXScannerEngine(context)
                scanEngine.start(lifecycleOwner, previewView)
                engine.value = scanEngine
                scanEngine.camera?.let { cam ->
                    torchControl = TorchControl(cam)
                }
            }
        )

        // Torch toggle button — top-right corner
        torchControl?.let { torch ->
            if (torch.isTorchAvailable) {
                IconButton(
                    onClick = {
                        torch.toggle()
                        torchOn = !torchOn
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = if (torchOn) Icons.Filled.FlashOff else Icons.Filled.FlashOn,
                        contentDescription = if (torchOn) "Turn off torch" else "Turn on torch",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
