package com.receiptr.presentation.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.receiptr.ui.theme.*
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@Composable
fun ScanScreen(
    navController: NavController
) {
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val cameraManager = remember { CameraManager(context) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Header with Close Button
            ScanTopAppBar(navController)
            
            // Camera Viewfinder Area (placeholder)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                CameraXView(
                    onImageCaptured = { uri ->
                        capturedImageUri = uri
                        Toast.makeText(context, "Photo captured successfully!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { exception ->
                        Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            
            // Bottom Section
            ScanBottomSection(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanTopAppBar(navController: NavController) {
    TopAppBar(
        title = { }, // Empty title
        actions = {
            IconButton(
                onClick = { navController.navigateUp() }
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun CameraControls(
    onTakePhoto: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gallery Button
        CameraControlButton(
            icon = Icons.Outlined.Image,
            size = 40.dp,
            onClick = { /* TODO: Open gallery */ }
        )
        
        // Main Camera Button (larger)
        CameraControlButton(
            icon = Icons.Filled.CameraAlt,
            size = 64.dp,
            onClick = onTakePhoto
        )
        
        // Effects/Filters Button (spiral icon placeholder)
        CameraControlButton(
            icon = Icons.Outlined.Tune, // Using tune icon as spiral placeholder
            size = 40.dp,
            onClick = { /* TODO: Open effects */ }
        )
    }
}

@Composable
fun CameraControlButton(
    icon: ImageVector,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(size),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black.copy(alpha = 0.4f),
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(if (size > 50.dp) 32.dp else 20.dp)
        )
    }
}

@Composable
fun ScanBottomSection(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Scan Receipt Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { 
                    // TODO: Process scanned receipt
                    // For now, navigate back to receipts
                    navController.navigate("receipts") {
                        popUpTo("scan") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .height(56.dp)
                    .widthIn(min = 160.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Receipt,
                    contentDescription = "Receipt",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Scan Receipt",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Bottom Sheet Handle (as in Figma)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            // Handle bar
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outline)
            )
        }
        
        // Bottom spacing
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

// Camera Permission and functionality would be implemented here
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewView() {
    val context = LocalContext.current
    val cameraManager = remember { CameraManager(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.CAMERA
    )

    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (cameraPermissionState.status.isGranted) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                previewView.scaleType = PreviewView.ScaleType.FILL_CENTER

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = CameraPreview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    cameraManager.bindCameraUseCases(
                        cameraProvider = cameraProvider,
                        preview = preview,
                        lifecycleOwner = lifecycleOwner
                    )
                }, ContextCompat.getMainExecutor(ctx))

                return@AndroidView previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Camera permission not granted.", color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScanScreenPreview() {
    ReceiptrTheme {
        ScanScreen(navController = rememberNavController())
    }
}
