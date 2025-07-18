package com.receiptr.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptr.domain.model.Receipt
import com.receiptr.domain.repository.ReceiptRepository
import com.receiptr.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceiptsUiState(
    val receipts: List<Receipt> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUserId: String = ""
)

@HiltViewModel
class ReceiptsViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ReceiptsUiState())
    val uiState: StateFlow<ReceiptsUiState> = _uiState.asStateFlow()
    
    init {
        loadReceipts()
    }
    
    private fun loadReceipts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Get current user ID from Flow
                authRepository.getCurrentUser().collect { currentUser ->
                    val userId = currentUser?.id ?: ""
                    
                    if (userId.isNotEmpty()) {
                        _uiState.update { it.copy(currentUserId = userId) }
                        
                        // Collect receipts and sort by date (newest first)
                        receiptRepository.getAllReceipts(userId)
                            .map { receipts ->
                                receipts.sortedByDescending { it.date }
                            }
                            .collect { sortedReceipts ->
                                _uiState.update { 
                                    it.copy(
                                        receipts = sortedReceipts,
                                        isLoading = false
                                    )
                                }
                            }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "User not authenticated"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }
    
    fun deleteReceipt(receiptId: String) {
        viewModelScope.launch {
            try {
                val result = receiptRepository.deleteReceipt(receiptId)
                if (result.isFailure) {
                    _uiState.update { 
                        it.copy(error = result.exceptionOrNull()?.message ?: "Failed to delete receipt")
                    }
                }
                // The receipts list will be automatically updated through the Flow
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "Failed to delete receipt")
                }
            }
        }
    }
    
    fun refreshReceipts() {
        loadReceipts()
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
