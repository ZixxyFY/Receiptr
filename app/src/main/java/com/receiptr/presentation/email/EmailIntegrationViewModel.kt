package com.receiptr.presentation.email

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptr.domain.model.EmailProvider
import com.receiptr.domain.model.EmailReceipt
import com.receiptr.domain.repository.AuthRepository
import com.receiptr.data.email.EmailReceiptRepository
import com.receiptr.data.email.EmailAuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmailIntegrationUiState(
    val isLoading: Boolean = false,
    val isEmailConnected: Boolean = false,
    val emailReceipts: List<EmailReceipt> = emptyList(),
    val connectedProvider: EmailProvider? = null,
    val error: String? = null
)

@HiltViewModel
class EmailIntegrationViewModel @Inject constructor(
    private val emailReceiptRepository: EmailReceiptRepository,
    private val authRepository: AuthRepository,
    private val emailAuthService: EmailAuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailIntegrationUiState())
    val uiState: StateFlow<EmailIntegrationUiState> = _uiState.asStateFlow()

    init {
        checkEmailConnectionStatus()
    }

    private fun checkEmailConnectionStatus() {
        viewModelScope.launch {
            try {
                // Check if user has email connected using EmailAuthService
                val currentUser = authRepository.getCurrentUser().first()
                val userId = currentUser?.id ?: ""
                
                if (userId.isNotEmpty()) {
                    val isConnected = emailAuthService.isEmailConnected(userId)
                    val connectedProvider = emailAuthService.getConnectedProvider(userId)
                    
                    _uiState.update { 
                        it.copy(
                            isEmailConnected = isConnected,
                            connectedProvider = connectedProvider
                        )
                    }
                    
                    if (isConnected) {
                        loadEmailReceipts()
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "Failed to check email connection status")
                }
            }
        }
    }

    fun connectEmail(provider: EmailProvider) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val currentUser = authRepository.getCurrentUser().first()
                val userId = currentUser?.id ?: throw Exception("User not authenticated")
                
                // Simulate email connection using EmailAuthService
                val connectionSuccess = emailAuthService.simulateEmailConnection(provider, userId)
                
                if (connectionSuccess) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isEmailConnected = true,
                            connectedProvider = provider
                        )
                    }
                    
                    // Load email receipts after connection
                    loadEmailReceipts()
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to connect to ${provider.displayName}"
                        )
                    }
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to connect email"
                    )
                }
            }
        }
    }

    fun refreshEmailReceipts() {
        loadEmailReceipts()
    }

    private fun loadEmailReceipts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val currentUser = authRepository.getCurrentUser().first()
                val userId = currentUser?.id ?: ""
                
                if (userId.isNotEmpty()) {
                    emailReceiptRepository.fetchAndProcessEmailReceipts(userId)
                        .collect { receipts ->
                            _uiState.update { 
                                it.copy(
                                    emailReceipts = receipts,
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
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load email receipts"
                    )
                }
            }
        }
    }

    fun processEmailReceipt(emailReceipt: EmailReceipt) {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser().first()
                val userId = currentUser?.id ?: return@launch
                
                val result = emailReceiptRepository.processEmailReceipt(emailReceipt, userId)
                
                if (result.isSuccess) {
                    // Mark as processed and refresh list
                    emailReceiptRepository.markEmailReceiptAsProcessed(emailReceipt.id)
                    refreshEmailReceipts()
                } else {
                    _uiState.update { 
                        it.copy(error = result.exceptionOrNull()?.message ?: "Failed to process email receipt")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "Failed to process email receipt")
                }
            }
        }
    }

    fun disconnectEmail() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser().first()
                val userId = currentUser?.id ?: return@launch
                
                // Disconnect email using EmailAuthService
                emailAuthService.disconnectEmail(userId)
                
                _uiState.update { 
                    EmailIntegrationUiState(
                        isEmailConnected = false,
                        connectedProvider = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "Failed to disconnect email")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // Demo function to add sample email receipts for testing
    fun addSampleEmailReceipts() {
        viewModelScope.launch {
            val sampleReceipts = listOf(
                EmailReceipt(
                    id = "email-1",
                    emailId = "msg-1",
                    from = "receipts@amazon.com",
                    subject = "Your Amazon.com order #123-4567890-1234567",
                    body = "Thank you for your order! Total: $45.99",
                    receivedDate = System.currentTimeMillis() - (24 * 60 * 60 * 1000), // 1 day ago
                    isProcessed = false
                ),
                EmailReceipt(
                    id = "email-2",
                    emailId = "msg-2",
                    from = "noreply@starbucks.com",
                    subject = "Starbucks Card Reload Receipt",
                    body = "You've successfully reloaded your Starbucks Card. Amount: $25.00",
                    receivedDate = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000), // 2 days ago
                    isProcessed = true
                ),
                EmailReceipt(
                    id = "email-3",
                    emailId = "msg-3",
                    from = "receipts@uber.com",
                    subject = "Trip receipt",
                    body = "Thanks for riding with Uber! Your trip total was $18.45",
                    receivedDate = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000), // 3 days ago
                    isProcessed = false
                )
            )
            
            _uiState.update { 
                it.copy(emailReceipts = sampleReceipts)
            }
        }
    }
}
