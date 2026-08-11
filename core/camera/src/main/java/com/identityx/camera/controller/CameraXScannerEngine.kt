package com.identityx.camera.controller

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.identityx.camera.analyzer.BarcodeAnalyzer
import com.identityx.camera.contract.BarcodeScannerEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * CameraX implementation of [BarcodeScannerEngine].
 *
 * Binds [Preview] + [ImageAnalysis] to the provided [LifecycleOwner].
 * Scanned barcodes are emitted via [scannedBarcodes] as a shared flow.
 */
class CameraXScannerEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : BarcodeScannerEngine {

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val _scannedBarcodes = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val scannedBarcodes: Flow<String> = _scannedBarcodes.asSharedFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    private var torchControl: TorchControl? = null

    override var camera: Camera? = null
        private set

    override fun start(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            cameraProvider = future.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(executor, BarcodeAnalyzer { raw ->
                        _scannedBarcodes.tryEmit(raw)
                    })
                }

            cameraProvider?.unbindAll()
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
            camera?.let { torchControl = TorchControl(it) }
        }, ContextCompat.getMainExecutor(context))
    }

    override fun setTorchEnabled(enabled: Boolean) {
        torchControl?.setTorch(enabled)
    }

    override fun stop() {
        cameraProvider?.unbindAll()
        executor.shutdown()
        camera = null
        torchControl = null
    }
}
