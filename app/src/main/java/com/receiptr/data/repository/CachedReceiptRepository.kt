package com.receiptr.data.repository

import android.util.Log
import com.receiptr.data.cache.*
import com.receiptr.domain.model.Receipt
import com.receiptr.domain.repository.ReceiptRepository
import com.receiptr.data.local.ReceiptDao
import com.receiptr.data.local.toDomain
import com.receiptr.data.local.toEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced Receipt Repository implementing Stale-While-Revalidate caching strategy
 * 
 * This repository provides:
 * 1. Immediate response from cache when available
 * 2. Background refresh for stale data
 * 3. Graceful error handling with fallback to cache
 * 4. Network simulation for demonstration
 */
@Singleton
class CachedReceiptRepository @Inject constructor(
    private val receiptDao: ReceiptDao,
    private val cachingService: CachingService,
    private val originalRepository: ReceiptRepository // Fallback to original implementation
) {
    companion object {
        private const val TAG = "CachedReceiptRepository"
    }

    /**
     * Get all receipts with Stale-While-Revalidate caching
     * 
     * Flow of operations:
     * 1. Check cache metadata to determine freshness
     * 2. Load from local Room database immediately if cache exists
     * 3. If cache is stale or missing, trigger background "network" fetch
     * 4. Simulate network delay and potential failures
     * 5. Update cache and emit fresh data when network call completes
     * 6. Handle errors gracefully by keeping stale cache data visible
     */
    fun getAllReceiptsWithCaching(userId: String): Flow<CacheResult<List<Receipt>>> {
        return cachingService.staleWhileRevalidate(
            cacheKey = "${CacheMetadata.RECEIPTS_CACHE_KEY}_$userId",
            
            // Step 1: Fetch from local cache (Room database)
            fetchFromCache = {
                Log.d(TAG, "Fetching receipts from local cache for user: $userId")
                try {
                    // Get receipts from Room database
                    val receiptEntities = receiptDao.getAllReceiptsForUserSync(userId)
                    val receipts = receiptEntities.map { it.toDomain() }
                    Log.d(TAG, "Found ${receipts.size} receipts in cache")
                    receipts
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching from cache", e)
                    null
                }
            },
            
            // Step 2: Simulate network fetch with realistic behavior
            fetchFromNetwork = {
                Log.d(TAG, "Simulating network fetch for receipts")
                simulateNetworkFetchReceipts(userId)
            },
            
            // Step 3: Save fresh data to local cache
            saveToCache = { freshReceipts ->
                Log.d(TAG, "Saving ${freshReceipts.size} receipts to cache")
                try {
                    // Convert to entities and save to Room
                    val entities = freshReceipts.map { it.toEntity() }
                    receiptDao.deleteAllReceiptsForUser(userId) // Clear old data
                    receiptDao.insertReceipts(entities) // Insert fresh data
                    Log.d(TAG, "Successfully saved receipts to cache")
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving to cache", e)
                    throw e
                }
            }
        )
    }

    /**
     * Force refresh receipts (pull-to-refresh functionality)
     */
    fun forceRefreshReceipts(userId: String): Flow<CacheResult<List<Receipt>>> {
        return cachingService.forceRefresh(
            cacheKey = "${CacheMetadata.RECEIPTS_CACHE_KEY}_$userId",
            fetchFromNetwork = {
                Log.d(TAG, "Force refreshing receipts from network")
                simulateNetworkFetchReceipts(userId)
            },
            saveToCache = { freshReceipts ->
                val entities = freshReceipts.map { it.toEntity() }
                receiptDao.deleteAllReceiptsForUser(userId)
                receiptDao.insertReceipts(entities)
            }
        )
    }

    /**
     * Simulate network fetch with realistic delays and occasional failures
     * In a real app, this would call your actual API
     */
    private suspend fun simulateNetworkFetchReceipts(userId: String): List<Receipt> {
        // Simulate network delay (1-3 seconds)
        val networkDelay = (1000..3000).random()
        delay(networkDelay.toLong())
        
        // Simulate occasional network failures (10% chance)
        if ((1..10).random() == 1) {
            throw Exception("Simulated network error - server temporarily unavailable")
        }
        
        // Generate sample fresh receipts with current timestamp
        val currentTime = System.currentTimeMillis()
        
        return listOf(
            Receipt(
                id = "cached_receipt_1",
                userId = userId,
                merchantName = "Fresh Market Co.",
                totalAmount = 89.99,
                currency = "USD",
                date = currentTime - 3600000, // 1 hour ago
                category = "Groceries",
                description = "Weekly grocery shopping",
                notes = "Fresh data from network - ${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date())}",
                createdAt = currentTime,
                updatedAt = currentTime,
                isProcessed = true
            ),
            Receipt(
                id = "cached_receipt_2",
                userId = userId,
                merchantName = "Coffee Bean Central",
                totalAmount = 12.50,
                currency = "USD",
                date = currentTime - 1800000, // 30 minutes ago
                category = "Food & Dining",
                description = "Morning coffee",
                notes = "Network fetch at ${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date())}",
                createdAt = currentTime,
                updatedAt = currentTime,
                isProcessed = true
            ),
            Receipt(
                id = "cached_receipt_3",
                userId = userId,
                merchantName = "Tech Store Plus",
                totalAmount = 299.99,
                currency = "USD",
                date = currentTime - 7200000, // 2 hours ago
                category = "Electronics",
                description = "Wireless headphones",
                notes = "Latest network data - ${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date())}",
                createdAt = currentTime,
                updatedAt = currentTime,
                isProcessed = true
            )
        )
    }

    /**
     * Invalidate receipts cache (useful after adding/editing receipts)
     */
    suspend fun invalidateReceiptsCache(userId: String) {
        cachingService.invalidateCache("${CacheMetadata.RECEIPTS_CACHE_KEY}_$userId")
    }

    /**
     * Fallback methods that delegate to original repository
     * These maintain compatibility with existing code
     */
    suspend fun saveReceipt(receipt: Receipt): Result<String> {
        val result = originalRepository.saveReceipt(receipt)
        if (result.isSuccess) {
            // Invalidate cache when new receipt is added
            invalidateReceiptsCache(receipt.userId)
        }
        return result
    }

    suspend fun deleteReceipt(id: String): Result<Unit> {
        // Get receipt to know which user's cache to invalidate
        val receiptResult = originalRepository.getReceipt(id)
        val result = originalRepository.deleteReceipt(id)
        
        if (result.isSuccess && receiptResult.isSuccess) {
            receiptResult.getOrNull()?.let { receipt ->
                invalidateReceiptsCache(receipt.userId)
            }
        }
        return result
    }

    suspend fun getReceipt(id: String): Result<Receipt> {
        return originalRepository.getReceipt(id)
    }

    suspend fun updateReceipt(receipt: Receipt): Result<Unit> {
        val result = originalRepository.updateReceipt(receipt)
        if (result.isSuccess) {
            invalidateReceiptsCache(receipt.userId)
        }
        return result
    }
}
