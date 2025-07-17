package com.receiptr.presentation.annotation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.receiptr.data.ml.annotation.AnnotationTask
import com.receiptr.data.ml.schema.ReceiptSchema
// import com.receiptr.ui.components.ReceiptrTextField // Commented out - using OutlinedTextField instead
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnotationScreen(
    taskId: String,
    viewModel: AnnotationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(taskId) {
        viewModel.loadAnnotationTask(taskId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Correct Receipt Data") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveCorrections() },
                        enabled = uiState.hasChanges && !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    ErrorMessage(
                        message = uiState.error!!,
                        onRetry = { viewModel.loadAnnotationTask(taskId) }
                    )
                }
                uiState.annotationTask != null -> {
                    AnnotationForm(
                        task = uiState.annotationTask!!,
                        correctedData = uiState.correctedData,
                        onFieldChange = viewModel::updateField,
                        onAddLineItem = viewModel::addLineItem,
                        onRemoveLineItem = viewModel::removeLineItem,
                        onNotesChange = viewModel::updateNotes
                    )
                }
            }
        }
    }
}

@Composable
private fun AnnotationForm(
    task: AnnotationTask,
    correctedData: ReceiptSchema?,
    onFieldChange: (String, Any?) -> Unit,
    onAddLineItem: () -> Unit,
    onRemoveLineItem: (Int) -> Unit,
    onNotesChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Original vs Corrected sections
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Merchant Information",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = correctedData?.merchantName ?: "",
                    onValueChange = { onFieldChange("merchantName", it) },
                    label = { Text("Merchant Name") },
                    placeholder = { Text("Enter merchant name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = correctedData?.merchantAddress ?: "",
                    onValueChange = { onFieldChange("merchantAddress", it) },
                    label = { Text("Merchant Address") },
                    placeholder = { Text("Enter address") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = correctedData?.phoneNumber ?: "",
                    onValueChange = { onFieldChange("phoneNumber", it) },
                    label = { Text("Phone Number") },
                    placeholder = { Text("Enter phone number") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Transaction Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Transaction Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                val dateString = correctedData?.transactionDate?.let { dateFormat.format(it) } ?: ""
                
                OutlinedTextField(
                    value = dateString,
                    onValueChange = { /* Handle date parsing */ },
                    label = { Text("Transaction Date") },
                    placeholder = { Text("MM/dd/yyyy") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = correctedData?.transactionTime ?: "",
                    onValueChange = { onFieldChange("transactionTime", it) },
                    label = { Text("Transaction Time") },
                    placeholder = { Text("HH:mm AM/PM") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = correctedData?.totalAmount?.toString() ?: "",
                    onValueChange = { onFieldChange("totalAmount", it.toFloatOrNull()) },
                    label = { Text("Total Amount") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = correctedData?.subtotalAmount?.toString() ?: "",
                    onValueChange = { onFieldChange("subtotalAmount", it.toFloatOrNull()) },
                    label = { Text("Subtotal") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = correctedData?.taxAmount?.toString() ?: "",
                    onValueChange = { onFieldChange("taxAmount", it.toFloatOrNull()) },
                    label = { Text("Tax Amount") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Line Items
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Line Items",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedButton(onClick = onAddLineItem) {
                        Text("Add Item")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                correctedData?.lineItems?.forEachIndexed { index, item ->
                    LineItemEditor(
                        item = item,
                        onUpdate = { field, value -> 
                            // Handle line item updates
                        },
                        onRemove = { onRemoveLineItem(index) }
                    )
                }
            }
        }
        
        // Notes
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Annotation Notes",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = task.notes ?: "",
                    onValueChange = onNotesChange,
                    label = { Text("Notes") },
                    placeholder = { Text("Add notes about corrections made...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        }
    }
}

@Composable
private fun LineItemEditor(
    item: com.receiptr.data.ml.schema.LineItem,
    onUpdate: (String, Any) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Item ${item.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                TextButton(onClick = onRemove) {
                    Text("Remove")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = item.name,
                    onValueChange = { onUpdate("name", it) },
                    label = { Text("Name") },
                    modifier = Modifier.weight(2f)
                )
                
                OutlinedTextField(
                    value = item.quantity.toString(),
                    onValueChange = { onUpdate("quantity", it.toFloatOrNull() ?: 1f) },
                    label = { Text("Qty") },
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = item.totalPrice.toString(),
                    onValueChange = { onUpdate("totalPrice", it.toFloatOrNull() ?: 0f) },
                    label = { Text("Price") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Error: $message",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
