package com.identityx.industrial_capture

import android.Manifest
import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.identityx.camera.ui.CameraPreview

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun IndustrialCaptureScreen(
    viewModel: IndustrialCaptureViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    when {
        cameraPermission.status.isGranted -> {
            // Permission granted — show the scanner
            CaptureContent(
                viewModel = viewModel,
                modifier = modifier
            )
        }
        cameraPermission.status.shouldShowRationale -> {
            // User previously denied — explain why we need it
            CameraPermissionRationale(
                onGrantPermission = { cameraPermission.launchPermissionRequest() },
                onDismiss = onNavigateBack
            )
        }
        else -> {
            // First time — request immediately
            androidx.compose.runtime.LaunchedEffect(Unit) {
                cameraPermission.launchPermissionRequest()
            }
            // Show rationale UI while waiting
            CameraPermissionRationale(
                onGrantPermission = { cameraPermission.launchPermissionRequest() },
                onDismiss = onNavigateBack
            )
        }
    }
}

@Composable
private fun CameraPermissionRationale(
    onGrantPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Camera Permission Required",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "This feature uses your camera to scan product barcodes. Please grant camera access to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(onClick = onGrantPermission, modifier = Modifier.fillMaxWidth()) {
                Text("Grant Camera Access")
            }
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Go Back")
            }
        }
    }
}

@Composable
private fun CaptureContent(
    viewModel: IndustrialCaptureViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier
        .fillMaxSize()
        .background(Color.Black)) {

        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onBarcodeScanned = { upc -> viewModel.onBarcodeScanned(upc) }
        )

        IndustrialReticleOverlay(modifier = Modifier.fillMaxSize())

        // Top controls — torch toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val isTorchOn = (uiState as? IndustrialCaptureUiState.Scanning)?.isTorchEnabled == true
                    viewModel.toggleTorch(
                        currentlyOn = isTorchOn,
                        setTorch = { /* torch control is owned by CameraPreview — no-op here */ }
                    )
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.5f)
                )
            ) {
                val isTorchOn = (uiState as? IndustrialCaptureUiState.Scanning)?.isTorchEnabled == true
                Icon(
                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = if (isTorchOn) "Torch on" else "Torch off",
                    tint = if (isTorchOn) Color.Yellow else Color.White
                )
            }
        }

        // Bottom sheet result
        when (val state = uiState) {
            is IndustrialCaptureUiState.ProcessingProduct -> {
                // Overlay shown while fetching product from backend
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                        Text(
                            text = "Looking up product…",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = state.upc,
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            is IndustrialCaptureUiState.ProductFound -> {
                ProductScanBottomSheet(
                    productName = state.product.name,
                    price = "$${state.product.price}",
                    upc = state.product.upc,
                    onDismiss = { viewModel.resumeScanning() }
                )
            }
            is IndustrialCaptureUiState.Error -> {
                ErrorScanBottomSheet(
                    message = state.message,
                    onRetry = { viewModel.resumeScanning() }
                )
            }
            else -> {}
        }
    }
}

@Composable
fun IndustrialReticleOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "Laser")
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue  = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserPosition"
    )

    Canvas(modifier = modifier) {
        val width    = size.width
        val height   = size.height
        val boxWidth = width * 0.8f
        val boxHeight = height * 0.3f
        val left = (width - boxWidth) / 2
        val top  = (height - boxHeight) / 2

        drawRoundRect(
            color = Color.Green,
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(12.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )

        val laserY = height * laserYRatio
        drawLine(
            color = Color.Red,
            start = Offset(left + 10, laserY),
            end   = Offset(left + boxWidth - 10, laserY),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScanBottomSheet(
    productName: String,
    price: String,
    upc: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Product Found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            SheetRow(label = "Name",  value = productName)
            SheetRow(label = "Price", value = price)
            SheetRow(label = "UPC",   value = upc)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Scan Next")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorScanBottomSheet(
    message: String,
    onRetry: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onRetry,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Scan Failed",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun SheetRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}
