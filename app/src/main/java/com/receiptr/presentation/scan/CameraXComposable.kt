package com.receiptr.presentation.scan

import android.net.Uri
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraXView(
    onImageCaptured: (Uri) -> Unit,
    onError: (Exception) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraManager = remember { CameraManager(context) }
    
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashEnabled by remember { mutableStateOf(false) }
    var zoomRatio by remember { mutableStateOf(1f) }
    
    val cameraPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.CAMERA
    )
    
    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    
    if (cameraPermissionState.status.isGranted) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()
                        
                        cameraManager.bindCameraUseCases(
                            cameraProvider = cameraProvider,
                            preview = preview,
                            lifecycleOwner = lifecycleOwner,
                            cameraSelector = cameraSelector
                        )
                    }, ContextCompat.getMainExecutor(ctx))
                    
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Camera controls overlay
            CameraControlsOverlay(
                onCaptureClick = {
                    cameraManager.takePhoto(
                        onImageCaptured = onImageCaptured,
                        onError = onError
                    )
                },
                onSwitchCamera = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                },
                onToggleFlash = {
                    flashEnabled = !flashEnabled
                    cameraManager.toggleFlash()
                },
                flashEnabled = flashEnabled,
                zoomRatio = zoomRatio,
                onZoomChange = { newRatio ->
                    zoomRatio = newRatio
                    cameraManager.setZoomRatio(newRatio)
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
            
            // Zoom control
            ZoomControl(
                zoomRatio = zoomRatio,
                onZoomChange = { newRatio ->
                    zoomRatio = newRatio
                    cameraManager.setZoomRatio(newRatio)
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(16.dp)
            )
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Camera,
                    contentDescription = "Camera permission required",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera permission is required to scan receipts",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() }
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

@Composable
fun CameraControlsOverlay(
    onCaptureClick: () -> Unit,
    onSwitchCamera: () -> Unit,
    onToggleFlash: () -> Unit,
    flashEnabled: Boolean,
    zoomRatio: Float,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flash toggle
        CameraControlIcon(
            icon = if (flashEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
            onClick = onToggleFlash,
            enabled = flashEnabled
        )
        
        // Capture button
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onCaptureClick() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
        }
        
        // Switch camera
        CameraControlIcon(
            icon = Icons.Filled.Cameraswitch,
            onClick = onSwitchCamera
        )
    }
}

@Composable
fun CameraControlIcon(
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (enabled) Color.Yellow.copy(alpha = 0.7f) 
                else Color.Black.copy(alpha = 0.5f)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun ZoomControl(
    zoomRatio: Float,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Zoom in button
        CameraControlIcon(
            icon = Icons.Filled.ZoomIn,
            onClick = { 
                val newRatio = (zoomRatio + 0.5f).coerceAtMost(5f)
                onZoomChange(newRatio)
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Zoom ratio display
        Box(
            modifier = Modifier
                .background(
                    Color.Black.copy(alpha = 0.5f),
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${zoomRatio.format(1)}x",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Zoom out button
        CameraControlIcon(
            icon = Icons.Filled.ZoomOut,
            onClick = { 
                val newRatio = (zoomRatio - 0.5f).coerceAtLeast(1f)
                onZoomChange(newRatio)
            }
        )
    }
}

private fun Float.format(digits: Int) = "%.${digits}f".format(this)
