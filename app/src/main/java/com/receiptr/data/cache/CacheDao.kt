package com.receiptr.data.cache

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for cache metadata operations
 * Supports the Stale-While-Revalidate caching strategy
 */
@Dao
interface CacheDao {
    
    /**
     * Gets cache metadata for a specific key
     * Used to determine if cached data is fresh, stale, or expired
     */
    @Query("SELECT * FROM cache_metadata WHERE key = :key")
    suspend fun getCacheMetadata(key: String): CacheMetadata?
    
    /**
     * Gets cache metadata as Flow for reactive updates
     */
    @Query("SELECT * FROM cache_metadata WHERE key = :key")
    fun getCacheMetadataFlow(key: String): Flow<CacheMetadata?>
    
    /**
     * Inserts or updates cache metadata
     * Called when data is fetched from network and cached
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCacheMetadata(metadata: CacheMetadata)
    
    /**
     * Updates cache metadata (marks as refreshed)
     */
    @Update
    suspend fun updateCacheMetadata(metadata: CacheMetadata)
    
    /**
     * Deletes cache metadata for a specific key
     * Used when cache is invalidated
     */
    @Query("DELETE FROM cache_metadata WHERE key = :key")
    suspend fun deleteCacheMetadata(key: String)
    
    /**
     * Gets all expired cache entries for cleanup
     */
    @Query("SELECT * FROM cache_metadata WHERE lastUpdated < :expiredBefore")
    suspend fun getExpiredCacheEntries(expiredBefore: Long): List<CacheMetadata>
    
    /**
     * Deletes all expired cache entries
     * Should be called periodically for cleanup
     */
    @Query("DELETE FROM cache_metadata WHERE lastUpdated < :expiredBefore")
    suspend fun deleteExpiredCacheEntries(expiredBefore: Long)
    
    /**
     * Gets all cache metadata entries
     * Useful for debugging and cache management
     */
    @Query("SELECT * FROM cache_metadata ORDER BY lastUpdated DESC")
    suspend fun getAllCacheMetadata(): List<CacheMetadata>
    
    /**
     * Clears all cache metadata
     * Nuclear option for cache reset
     */
    @Query("DELETE FROM cache_metadata")
    suspend fun clearAllCacheMetadata()
}
