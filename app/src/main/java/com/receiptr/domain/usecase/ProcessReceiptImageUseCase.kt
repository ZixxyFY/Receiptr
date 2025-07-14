package com.receiptr.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.receiptr.data.ml.ReceiptData
import com.receiptr.data.ml.ReceiptParserService
import com.receiptr.data.ml.TextRecognitionService
import com.receiptr.domain.model.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for processing receipt images and extracting structured data
 */
class ProcessReceiptImageUseCase @Inject constructor(
    private val textRecognitionService: TextRecognitionService,
    private val receiptParserService: ReceiptParserService
) {
    
    /**
     * Process receipt from bitmap and return structured data
     */
    suspend fun processReceiptFromBitmap(bitmap: Bitmap): Flow<UiState<ReceiptData>> = flow {
        emit(UiState.Loading)
        
        try {
            // Step 1: Extract text from image
            val textResult = textRecognitionService.extractTextFromBitmap(bitmap)
            
            if (!textResult.isSuccess || textResult.fullText.isBlank()) {
                emit(UiState.Error("Failed to extract text from image"))
                return@flow
            }
            
            // Step 2: Parse receipt data from extracted text
            val receiptData = receiptParserService.parseReceipt(textResult)
            
            if (receiptData.total == null && receiptData.items.isEmpty()) {
                emit(UiState.Error("No receipt data found in the image"))
                return@flow
            }
            
            emit(UiState.Success(receiptData))
            
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to process receipt image"))
        }
    }
    
    /**
     * Process receipt from URI and return structured data
     */
    suspend fun processReceiptFromUri(uri: Uri): Flow<UiState<ReceiptData>> = flow {
        emit(UiState.Loading)
        
        try {
            // Step 1: Extract text from image
            val textResult = textRecognitionService.extractTextFromUri(uri)
            
            if (!textResult.isSuccess || textResult.fullText.isBlank()) {
                emit(UiState.Error("Failed to extract text from image"))
                return@flow
            }
            
            // Step 2: Parse receipt data from extracted text
            val receiptData = receiptParserService.parseReceipt(textResult)
            
            if (receiptData.total == null && receiptData.items.isEmpty()) {
                emit(UiState.Error("No receipt data found in the image"))
                return@flow
            }
            
            emit(UiState.Success(receiptData))
            
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to process receipt image"))
        }
    }
    
    /**
     * Extract raw text from image (for debugging or manual processing)
     */
    suspend fun extractRawTextFromBitmap(bitmap: Bitmap): Flow<UiState<String>> = flow {
        emit(UiState.Loading)
        
        try {
            val textResult = textRecognitionService.extractTextFromBitmap(bitmap)
            
            if (!textResult.isSuccess || textResult.fullText.isBlank()) {
                emit(UiState.Error("Failed to extract text from image"))
                return@flow
            }
            
            emit(UiState.Success(textResult.fullText))
            
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to extract text from image"))
        }
    }
}
