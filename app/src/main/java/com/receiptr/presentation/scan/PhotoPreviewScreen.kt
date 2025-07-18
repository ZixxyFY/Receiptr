package com.receiptr.presentation.scan

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
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
import com.receiptr.data.ml.ReceiptData
import com.receiptr.data.ml.models.ReceiptCategory
import android.widget.Toast

@Composable
fun PhotoPreviewScreen(
    navController: NavController,
    photoUri: Uri,
    viewModel: PhotoPreviewViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Start processing automatically when screen loads
    LaunchedEffect(photoUri) {
        viewModel.processReceiptImageFromUri(photoUri)
    }
    
    // Handle success state
    LaunchedEffect(uiState.receiptSaved) {
        if (uiState.receiptSaved) {
            navController.navigate("home") {
                popUpTo("scan") { inclusive = true }
            }
        }
    }
    
    // Handle error state
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
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
            
            // Content based on processing state
            when {
                uiState.isProcessing -> {
                    ProcessingView(photoUri = photoUri)
                }
                uiState.receiptData != null -> {
                    ExtractedDataView(
                        photoUri = photoUri,
                        receiptData = uiState.receiptData!!,
                        isLoading = uiState.isLoading,
                        onSave = {
                            viewModel.saveReceipt(
                                photoUri = photoUri,
                                merchantName = uiState.receiptData!!.merchantName ?: "",
                                totalAmount = uiState.receiptData!!.total?.toDoubleOrNull() ?: 0.0,
                                category = uiState.receiptData!!.category.displayName,
                                notes = "Auto-processed receipt"
                            )
                        },
                        onRetry = {
                            viewModel.processReceiptImageFromUri(photoUri)
                        },
                        onEdit = {
                            navController.navigate("review_receipt/${java.net.URLEncoder.encode(photoUri.toString(), "UTF-8")}")
                        }
                    )
            }
            uiState.error != null -> {
                ErrorView(
                    error = uiState.error!!,
                    onRetry = {
                        viewModel.processReceiptImageFromUri(photoUri)
                    }
                )
            }
            else -> {
                // Show basic photo preview while waiting for processing to start
                ProcessingView(photoUri = photoUri)
            }
            }
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

// Helper functions from ReceiptPreviewScreen
@Composable
fun ProcessingView(photoUri: Uri) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Photo Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Receipt photo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Processing indicator
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Processing Receipt...",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Extracting text and categorizing expenses",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ExtractedDataView(
    photoUri: Uri,
    receiptData: ReceiptData,
    isLoading: Boolean,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Small photo preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Receipt photo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
        }
        
        // Extracted data cards
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Extracted Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Category
                DataRow(
                    label = "Category",
                    value = receiptData.category.displayName,
                    icon = Icons.Filled.Category
                )
                
                // Merchant
                receiptData.merchantName?.let { merchantName ->
                    DataRow(
                        label = "Merchant",
                        value = merchantName,
                        icon = Icons.Filled.Store
                    )
                }
                
                // Total
                receiptData.total?.let { total ->
                    DataRow(
                        label = "Total",
                        value = "$$total",
                        icon = Icons.Filled.AttachMoney
                    )
                }
                
                // Date
                receiptData.date?.let { date ->
                    DataRow(
                        label = "Date",
                        value = date,
                        icon = Icons.Filled.CalendarToday
                    )
                }
                
                // Payment Method
                receiptData.paymentMethod?.let { paymentMethod ->
                    DataRow(
                        label = "Payment",
                        value = paymentMethod,
                        icon = Icons.Filled.Payment
                    )
                }
            }
        }
        
        // Action buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Edit button - primary action
            Button(
                onClick = onEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Review & Edit",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Secondary actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Retry",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Retry",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Button(
                    onClick = onSave,
                    enabled = !isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onSecondary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Save",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLoading) "Saving..." else "Save As-Is",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DataRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ErrorView(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Processing Failed",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = error,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Retry",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Try Again",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
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
