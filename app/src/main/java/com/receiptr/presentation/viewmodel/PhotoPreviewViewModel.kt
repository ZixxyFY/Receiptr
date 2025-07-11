package com.receiptr.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
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
}

data class PhotoPreviewUiState(
    val isLoading: Boolean = false,
    val receiptSaved: Boolean = false,
    val savedReceiptId: String? = null,
    val error: String? = null
)
