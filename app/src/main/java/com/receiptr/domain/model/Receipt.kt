package com.receiptr.domain.model

import android.net.Uri

data class Receipt(
    val id: String = "",
    val userId: String = "",
    val photoUri: Uri? = null,
    val photoPath: String = "",
    val merchantName: String = "",
    val totalAmount: Double = 0.0,
    val currency: String = "USD",
    val date: Long = System.currentTimeMillis(),
    val category: String = "",
    val description: String = "",
    val items: List<ReceiptItem> = emptyList(),
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isProcessed: Boolean = false,
    val ocrText: String = ""
)

data class ReceiptItem(
    val name: String = "",
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0,
    val category: String = ""
)

enum class ReceiptCategory(val displayName: String) {
    FOOD("Food & Dining"),
    SHOPPING("Shopping"),
    TRANSPORTATION("Transportation"),
    UTILITIES("Utilities"),
    ENTERTAINMENT("Entertainment"),
    HEALTHCARE("Healthcare"),
    BUSINESS("Business"),
    TRAVEL("Travel"),
    EDUCATION("Education"),
    OTHER("Other")
}
