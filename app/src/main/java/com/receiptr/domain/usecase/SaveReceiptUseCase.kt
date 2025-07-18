package com.receiptr.domain.usecase

import android.net.Uri
import com.receiptr.domain.model.Receipt
import com.receiptr.domain.repository.ReceiptRepository
import com.receiptr.data.sync.ReceiptSyncService
import com.receiptr.data.notification.NotificationManager
import javax.inject.Inject

class SaveReceiptUseCase @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val syncService: ReceiptSyncService,
    private val notificationManager: NotificationManager
) {
    suspend fun execute(
        userId: String,
        photoUri: Uri,
        merchantName: String = "",
        totalAmount: Double = 0.0,
        category: String = "",
        notes: String = ""
    ): Result<String> {
        return try {
            val receiptId = java.util.UUID.randomUUID().toString()
            
            // Save photo first
            val photoResult = receiptRepository.saveReceiptPhoto(photoUri, receiptId)
            
            if (photoResult.isFailure) {
                return Result.failure(photoResult.exceptionOrNull() ?: Exception("Failed to save photo"))
            }
            
            val photoPath = photoResult.getOrThrow()
            
            // Create receipt
            val receipt = Receipt(
                id = receiptId,
                userId = userId,
                photoUri = photoUri,
                photoPath = photoPath,
                merchantName = merchantName,
                totalAmount = totalAmount,
                category = category,
                notes = notes,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isProcessed = false
            )
            
            // Save receipt
            val saveResult = receiptRepository.saveReceipt(receipt)
            
            if (saveResult.isSuccess) {
                // Notify sync service about new receipt
                syncService.notifyReceiptAccepted(receipt)
                
                // Send success notification
                notificationManager.sendReceiptScannedNotification(receipt)
                
                // Send large purchase notification if applicable
                notificationManager.sendLargePurchaseNotification(receipt)
                
                Result.success(receiptId)
            } else {
                Result.failure(saveResult.exceptionOrNull() ?: Exception("Failed to save receipt"))
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
