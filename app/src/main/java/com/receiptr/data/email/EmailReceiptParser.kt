package com.receiptr.data.email

import com.receiptr.domain.model.EmailReceipt
import com.receiptr.domain.model.ExtractedReceiptData
import com.receiptr.domain.model.EmailReceiptItem
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmailReceiptParser @Inject constructor() {

    companion object {
        private val AMOUNT_PATTERNS = listOf(
            Pattern.compile("(?:total|amount|sum)\\s*:?\\s*\\$?([0-9,]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\$([0-9,]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("([0-9,]+\\.?[0-9]*)\\s*(?:USD|usd|\$)", Pattern.CASE_INSENSITIVE)
        )
        
        private val MERCHANT_PATTERNS = listOf(
            Pattern.compile("from\\s+([\\w\\s&.-]+?)(?:\\s|$)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("receipt\\s+from\\s+([\\w\\s&.-]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:store|shop|merchant)\\s*:?\\s*([\\w\\s&.-]+)", Pattern.CASE_INSENSITIVE)
        )
        
        private val DATE_PATTERNS = listOf(
            Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{2,4})"),
            Pattern.compile("(\\d{4}-\\d{1,2}-\\d{1,2})"),
            Pattern.compile("(\\w+\\s+\\d{1,2},\\s+\\d{4})")
        )

        private val COMMON_MERCHANTS = mapOf(
            "amazon" to "Amazon",
            "walmart" to "Walmart",
            "target" to "Target",
            "costco" to "Costco",
            "starbucks" to "Starbucks",
            "mcdonalds" to "McDonald's",
            "uber" to "Uber",
            "lyft" to "Lyft",
            "airbnb" to "Airbnb",
            "booking" to "Booking.com",
            "expedia" to "Expedia",
            "paypal" to "PayPal",
            "stripe" to "Stripe",
            "square" to "Square"
        )

        private val CATEGORY_KEYWORDS = mapOf(
            "food" to listOf("restaurant", "food", "dining", "cafe", "coffee", "pizza", "burger"),
            "groceries" to listOf("grocery", "supermarket", "market", "walmart", "target", "costco"),
            "transportation" to listOf("uber", "lyft", "taxi", "gas", "fuel", "parking", "metro", "bus"),
            "shopping" to listOf("amazon", "store", "shop", "retail", "clothing", "fashion"),
            "travel" to listOf("hotel", "airbnb", "flight", "airline", "booking", "expedia", "travel"),
            "utilities" to listOf("electric", "gas", "water", "internet", "phone", "cable"),
            "entertainment" to listOf("movie", "theater", "netflix", "spotify", "game", "concert")
        )
    }

    fun parseEmailReceipt(emailReceipt: EmailReceipt): EmailReceipt {
        val combinedText = "${emailReceipt.subject} ${emailReceipt.body}".lowercase()
        
        val extractedData = ExtractedReceiptData(
            merchantName = extractMerchantName(combinedText, emailReceipt.from),
            totalAmount = extractAmount(combinedText),
            currency = extractCurrency(combinedText),
            transactionDate = extractDate(combinedText),
            category = extractCategory(combinedText, emailReceipt.from),
            items = extractItems(combinedText),
            paymentMethod = extractPaymentMethod(combinedText),
            transactionId = extractTransactionId(combinedText)
        )
        
        val confidence = calculateConfidence(extractedData, combinedText)
        
        return emailReceipt.copy(
            extractedData = extractedData,
            confidence = confidence,
            isProcessed = true
        )
    }

    private fun extractMerchantName(text: String, fromEmail: String): String {
        // Check for common merchants first
        COMMON_MERCHANTS.forEach { (key, value) ->
            if (text.contains(key, ignoreCase = true) || fromEmail.contains(key, ignoreCase = true)) {
                return value
            }
        }
        
        // Try pattern matching
        MERCHANT_PATTERNS.forEach { pattern ->
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                return matcher.group(1)?.trim()?.replaceFirstChar { 
                    if (it.isLowerCase()) it.titlecase() else it.toString() 
                } ?: "Unknown Merchant"
            }
        }
        
        // Extract from email domain as fallback
        val emailDomain = fromEmail.substringAfter("@").substringBefore(".")
        return if (emailDomain.isNotEmpty()) {
            emailDomain.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } else {
            "Unknown Merchant"
        }
    }

    private fun extractAmount(text: String): Double {
        AMOUNT_PATTERNS.forEach { pattern ->
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val amountStr = matcher.group(1)?.replace(",", "") ?: ""
                try {
                    return amountStr.toDouble()
                } catch (e: NumberFormatException) {
                    // Continue to next pattern
                }
            }
        }
        return 0.0
    }

    private fun extractCurrency(text: String): String {
        return when {
            text.contains("$") || text.contains("usd", ignoreCase = true) -> "USD"
            text.contains("€") || text.contains("eur", ignoreCase = true) -> "EUR"
            text.contains("£") || text.contains("gbp", ignoreCase = true) -> "GBP"
            text.contains("¥") || text.contains("jpy", ignoreCase = true) -> "JPY"
            else -> "USD"
        }
    }

    private fun extractDate(text: String): Long {
        DATE_PATTERNS.forEach { pattern ->
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val dateStr = matcher.group(1) ?: ""
                try {
                    val formats = listOf(
                        SimpleDateFormat("MM/dd/yyyy", Locale.US),
                        SimpleDateFormat("yyyy-MM-dd", Locale.US),
                        SimpleDateFormat("MMMM dd, yyyy", Locale.US)
                    )
                    
                    formats.forEach { format ->
                        try {
                            return format.parse(dateStr)?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            // Continue to next format
                        }
                    }
                } catch (e: Exception) {
                    // Continue to next pattern
                }
            }
        }
        return System.currentTimeMillis()
    }

    private fun extractCategory(text: String, fromEmail: String): String {
        val combinedText = "$text $fromEmail".lowercase()
        
        CATEGORY_KEYWORDS.forEach { (category, keywords) ->
            keywords.forEach { keyword ->
                if (combinedText.contains(keyword)) {
                    return category.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            }
        }
        return "Other"
    }

    private fun extractItems(text: String): List<EmailReceiptItem> {
        // Basic item extraction - this could be improved with more sophisticated parsing
        val items = mutableListOf<EmailReceiptItem>()
        
        // Look for patterns like "1x Item Name $12.99"
        val itemPattern = Pattern.compile("(\\d+)x?\\s+([\\w\\s]+?)\\s+\\$([0-9.]+)", Pattern.CASE_INSENSITIVE)
        val matcher = itemPattern.matcher(text)
        
        while (matcher.find()) {
            val quantity = matcher.group(1)?.toIntOrNull() ?: 1
            val name = matcher.group(2)?.trim() ?: ""
            val price = matcher.group(3)?.toDoubleOrNull() ?: 0.0
            
            items.add(
                EmailReceiptItem(
                    name = name,
                    quantity = quantity,
                    unitPrice = price / quantity,
                    totalPrice = price
                )
            )
        }
        
        return items
    }

    private fun extractPaymentMethod(text: String): String {
        return when {
            text.contains("visa", ignoreCase = true) -> "Visa"
            text.contains("mastercard", ignoreCase = true) -> "Mastercard"
            text.contains("amex", ignoreCase = true) || text.contains("american express", ignoreCase = true) -> "American Express"
            text.contains("paypal", ignoreCase = true) -> "PayPal"
            text.contains("apple pay", ignoreCase = true) -> "Apple Pay"
            text.contains("google pay", ignoreCase = true) -> "Google Pay"
            else -> "Unknown"
        }
    }

    private fun extractTransactionId(text: String): String {
        val patterns = listOf(
            Pattern.compile("(?:transaction|order|receipt)\\s+(?:id|number)\\s*:?\\s*([a-z0-9-]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("confirmation\\s+(?:code|number)\\s*:?\\s*([a-z0-9-]+)", Pattern.CASE_INSENSITIVE)
        )
        
        patterns.forEach { pattern ->
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                return matcher.group(1) ?: ""
            }
        }
        return ""
    }

    private fun calculateConfidence(extractedData: ExtractedReceiptData, text: String): Float {
        var confidence = 0.0f
        var totalChecks = 0

        // Check merchant name
        totalChecks++
        if (extractedData.merchantName.isNotEmpty() && extractedData.merchantName != "Unknown Merchant") {
            confidence += 0.3f
        }

        // Check amount
        totalChecks++
        if (extractedData.totalAmount > 0) {
            confidence += 0.4f
        }

        // Check date
        totalChecks++
        if (extractedData.transactionDate > 0) {
            confidence += 0.1f
        }

        // Check category
        totalChecks++
        if (extractedData.category != "Other") {
            confidence += 0.1f
        }

        // Check for receipt-related keywords
        totalChecks++
        val receiptKeywords = listOf("receipt", "order", "purchase", "transaction", "payment", "invoice")
        if (receiptKeywords.any { text.contains(it, ignoreCase = true) }) {
            confidence += 0.1f
        }

        return confidence
    }
}
