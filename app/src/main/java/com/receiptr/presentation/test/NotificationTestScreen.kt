package com.receiptr.presentation.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.receiptr.data.notification.NotificationManager
import com.receiptr.domain.model.Receipt
import java.util.*

@Composable
fun NotificationTestScreen(
    notificationManager: NotificationManager
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Notification Test",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        
        item {
            Button(
                onClick = {
                    val testReceipt = Receipt(
                        id = UUID.randomUUID().toString(),
                        userId = "test_user",
                        merchantName = "Test Store",
                        totalAmount = 25.50,
                        currency = "USD",
                        date = System.currentTimeMillis(),
                        category = "Food & Dining"
                    )
                    notificationManager.sendReceiptScannedNotification(testReceipt)
                }
            ) {
                Text("Test Receipt Scanned")
            }
        }
        
        item {
            Button(
                onClick = {
                    val testReceipt = Receipt(
                        id = UUID.randomUUID().toString(),
                        userId = "test_user",
                        merchantName = "Email Store",
                        totalAmount = 45.75,
                        currency = "USD",
                        date = System.currentTimeMillis(),
                        category = "Shopping"
                    )
                    notificationManager.sendEmailReceiptNotification(testReceipt)
                }
            ) {
                Text("Test Email Receipt")
            }
        }
        
        item {
            Button(
                onClick = {
                    notificationManager.sendPdfExportNotification("test_receipt_123", "/path/to/receipt.pdf")
                }
            ) {
                Text("Test PDF Export")
            }
        }
        
        item {
            Button(
                onClick = {
                    notificationManager.sendWeeklySummaryNotification(234.56, "USD")
                }
            ) {
                Text("Test Weekly Summary")
            }
        }
        
        item {
            Button(
                onClick = {
                    notificationManager.sendReminderScanNotification()
                }
            ) {
                Text("Test Scan Reminder")
            }
        }
        
        item {
            Button(
                onClick = {
                    notificationManager.sendBudgetAlertNotification(
                        category = "Food & Dining",
                        amountSpent = 450.00,
                        totalBudget = 500.00,
                        currency = "USD"
                    )
                }
            ) {
                Text("Test Budget Alert")
            }
        }
        
        item {
            Button(
                onClick = {
                    val testReceipt = Receipt(
                        id = UUID.randomUUID().toString(),
                        userId = "test_user",
                        merchantName = "Expensive Store",
                        totalAmount = 150.00,
                        currency = "USD",
                        date = System.currentTimeMillis(),
                        category = "Electronics"
                    )
                    notificationManager.sendLargePurchaseNotification(testReceipt)
                }
            ) {
                Text("Test Large Purchase")
            }
        }
    }
}
