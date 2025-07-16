package com.receiptr.data.ml

import com.receiptr.data.ml.enhanced.ReceiptCategorizationService
import com.receiptr.data.ml.enhanced.ReceiptCategory as EnhancedReceiptCategory
import com.receiptr.data.ml.models.ReceiptCategory as ModelReceiptCategory
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service to parse receipt data from recognized text
 */
@Singleton
class ReceiptParserService @Inject constructor() {
    private val categorizationService = ReceiptCategorizationService()
    
    // Common regex patterns for receipt parsing
    private val pricePattern = Pattern.compile("\\$?\\d+\\.\\d{2}")
    private val datePattern = Pattern.compile("(\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4})")
    private val timePattern = Pattern.compile("(\\d{1,2}:\\d{2}\\s?(AM|PM|am|pm)?)")
    private val phonePattern = Pattern.compile("(\\(?\\d{3}\\)?[-.]?\\d{3}[-.]?\\d{4})")
    private val totalPattern = Pattern.compile("(?i)total\\s*:?\\s*\\$?(\\d+\\.\\d{2})")
    private val subtotalPattern = Pattern.compile("(?i)subtotal\\s*:?\\s*\\$?(\\d+\\.\\d{2})")
    private val taxPattern = Pattern.compile("(?i)tax\\s*:?\\s*\\$?(\\d+\\.\\d{2})")
    
    // Common merchant names/keywords
    private val merchantKeywords = setOf(
        "walmart", "target", "starbucks", "mcdonalds", "subway", "cvs", "walgreens",
        "kroger", "publix", "costco", "best buy", "home depot", "lowes", "amazon"
    )
    
    /**
     * Parses a receipt from recognized text
     */
fun parseReceipt(textResult: TextRecognitionResult): ReceiptData {
        val lines = textResult.fullText.split("\n").filter { it.isNotBlank() }
        val merchantName = extractMerchantName(lines)
        val category = classifyReceiptCategory(textResult.fullText, merchantName)
        
        return ReceiptData(
            merchantName = merchantName,
            merchantAddress = extractMerchantAddress(lines),
            phoneNumber = extractPhoneNumber(textResult.fullText),
            date = extractDate(textResult.fullText),
            time = extractTime(textResult.fullText),
            items = extractItems(lines),
            subtotal = extractSubtotal(textResult.fullText),
            tax = extractTax(textResult.fullText),
            total = extractTotal(textResult.fullText),
            paymentMethod = extractPaymentMethod(lines),
            category = category,
            rawText = textResult.fullText
        )
    }
    
    private fun extractMerchantName(lines: List<String>): String? {
        // Look for merchant name in the first few lines
        for (i in 0 until minOf(5, lines.size)) {
            val line = lines[i].lowercase().trim()
            
            // Check for known merchant keywords
            for (keyword in merchantKeywords) {
                if (line.contains(keyword)) {
                    return lines[i].trim()
                }
            }
            
            // If line is all caps and longer than 3 characters, likely merchant name
            if (lines[i].length > 3 && lines[i] == lines[i].uppercase()) {
                return lines[i].trim()
            }
        }
        
        // Fallback to first line if no merchant found
        return lines.firstOrNull()?.trim()
    }
    
    private fun extractMerchantAddress(lines: List<String>): String? {
        // Look for address patterns in first 10 lines
        for (i in 0 until minOf(10, lines.size)) {
            val line = lines[i].lowercase()
            
            // Look for common address indicators
            if (line.contains("street") || line.contains("st") || 
                line.contains("avenue") || line.contains("ave") ||
                line.contains("road") || line.contains("rd") ||
                line.contains("boulevard") || line.contains("blvd") ||
                line.matches(Regex(".*\\d+.*"))) {
                
                // Try to get address with next line (city, state, zip)
                val addressLines = mutableListOf<String>()
                addressLines.add(lines[i].trim())
                
                if (i + 1 < lines.size) {
                    val nextLine = lines[i + 1].trim()
                    if (nextLine.matches(Regex(".*\\d{5}.*"))) { // Contains zip code
                        addressLines.add(nextLine)
                    }
                }
                
                return addressLines.joinToString(", ")
            }
        }
        
        return null
    }
    
    private fun extractPhoneNumber(text: String): String? {
        val matcher = phonePattern.matcher(text)
        return if (matcher.find()) {
            matcher.group(0)
        } else null
    }
    
    private fun extractDate(text: String): String? {
        val matcher = datePattern.matcher(text)
        return if (matcher.find()) {
            matcher.group(0)
        } else null
    }
    
    private fun extractTime(text: String): String? {
        val matcher = timePattern.matcher(text)
        return if (matcher.find()) {
            matcher.group(0)
        } else null
    }
    
    private fun extractTotal(text: String): String? {
        val matcher = totalPattern.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }
    
    private fun extractSubtotal(text: String): String? {
        val matcher = subtotalPattern.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }
    
    private fun extractTax(text: String): String? {
        val matcher = taxPattern.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }
    
    private fun extractItems(lines: List<String>): List<ReceiptItem> {
        val items = mutableListOf<ReceiptItem>()
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            // Skip common non-item lines
            if (isNonItemLine(trimmedLine)) {
                continue
            }
            
            // Look for lines with prices
            val priceMatcher = pricePattern.matcher(trimmedLine)
            if (priceMatcher.find()) {
                val price = priceMatcher.group(0)
                if (price != null) {
                    val itemName = trimmedLine.replace(price, "").trim()
                    
                    if (itemName.isNotBlank() && itemName.length > 2) {
                        items.add(ReceiptItem(
                            name = itemName,
                            price = price.replace("$", ""),
                            total = price.replace("$", "")
                        ))
                    }
                }
            }
        }
        
        return items
    }
    
    private fun isNonItemLine(line: String): Boolean {
        val lowerLine = line.lowercase()
        
        // Common non-item patterns
        val nonItemPatterns = listOf(
            "total", "subtotal", "tax", "change", "cash", "credit", "debit",
            "thank you", "receipt", "store", "cashier", "date", "time",
            "phone", "address", "welcome", "visit", "again"
        )
        
        return nonItemPatterns.any { lowerLine.contains(it) } ||
               line.length < 3 ||
               line.matches(Regex("^[\\d\\s\\-:/.]+$")) // Only numbers, spaces, and separators
    }
    
    private fun extractPaymentMethod(lines: List<String>): String? {
        val paymentMethods = listOf("cash", "credit", "debit", "visa", "mastercard", "amex", "discover")
        
        for (line in lines) {
            val lowerLine = line.lowercase()
            for (method in paymentMethods) {
                if (lowerLine.contains(method)) {
                    return method.uppercase()
                }
            }
        }
        
        return null
    }
    
    /**
     * Classifies receipt category based on merchant name and text content
     */
    private fun classifyReceiptCategory(text: String, merchantName: String?): ModelReceiptCategory {
        val receiptData = ReceiptData(
            merchantName = merchantName,
            rawText = text
            // Add other extracted data as needed
        )
        val categoryResult = categorizationService.getDetailedCategorization(receiptData)
        return when (categoryResult.primaryCategory) {
            EnhancedReceiptCategory.GROCERIES -> ModelReceiptCategory.GROCERIES
            EnhancedReceiptCategory.DINING -> ModelReceiptCategory.DINING
            EnhancedReceiptCategory.TRANSPORTATION -> ModelReceiptCategory.TRANSPORTATION
            EnhancedReceiptCategory.ELECTRONICS -> ModelReceiptCategory.ELECTRONICS
            EnhancedReceiptCategory.CLOTHING -> ModelReceiptCategory.CLOTHING
            EnhancedReceiptCategory.HEALTHCARE -> ModelReceiptCategory.HEALTHCARE
            EnhancedReceiptCategory.ENTERTAINMENT -> ModelReceiptCategory.ENTERTAINMENT
            EnhancedReceiptCategory.HOME_GARDEN -> ModelReceiptCategory.HOME_GARDEN
            EnhancedReceiptCategory.AUTOMOTIVE -> ModelReceiptCategory.AUTOMOTIVE
            EnhancedReceiptCategory.BUSINESS -> ModelReceiptCategory.BUSINESS
            EnhancedReceiptCategory.TRAVEL -> ModelReceiptCategory.TRAVEL
            EnhancedReceiptCategory.EDUCATION -> ModelReceiptCategory.EDUCATION
            EnhancedReceiptCategory.UTILITIES -> ModelReceiptCategory.UTILITIES
            else -> ModelReceiptCategory.OTHER
        }
    }
}
