package com.receiptr.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.receiptr.domain.model.Receipt
import com.receiptr.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

class ReceiptRepositoryImpl @Inject constructor(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ReceiptRepository {
    
    companion object {
        private const val RECEIPTS_COLLECTION = "receipts"
        private const val RECEIPT_IMAGES_PATH = "receipt_images"
    }
    
    private val receiptsCache = mutableMapOf<String, Receipt>()
    private val userReceipts = mutableMapOf<String, MutableList<Receipt>>()
    
    override suspend fun saveReceipt(receipt: Receipt): Result<String> {
        return try {
            receiptsCache[receipt.id] = receipt
            
            // Add to user's receipts
            val userReceiptsList = userReceipts.getOrPut(receipt.userId) { mutableListOf() }
            userReceiptsList.add(receipt)
            
            Result.success(receipt.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getReceipt(id: String): Result<Receipt> {
        return try {
            val receipt = receiptsCache[id]
            if (receipt != null) {
                Result.success(receipt)
            } else {
                Result.failure(Exception("Receipt not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAllReceipts(userId: String): Flow<List<Receipt>> {
        return flowOf(userReceipts[userId] ?: emptyList())
    }
    
    override suspend fun updateReceipt(receipt: Receipt): Result<Unit> {
        return try {
            receiptsCache[receipt.id] = receipt
            
            // Update in user's receipts
            val userReceiptsList = userReceipts[receipt.userId]
            userReceiptsList?.let { list ->
                val index = list.indexOfFirst { it.id == receipt.id }
                if (index != -1) {
                    list[index] = receipt
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteReceipt(id: String): Result<Unit> {
        return try {
            val receipt = receiptsCache[id]
            if (receipt != null) {
                receiptsCache.remove(id)
                
                // Remove from user's receipts
                val userReceiptsList = userReceipts[receipt.userId]
                userReceiptsList?.removeIf { it.id == id }
                
                // Delete photo file if exists
                if (receipt.photoPath.isNotEmpty()) {
                    val file = File(receipt.photoPath)
                    if (file.exists()) {
                        file.delete()
                    }
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun saveReceiptPhoto(uri: Uri, receiptId: String): Result<String> {
        return try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream = contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Unable to open photo"))
            
            // Create app-specific directory for receipts
            val receiptsDir = File(context.filesDir, "receipts")
            if (!receiptsDir.exists()) {
                receiptsDir.mkdirs()
            }
            
            // Create unique file name
            val fileName = "receipt_${receiptId}_${System.currentTimeMillis()}.jpg"
            val file = File(receiptsDir, fileName)
            
            // Copy the image
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun processReceiptPhoto(photoPath: String): Result<String> {
        return try {
            // TODO: Implement OCR processing
            // For now, return empty string
            Result.success("")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun searchReceipts(userId: String, query: String): Flow<List<Receipt>> {
        val userReceiptsList = userReceipts[userId] ?: emptyList()
        val filteredReceipts = userReceiptsList.filter { receipt ->
            receipt.merchantName.contains(query, ignoreCase = true) ||
            receipt.description.contains(query, ignoreCase = true) ||
            receipt.category.contains(query, ignoreCase = true) ||
            receipt.notes.contains(query, ignoreCase = true)
        }
        return flowOf(filteredReceipts)
    }
}
