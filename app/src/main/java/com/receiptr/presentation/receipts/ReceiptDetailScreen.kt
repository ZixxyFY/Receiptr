package com.receiptr.presentation.receipts

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.receiptr.domain.model.Receipt
import com.receiptr.domain.model.ReceiptItem
import com.receiptr.domain.model.UiState
import com.receiptr.ui.theme.ReceiptrTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDetailScreen(
    receiptId: String,
    navController: NavController,
    viewModel: ReceiptDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val receiptState by viewModel.receiptState.collectAsState()
    val isGeneratingPdf by viewModel.isGeneratingPdf.collectAsState()

    // Handle PDF share event
    LaunchedEffect(Unit) {
        viewModel.pdfShareEvent.collect { pdfUri ->
            shareReceiptPdf(context, pdfUri)
        }
    }

    // Load receipt when screen is created
    LaunchedEffect(receiptId) {
        viewModel.loadReceipt(receiptId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipt Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onSharePdfClicked() },
                        enabled = receiptState is UiState.Success && !isGeneratingPdf
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share as PDF"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val currentState = receiptState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is UiState.Success -> {
                ReceiptDetailContent(
                    receipt = currentState.data,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Error loading receipt",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = currentState.message,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.loadReceipt(receiptId) }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            is UiState.Idle -> {
                // Initial state, loading should start automatically
            }

            is UiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Receipt not found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ReceiptDetailContent(
    receipt: Receipt,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Receipt Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Merchant
                    DetailRow(
                        label = "Merchant:",
                        value = receipt.merchantName.ifEmpty { "Unknown" }
                    )
                    
                    // Date
                    DetailRow(
                        label = "Date:",
                        value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            .format(Date(receipt.date))
                    )
                    
                    // Total Amount
                    DetailRow(
                        label = "Total Amount:",
                        value = "${receipt.currency} ${String.format("%.2f", receipt.totalAmount)}"
                    )
                    
                    // Category
                    if (receipt.category.isNotEmpty()) {
                        DetailRow(
                            label = "Category:",
                            value = receipt.category
                        )
                    }
                    
                    // Notes
                    if (receipt.notes.isNotEmpty()) {
                        DetailRow(
                            label = "Notes:",
                            value = receipt.notes
                        )
                    }
                }
            }
        }

        // Line Items
        if (receipt.items.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Items",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Table Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Item",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(2f)
                            )
                            Text(
                                text = "Qty",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Price",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Items List
            items(receipt.items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.name,
                            modifier = Modifier.weight(2f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${item.quantity}",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${receipt.currency} ${String.format("%.2f", item.totalPrice)}",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun shareReceiptPdf(context: Context, pdfUri: android.net.Uri) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, pdfUri)
        putExtra(Intent.EXTRA_SUBJECT, "Receipt Details")
        putExtra(Intent.EXTRA_TEXT, "Please find the receipt details attached.")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    
    context.startActivity(Intent.createChooser(shareIntent, "Share Receipt"))
}
