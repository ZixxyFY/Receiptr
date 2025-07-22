package com.receiptr.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.receiptr.domain.model.Receipt
import com.receiptr.domain.repository.ReceiptRepository
import com.receiptr.data.local.ReceiptDao
import com.receiptr.data.local.toEntity
import com.receiptr.data.local.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

class ReceiptRepositoryImpl @Inject constructor(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val receiptDao: ReceiptDao
) : ReceiptRepository {
    
    companion object {
        private const val RECEIPTS_COLLECTION = "receipts"
    }
    
    private val receiptsCache = mutableMapOf<String, Receipt>()
    
    override suspend fun saveReceipt(receipt: Receipt): Result<String> {
        return try {
            // Save to local database
            receiptDao.insertReceipt(receipt.toEntity())
            
            // Keep in cache for faster access
            receiptsCache[receipt.id] = receipt
            
            // Also save to Firestore for cloud sync (optional)
            try {
                firestore.collection(RECEIPTS_COLLECTION)
                    .document(receipt.id)
                    .set(receipt)
                    .await()
            } catch (e: Exception) {
                // Cloud sync failed, but local save succeeded
                // This is acceptable for offline functionality
            }
            
            Result.success(receipt.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getReceipt(id: String): Result<Receipt> {
        return try {
            // Try cache first
            val cachedReceipt = receiptsCache[id]
            if (cachedReceipt != null) {
                return Result.success(cachedReceipt)
            }
            
            // Try local database
            val receiptEntity = receiptDao.getReceiptById(id)
            if (receiptEntity != null) {
                val receipt = receiptEntity.toDomain()
                receiptsCache[id] = receipt
                Result.success(receipt)
            } else {
                Result.failure(Exception("Receipt not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAllReceipts(userId: String): Flow<List<Receipt>> {
        return receiptDao.getAllReceiptsForUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun updateReceipt(receipt: Receipt): Result<Unit> {
        return try {
            // Update in local database
            receiptDao.updateReceipt(receipt.toEntity())
            
            // Update cache
            receiptsCache[receipt.id] = receipt
            
            // Also update in Firestore for cloud sync (optional)
            try {
                firestore.collection(RECEIPTS_COLLECTION)
                    .document(receipt.id)
                    .set(receipt)
                    .await()
            } catch (e: Exception) {
                // Cloud sync failed, but local update succeeded
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteReceipt(id: String): Result<Unit> {
        return try {
            // Get receipt info before deletion
            val receiptEntity = receiptDao.getReceiptById(id)
            
            // Delete from local database
            receiptDao.deleteReceiptById(id)
            
            // Remove from cache
            receiptsCache.remove(id)
            
            // Delete photo file if exists
            receiptEntity?.let { entity ->
                if (entity.photoPath.isNotEmpty()) {
                    val file = File(entity.photoPath)
                    if (file.exists()) {
                        file.delete()
                    }
                }
            }
            
            // Also delete from Firestore for cloud sync (optional)
            try {
                firestore.collection(RECEIPTS_COLLECTION)
                    .document(id)
                    .delete()
                    .await()
            } catch (e: Exception) {
                // Cloud sync failed, but local deletion succeeded
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
        return receiptDao.searchReceipts(userId, query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
