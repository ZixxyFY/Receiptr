package com.receiptr.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.receiptr.domain.model.Receipt
import com.receiptr.domain.model.ReceiptItem

@Entity(tableName = "receipts")
@TypeConverters(Converters::class)
data class ReceiptEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val photoPath: String,
    val merchantName: String,
    val totalAmount: Double,
    val currency: String,
    val date: Long,
    val category: String,
    val description: String,
    val items: List<ReceiptItem>,
    val tags: List<String>,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isProcessed: Boolean,
    val ocrText: String
)

class Converters {
    @TypeConverter
    fun fromReceiptItemList(value: List<ReceiptItem>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toReceiptItemList(value: String): List<ReceiptItem> {
        val listType = object : TypeToken<List<ReceiptItem>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }
}

// Extension functions for conversion
fun Receipt.toEntity(): ReceiptEntity {
    return ReceiptEntity(
        id = this.id,
        userId = this.userId,
        photoPath = this.photoPath,
        merchantName = this.merchantName,
        totalAmount = this.totalAmount,
        currency = this.currency,
        date = this.date,
        category = this.category,
        description = this.description,
        items = this.items,
        tags = this.tags,
        notes = this.notes,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        isProcessed = this.isProcessed,
        ocrText = this.ocrText
    )
}

fun ReceiptEntity.toDomain(): Receipt {
    return Receipt(
        id = this.id,
        userId = this.userId,
        photoPath = this.photoPath,
        merchantName = this.merchantName,
        totalAmount = this.totalAmount,
        currency = this.currency,
        date = this.date,
        category = this.category,
        description = this.description,
        items = this.items,
        tags = this.tags,
        notes = this.notes,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        isProcessed = this.isProcessed,
        ocrText = this.ocrText
    )
}
