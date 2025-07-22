package com.receiptr.data.email

import com.receiptr.domain.model.EmailReceipt
import com.receiptr.domain.model.Receipt
import com.receiptr.domain.repository.ReceiptRepository
import com.receiptr.data.notification.NotificationManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmailReceiptRepository @Inject constructor(
    private val emailService: EmailService,
    private val emailReceiptParser: EmailReceiptParser,
    private val receiptRepository: ReceiptRepository,
    private val notificationManager: NotificationManager
) {

    suspend fun fetchAndProcessEmailReceipts(userId: String): Flow<List<EmailReceipt>> = flow {
        try {
            // Get sample email receipts for demonstration
            val emailReceipts = emailService.getSampleEmailReceipts()
            
            // Parse each email receipt to extract data
            val parsedReceipts = emailReceipts.map { emailReceipt ->
                emailReceiptParser.parseEmailReceipt(emailReceipt)
            }
            
            emit(parsedReceipts)
            
            // In a real implementation, you would:
            // 1. Check if user has email connected
            // 2. Get stored access token
            // 3. Connect to their email service (Gmail/Outlook/Yahoo)
            // 4. Search for receipt-like emails
            // 5. Parse and process them
            // 6. Save them as receipts
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun processEmailReceipt(emailReceipt: EmailReceipt, userId: String): Result<Receipt> {
        return try {
            // Parse the email to extract receipt data
            val parsedEmailReceipt = emailReceiptParser.parseEmailReceipt(emailReceipt)
            
            if (parsedEmailReceipt.extractedData != null && parsedEmailReceipt.confidence > 0.5f) {
                // Convert to Receipt object
                val receipt = convertEmailReceiptToReceipt(parsedEmailReceipt, userId)
                
                // Save to receipt repository
                val saveResult = receiptRepository.saveReceipt(receipt)
                
                if (saveResult.isSuccess) {
                    // Send notification
                    notificationManager.sendEmailReceiptNotification(receipt)
                    Result.success(receipt)
                } else {
                    Result.failure(saveResult.exceptionOrNull() ?: Exception("Failed to save receipt"))
                }
            } else {
                Result.failure(Exception("Could not parse receipt data with sufficient confidence"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun convertEmailReceiptToReceipt(emailReceipt: EmailReceipt, userId: String): Receipt {
        val extractedData = emailReceipt.extractedData!!
        
        return Receipt(
            id = emailReceipt.id,
            userId = userId,
            merchantName = extractedData.merchantName,
            totalAmount = extractedData.totalAmount,
            currency = extractedData.currency,
            date = extractedData.transactionDate,
            category = extractedData.category,
            description = "Imported from email: ${emailReceipt.subject}",
            notes = "Email receipt from ${emailReceipt.from}",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isProcessed = true,
            ocrText = emailReceipt.body
        )
    }

    suspend fun getStoredEmailReceipts(userId: String): Flow<List<EmailReceipt>> = flow {
        // In a real implementation, you'd store email receipts in a database
        // and retrieve them here. For now, return empty list.
        emit(emptyList())
    }

    suspend fun markEmailReceiptAsProcessed(emailReceiptId: String): Result<Unit> {
        return try {
            // Mark email receipt as processed in storage
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
