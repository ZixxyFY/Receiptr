package com.receiptr.presentation.scan

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
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
import androidx.compose.ui.text.input.KeyboardType
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReviewReceiptScreen(
    navController: NavController,
    photoUri: Uri,
    viewModel: PhotoPreviewViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Editable fields state
    var merchantName by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ReceiptCategory.OTHER) }
    var paymentMethod by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    // Trigger image processing when the screen loads
    LaunchedEffect(photoUri) {
        viewModel.processReceiptImageFromUri(photoUri)
    }
    
    // Initialize fields when data is loaded
    LaunchedEffect(uiState.receiptData) {
        uiState.receiptData?.let { data ->
            merchantName = data.merchantName ?: ""
            totalAmount = data.total ?: ""
            date = data.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            category = data.category
            paymentMethod = data.paymentMethod ?: ""
            notes = ""
        }
    }
    
    // Handle success navigation
    LaunchedEffect(uiState.receiptSaved) {
        if (uiState.receiptSaved) {
            navController.navigate("home") {
                popUpTo("scan") { inclusive = true }
            }
        }
    }
    
    // Handle error
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
            ReviewReceiptTopAppBar(
                navController = navController,
                onCancel = {
                    navController.navigate("scan") {
                        popUpTo("scan") { inclusive = true }
                    }
                }
            )
            
            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Receipt image preview
                ReceiptImagePreview(photoUri = photoUri)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Show processing indicator
                if (uiState.isProcessing) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Processing receipt data...",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Editable form
                ReviewReceiptForm(
                    merchantName = merchantName,
                    onMerchantNameChange = { merchantName = it },
                    totalAmount = totalAmount,
                    onTotalAmountChange = { totalAmount = it },
                    date = date,
                    onDateChange = { date = it },
                    category = category,
                    onCategoryChange = { category = it },
                    paymentMethod = paymentMethod,
                    onPaymentMethodChange = { paymentMethod = it },
                    notes = notes,
                    onNotesChange = { notes = it }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Action buttons
                ReviewReceiptActions(
                    isLoading = uiState.isLoading,
                    onSave = {
                        viewModel.saveReceipt(
                            photoUri = photoUri,
                            merchantName = merchantName,
                            totalAmount = totalAmount.toDoubleOrNull() ?: 0.0,
                            category = category.displayName,
                            notes = notes
                        )
                    },
                    onCancel = {
                        navController.navigate("scan") {
                            popUpTo("scan") { inclusive = true }
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewReceiptTopAppBar(
    navController: NavController,
    onCancel: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Review Receipt",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        actions = {
            TextButton(onClick = onCancel) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun ReceiptImagePreview(photoUri: Uri) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
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
    }
}

@Composable
fun ReviewReceiptForm(
    merchantName: String,
    onMerchantNameChange: (String) -> Unit,
    totalAmount: String,
    onTotalAmountChange: (String) -> Unit,
    date: String,
    onDateChange: (String) -> Unit,
    category: ReceiptCategory,
    onCategoryChange: (ReceiptCategory) -> Unit,
    paymentMethod: String,
    onPaymentMethodChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Receipt Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Merchant Name
            OutlinedTextField(
                value = merchantName,
                onValueChange = onMerchantNameChange,
                label = { Text("Merchant Name") },
                placeholder = { Text("Enter merchant name") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Store,
                        contentDescription = "Merchant",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Total Amount
            OutlinedTextField(
                value = totalAmount,
                onValueChange = onTotalAmountChange,
                label = { Text("Total Amount") },
                placeholder = { Text("0.00") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.AttachMoney,
                        contentDescription = "Amount",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Date
            OutlinedTextField(
                value = date,
                onValueChange = onDateChange,
                label = { Text("Date") },
                placeholder = { Text("YYYY-MM-DD") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = "Date",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Category Selector
            CategorySelector(
                selectedCategory = category,
                onCategorySelected = onCategoryChange
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Payment Method
            OutlinedTextField(
                value = paymentMethod,
                onValueChange = onPaymentMethodChange,
                label = { Text("Payment Method") },
                placeholder = { Text("Cash, Card, etc.") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Payment,
                        contentDescription = "Payment",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Notes") },
                placeholder = { Text("Add any notes...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Notes,
                        contentDescription = "Notes",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    selectedCategory: ReceiptCategory,
    onCategorySelected: (ReceiptCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = "Category",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCategory.displayName,
                onValueChange = {},
                readOnly = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Category,
                        contentDescription = "Category",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                ReceiptCategory.values().forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.displayName) },
                        onClick = {
                            onCategorySelected(category)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewReceiptActions(
    isLoading: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cancel Button
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Cancel",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Save Button
        Button(
            onClick = onSave,
            enabled = !isLoading,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
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
                text = if (isLoading) "Saving..." else "Save Receipt",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewReceiptScreenPreview() {
    ReceiptrTheme {
        ReviewReceiptScreen(
            navController = rememberNavController(),
            photoUri = Uri.EMPTY
        )
    }
}
