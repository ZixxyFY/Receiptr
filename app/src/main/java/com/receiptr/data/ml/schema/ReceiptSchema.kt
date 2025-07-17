package com.receiptr.data.ml.schema

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * Formal receipt data schema for ML training and validation
 */
@Entity(tableName = "receipts_ml_schema")
@TypeConverters(ReceiptSchemaConverters::class)
data class ReceiptSchema(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val merchantName: String?,
    val merchantAddress: String?,
    val phoneNumber: String?,
    val transactionDate: Date?,
    val transactionTime: String?,
    val totalAmount: Float?,
    val subtotalAmount: Float?,
    val taxAmount: Float?,
    val tipAmount: Float?,
    val discountAmount: Float?,
    val lineItems: List<LineItem>,
    val category: ReceiptCategory,
    val paymentMethod: PaymentMethod?,
    val currency: String = "USD",
    val confidence: Float = 0.0f,
    val isManuallyVerified: Boolean = false,
    val annotationNotes: String?,
    val rawText: String,
    val imageUri: String?,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

/**
 * Line item schema with detailed structure
 */
data class LineItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String?,
    val quantity: Float = 1.0f,
    val unitPrice: Float?,
    val totalPrice: Float,
    val category: String?,
    val sku: String?,
    val barcode: String?,
    val discount: Float?,
    val tax: Float?,
    val confidence: Float = 0.0f,
    val boundingBox: BoundingBox? = null
)

/**
 * Bounding box for spatial information
 */
data class BoundingBox(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

/**
 * Receipt categories for classification
 */
enum class ReceiptCategory {
    GROCERIES,
    DINING,
    TRANSPORTATION,
    ELECTRONICS,
    CLOTHING,
    HEALTHCARE,
    ENTERTAINMENT,
    HOME_GARDEN,
    AUTOMOTIVE,
    BUSINESS,
    TRAVEL,
    EDUCATION,
    UTILITIES,
    RETAIL,
    SERVICES,
    OTHER
}

/**
 * Payment method types
 */
enum class PaymentMethod {
    CASH,
    CREDIT_CARD,
    DEBIT_CARD,
    MOBILE_PAYMENT,
    GIFT_CARD,
    CHECK,
    DIGITAL_WALLET,
    CRYPTOCURRENCY,
    OTHER
}

/**
 * Validation result for receipt data
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError>,
    val warnings: List<ValidationWarning>,
    val confidence: Float
)

/**
 * Validation error types
 */
data class ValidationError(
    val field: String,
    val message: String,
    val severity: ErrorSeverity
)

/**
 * Validation warning types
 */
data class ValidationWarning(
    val field: String,
    val message: String,
    val suggestion: String?
)

/**
 * Error severity levels
 */
enum class ErrorSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Type converters for Room database
 */
class ReceiptSchemaConverters {
    
    @TypeConverter
    fun fromLineItemList(value: List<LineItem>): String {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toLineItemList(value: String): List<LineItem> {
        val listType = object : TypeToken<List<LineItem>>() {}.type
        return Gson().fromJson(value, listType)
    }
    
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
    
    @TypeConverter
    fun fromReceiptCategory(category: ReceiptCategory): String {
        return category.name
    }
    
    @TypeConverter
    fun toReceiptCategory(category: String): ReceiptCategory {
        return ReceiptCategory.valueOf(category)
    }
    
    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod?): String? {
        return method?.name
    }
    
    @TypeConverter
    fun toPaymentMethod(method: String?): PaymentMethod? {
        return method?.let { PaymentMethod.valueOf(it) }
    }
    
    @TypeConverter
    fun fromBoundingBox(box: BoundingBox?): String? {
        return box?.let { Gson().toJson(it) }
    }
    
    @TypeConverter
    fun toBoundingBox(value: String?): BoundingBox? {
        return value?.let { Gson().fromJson(it, BoundingBox::class.java) }
    }
}

/**
 * Extension functions for validation
 */
fun ReceiptSchema.validate(): ValidationResult {
    val errors = mutableListOf<ValidationError>()
    val warnings = mutableListOf<ValidationWarning>()
    
    // Validate total amount
    if (totalAmount == null || totalAmount <= 0) {
        errors.add(ValidationError("totalAmount", "Total amount is required and must be positive", ErrorSeverity.HIGH))
    }
    
    // Validate subtotal + tax = total (with tolerance)
    if (totalAmount != null && subtotalAmount != null && taxAmount != null) {
        val calculatedTotal = subtotalAmount + taxAmount + (tipAmount ?: 0f) - (discountAmount ?: 0f)
        val tolerance = 0.50f
        if (kotlin.math.abs(totalAmount - calculatedTotal) > tolerance) {
            warnings.add(ValidationWarning(
                "totalAmount",
                "Total amount doesn't match calculated total (subtotal + tax + tip - discount)",
                "Verify arithmetic calculations"
            ))
        }
    }
    
    // Validate merchant name
    if (merchantName.isNullOrBlank()) {
        errors.add(ValidationError("merchantName", "Merchant name is required", ErrorSeverity.MEDIUM))
    }
    
    // Validate date
    if (transactionDate == null) {
        errors.add(ValidationError("transactionDate", "Transaction date is required", ErrorSeverity.MEDIUM))
    } else {
        // Check if date is reasonable (not in future, not too old)
        val now = Date()
        val oneYearAgo = Date(now.time - 365L * 24 * 60 * 60 * 1000)
        if (transactionDate.after(now)) {
            errors.add(ValidationError("transactionDate", "Transaction date cannot be in the future", ErrorSeverity.HIGH))
        } else if (transactionDate.before(oneYearAgo)) {
            warnings.add(ValidationWarning("transactionDate", "Transaction date is more than 1 year old", "Verify date accuracy"))
        }
    }
    
    // Validate line items
    if (lineItems.isEmpty()) {
        warnings.add(ValidationWarning("lineItems", "No line items found", "Consider reviewing item extraction"))
    } else {
        val totalItemsPrice = lineItems.sumOf { it.totalPrice.toDouble() }.toFloat()
        if (subtotalAmount != null && kotlin.math.abs(totalItemsPrice - subtotalAmount) > 1.0f) {
            warnings.add(ValidationWarning(
                "lineItems",
                "Sum of line items doesn't match subtotal",
                "Verify line item extraction accuracy"
            ))
        }
    }
    
    // Calculate overall confidence
    val baseConfidence = confidence
    val errorPenalty = errors.size * 0.1f
    val warningPenalty = warnings.size * 0.05f
    val finalConfidence = (baseConfidence - errorPenalty - warningPenalty).coerceIn(0f, 1f)
    
    return ValidationResult(
        isValid = errors.isEmpty(),
        errors = errors,
        warnings = warnings,
        confidence = finalConfidence
    )
}

/**
 * Convert legacy ReceiptData to new schema
 */
fun com.receiptr.data.ml.ReceiptData.toSchema(): ReceiptSchema {
    return ReceiptSchema(
        merchantName = this.merchantName,
        merchantAddress = this.merchantAddress,
        phoneNumber = this.phoneNumber,
        transactionDate = this.date?.let { 
            try {
                java.text.SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(it)
            } catch (e: Exception) {
                null
            }
        },
        transactionTime = this.time,
        totalAmount = this.total?.toFloatOrNull(),
        subtotalAmount = this.subtotal?.toFloatOrNull(),
        taxAmount = this.tax?.toFloatOrNull(),
        tipAmount = null,
        discountAmount = null,
        lineItems = this.items.map { item ->
            LineItem(
                name = item.name,
                description = null,
                quantity = item.quantity?.toFloatOrNull() ?: 1.0f,
                unitPrice = item.price?.toFloatOrNull(),
                totalPrice = item.total?.toFloatOrNull() ?: item.price?.toFloatOrNull() ?: 0.0f,
                category = null,
                sku = null,
                barcode = null,
                discount = null,
                tax = null
            )
        },
        category = when (this.category) {
            com.receiptr.data.ml.models.ReceiptCategory.GROCERIES -> ReceiptCategory.GROCERIES
            com.receiptr.data.ml.models.ReceiptCategory.DINING -> ReceiptCategory.DINING
            com.receiptr.data.ml.models.ReceiptCategory.TRANSPORTATION -> ReceiptCategory.TRANSPORTATION
            com.receiptr.data.ml.models.ReceiptCategory.ELECTRONICS -> ReceiptCategory.ELECTRONICS
            com.receiptr.data.ml.models.ReceiptCategory.CLOTHING -> ReceiptCategory.CLOTHING
            com.receiptr.data.ml.models.ReceiptCategory.HEALTHCARE -> ReceiptCategory.HEALTHCARE
            com.receiptr.data.ml.models.ReceiptCategory.ENTERTAINMENT -> ReceiptCategory.ENTERTAINMENT
            com.receiptr.data.ml.models.ReceiptCategory.HOME_GARDEN -> ReceiptCategory.HOME_GARDEN
            com.receiptr.data.ml.models.ReceiptCategory.AUTOMOTIVE -> ReceiptCategory.AUTOMOTIVE
            com.receiptr.data.ml.models.ReceiptCategory.BUSINESS -> ReceiptCategory.BUSINESS
            com.receiptr.data.ml.models.ReceiptCategory.TRAVEL -> ReceiptCategory.TRAVEL
            com.receiptr.data.ml.models.ReceiptCategory.EDUCATION -> ReceiptCategory.EDUCATION
            com.receiptr.data.ml.models.ReceiptCategory.UTILITIES -> ReceiptCategory.UTILITIES
            else -> ReceiptCategory.OTHER
        },
        paymentMethod = when (this.paymentMethod?.lowercase()) {
            "cash" -> PaymentMethod.CASH
            "credit" -> PaymentMethod.CREDIT_CARD
            "debit" -> PaymentMethod.DEBIT_CARD
            "mobile" -> PaymentMethod.MOBILE_PAYMENT
            "gift" -> PaymentMethod.GIFT_CARD
            else -> PaymentMethod.OTHER
        },
        rawText = this.rawText ?: "",
        imageUri = null,
        confidence = 0.5f,
        annotationNotes = null
    )
}
