package com.receiptr.data.repository

import com.receiptr.data.local.ReceiptDao
import com.receiptr.data.local.ReceiptEntity
import com.receiptr.data.local.toDomain
import com.receiptr.data.local.toEntity
import com.receiptr.domain.model.Receipt
import org.junit.Test
import org.junit.Assert.*

class ReceiptRepositoryTest {

    @Test
    fun `Receipt entity conversion works correctly`() {
        // Given
        val receipt = Receipt(
            id = "test-id",
            userId = "user-123",
            merchantName = "Test Store",
            totalAmount = 25.50,
            currency = "USD",
            category = "Food"
        )
        
        // When
        val entity = receipt.toEntity()
        val convertedBack = entity.toDomain()
        
        // Then
        assertEquals(receipt.id, convertedBack.id)
        assertEquals(receipt.userId, convertedBack.userId)
        assertEquals(receipt.merchantName, convertedBack.merchantName)
        assertEquals(receipt.totalAmount, convertedBack.totalAmount, 0.01)
        assertEquals(receipt.currency, convertedBack.currency)
        assertEquals(receipt.category, convertedBack.category)
    }

}
