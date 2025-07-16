package com.receiptr.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptr.domain.model.UiState
import com.receiptr.presentation.home.ReceiptItem
import com.receiptr.presentation.home.QuickAction
import com.receiptr.data.sync.ReceiptSyncService
import com.receiptr.data.sync.ReceiptUpdateType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject

data class HomeScreenData(
    val recentReceipts: List<ReceiptItem>,
    val monthlySpending: Double,
    val spendingChange: String,
    val quickActions: List<QuickAction>
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val receiptRepository: com.receiptr.domain.repository.ReceiptRepository,
    private val analyticsService: com.receiptr.data.analytics.ReceiptAnalyticsService,
    private val syncService: ReceiptSyncService
) : ViewModel() {
    
    private val _homeDataState = MutableStateFlow<UiState<HomeScreenData>>(UiState.Idle)
    val homeDataState: StateFlow<UiState<HomeScreenData>> = _homeDataState.asStateFlow()
    
    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()
    
    init {
        loadHomeData()
        observeSyncUpdates()
    }
    
    fun refreshHomeData() {
        loadHomeData()
    }
    
    fun loadHomeData() {
        viewModelScope.launch {
            _homeDataState.value = UiState.Loading
            
            try {
                // Simulate network delay
                delay(1500) // 1.5 second delay to show skeleton
                
                // Fetch real analytics data
                val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                
                // Get spending analytics
                val spendingAnalytics = analyticsService.getSpendingAnalytics(userId).first()
                
                // Get recent receipts
                val recentReceiptItems = analyticsService.getRecentReceipts(userId, 10).first()
                
                val homeData = HomeScreenData(
                    recentReceipts = recentReceiptItems.map {
                        ReceiptItem(
                            id = it.id, 
                            storeName = it.merchantName, 
                            amount = it.totalAmount, 
                            date = it.date, 
                            category = it.category.displayName
                        )
                    },
                    monthlySpending = spendingAnalytics.totalSpending,
                    spendingChange = spendingAnalytics.spendingChange,
                    quickActions = listOf() // Placeholder, define actual actions
                )
                
                _homeDataState.value = UiState.Success(homeData)
            } catch (e: Exception) {
                _homeDataState.value = UiState.Error(
                    message = "Failed to load home data: ${e.message}",
                    throwable = e
                )
            }
        }
    }
    
    fun refreshData() {
        viewModelScope.launch {
            _refreshing.value = true
            
            try {
                // Simulate refresh delay
                delay(1000)
                
                // Fetch real analytics data
                val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                
                // Get spending analytics
                val spendingAnalytics = analyticsService.getSpendingAnalytics(userId).first()
                
                // Get recent receipts
                val recentReceiptItems = analyticsService.getRecentReceipts(userId, 10).first()
                
                val homeData = HomeScreenData(
                    recentReceipts = recentReceiptItems.map {
                        ReceiptItem(
                            id = it.id, 
                            storeName = it.merchantName, 
                            amount = it.totalAmount, 
                            date = it.date, 
                            category = it.category.displayName
                        )
                    },
                    monthlySpending = spendingAnalytics.totalSpending,
                    spendingChange = spendingAnalytics.spendingChange,
                    quickActions = listOf() // Placeholder, define actual actions
                )
                _homeDataState.value = UiState.Success(homeData)
            } catch (e: Exception) {
                _homeDataState.value = UiState.Error(
                    message = "Failed to refresh data: ${e.message}",
                    throwable = e
                )
            } finally {
                _refreshing.value = false
            }
        }
    }
    
    /**
     * Observe sync service updates for real-time data refresh
     */
    private fun observeSyncUpdates() {
        viewModelScope.launch {
            syncService.receiptUpdates.collect { update ->
                when (update.type) {
                    ReceiptUpdateType.ADDED, 
                    ReceiptUpdateType.UPDATED, 
                    ReceiptUpdateType.DELETED,
                    ReceiptUpdateType.REFRESH_ALL -> {
                        // Refresh home data when receipts are updated
                        loadHomeDataSilently()
                    }
                    ReceiptUpdateType.ERROR -> {
                        // Handle error if needed
                    }
                }
            }
        }
        
        viewModelScope.launch {
            syncService.analyticsUpdates.collect { update ->
                if (update.error == null) {
                    // Update home data with new analytics
                    update.spendingAnalytics?.let { analytics ->
                        val homeData = HomeScreenData(
                            recentReceipts = update.recentReceipts.map {
                                ReceiptItem(
                                    id = it.id,
                                    storeName = it.merchantName,
                                    amount = it.totalAmount,
                                    date = it.date,
                                    category = it.category.displayName
                                )
                            },
                            monthlySpending = analytics.totalSpending,
                            spendingChange = analytics.spendingChange,
                            quickActions = listOf()
                        )
                        _homeDataState.value = UiState.Success(homeData)
                    }
                }
            }
        }
    }
    
    /**
     * Load home data silently without showing loading state
     */
    private fun loadHomeDataSilently() {
        viewModelScope.launch {
            try {
                val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                
                // Get spending analytics
                val spendingAnalytics = analyticsService.getSpendingAnalytics(userId).first()
                
                // Get recent receipts
                val recentReceiptItems = analyticsService.getRecentReceipts(userId, 10).first()
                
                val homeData = HomeScreenData(
                    recentReceipts = recentReceiptItems.map {
                        ReceiptItem(
                            id = it.id, 
                            storeName = it.merchantName, 
                            amount = it.totalAmount, 
                            date = it.date, 
                            category = it.category.displayName
                        )
                    },
                    monthlySpending = spendingAnalytics.totalSpending,
                    spendingChange = spendingAnalytics.spendingChange,
                    quickActions = listOf()
                )
                
                _homeDataState.value = UiState.Success(homeData)
            } catch (e: Exception) {
                // Keep current state on error during silent refresh
            }
        }
    }
}
