package com.receiptr.presentation.receipts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptr.domain.model.Receipt
import com.receiptr.domain.repository.ReceiptRepository
import com.receiptr.domain.usecase.GenerateReceiptPdfUseCase
import com.receiptr.domain.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import android.net.Uri
import javax.inject.Inject

@HiltViewModel
class ReceiptDetailViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val generatePdfUseCase: GenerateReceiptPdfUseCase
) : ViewModel() {

    private val _receiptState = MutableStateFlow<UiState<Receipt>>(UiState.Idle)
    val receiptState: StateFlow<UiState<Receipt>> = _receiptState.asStateFlow()

    private val _pdfShareEvent = MutableSharedFlow<Uri>()
    val pdfShareEvent: SharedFlow<Uri> = _pdfShareEvent.asSharedFlow()

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    fun loadReceipt(receiptId: String) {
        viewModelScope.launch {
            _receiptState.value = UiState.Loading
            try {
                val result = receiptRepository.getReceipt(receiptId)
                if (result.isSuccess) {
                    val receipt = result.getOrNull()!!
                    _receiptState.value = UiState.Success(receipt)
                } else {
                    _receiptState.value = UiState.Error(
                        message = "Failed to load receipt: ${result.exceptionOrNull()?.message}",
                        throwable = result.exceptionOrNull()
                    )
                }
            } catch (e: Exception) {
                _receiptState.value = UiState.Error(
                    message = "Failed to load receipt: ${e.message}",
                    throwable = e
                )
            }
        }
    }

    fun onSharePdfClicked() {
        val currentReceipt = (_receiptState.value as? UiState.Success)?.data
        if (currentReceipt == null) return

        viewModelScope.launch {
            _isGeneratingPdf.value = true
            try {
                val pdfUri = generatePdfUseCase.execute(currentReceipt)
                _pdfShareEvent.emit(pdfUri)
            } catch (e: Exception) {
                // Handle error - could emit to an error state
                // For now, we'll just stop the loading indicator
            } finally {
                _isGeneratingPdf.value = false
            }
        }
    }
}
