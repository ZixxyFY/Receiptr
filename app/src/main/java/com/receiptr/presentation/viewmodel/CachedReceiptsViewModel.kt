package com.receiptr.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptr.data.cache.*
import com.receiptr.data.repository.CachedReceiptRepository
import com.receiptr.domain.model.Receipt
import com.receiptr.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Enhanced ReceiptsViewModel with Stale-While-Revalidate caching support
 * 
 * This ViewModel demonstrates the complete caching strategy:
 * 1. Immediate UI updates from cache
 * 2. Background refresh for stale data
 * 3. Loading states and error handling
 * 4. Pull-to-refresh functionality
 * 5. Cache invalidation on data changes
 */
data class CachedReceiptsUiState(
    val receipts: List<Receipt> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val cacheState: CacheState = CacheState.MISSING,
    val lastUpdated: Long? = null,
    val dataSource: CacheSource = CacheSource.CACHE
)

@HiltViewModel
class CachedReceiptsViewModel @Inject constructor(
    private val cachedReceiptRepository: CachedReceiptRepository,
    private val authRepository: AuthRepository,
    private val cachingService: CachingService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CachedReceiptsUiState())
    val uiState: StateFlow<CachedReceiptsUiState> = _uiState.asStateFlow()

    init {
        // Clean up expired cache on start
        viewModelScope.launch {
            cachingService.cleanupExpiredCache()
        }
        loadReceipts()
    }

    /**
     * Load receipts with Stale-While-Revalidate caching
     * 
     * This method demonstrates the complete flow:
     * 1. Shows loading state only if no cache exists
     * 2. Immediately displays cached data if available (even if stale)
     * 3. Triggers background refresh for stale data
     * 4. Updates UI when fresh data arrives
     * 5. Handles errors gracefully
     */
    private fun loadReceipts() {
        viewModelScope.launch {
            try {
                // Get current user
                val currentUser = authRepository.getCurrentUser().first()
                val userId = currentUser?.id ?: ""
                
                if (userId.isEmpty()) {
                    _uiState.update { 
                        it.copy(
                            error = "User not authenticated",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                // Start caching flow
                cachedReceiptRepository.getAllReceiptsWithCaching(userId)
                    .collect { cacheResult ->
                        handleCacheResult(cacheResult)
                    }
                    
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message ?: "Unknown error occurred",
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
        }
    }

    /**
     * Handle cache results and update UI accordingly
     * 
     * This method shows how to properly handle different cache states:
     * - MISSING: Show loading spinner
     * - FRESH/STALE: Show data immediately
     * - Background refresh: Show subtle refresh indicator
     */
    private fun handleCacheResult(cacheResult: CacheResult<List<Receipt>>) {
        val currentState = _uiState.value
        
        when (cacheResult.state) {
            CacheState.MISSING -> {
                // No cached data - show loading state
                _uiState.update { 
                    it.copy(
                        isLoading = true,
                        error = null,
                        cacheState = cacheResult.state,
                        dataSource = cacheResult.source
                    )
                }
            }
            
            CacheState.FRESH, CacheState.STALE -> {
                // We have data (fresh or stale) - show it immediately
                val receipts = cacheResult.data ?: emptyList()
                _uiState.update { 
                    it.copy(
                        receipts = receipts,
                        isLoading = false,
                        error = null,
                        cacheState = cacheResult.state,
                        lastUpdated = cacheResult.lastUpdated,
                        dataSource = cacheResult.source,
                        // Show subtle refresh indicator for background updates
                        isRefreshing = cacheResult.source == CacheSource.NETWORK && 
                                     currentState.receipts.isNotEmpty()
                    )
                }
            }
            
            CacheState.EXPIRED -> {
                // Expired data - treat as missing
                _uiState.update { 
                    it.copy(
                        isLoading = true,
                        cacheState = cacheResult.state,
                        dataSource = cacheResult.source
                    )
                }
            }
        }
        
        // If this is fresh network data, stop all loading indicators
        if (cacheResult.source == CacheSource.NETWORK || cacheResult.source == CacheSource.BOTH) {
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    isRefreshing = false
                )
            }
        }
    }

    /**
     * Force refresh receipts (pull-to-refresh)
     * 
     * This method shows how to implement pull-to-refresh:
     * 1. Show refresh indicator
     * 2. Force cache invalidation
     * 3. Fetch fresh data from "network"
     * 4. Update UI and hide refresh indicator
     */
    fun refreshReceipts() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser().first()
                val userId = currentUser?.id ?: return@launch
                
                _uiState.update { it.copy(isRefreshing = true, error = null) }
                
                cachedReceiptRepository.forceRefreshReceipts(userId)
                    .collect { cacheResult ->
                        handleCacheResult(cacheResult)
                    }
                    
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message ?: "Refresh failed",
                        isRefreshing = false
                    )
                }
            }
        }
    }

    /**
     * Delete receipt and invalidate cache
     */
    fun deleteReceipt(receiptId: String) {
        viewModelScope.launch {
            try {
                val result = cachedReceiptRepository.deleteReceipt(receiptId)
                if (result.isFailure) {
                    _uiState.update { 
                        it.copy(error = result.exceptionOrNull()?.message ?: "Failed to delete receipt")
                    }
                }
                // Cache invalidation is handled automatically in the repository
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "Failed to delete receipt")
                }
            }
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Get cache status for debugging/display
     */
    fun getCacheStatusText(): String {
        val state = _uiState.value
        return when (state.cacheState) {
            CacheState.FRESH -> "✅ Data is fresh"
            CacheState.STALE -> "⚡ Updating in background..."
            CacheState.EXPIRED -> "❌ Data expired, refreshing..."
            CacheState.MISSING -> "🔄 Loading..."
        } + " • Source: ${state.dataSource.name.lowercase()}"
    }

    /**
     * Manually invalidate cache (for testing)
     */
    fun invalidateCache() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser().first()
                val userId = currentUser?.id ?: return@launch
                cachedReceiptRepository.invalidateReceiptsCache(userId)
            } catch (e: Exception) {
                // Handle error silently for this debug function
            }
        }
    }
}
