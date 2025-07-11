package com.receiptr.domain.repository

import android.net.Uri
import com.receiptr.domain.model.Receipt
import kotlinx.coroutines.flow.Flow

interface ReceiptRepository {
    suspend fun saveReceipt(receipt: Receipt): Result<String>
    suspend fun getReceipt(id: String): Result<Receipt>
    suspend fun getAllReceipts(userId: String): Flow<List<Receipt>>
    suspend fun updateReceipt(receipt: Receipt): Result<Unit>
    suspend fun deleteReceipt(id: String): Result<Unit>
    suspend fun saveReceiptPhoto(uri: Uri, receiptId: String): Result<String>
    suspend fun processReceiptPhoto(photoPath: String): Result<String>
    suspend fun searchReceipts(userId: String, query: String): Flow<List<Receipt>>
}
