package com.receiptr.data.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.receiptr.data.ml.preprocessing.ImagePreprocessingService
import com.receiptr.data.ml.scanner.DocumentScannerService
import com.receiptr.data.ml.scanner.ScanResult
import com.receiptr.domain.model.Receipt
import com.receiptr.domain.model.UiState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Complete receipt processing pipeline that integrates:
 * 1. Google Document Scanner API for image capture and cleaning
 * 2. Image preprocessing for optimization
 * 3. ML Kit Text Recognition
 * 4. Enhanced regex-based parsing
 */
@Singleton
class ReceiptProcessingPipeline @Inject constructor(
    @ApplicationContext private val context: Context,
    private val documentScannerService: DocumentScannerService,
    private val textRecognitionService: TextRecognitionService,
    private val receiptParserService: ReceiptParserService,
    private val imagePreprocessingService: ImagePreprocessingService
) {
    
    companion object {
        private const val TAG = "ReceiptProcessingPipeline"
    }
    
    /**
     * Complete receipt processing flow from Document Scanner result
     */
    fun processScannedReceipt(scanResult: ScanResult): Flow<UiState<Receipt>> = flow {
        emit(UiState.Loading)
        
        try {
            if (!scanResult.isSuccess || scanResult.imageUri == null) {
                emit(UiState.Error("Failed to capture receipt: ${scanResult.error}"))
                return@flow
            }
            
            Log.d(TAG, "Starting receipt processing pipeline")
            
            // Step 1: Load and preprocess the scanned image
            emit(UiState.Loading) // Update with preprocessing status
            val bitmap = loadBitmapFromUri(scanResult.imageUri)
            if (bitmap == null) {
                emit(UiState.Error("Failed to load scanned image"))
                return@flow
            }
            
            // Step 2: Apply image preprocessing for better OCR results
            val preprocessResult = imagePreprocessingService.preprocessReceiptImage(bitmap)
            if (!preprocessResult.isSuccess) {
                Log.w(TAG, "Image preprocessing failed, using original image")
            }
            
            val processedBitmap = if (preprocessResult.isSuccess) {
                preprocessResult.processedBitmap
            } else {
                bitmap
            }
            
            // Step 3: Extract text using ML Kit
            emit(UiState.Loading) // Update with OCR status
            val textResult = textRecognitionService.extractTextFromBitmap(processedBitmap)
            if (!textResult.isSuccess) {
                emit(UiState.Error("Failed to extract text from receipt"))
                return@flow
            }
            
            Log.d(TAG, "Extracted text: ${textResult.fullText}")
            
            // Step 4: Parse receipt data using enhanced regex patterns
            val receiptData = receiptParserService.parseReceipt(textResult)
            
            // Step 5: Create Receipt object with extracted data
            val receipt = Receipt(
                id = UUID.randomUUID().toString(),
                photoUri = scanResult.imageUri,
                merchantName = receiptData.merchantName ?: "Unknown Merchant",
                totalAmount = parseAmount(receiptData.total) ?: 0.0,
                currency = extractCurrency(receiptData.total) ?: "USD",
                date = parseDate(receiptData.date) ?: System.currentTimeMillis(),
                category = mapCategory(receiptData.category),
                description = "Scanned receipt from ${receiptData.merchantName ?: "Unknown"}",
                items = receiptData.items.map { item ->
                    com.receiptr.domain.model.ReceiptItem(
                        name = item.name,
                        quantity = parseQuantity(item.quantity),
                        unitPrice = parseAmount(item.price) ?: 0.0,
                        totalPrice = parseAmount(item.total) ?: 0.0
                    )
                },
                notes = "Processed via Document Scanner API",
                isProcessed = true,
                ocrText = textResult.fullText
            )
            
            Log.d(TAG, "Receipt processing completed successfully")
            Log.d(TAG, "Merchant: ${receipt.merchantName}")
            Log.d(TAG, "Total: ${receipt.totalAmount} ${receipt.currency}")
            Log.d(TAG, "Date: ${Date(receipt.date)}")
            
            emit(UiState.Success(receipt))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in receipt processing pipeline", e)
            emit(UiState.Error("Failed to process receipt: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Process receipt from URI (for gallery imports or other sources)
     */
    fun processReceiptFromUri(imageUri: Uri): Flow<UiState<Receipt>> = flow {
        emit(UiState.Loading)
        
        try {
            Log.d(TAG, "Processing receipt from URI: $imageUri")
            
            val bitmap = loadBitmapFromUri(imageUri)
            if (bitmap == null) {
                emit(UiState.Error("Failed to load image from URI"))
                return@flow
            }
            
            // Create a mock scan result for consistency
            val mockScanResult = ScanResult(
                isSuccess = true,
                imageUri = imageUri,
                pageCount = 1,
                processingQuality = 0.8f
            )
            
            // Use the same processing pipeline
            processScannedReceipt(mockScanResult).collect { state ->
                emit(state)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing receipt from URI", e)
            emit(UiState.Error("Failed to process receipt: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Load bitmap from URI with error handling
     */
    private suspend fun loadBitmapFromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load bitmap from URI: $uri", e)
            null
        }
    }
    
    /**
     * Parse amount string to double
     */
    private fun parseAmount(amountString: String?): Double? {
        return try {
            amountString?.replace(Regex("[^\\d.]"), "")?.toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Extract currency from amount string
     */
    private fun extractCurrency(amountString: String?): String? {
        return when {
            amountString?.contains("Rs", ignoreCase = true) == true -> "INR"
            amountString?.contains("₹") == true -> "INR"
            amountString?.contains("$") == true -> "USD"
            amountString?.contains("€") == true -> "EUR"
            amountString?.contains("£") == true -> "GBP"
            else -> "USD" // Default currency
        }
    }
    
    /**
     * Parse date string to timestamp
     */
    private fun parseDate(dateString: String?): Long? {
        return try {
            // For now, return current timestamp
            // In a real implementation, you'd parse the date string
            System.currentTimeMillis()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Parse quantity string to double
     */
    private fun parseQuantity(quantityString: String?): Double {
        return try {
            quantityString?.toDoubleOrNull() ?: 1.0
        } catch (e: Exception) {
            1.0
        }
    }
    
    /**
     * Map receipt category from parser to domain model
     */
    private fun mapCategory(category: com.receiptr.data.ml.models.ReceiptCategory): String {
        return when (category) {
            com.receiptr.data.ml.models.ReceiptCategory.GROCERIES -> "Food & Dining"
            com.receiptr.data.ml.models.ReceiptCategory.DINING -> "Food & Dining"
            com.receiptr.data.ml.models.ReceiptCategory.FUEL -> "Transportation"
            com.receiptr.data.ml.models.ReceiptCategory.RETAIL -> "Shopping"
            com.receiptr.data.ml.models.ReceiptCategory.ELECTRONICS -> "Electronics"
            com.receiptr.data.ml.models.ReceiptCategory.CLOTHING -> "Shopping"
            com.receiptr.data.ml.models.ReceiptCategory.HEALTHCARE -> "Healthcare"
            com.receiptr.data.ml.models.ReceiptCategory.TRANSPORTATION -> "Transportation"
            com.receiptr.data.ml.models.ReceiptCategory.ENTERTAINMENT -> "Entertainment"
            com.receiptr.data.ml.models.ReceiptCategory.HOME_GARDEN -> "Home & Garden"
            com.receiptr.data.ml.models.ReceiptCategory.AUTOMOTIVE -> "Transportation"
            com.receiptr.data.ml.models.ReceiptCategory.UTILITIES -> "Utilities"
            com.receiptr.data.ml.models.ReceiptCategory.BUSINESS -> "Business"
            com.receiptr.data.ml.models.ReceiptCategory.TRAVEL -> "Travel"
            com.receiptr.data.ml.models.ReceiptCategory.EDUCATION -> "Education"
            else -> "Other"
        }
    }
    
    /**
     * Get processing pipeline status
     */
    fun getPipelineInfo(): ProcessingPipelineInfo {
        val scannerInfo = documentScannerService.getScannerInfo()
        
        return ProcessingPipelineInfo(
            documentScannerAvailable = scannerInfo.isAvailable,
            textRecognitionAvailable = true, // ML Kit is always available
            imagePreprocessingEnabled = true,
            enhancedParsingEnabled = true,
            supportedInputFormats = listOf("JPEG", "PNG", "PDF"),
            processingSteps = listOf(
                "Document Scanning & Cleaning",
                "Image Preprocessing",
                "ML Kit Text Recognition",
                "Enhanced Regex Parsing",
                "Data Validation & Mapping"
            )
        )
    }
}

/**
 * Information about processing pipeline capabilities
 */
data class ProcessingPipelineInfo(
    val documentScannerAvailable: Boolean,
    val textRecognitionAvailable: Boolean,
    val imagePreprocessingEnabled: Boolean,
    val enhancedParsingEnabled: Boolean,
    val supportedInputFormats: List<String>,
    val processingSteps: List<String>
)
