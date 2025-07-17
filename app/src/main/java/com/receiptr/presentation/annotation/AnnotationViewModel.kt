package com.receiptr.presentation.annotation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptr.data.ml.annotation.AnnotationService
import com.receiptr.data.ml.annotation.AnnotationTask
import com.receiptr.data.ml.schema.ReceiptSchema
import com.receiptr.data.ml.schema.LineItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AnnotationViewModel @Inject constructor(
    private val annotationService: AnnotationService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AnnotationUiState())
    val uiState: StateFlow<AnnotationUiState> = _uiState.asStateFlow()
    
    fun loadAnnotationTask(taskId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val tasks = annotationService.getPendingTasks(limit = 1000)
                val task = tasks.find { it.id == taskId }
                
                if (task != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        annotationTask = task,
                        correctedData = task.correctedData ?: task.originalData.copy(),
                        originalData = task.originalData
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Annotation task not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    fun updateField(fieldName: String, value: Any?) {
        val currentData = _uiState.value.correctedData ?: return
        
        val updatedData = when (fieldName) {
            "merchantName" -> currentData.copy(merchantName = value as? String)
            "merchantAddress" -> currentData.copy(merchantAddress = value as? String)
            "phoneNumber" -> currentData.copy(phoneNumber = value as? String)
            "transactionTime" -> currentData.copy(transactionTime = value as? String)
            "totalAmount" -> currentData.copy(totalAmount = value as? Float)
            "subtotalAmount" -> currentData.copy(subtotalAmount = value as? Float)
            "taxAmount" -> currentData.copy(taxAmount = value as? Float)
            "tipAmount" -> currentData.copy(tipAmount = value as? Float)
            "discountAmount" -> currentData.copy(discountAmount = value as? Float)
            else -> currentData
        }
        
        _uiState.value = _uiState.value.copy(
            correctedData = updatedData.copy(updatedAt = Date()),
            hasChanges = true
        )
    }
    
    fun addLineItem() {
        val currentData = _uiState.value.correctedData ?: return
        
        val newItem = LineItem(
            name = "New Item",
            description = null,
            quantity = 1.0f,
            unitPrice = 0.0f,
            totalPrice = 0.0f,
            category = null,
            sku = null,
            barcode = null,
            discount = null,
            tax = null
        )
        
        val updatedItems = currentData.lineItems + newItem
        val updatedData = currentData.copy(
            lineItems = updatedItems,
            updatedAt = Date()
        )
        
        _uiState.value = _uiState.value.copy(
            correctedData = updatedData,
            hasChanges = true
        )
    }
    
    fun removeLineItem(index: Int) {
        val currentData = _uiState.value.correctedData ?: return
        
        if (index in currentData.lineItems.indices) {
            val updatedItems = currentData.lineItems.toMutableList().apply {
                removeAt(index)
            }
            
            val updatedData = currentData.copy(
                lineItems = updatedItems,
                updatedAt = Date()
            )
            
            _uiState.value = _uiState.value.copy(
                correctedData = updatedData,
                hasChanges = true
            )
        }
    }
    
    fun updateNotes(notes: String) {
        val currentTask = _uiState.value.annotationTask ?: return
        
        _uiState.value = _uiState.value.copy(
            annotationTask = currentTask.copy(notes = notes),
            hasChanges = true
        )
    }
    
    fun saveCorrections() {
        val currentTask = _uiState.value.annotationTask ?: return
        val correctedData = _uiState.value.correctedData ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val completedTask = annotationService.completeAnnotation(
                    taskId = currentTask.id,
                    correctedData = correctedData.copy(
                        isManuallyVerified = true,
                        updatedAt = Date()
                    ),
                    annotatorId = "current_user", // TODO: Get actual user ID
                    notes = currentTask.notes
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    annotationTask = completedTask,
                    hasChanges = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save corrections"
                )
            }
        }
    }
}

data class AnnotationUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val annotationTask: AnnotationTask? = null,
    val originalData: ReceiptSchema? = null,
    val correctedData: ReceiptSchema? = null,
    val hasChanges: Boolean = false,
    val isSaved: Boolean = false
)
