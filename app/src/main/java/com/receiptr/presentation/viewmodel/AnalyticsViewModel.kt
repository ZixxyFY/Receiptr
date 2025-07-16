package com.receiptr.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptr.domain.model.UiState
import com.receiptr.data.analytics.ReceiptAnalyticsService
import com.receiptr.data.analytics.SpendingAnalytics
import com.receiptr.data.analytics.MonthlySpending
import com.receiptr.data.analytics.CategorySpending
import com.receiptr.data.sync.ReceiptSyncService
import com.receiptr.data.sync.ReceiptUpdateType
import com.receiptr.data.ml.enhanced.ReceiptCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsScreenData(
    val spendingAnalytics: SpendingAnalytics,
    val monthlyTrends: List<MonthlySpending>,
    val categoryBreakdown: List<CategorySpending>,
    val timePeriod: TimePeriod = TimePeriod.MONTHLY
)

enum class TimePeriod(val displayName: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"), 
    YEARLY("Yearly")
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsService: ReceiptAnalyticsService,
    private val syncService: ReceiptSyncService
) : ViewModel() {
    
    private val _analyticsState = MutableStateFlow<UiState<AnalyticsScreenData>>(UiState.Idle)
    val analyticsState: StateFlow<UiState<AnalyticsScreenData>> = _analyticsState.asStateFlow()
    
    private val _selectedPeriod = MutableStateFlow(TimePeriod.MONTHLY)
    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()
    
    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()
    
    init {
        loadAnalyticsData()
        observeSyncUpdates()
    }
    
    fun selectTimePeriod(period: TimePeriod) {
        _selectedPeriod.value = period
        loadAnalyticsData()
    }
    
    fun refreshAnalytics() {
        loadAnalyticsData()
    }
    
    private fun loadAnalyticsData() {
        viewModelScope.launch {
            _analyticsState.value = UiState.Loading
            
            try {
                val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                
                // Get spending analytics
                val spendingAnalytics = analyticsService.getSpendingAnalytics(userId).first()
                
                // Get monthly trends based on selected period
                val monthsToFetch = when (_selectedPeriod.value) {
                    TimePeriod.WEEKLY -> 2  // Show 2 months for weekly view
                    TimePeriod.MONTHLY -> 12 // Show 12 months for monthly view
                    TimePeriod.YEARLY -> 36  // Show 3 years for yearly view
                }
                
                val monthlyTrends = analyticsService.getSpendingTrends(userId, monthsToFetch).first()
                
                // Convert category breakdown to list
                val categoryBreakdown = spendingAnalytics.topCategories
                
                val analyticsData = AnalyticsScreenData(
                    spendingAnalytics = spendingAnalytics,
                    monthlyTrends = monthlyTrends,
                    categoryBreakdown = categoryBreakdown,
                    timePeriod = _selectedPeriod.value
                )
                
                _analyticsState.value = UiState.Success(analyticsData)
                
            } catch (e: Exception) {
                _analyticsState.value = UiState.Error(
                    message = "Failed to load analytics: ${e.message}",
                    throwable = e
                )
            }
        }
    }
    
    private fun loadAnalyticsDataSilently() {
        viewModelScope.launch {
            try {
                val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                
                val spendingAnalytics = analyticsService.getSpendingAnalytics(userId).first()
                val monthsToFetch = when (_selectedPeriod.value) {
                    TimePeriod.WEEKLY -> 2
                    TimePeriod.MONTHLY -> 12
                    TimePeriod.YEARLY -> 36
                }
                
                val monthlyTrends = analyticsService.getSpendingTrends(userId, monthsToFetch).first()
                val categoryBreakdown = spendingAnalytics.topCategories
                
                val analyticsData = AnalyticsScreenData(
                    spendingAnalytics = spendingAnalytics,
                    monthlyTrends = monthlyTrends,
                    categoryBreakdown = categoryBreakdown,
                    timePeriod = _selectedPeriod.value
                )
                
                _analyticsState.value = UiState.Success(analyticsData)
                
            } catch (e: Exception) {
                // Keep current state on error during silent refresh
            }
        }
    }
    
    /**
     * Observe sync service updates for real-time analytics refresh
     */
    private fun observeSyncUpdates() {
        viewModelScope.launch {
            syncService.analyticsUpdates.collect { update ->
                if (update.error == null) {
                    // Update analytics data with new information
                    update.spendingAnalytics?.let { analytics ->
                        val analyticsData = AnalyticsScreenData(
                            spendingAnalytics = analytics,
                            monthlyTrends = update.spendingTrends,
                            categoryBreakdown = analytics.topCategories,
                            timePeriod = _selectedPeriod.value
                        )
                        _analyticsState.value = UiState.Success(analyticsData)
                    }
                }
            }
        }
        
        viewModelScope.launch {
            syncService.receiptUpdates.collect { update ->
                when (update.type) {
                    ReceiptUpdateType.ADDED,
                    ReceiptUpdateType.UPDATED,
                    ReceiptUpdateType.DELETED,
                    ReceiptUpdateType.REFRESH_ALL -> {
                        // Refresh analytics when receipts are updated
                        loadAnalyticsDataSilently()
                    }
                    ReceiptUpdateType.ERROR -> {
                        // Handle error if needed
                    }
                }
            }
        }
    }
    
    /**
     * Get formatted spending amount
     */
    fun getFormattedAmount(amount: Double): String {
        return "$${String.format("%.2f", amount)}"
    }
    
    /**
     * Get category color
     */
    fun getCategoryColor(category: ReceiptCategory): String {
        return category.color
    }
}
