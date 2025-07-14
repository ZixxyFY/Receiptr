package com.receiptr.presentation.test

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.receiptr.data.ml.ReceiptData
import com.receiptr.domain.model.UiState
import com.receiptr.domain.usecase.ProcessReceiptImageUseCase
import com.receiptr.ui.theme.ReceiptrTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TestMLKitActivity : ComponentActivity() {
    
    @Inject
    lateinit var processReceiptImageUseCase: ProcessReceiptImageUseCase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ReceiptrTheme {
                TestMLKitScreen()
            }
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TestMLKitScreen() {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var isProcessing by remember { mutableStateOf(false) }
        var receiptData by remember { mutableStateOf<ReceiptData?>(null) }
        var error by remember { mutableStateOf<String?>(null) }
        
        // Permission launcher
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                error = "Camera permission is required"
            }
        }
        
        // Image picker launcher
        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                    selectedBitmap = bitmap
                } catch (e: Exception) {
                    error = "Failed to load image: ${e.message}"
                }
            }
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ML Kit Test") }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Image selection button
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            imagePickerLauncher.launch("image/*")
                        } else {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select Receipt Image")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Selected image display
                selectedBitmap?.let { bitmap ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Selected Receipt",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Process button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                error = null
                                receiptData = null
                                
                                processReceiptImageUseCase.processReceiptFromBitmap(bitmap)
                                    .collect { result ->
                                        when (result) {
                                            is UiState.Loading -> {
                                                isProcessing = true
                                            }
                                            is UiState.Success -> {
                                                isProcessing = false
                                                receiptData = result.data
                                            }
                                            is UiState.Error -> {
                                                isProcessing = false
                                                error = result.message
                                            }
                                            else -> {}
                                        }
                                    }
                            }
                        },
                        enabled = !isProcessing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Process Receipt")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Error display
                error?.let { errorMessage ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                
                // Receipt data display
                receiptData?.let { data ->
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "Extracted Receipt Data",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Merchant: ${data.merchantName ?: "Not found"}")
                                    Text("Date: ${data.date ?: "Not found"}")
                                    Text("Total: $${data.total ?: "Not found"}")
                                    Text("Items count: ${data.items.size}")
                                }
                            }
                        }
                        
                        if (data.items.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Items:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            items(data.items) { item ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = item.name,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text("Price: $${item.price ?: "N/A"}")
                                    }
                                }
                            }
                        }
                        
                        item {
                            Text(
                                text = "Raw Text:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = data.rawText,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
