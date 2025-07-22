package com.receiptr.data.cache

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service implementing Stale-While-Revalidate caching strategy
 * 
 * How it works:
 * 1. Check if cached data exists and is not expired
 * 2. If cached data exists, return it immediately (even if stale)
 * 3. If cached data is stale or missing, trigger background network fetch
 * 4. When network data arrives, update cache and emit fresh data
 * 5. Handle errors gracefully by falling back to stale cache when possible
 */
@Singleton
class CachingService @Inject constructor(
    private val cacheDao: CacheDao
) {
    companion object {
        private const val TAG = "CachingService"
    }
    
    /**
     * Generic Stale-While-Revalidate implementation
     * 
     * @param cacheKey Unique key for this cached data
     * @param fetchFromCache Function to retrieve data from local cache
     * @param fetchFromNetwork Suspend function to fetch fresh data from network
     * @param saveToCache Function to save fresh data to local cache
     * @return Flow that emits cached data first, then network data when available
     */
    fun <T> staleWhileRevalidate(
        cacheKey: String,
        fetchFromCache: suspend () -> T?,
        fetchFromNetwork: suspend () -> T,
        saveToCache: suspend (T) -> Unit
    ): Flow<out CacheResult<T>> = flow {
        Log.d(TAG, "Starting stale-while-revalidate for key: $cacheKey")
        
        // Step 1: Check cache metadata to understand cache state
        val cacheMetadata = cacheDao.getCacheMetadata(cacheKey)
        val cacheState = determineCacheState(cacheMetadata)
        
        Log.d(TAG, "Cache state for $cacheKey: $cacheState")
        
        // Step 2: Try to load from cache first (immediate response)
        val cachedData = if (cacheState != CacheState.EXPIRED && cacheState != CacheState.MISSING) {
            try {
                fetchFromCache()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching from cache for $cacheKey", e)
                null
            }
        } else {
            null
        }
        
        // Step 3: Emit cached data immediately if available
        if (cachedData != null) {
            Log.d(TAG, "Emitting cached data for $cacheKey")
            emit(CacheResult(
                data = cachedData,
                state = cacheState,
                lastUpdated = cacheMetadata?.lastUpdated,
                source = CacheSource.CACHE
            ))
        }
        
        // Step 4: Determine if we need to fetch from network
        val shouldFetchFromNetwork = cacheState == CacheState.MISSING || 
                                   cacheState == CacheState.STALE || 
                                   cacheState == CacheState.EXPIRED ||
                                   cachedData == null
        
        // Step 5: Fetch from network in background if needed
        if (shouldFetchFromNetwork) {
            Log.d(TAG, "Fetching fresh data from network for $cacheKey")
            
            try {
                // Fetch fresh data from network
                val freshData = fetchFromNetwork()
                Log.d(TAG, "Successfully fetched fresh data for $cacheKey")
                
                // Save to cache
                saveToCache(freshData)
                
                // Update cache metadata
                val newMetadata = CacheMetadata(key = cacheKey).refresh()
                cacheDao.insertCacheMetadata(newMetadata)
                
                // Emit fresh data
                emit(CacheResult(
                    data = freshData,
                    state = CacheState.FRESH,
                    lastUpdated = newMetadata.lastUpdated,
                    source = if (cachedData != null) CacheSource.BOTH else CacheSource.NETWORK
                ))
                
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching from network for $cacheKey", e)
                
                // If we have cached data, don't emit error, just log it
                // If we don't have cached data, emit error state
                if (cachedData == null) {
                    emit(CacheResult(
                        data = null,
                        state = CacheState.MISSING,
                        lastUpdated = null,
                        source = CacheSource.NETWORK
                    ))
                }
            }
        }
    }.distinctUntilChanged { old, new ->
        // Only emit if data actually changed
        old.data == new.data && old.state == new.state
    }
    
    /**
     * Determines the current state of cached data
     */
    private fun determineCacheState(metadata: CacheMetadata?): CacheState {
        return when {
            metadata == null -> CacheState.MISSING
            metadata.isExpired() -> CacheState.EXPIRED
            metadata.isStale() -> CacheState.STALE
            else -> CacheState.FRESH
        }
    }
    
    /**
     * Invalidates cache for a specific key
     * This will force a fresh network fetch on next access
     */
    suspend fun invalidateCache(cacheKey: String) {
        Log.d(TAG, "Invalidating cache for key: $cacheKey")
        cacheDao.deleteCacheMetadata(cacheKey)
    }
    
    /**
     * Clears all expired cache entries
     * Should be called periodically (e.g., on app start)
     */
    suspend fun cleanupExpiredCache() {
        Log.d(TAG, "Cleaning up expired cache entries")
        val expiredBefore = System.currentTimeMillis() - (24 * 60 * 60 * 1000L) // 24 hours ago
        cacheDao.deleteExpiredCacheEntries(expiredBefore)
    }
    
    /**
     * Force refresh - invalidates cache and fetches fresh data
     */
    fun <T> forceRefresh(
        cacheKey: String,
        fetchFromNetwork: suspend () -> T,
        saveToCache: suspend (T) -> Unit
    ): Flow<out CacheResult<T>> = flow {
        // Invalidate existing cache
        invalidateCache(cacheKey)
        
        // Emit loading state
        emit(CacheResult(
            data = null,
            state = CacheState.MISSING,
            lastUpdated = null,
            source = CacheSource.NETWORK
        ))
        
        try {
            // Fetch fresh data
            val freshData = fetchFromNetwork()
            
            // Save to cache
            saveToCache(freshData)
            
            // Update metadata
            val newMetadata = CacheMetadata(key = cacheKey).refresh()
            cacheDao.insertCacheMetadata(newMetadata)
            
            // Emit fresh data
            emit(CacheResult(
                data = freshData,
                state = CacheState.FRESH,
                lastUpdated = newMetadata.lastUpdated,
                source = CacheSource.NETWORK
            ))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in force refresh for $cacheKey", e)
            emit(CacheResult(
                data = null,
                state = CacheState.MISSING,
                lastUpdated = null,
                source = CacheSource.NETWORK
            ))
        }
    }
}
