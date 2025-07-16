package com.receiptr.data.sync

import com.receiptr.domain.model.Receipt
import com.receiptr.domain.repository.ReceiptRepository
import com.receiptr.data.analytics.ReceiptAnalyticsService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service to handle real-time synchronization of receipt data
 * Updates home screen and analytics when new receipts are added
 */
@Singleton
class ReceiptSyncService @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val analyticsService: ReceiptAnalyticsService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Flow to emit receipt updates
    private val _receiptUpdates = MutableSharedFlow<ReceiptUpdate>()
    val receiptUpdates: SharedFlow<ReceiptUpdate> = _receiptUpdates.asSharedFlow()
    
    // Flow to emit analytics updates
    private val _analyticsUpdates = MutableSharedFlow<AnalyticsUpdate>()
    val analyticsUpdates: SharedFlow<AnalyticsUpdate> = _analyticsUpdates.asSharedFlow()
    
    /**
     * Notify that a new receipt has been processed and accepted
     */
    suspend fun notifyReceiptAccepted(receipt: Receipt) {
        try {
            // Update receipt in repository
            val updatedReceipt = receipt.copy(
                isProcessed = true,
                updatedAt = System.currentTimeMillis()
            )
            receiptRepository.updateReceipt(updatedReceipt)
            
            // Emit receipt update
            _receiptUpdates.emit(ReceiptUpdate(
                type = ReceiptUpdateType.ADDED,
                receipt = updatedReceipt
            ))
            
            // Trigger analytics recalculation
            recalculateAnalytics(receipt.userId)
            
        } catch (e: Exception) {
            // Handle error - could emit error state
            _receiptUpdates.emit(ReceiptUpdate(
                type = ReceiptUpdateType.ERROR,
                receipt = receipt,
                error = e.message
            ))
        }
    }
    
    /**
     * Notify that a receipt has been edited/updated
     */
    suspend fun notifyReceiptUpdated(receipt: Receipt) {
        try {
            val updatedReceipt = receipt.copy(
                updatedAt = System.currentTimeMillis()
            )
            receiptRepository.updateReceipt(updatedReceipt)
            
            _receiptUpdates.emit(ReceiptUpdate(
                type = ReceiptUpdateType.UPDATED,
                receipt = updatedReceipt
            ))
            
            // Trigger analytics recalculation
            recalculateAnalytics(receipt.userId)
            
        } catch (e: Exception) {
            _receiptUpdates.emit(ReceiptUpdate(
                type = ReceiptUpdateType.ERROR,
                receipt = receipt,
                error = e.message
            ))
        }
    }
    
    /**
     * Notify that a receipt has been deleted
     */
    suspend fun notifyReceiptDeleted(receiptId: String, userId: String) {
        try {
            receiptRepository.deleteReceipt(receiptId)
            
            _receiptUpdates.emit(ReceiptUpdate(
                type = ReceiptUpdateType.DELETED,
                receipt = null,
                deletedReceiptId = receiptId
            ))
            
            // Trigger analytics recalculation
            recalculateAnalytics(userId)
            
        } catch (e: Exception) {
            _receiptUpdates.emit(ReceiptUpdate(
                type = ReceiptUpdateType.ERROR,
                receipt = null,
                error = e.message
            ))
        }
    }
    
    /**
     * Manually trigger a full data refresh
     */
    suspend fun refreshAllData(userId: String) {
        try {
            _receiptUpdates.emit(ReceiptUpdate(
                type = ReceiptUpdateType.REFRESH_ALL,
                receipt = null
            ))
            
            recalculateAnalytics(userId)
            
        } catch (e: Exception) {
            _receiptUpdates.emit(ReceiptUpdate(
                type = ReceiptUpdateType.ERROR,
                receipt = null,
                error = e.message
            ))
        }
    }
    
    /**
     * Recalculate analytics and emit update
     */
    private suspend fun recalculateAnalytics(userId: String) {
        scope.launch {
            try {
                // Get updated spending analytics
                val spendingAnalytics = analyticsService.getSpendingAnalytics(userId).first()
                
                // Get recent receipts
                val recentReceipts = analyticsService.getRecentReceipts(userId, 10).first()
                
                // Get spending trends
                val spendingTrends = analyticsService.getSpendingTrends(userId, 7).first()
                
                _analyticsUpdates.emit(AnalyticsUpdate(
                    spendingAnalytics = spendingAnalytics,
                    recentReceipts = recentReceipts,
                    spendingTrends = spendingTrends,
                    lastUpdated = System.currentTimeMillis()
                ))
                
            } catch (e: Exception) {
                _analyticsUpdates.emit(AnalyticsUpdate(
                    spendingAnalytics = null,
                    recentReceipts = emptyList(),
                    spendingTrends = emptyList(),
                    lastUpdated = System.currentTimeMillis(),
                    error = e.message
                ))
            }
        }
    }
    
    /**
     * Start periodic sync (optional - for future use)
     */
    fun startPeriodicSync(userId: String, intervalMinutes: Long = 5) {
        // Implementation for periodic background sync
        // This could be useful for multi-device sync or server updates
    }
}

/**
 * Data classes for sync updates
 */
data class ReceiptUpdate(
    val type: ReceiptUpdateType,
    val receipt: Receipt?,
    val deletedReceiptId: String? = null,
    val error: String? = null
)

enum class ReceiptUpdateType {
    ADDED,
    UPDATED,
    DELETED,
    REFRESH_ALL,
    ERROR
}

data class AnalyticsUpdate(
    val spendingAnalytics: com.receiptr.data.analytics.SpendingAnalytics?,
    val recentReceipts: List<com.receiptr.data.analytics.EnhancedReceiptItem>,
    val spendingTrends: List<com.receiptr.data.analytics.MonthlySpending>,
    val lastUpdated: Long,
    val error: String? = null
)

