package com.receiptr.presentation.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.receiptr.data.ml.ReceiptData
import com.receiptr.domain.model.UiState
import com.receiptr.domain.usecase.ProcessReceiptImageUseCase
import com.receiptr.domain.usecase.SaveReceiptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoPreviewViewModel @Inject constructor(
    private val saveReceiptUseCase: SaveReceiptUseCase,
    private val processReceiptImageUseCase: ProcessReceiptImageUseCase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoPreviewUiState())
    val uiState: StateFlow<PhotoPreviewUiState> = _uiState.asStateFlow()

    fun saveReceipt(
        photoUri: Uri,
        merchantName: String = "",
        totalAmount: Double = 0.0,
        category: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "User not authenticated"
                )
                return@launch
            }

            val result = saveReceiptUseCase.execute(
                userId = currentUser.uid,
                photoUri = photoUri,
                merchantName = merchantName,
                totalAmount = totalAmount,
                category = category,
                notes = notes
            )

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    receiptSaved = true,
                    savedReceiptId = result.getOrNull()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to save receipt"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetState() {
        _uiState.value = PhotoPreviewUiState()
    }
    
    /**
     * Process receipt image with ML Kit to extract text and data
     */
    fun processReceiptImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)
            
            processReceiptImageUseCase.processReceiptFromBitmap(bitmap)
                .collect { result ->
                    when (result) {
                        is UiState.Loading -> {
                            _uiState.value = _uiState.value.copy(isProcessing = true)
                        }
                        is UiState.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                receiptData = result.data,
                                extractedText = result.data.rawText
                            )
                        }
                        is UiState.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                error = result.message
                            )
                        }
                        else -> {}
                    }
                }
        }
    }
    
    /**
     * Process receipt image from URI with ML Kit
     */
    fun processReceiptImageFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)
            
            processReceiptImageUseCase.processReceiptFromUri(uri)
                .collect { result ->
                    when (result) {
                        is UiState.Loading -> {
                            _uiState.value = _uiState.value.copy(isProcessing = true)
                        }
                        is UiState.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                receiptData = result.data,
                                extractedText = result.data.rawText
                            )
                        }
                        is UiState.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                error = result.message
                            )
                        }
                        else -> {}
                    }
                }
        }
    }
    
    /**
     * Update receipt data manually (for user corrections)
     */
    fun updateReceiptData(receiptData: ReceiptData) {
        _uiState.value = _uiState.value.copy(receiptData = receiptData)
    }
}

data class PhotoPreviewUiState(
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val receiptSaved: Boolean = false,
    val savedReceiptId: String? = null,
    val receiptData: ReceiptData? = null,
    val extractedText: String? = null,
    val error: String? = null
)
