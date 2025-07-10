package com.receiptr.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptr.domain.model.UiState
import com.receiptr.presentation.home.ReceiptItem
import com.receiptr.presentation.home.QuickAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class HomeScreenData(
    val recentReceipts: List<ReceiptItem>,
    val monthlySpending: Double,
    val spendingChange: String,
    val quickActions: List<QuickAction>
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    
    private val _homeDataState = MutableStateFlow<UiState<HomeScreenData>>(UiState.Idle)
    val homeDataState: StateFlow<UiState<HomeScreenData>> = _homeDataState.asStateFlow()
    
    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()
    
    init {
        loadHomeData()
    }
    
    fun loadHomeData() {
        viewModelScope.launch {
            _homeDataState.value = UiState.Loading
            
            try {
                // Simulate network delay
                delay(1500) // 1.5 second delay to show skeleton
                
                // Generate sample data (replace with actual repository calls)
                val homeData = generateSampleHomeData()
                
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
                
                val homeData = generateSampleHomeData()
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
    
    private fun generateSampleHomeData(): HomeScreenData {
        val recentReceipts = listOf(
            ReceiptItem("1", "Starbucks", 12.50, Date(), "Food & Dining"),
            ReceiptItem("2", "Amazon", 89.99, Date(), "Shopping"),
            ReceiptItem("3", "Shell Gas", 45.20, Date(), "Transportation"),
            ReceiptItem("4", "Grocery Store", 156.78, Calendar.getInstance().apply { 
                add(Calendar.DAY_OF_MONTH, -1) 
            }.time, "Groceries"),
            ReceiptItem("5", "Netflix", 15.99, Calendar.getInstance().apply { 
                add(Calendar.DAY_OF_MONTH, -2) 
            }.time, "Entertainment")
        )
        
        return HomeScreenData(
            recentReceipts = recentReceipts,
            monthlySpending = recentReceipts.sumOf { it.amount },
            spendingChange = "+12% from last month",
            quickActions = emptyList() // Quick actions will be handled in the UI
        )
    }
}
