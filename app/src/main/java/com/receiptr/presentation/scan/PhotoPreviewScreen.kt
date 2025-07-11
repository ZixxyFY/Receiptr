package com.receiptr.presentation.scan

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.receiptr.ui.theme.ReceiptrTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.receiptr.presentation.viewmodel.PhotoPreviewViewModel
import android.widget.Toast

@Composable
fun PhotoPreviewScreen(
    navController: NavController,
    photoUri: Uri,
    viewModel: PhotoPreviewViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Handle success state
    LaunchedEffect(uiState.receiptSaved) {
        if (uiState.receiptSaved) {
            navController.navigate("receipts") {
                popUpTo("scan") { inclusive = true }
            }
        }
    }
    
    // Handle error state
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // Show error toast or snackbar
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            PhotoPreviewTopAppBar(
                navController = navController,
                onRetakePhoto = {
                    navController.navigateUp()
                }
            )
            
            // Photo Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(photoUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Captured receipt photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }
            
            // Bottom Actions
            PhotoPreviewBottomActions(
                navController = navController,
                isLoading = uiState.isLoading,
                onProcessReceipt = {
                    viewModel.saveReceipt(photoUri)
                },
                onRetakePhoto = {
                    navController.navigateUp()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoPreviewTopAppBar(
    navController: NavController,
    onRetakePhoto: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Preview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = onRetakePhoto) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Retake photo"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun PhotoPreviewBottomActions(
    navController: NavController,
    isLoading: Boolean = false,
    onProcessReceipt: () -> Unit,
    onRetakePhoto: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Retake Photo Button
            OutlinedButton(
                onClick = onRetakePhoto,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "Retake",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Retake",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Process Receipt Button
            Button(
                onClick = onProcessReceipt,
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Receipt,
                        contentDescription = "Process",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isLoading) "Processing..." else "Process",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PhotoPreviewScreenPreview() {
    ReceiptrTheme {
        PhotoPreviewScreen(
            navController = rememberNavController(),
            photoUri = Uri.EMPTY
        )
    }
}
