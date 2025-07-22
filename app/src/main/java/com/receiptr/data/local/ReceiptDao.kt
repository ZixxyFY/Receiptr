package com.receiptr.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {
    @Query("SELECT * FROM receipts WHERE userId = :userId ORDER BY date DESC")
    fun getAllReceiptsForUser(userId: String): Flow<List<ReceiptEntity>>
    
    @Query("SELECT * FROM receipts WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllReceiptsForUserSync(userId: String): List<ReceiptEntity>
    
    @Query("SELECT * FROM receipts WHERE id = :id")
    suspend fun getReceiptById(id: String): ReceiptEntity?
    
    
    @Insert
    suspend fun insertReceipts(receipts: List<ReceiptEntity>)
    
    @Query("DELETE FROM receipts WHERE userId = :userId")
    suspend fun deleteAllReceiptsForUser(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity)

    @Update
    suspend fun updateReceipt(receipt: ReceiptEntity)

    @Delete
    suspend fun deleteReceipt(receipt: ReceiptEntity)

    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun deleteReceiptById(id: String)

    @Query("SELECT * FROM receipts WHERE userId = :userId AND (merchantName LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%')")
    fun searchReceipts(userId: String, query: String): Flow<List<ReceiptEntity>>

    @Query("SELECT COUNT(*) FROM receipts WHERE userId = :userId")
    suspend fun getReceiptCount(userId: String): Int
}
