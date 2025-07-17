package com.receiptr.data.ml

import android.graphics.Rect
import android.util.Log
import com.receiptr.data.ml.enhanced.ReceiptCategorizationService
import com.receiptr.data.ml.enhanced.ReceiptCategory as EnhancedReceiptCategory
import com.receiptr.data.ml.models.ReceiptCategory as ModelReceiptCategory
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Service to parse receipt data from recognized text
 */
@Singleton
class ReceiptParserService @Inject constructor() {
    private val categorizationService = ReceiptCategorizationService()
    
    companion object {
        private const val TAG = "ReceiptParserService"
        private const val CONFIDENCE_THRESHOLD = 0.7f
        private const val MIN_FONT_SIZE_RATIO = 0.1f
        private const val MAX_FONT_SIZE_RATIO = 0.9f
    }
    
    // Enhanced regex patterns with better coverage
    private val currencySymbols = "[$₹€£¥₩₪₦₨₱₡₽₴₫₿]"
    private val pricePattern = Pattern.compile("$currencySymbols?\\s*([0-9]{1,3}(?:,?[0-9]{3})*(?:\\.[0-9]{2})?)", Pattern.CASE_INSENSITIVE)
    private val strictPricePattern = Pattern.compile("$currencySymbols\\s*([0-9]{1,3}(?:,?[0-9]{3})*\\.[0-9]{2})", Pattern.CASE_INSENSITIVE)
    
    // Total amount keywords with variations and misspellings
    private val totalKeywords = arrayOf(
        "total", "totai", "tota1", "amount\\s+due", "amt\\s+due", "balance", "paid", "amt",
        "grand\\s+total", "final\\s+total", "net\\s+total", "sum", "due", "owing", "charge",
        "payment", "invoice\\s+total", "bill\\s+total", "order\\s+total"
    )
    
    // Date patterns for various formats
    private val datePatterns = arrayOf(
        "(\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4})",
        "(\\d{2,4}[-/]\\d{1,2}[-/]\\d{1,2})",
        "(\\d{1,2}\\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d{2,4})",
        "((Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d{1,2},?\\s+\\d{2,4})",
        "(\\d{1,2}\\s+(January|February|March|April|May|June|July|August|September|October|November|December)\\s+\\d{2,4})"
    )
    
    // Time patterns
    private val timePatterns = arrayOf(
        "(\\d{1,2}:\\d{2}\\s?(?:AM|PM|am|pm))",
        "(\\d{1,2}:\\d{2}:\\d{2}\\s?(?:AM|PM|am|pm)?)",
        "(\\d{1,2}:\\d{2})"
    )
    
    // Phone patterns
    private val phonePattern = Pattern.compile("(\\(?\\d{3}\\)?[-\\s.]?\\d{3}[-\\s.]?\\d{4})")
    
    // Enhanced merchant keywords
    private val merchantKeywords = setOf(
        "walmart", "target", "starbucks", "mcdonalds", "subway", "cvs", "walgreens",
        "kroger", "publix", "costco", "best buy", "home depot", "lowes", "amazon",
        "safeway", "albertsons", "whole foods", "trader joes", "aldi", "wegmans",
        "dunkin", "burger king", "kfc", "pizza hut", "dominos", "taco bell",
        "shell", "exxon", "bp", "chevron", "mobil", "citgo", "marathon"
    )
    
    // Payment method keywords
    private val paymentMethods = mapOf(
        "cash" to arrayOf("cash", "change", "tendered"),
        "credit" to arrayOf("credit", "visa", "mastercard", "amex", "discover", "american express"),
        "debit" to arrayOf("debit", "pin", "chip"),
        "gift" to arrayOf("gift card", "store credit", "rewards"),
        "mobile" to arrayOf("apple pay", "google pay", "samsung pay", "paypal", "venmo")
    )
    
    /**
     * Parses a receipt from recognized text with sophisticated heuristics
     */
    fun parseReceipt(textResult: TextRecognitionResult): ReceiptData {
        Log.d(TAG, "Starting receipt parsing with ${textResult.textBlocks.size} text blocks")
        
        // Extract data with enhanced heuristics
        val extractedData = ExtractedData(
            merchantName = extractMerchantNameAdvanced(textResult.textBlocks),
            merchantAddress = extractMerchantAddressAdvanced(textResult.textBlocks),
            phoneNumber = extractPhoneNumberAdvanced(textResult.textBlocks),
            date = extractDateAdvanced(textResult.textBlocks),
            time = extractTimeAdvanced(textResult.textBlocks),
            totalAmount = extractTotalAmountAdvanced(textResult.textBlocks),
            subtotal = extractSubtotalAdvanced(textResult.textBlocks),
            tax = extractTaxAdvanced(textResult.textBlocks),
            items = extractLineItemsAdvanced(textResult.textBlocks),
            paymentMethod = extractPaymentMethodAdvanced(textResult.textBlocks)
        )
        
        // Validate and post-process extracted data
        val validatedData = validateAndPostProcess(extractedData)
        
        // Classify receipt category
        val category = classifyReceiptCategory(textResult.fullText, validatedData.merchantName)
        
        return ReceiptData(
            merchantName = validatedData.merchantName,
            merchantAddress = validatedData.merchantAddress,
            phoneNumber = validatedData.phoneNumber,
            date = validatedData.date,
            time = validatedData.time,
            items = validatedData.items,
            subtotal = validatedData.subtotal,
            tax = validatedData.tax,
            total = validatedData.totalAmount,
            paymentMethod = validatedData.paymentMethod,
            category = category,
            rawText = textResult.fullText
        )
    }
    
    // Data class for internal extraction results
    private data class ExtractedData(
        val merchantName: String?,
        val merchantAddress: String?,
        val phoneNumber: String?,
        val date: String?,
        val time: String?,
        val totalAmount: String?,
        val subtotal: String?,
        val tax: String?,
        val items: List<ReceiptItem>,
        val paymentMethod: String?
    )
    
    private data class PriceCandidate(
        val value: String,
        val confidence: Float,
        val position: Int, // Position from bottom (0 = bottom)
        val hasKeyword: Boolean,
        val hasCurrencySymbol: Boolean,
        val boundingBox: Rect?
    )
    
    /**
     * Advanced total amount extraction with sophisticated heuristics
     */
    private fun extractTotalAmountAdvanced(textBlocks: List<TextBlock>): String? {
        Log.d(TAG, "Extracting total amount from ${textBlocks.size} text blocks")
        
        val candidates = mutableListOf<PriceCandidate>()
        
        // Sort text blocks by position (bottom to top for total extraction)
        val sortedBlocks = textBlocks.sortedByDescending { it.boundingBox?.bottom ?: 0 }
        
        for ((index, block) in sortedBlocks.withIndex()) {
            for (line in block.lines) {
                val lineText = line.text.lowercase()
                val originalText = line.text
                
                // Check if line contains total keywords
                var hasKeyword = false
                for (keyword in totalKeywords) {
                    if (Pattern.compile(keyword, Pattern.CASE_INSENSITIVE).matcher(lineText).find()) {
                        hasKeyword = true
                        break
                    }
                }
                
                // Extract prices from the line
                val priceMatcher = strictPricePattern.matcher(originalText)
                while (priceMatcher.find()) {
                    val priceValue = priceMatcher.group(1)
                    if (priceValue != null && isValidPrice(priceValue)) {
                        val confidence = calculateTotalConfidence(
                            priceValue = priceValue,
                            hasKeyword = hasKeyword,
                            position = index,
                            lineConfidence = 0.8f, // Default confidence since TextElement doesn't have confidence property
                            lineText = lineText
                        )
                        
                        candidates.add(PriceCandidate(
                            value = priceValue,
                            confidence = confidence,
                            position = index,
                            hasKeyword = hasKeyword,
                            hasCurrencySymbol = originalText.contains(Regex(currencySymbols)),
                            boundingBox = line.boundingBox
                        ))
                    }
                }
            }
        }
        
        // Find the best candidate
        val bestCandidate = candidates.maxByOrNull { it.confidence }
        
        Log.d(TAG, "Found ${candidates.size} total candidates, best confidence: ${bestCandidate?.confidence}")
        
        return bestCandidate?.value
    }
    
    private fun calculateTotalConfidence(
        priceValue: String,
        hasKeyword: Boolean,
        position: Int,
        lineConfidence: Float,
        lineText: String
    ): Float {
        var confidence = lineConfidence
        
        // Boost confidence for keyword presence
        if (hasKeyword) {
            confidence += 0.3f
        }
        
        // Boost confidence for position (closer to bottom is better)
        val positionBoost = if (position < 3) 0.2f else if (position < 5) 0.1f else 0f
        confidence += positionBoost
        
        // Boost confidence for currency symbol
        if (lineText.contains(Regex(currencySymbols))) {
            confidence += 0.1f
        }
        
        // Boost confidence for reasonable price values
        val priceFloat = parsePrice(priceValue)
        if (priceFloat != null && priceFloat > 0.01f && priceFloat < 10000f) {
            confidence += 0.1f
        }
        
        // Penalize very small amounts that are likely tax or fees
        if (priceFloat != null && priceFloat < 1.0f) {
            confidence -= 0.2f
        }
        
        return confidence.coerceIn(0f, 1f)
    }
    
    private fun isValidPrice(priceValue: String): Boolean {
        val price = parsePrice(priceValue)
        return price != null && price > 0.0f && price < 100000.0f
    }
    
    private fun parsePrice(priceValue: String): Float? {
        return try {
            priceValue.replace(",", "").toFloat()
        } catch (e: NumberFormatException) {
            null
        }
    }
    
    /**
     * Advanced merchant name extraction with font size and position heuristics
     */
    private fun extractMerchantNameAdvanced(textBlocks: List<TextBlock>): String? {
        Log.d(TAG, "Extracting merchant name from ${textBlocks.size} text blocks")
        
        val candidates = mutableListOf<Pair<String, Float>>()
        
        // Sort blocks by position (top to bottom)
        val sortedBlocks = textBlocks.sortedBy { it.boundingBox?.top ?: Int.MAX_VALUE }
        
        for ((index, block) in sortedBlocks.withIndex()) {
            if (index > 4) break // Only consider top 5 blocks
            
            for (line in block.lines) {
                val lineText = line.text.trim()
                if (lineText.length < 2) continue
                
                var confidence = 0.5f
                
                // Check for known merchant keywords
                val lowerText = lineText.lowercase()
                for (keyword in merchantKeywords) {
                    if (lowerText.contains(keyword)) {
                        confidence += 0.4f
                        break
                    }
                }
                
                // Boost confidence for position (earlier is better)
                confidence += (5 - index) * 0.1f
                
                // Boost confidence for uppercase text (common for merchant names)
                if (lineText == lineText.uppercase() && lineText.length > 3) {
                    confidence += 0.2f
                }
                
                // Boost confidence for reasonable length
                if (lineText.length in 4..50) {
                    confidence += 0.1f
                }
                
                // Penalize if contains common non-merchant patterns
                if (lowerText.contains(Regex("\\d{2}[/\\-]\\d{2}")) || // Date
                    lowerText.contains(Regex("\\d{3}[\\-\\s]\\d{3}")) || // Phone
                    lowerText.contains("receipt") ||
                    lowerText.contains("transaction")) {
                    confidence -= 0.3f
                }
                
                candidates.add(Pair(lineText, confidence))
            }
        }
        
        val bestCandidate = candidates.maxByOrNull { it.second }
        Log.d(TAG, "Best merchant name candidate: '${bestCandidate?.first}' with confidence ${bestCandidate?.second}")
        
        return bestCandidate?.first
    }
    
    /**
     * Advanced date extraction with robust pattern matching
     */
    private fun extractDateAdvanced(textBlocks: List<TextBlock>): String? {
        Log.d(TAG, "Extracting date from ${textBlocks.size} text blocks")
        
        val candidates = mutableListOf<Pair<String, Float>>()
        
        // Sort blocks by position (top to bottom, dates are usually near the top)
        val sortedBlocks = textBlocks.sortedBy { it.boundingBox?.top ?: Int.MAX_VALUE }
        
        for ((index, block) in sortedBlocks.withIndex()) {
            if (index > 7) break // Only consider top 8 blocks
            
            for (line in block.lines) {
                val lineText = line.text
                val lowerText = lineText.lowercase()
                
                // Check for date keywords
                val hasDateKeyword = lowerText.contains("date") || lowerText.contains("tran") || lowerText.contains("time")
                
                // Try each date pattern
                for (pattern in datePatterns) {
                    val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(lineText)
                    if (matcher.find()) {
                        val dateValue = matcher.group(1)
                        if (dateValue != null && isValidDate(dateValue)) {
                            var confidence = 0.5f
                            
                            // Boost confidence for date keywords
                            if (hasDateKeyword) confidence += 0.3f
                            
                            // Boost confidence for position (earlier is better)
                            confidence += (8 - index) * 0.05f
                            
                            // Boost confidence for line confidence (default since TextElement doesn't have confidence)
                            confidence += 0.8f * 0.2f
                            
                            candidates.add(Pair(dateValue, confidence))
                        }
                    }
                }
            }
        }
        
        val bestCandidate = candidates.maxByOrNull { it.second }
        Log.d(TAG, "Best date candidate: '${bestCandidate?.first}' with confidence ${bestCandidate?.second}")
        
        return bestCandidate?.first
    }
    
    /**
     * Advanced time extraction
     */
    private fun extractTimeAdvanced(textBlocks: List<TextBlock>): String? {
        for (block in textBlocks.sortedBy { it.boundingBox?.top ?: Int.MAX_VALUE }) {
            for (line in block.lines) {
                val lineText = line.text.lowercase()
                
                // Check for time keywords
                if (lineText.contains("time") || lineText.contains("tran")) {
                    for (pattern in timePatterns) {
                        val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(line.text)
                        if (matcher.find()) {
                            val timeValue = matcher.group(1)
                            if (timeValue != null && isValidTime(timeValue)) {
                                return timeValue
                            }
                        }
                    }
                }
            }
        }
        
        // Fallback: look for time patterns anywhere in top portion
        for (block in textBlocks.sortedBy { it.boundingBox?.top ?: Int.MAX_VALUE }.take(10)) {
            for (line in block.lines) {
                for (pattern in timePatterns) {
                    val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(line.text)
                    if (matcher.find()) {
                        val timeValue = matcher.group(1)
                        if (timeValue != null && isValidTime(timeValue)) {
                            return timeValue
                        }
                    }
                }
            }
        }
        
        return null
    }
    
    // Add remaining advanced extraction methods and validation functions
    private fun extractMerchantAddressAdvanced(textBlocks: List<TextBlock>): String? {
        // Look for address patterns in first 10 blocks
        for (block in textBlocks.sortedBy { it.boundingBox?.top ?: Int.MAX_VALUE }.take(10)) {
            for (line in block.lines) {
                val lineText = line.text.lowercase()
                
                // Look for common address indicators
                if (lineText.contains("street") || lineText.contains("st") || 
                    lineText.contains("avenue") || lineText.contains("ave") ||
                    lineText.contains("road") || lineText.contains("rd") ||
                    lineText.contains("boulevard") || lineText.contains("blvd") ||
                    lineText.matches(Regex(".*\\d+.*"))) {
                    
                    // Try to get address with next line (city, state, zip)
                    val addressLines = mutableListOf<String>()
                    addressLines.add(line.text.trim())
                    
                    // Look for subsequent lines with zip code
                    val blockIndex = textBlocks.indexOf(block)
                    if (blockIndex + 1 < textBlocks.size) {
                        val nextBlock = textBlocks[blockIndex + 1]
                        for (nextLine in nextBlock.lines) {
                            if (nextLine.text.trim().matches(Regex(".*\\d{5}.*"))) {
                                addressLines.add(nextLine.text.trim())
                                break
                            }
                        }
                    }
                    
                    return addressLines.joinToString(", ")
                }
            }
        }
        
        return null
    }
    
    private fun extractPhoneNumberAdvanced(textBlocks: List<TextBlock>): String? {
        for (block in textBlocks.take(10)) {
            for (line in block.lines) {
                val matcher = phonePattern.matcher(line.text)
                if (matcher.find()) {
                    return matcher.group(0)
                }
            }
        }
        return null
    }
    
    private fun extractSubtotalAdvanced(textBlocks: List<TextBlock>): String? {
        val subtotalKeywords = arrayOf("subtotal", "sub total", "sub-total", "merchandise", "items")
        
        for (block in textBlocks.sortedByDescending { it.boundingBox?.bottom ?: 0 }) {
            for (line in block.lines) {
                val lineText = line.text.lowercase()
                
                for (keyword in subtotalKeywords) {
                    if (lineText.contains(keyword)) {
                        val priceMatcher = strictPricePattern.matcher(line.text)
                        if (priceMatcher.find()) {
                            val priceValue = priceMatcher.group(1)
                            if (priceValue != null && isValidPrice(priceValue)) {
                                return priceValue
                            }
                        }
                    }
                }
            }
        }
        
        return null
    }
    
    private fun extractTaxAdvanced(textBlocks: List<TextBlock>): String? {
        val taxKeywords = arrayOf("tax", "gst", "hst", "pst", "vat", "sales tax")
        
        for (block in textBlocks.sortedByDescending { it.boundingBox?.bottom ?: 0 }) {
            for (line in block.lines) {
                val lineText = line.text.lowercase()
                
                for (keyword in taxKeywords) {
                    if (lineText.contains(keyword)) {
                        val priceMatcher = strictPricePattern.matcher(line.text)
                        if (priceMatcher.find()) {
                            val priceValue = priceMatcher.group(1)
                            if (priceValue != null && isValidPrice(priceValue)) {
                                return priceValue
                            }
                        }
                    }
                }
            }
        }
        
        return null
    }
    
    private fun extractPaymentMethodAdvanced(textBlocks: List<TextBlock>): String? {
        for (block in textBlocks.sortedByDescending { it.boundingBox?.bottom ?: 0 }) {
            for (line in block.lines) {
                val lineText = line.text.lowercase()
                
                for ((method, keywords) in paymentMethods) {
                    for (keyword in keywords) {
                        if (lineText.contains(keyword)) {
                            return method.uppercase()
                        }
                    }
                }
            }
        }
        
        return null
    }
    
    /**
     * Advanced line item extraction with pattern recognition
     */
    private fun extractLineItemsAdvanced(textBlocks: List<TextBlock>): List<ReceiptItem> {
        Log.d(TAG, "Extracting line items from ${textBlocks.size} text blocks")
        
        val items = mutableListOf<ReceiptItem>()
        
        // Find the middle section of the receipt (skip top header and bottom totals)
        val middleBlocks = textBlocks.sortedBy { it.boundingBox?.top ?: Int.MAX_VALUE }
            .drop(3) // Skip top 3 blocks (header)
            .dropLast(5) // Skip bottom 5 blocks (totals)
        
        for (block in middleBlocks) {
            for (line in block.lines) {
                val lineText = line.text.trim()
                
                // Skip common non-item lines
                if (isNonItemLine(lineText)) {
                    continue
                }
                
                // Look for lines with price patterns
                val priceMatcher = strictPricePattern.matcher(lineText)
                val priceMatches = mutableListOf<String>()
                
                while (priceMatcher.find()) {
                    val priceValue = priceMatcher.group(1)
                    if (priceValue != null && isValidPrice(priceValue)) {
                        priceMatches.add(priceValue)
                    }
                }
                
                // If we found prices, try to extract item info
                if (priceMatches.isNotEmpty()) {
                    val itemName = extractItemName(lineText, priceMatches)
                    val quantity = extractQuantity(lineText)
                    val unitPrice = if (priceMatches.size > 1) priceMatches[0] else null
                    val totalPrice = priceMatches.last()
                    
                    if (itemName.isNotBlank() && itemName.length > 2) {
                        items.add(ReceiptItem(
                            name = itemName,
                            quantity = quantity,
                            price = unitPrice ?: totalPrice,
                            total = totalPrice
                        ))
                    }
                }
            }
        }
        
        Log.d(TAG, "Extracted ${items.size} line items")
        return items
    }
    
    private fun extractItemName(lineText: String, priceMatches: List<String>): String {
        var itemName = lineText
        
        // Remove all price matches from the line
        for (price in priceMatches) {
            itemName = itemName.replace("$${price}", "")
            itemName = itemName.replace(price, "")
        }
        
        // Remove common quantity patterns
        itemName = itemName.replace(Regex("\\d+\\s*x\\s*", RegexOption.IGNORE_CASE), "")
        itemName = itemName.replace(Regex("\\d+\\s*@\\s*", RegexOption.IGNORE_CASE), "")
        itemName = itemName.replace(Regex("qty\\s*\\d+", RegexOption.IGNORE_CASE), "")
        
        return itemName.trim()
    }
    
    private fun extractQuantity(lineText: String): String? {
        // Look for quantity patterns
        val quantityPatterns = arrayOf(
            "(\\d+)\\s*x\\s*",
            "(\\d+)\\s*@\\s*",
            "qty\\s*(\\d+)",
            "quantity\\s*(\\d+)"
        )
        
        for (pattern in quantityPatterns) {
            val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(lineText)
            if (matcher.find()) {
                return matcher.group(1)
            }
        }
        
        return null
    }
    
    /**
     * Validation and post-processing of extracted data
     */
    private fun validateAndPostProcess(extractedData: ExtractedData): ExtractedData {
        Log.d(TAG, "Validating and post-processing extracted data")
        
        // Validate total vs subtotal + tax
        val total = parsePrice(extractedData.totalAmount ?: "")
        val subtotal = parsePrice(extractedData.subtotal ?: "")
        val tax = parsePrice(extractedData.tax ?: "")
        
        if (total != null && subtotal != null && tax != null) {
            val calculatedTotal = subtotal + tax
            val difference = abs(total - calculatedTotal)
            
            // If difference is significant, flag as potential error
            if (difference > 0.50f) {
                Log.w(TAG, "Total validation failed: total=$total, subtotal+tax=$calculatedTotal, diff=$difference")
            }
        }
        
        // Validate date format
        val validatedDate = validateDate(extractedData.date)
        
        // Validate time format
        val validatedTime = validateTime(extractedData.time)
        
        // Return validated data
        return extractedData.copy(
            date = validatedDate,
            time = validatedTime
        )
    }
    
    private fun isValidDate(dateString: String): Boolean {
        val dateFormats = arrayOf(
            "MM/dd/yyyy", "MM/dd/yy", "dd/MM/yyyy", "dd/MM/yy",
            "yyyy-MM-dd", "yyyy/MM/dd", "dd-MM-yyyy",
            "MMM dd, yyyy", "dd MMM yyyy", "MMMM dd, yyyy"
        )
        
        for (format in dateFormats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.isLenient = false
                sdf.parse(dateString)
                return true
            } catch (e: ParseException) {
                // Try next format
            }
        }
        
        return false
    }
    
    private fun isValidTime(timeString: String): Boolean {
        val timeFormats = arrayOf(
            "HH:mm", "HH:mm:ss", "hh:mm a", "hh:mm:ss a",
            "H:mm", "h:mm a"
        )
        
        for (format in timeFormats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.isLenient = false
                sdf.parse(timeString)
                return true
            } catch (e: ParseException) {
                // Try next format
            }
        }
        
        return false
    }
    
    private fun validateDate(dateString: String?): String? {
        if (dateString == null) return null
        return if (isValidDate(dateString)) dateString else null
    }
    
    private fun validateTime(timeString: String?): String? {
        if (timeString == null) return null
        return if (isValidTime(timeString)) timeString else null
    }
    
    private fun isNonItemLine(line: String): Boolean {
        val lowerLine = line.lowercase()
        
        // Common non-item patterns
        val nonItemPatterns = listOf(
            "total", "subtotal", "tax", "change", "cash", "credit", "debit",
            "thank you", "receipt", "store", "cashier", "date", "time",
            "phone", "address", "welcome", "visit", "again", "transaction",
            "refund", "return", "policy", "hours", "open", "closed",
            "customer", "service", "manager", "associate", "trainee"
        )
        
        return nonItemPatterns.any { lowerLine.contains(it) } ||
               line.length < 3 ||
               line.matches(Regex("^[\\d\\s\\-:/.]+$")) || // Only numbers, spaces, and separators
               line.matches(Regex("^[*\\-=_+]+$")) // Only special characters
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
