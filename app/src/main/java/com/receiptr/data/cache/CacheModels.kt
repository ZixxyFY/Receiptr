package com.receiptr.data.cache

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.concurrent.TimeUnit

/**
 * Represents cache metadata for tracking data freshness
 * Used in Stale-While-Revalidate caching strategy
 */
@Entity(tableName = "cache_metadata")
data class CacheMetadata(
    @PrimaryKey
    val key: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val expirationTime: Long = System.currentTimeMillis() + DEFAULT_CACHE_DURATION,
    val version: Int = 1
) {
    companion object {
        // Default cache duration: 5 minutes
        const val DEFAULT_CACHE_DURATION = 5 * 60 * 1000L
        
        // Cache keys for different data types
        const val RECEIPTS_CACHE_KEY = "receipts_cache"
        const val USER_PROFILE_CACHE_KEY = "user_profile_cache"
        const val ANALYTICS_CACHE_KEY = "analytics_cache"
    }
    
    /**
     * Determines if the cached data is considered stale
     * Stale data can still be shown to user while fresh data is fetched
     */
    fun isStale(): Boolean {
        return System.currentTimeMillis() > expirationTime
    }
    
    /**
     * Determines if the cached data is expired and should not be shown
     * This is for cases where data is too old to be useful
     */
    fun isExpired(): Boolean {
        val maxAge = 24 * 60 * 60 * 1000L // 24 hours
        return System.currentTimeMillis() > (lastUpdated + maxAge)
    }
    
    /**
     * Creates a fresh cache metadata entry
     */
    fun refresh(): CacheMetadata {
        return copy(
            lastUpdated = System.currentTimeMillis(),
            expirationTime = System.currentTimeMillis() + DEFAULT_CACHE_DURATION,
            version = version + 1
        )
    }
}

/**
 * Enum representing different cache states
 */
enum class CacheState {
    FRESH,      // Data is fresh and up-to-date
    STALE,      // Data is stale but still usable while revalidating
    EXPIRED,    // Data is too old and should not be used
    MISSING     // No cached data exists
}

/**
 * Data class representing the result of a cache operation
 */
data class CacheResult<out T>(
    val data: T?,
    val state: CacheState,
    val lastUpdated: Long? = null,
    val source: CacheSource
)

/**
 * Enum representing the source of data
 */
enum class CacheSource {
    CACHE,      // Data came from local cache
    NETWORK,    // Data came from network
    BOTH        // Data came from both (cache first, then network)
}
