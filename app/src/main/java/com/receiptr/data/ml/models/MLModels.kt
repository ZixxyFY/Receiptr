package com.receiptr.data.ml.models

import android.graphics.Rect
import android.graphics.Point
import java.util.Date

/**
 * Enhanced data models for advanced ML-based receipt processing
 */

// Entity Types for Named Entity Recognition
enum class ReceiptEntityType {
    MERCHANT_NAME,
    MERCHANT_ADDRESS,
    MERCHANT_PHONE,
    DATE,
    TIME,
    ITEM_NAME,
    ITEM_QUANTITY,
    ITEM_PRICE,
    ITEM_TOTAL,
    SUBTOTAL,
    TAX,
    TOTAL_AMOUNT,
    DISCOUNT,
    PAYMENT_METHOD,
    CASHIER,
    RECEIPT_ID,
    UNKNOWN
}

// Categories for Receipt Classification
enum class ReceiptCategory(val displayName: String, val keywords: List<String>) {
    GROCERIES("Groceries", listOf("walmart", "kroger", "safeway", "publix", "target", "costco", "sam's club", "whole foods", "trader joe's", "aldi")),
    DINING("Dining", listOf("restaurant", "mcdonalds", "starbucks", "subway", "pizza", "cafe", "bar", "grill", "diner", "fast food")),
    FUEL("Fuel", listOf("shell", "exxon", "bp", "chevron", "mobil", "texaco", "citgo", "sunoco", "arco", "gas station")),
    RETAIL("Retail", listOf("best buy", "amazon", "ebay", "mall", "store", "shop", "clothing", "electronics", "furniture")),
    ELECTRONICS("Electronics", listOf("best buy", "apple", "samsung", "microsoft", "computer", "phone", "tablet", "electronics")),
    CLOTHING("Clothing", listOf("nike", "adidas", "gap", "h&m", "zara", "clothing", "fashion", "shoes", "apparel")),
    HEALTHCARE("Healthcare", listOf("pharmacy", "cvs", "walgreens", "rite aid", "hospital", "clinic", "doctor", "medical", "prescription")),
    TRANSPORTATION("Transportation", listOf("uber", "lyft", "taxi", "bus", "train", "airline", "parking", "toll", "metro")),
    ENTERTAINMENT("Entertainment", listOf("movie", "theater", "concert", "park", "museum", "gym", "sports", "recreation")),
    HOME_GARDEN("Home & Garden", listOf("home depot", "lowe's", "ikea", "furniture", "garden", "hardware", "home improvement")),
    AUTOMOTIVE("Automotive", listOf("autozone", "jiffy lube", "car wash", "mechanic", "auto", "vehicle", "car", "oil change")),
    UTILITIES("Utilities", listOf("electric", "gas", "water", "phone", "internet", "cable", "utility", "bill")),
    BUSINESS("Business", listOf("office", "supplies", "equipment", "service", "consultation", "professional")),
    TRAVEL("Travel", listOf("hotel", "motel", "airbnb", "flight", "car rental", "travel", "vacation", "trip")),
    EDUCATION("Education", listOf("school", "university", "college", "tuition", "books", "supplies", "course")),
    OTHER("Other", emptyList())
}

// Enhanced entity with confidence and location
data class ReceiptEntity(
    val text: String,
    val type: ReceiptEntityType,
    val confidence: Float,
    val boundingBox: Rect?,
    val startIndex: Int,
    val endIndex: Int,
    val normalizedValue: String? = null // Processed/normalized version
)

// Enhanced receipt data with ML annotations
data class EnhancedReceiptData(
    val originalText: String,
    val entities: List<ReceiptEntity>,
    val category: ReceiptCategory,
    val categoryConfidence: Float,
    val merchantName: String? = null,
    val merchantAddress: String? = null,
    val merchantPhone: String? = null,
    val date: Date? = null,
    val time: String? = null,
    val items: List<EnhancedReceiptItem> = emptyList(),
    val subtotal: Double? = null,
    val tax: Double? = null,
    val total: Double? = null,
    val discount: Double? = null,
    val paymentMethod: String? = null,
    val processingMetadata: ProcessingMetadata
)

data class EnhancedReceiptItem(
    val name: String,
    val quantity: Double? = null,
    val unitPrice: Double? = null,
    val totalPrice: Double? = null,
    val category: String? = null,
    val confidence: Float,
    val boundingBox: Rect?
)

data class ProcessingMetadata(
    val processingTime: Long,
    val ocrConfidence: Float,
    val nerConfidence: Float,
    val classificationConfidence: Float,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

// Training data structures
data class AnnotatedReceipt(
    val id: String,
    val imageUri: String,
    val ocrText: String,
    val entities: List<ReceiptEntity>,
    val category: ReceiptCategory,
    val verificationStatus: VerificationStatus,
    val annotatedBy: String,
    val annotatedAt: Date,
    val qualityScore: Float
)

enum class VerificationStatus {
    PENDING,
    VERIFIED,
    NEEDS_REVIEW,
    REJECTED
}

// User feedback for continuous learning
data class UserFeedback(
    val receiptId: String,
    val userId: String,
    val originalData: EnhancedReceiptData,
    val corrections: List<EntityCorrection>,
    val categoryCorrection: ReceiptCategory?,
    val overallRating: Int, // 1-5 scale
    val timestamp: Date
)

data class EntityCorrection(
    val originalEntity: ReceiptEntity,
    val correctedText: String?,
    val correctedType: ReceiptEntityType?,
    val action: CorrectionAction
)

enum class CorrectionAction {
    MODIFY,
    DELETE,
    ADD
}

// Model performance metrics
data class ModelMetrics(
    val modelVersion: String,
    val accuracy: Float,
    val precision: Float,
    val recall: Float,
    val f1Score: Float,
    val processingSpeed: Float, // receipts per second
    val evaluatedOn: Date,
    val testDataSize: Int
)
