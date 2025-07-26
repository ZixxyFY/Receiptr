package com.receiptr.data.ml.parser

import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced date parsing service that handles various date formats and 
 * converts 2-digit years to 4-digit years intelligently
 */
@Singleton
class DateParserService @Inject constructor() {
    
    companion object {
        private const val TAG = "DateParserService"
        private val CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR)
        private const val CENTURY_THRESHOLD = 50 // Years < 50 are 20xx, >= 50 are 19xx
    }
    
    // Comprehensive date patterns with named groups
    private val datePatterns = listOf(
        // DD/MM/YYYY or MM/DD/YYYY formats
        DatePattern(
            pattern = Pattern.compile("\\b(\\d{1,2})[-/\\.](\\d{1,2})[-/\\.](\\d{2,4})\\b"),
            format = "flexible", // Will be determined by context
            description = "Numeric date with separators"
        ),
        
        // YYYY/MM/DD or YYYY-MM-DD formats
        DatePattern(
            pattern = Pattern.compile("\\b(\\d{4})[-/\\.](\\d{1,2})[-/\\.](\\d{1,2})\\b"),
            format = "yyyy-MM-dd",
            description = "ISO-like date format"
        ),
        
        // DD Month YYYY (e.g., "17 Jul 2025", "17 July 2025")
        DatePattern(
            pattern = Pattern.compile("\\b(\\d{1,2})\\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+(\\d{2,4})\\b", Pattern.CASE_INSENSITIVE),
            format = "dd MMM yyyy",
            description = "Day month year with abbreviated month"
        ),
        
        // Month DD, YYYY (e.g., "Jul 17, 2025", "July 17, 2025")
        DatePattern(
            pattern = Pattern.compile("\\b(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+(\\d{1,2}),?\\s+(\\d{2,4})\\b", Pattern.CASE_INSENSITIVE),
            format = "MMM dd yyyy",
            description = "Month day year format"
        ),
        
        // DD-Month-YYYY (e.g., "17-Jul-25")
        DatePattern(
            pattern = Pattern.compile("\\b(\\d{1,2})-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-(\\d{2,4})\\b", Pattern.CASE_INSENSITIVE),
            format = "dd-MMM-yyyy",
            description = "Day-month-year with dashes"
        ),
        
        // Full month names
        DatePattern(
            pattern = Pattern.compile("\\b(\\d{1,2})\\s+(January|February|March|April|May|June|July|August|September|October|November|December)\\s+(\\d{2,4})\\b", Pattern.CASE_INSENSITIVE),
            format = "dd MMMM yyyy",
            description = "Day month year with full month name"
        )
    )
    
    /**
     * Parse date string and return timestamp
     */
    fun parseDate(dateString: String?): Long? {
        if (dateString.isNullOrBlank()) {
            Log.d(TAG, "Date string is null or blank")
            return null
        }
        
        Log.d(TAG, "Parsing date: '$dateString'")
        
        for (datePattern in datePatterns) {
            val matcher = datePattern.pattern.matcher(dateString.trim())
            if (matcher.find()) {
                try {
                    val parsedDate = when (datePattern.format) {
                        "flexible" -> parseFlexibleDate(matcher)
                        "yyyy-MM-dd" -> parseIsoLikeDate(matcher)
                        "dd MMM yyyy" -> parseDayMonthYear(matcher)
                        "MMM dd yyyy" -> parseMonthDayYear(matcher)
                        "dd-MMM-yyyy" -> parseDayMonthYearDashed(matcher)
                        "dd MMMM yyyy" -> parseDayFullMonthYear(matcher)
                        else -> null
                    }
                    
                    if (parsedDate != null) {
                        Log.d(TAG, "Successfully parsed date: $dateString -> ${Date(parsedDate)}")
                        return parsedDate
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse date with pattern ${datePattern.description}: $dateString", e)
                }
            }
        }
        
        Log.w(TAG, "No pattern matched for date: $dateString")
        return null
    }
    
    /**
     * Parse flexible date format (DD/MM/YYYY or MM/DD/YYYY)
     */
    private fun parseFlexibleDate(matcher: java.util.regex.Matcher): Long? {
        val part1 = matcher.group(1)?.toIntOrNull() ?: return null
        val part2 = matcher.group(2)?.toIntOrNull() ?: return null
        val yearStr = matcher.group(3) ?: return null
        val year = normalizeYear(yearStr.toIntOrNull() ?: return null)
        
        // Try both DD/MM/YYYY and MM/DD/YYYY
        val dates = listOf(
            createDate(part1, part2, year), // DD/MM/YYYY
            createDate(part2, part1, year)  // MM/DD/YYYY
        )
        
        // Return the first valid date
        for (date in dates) {
            if (date != null && isValidDate(date)) {
                return date
            }
        }
        
        return null
    }
    
    /**
     * Parse ISO-like date format (YYYY/MM/DD)
     */
    private fun parseIsoLikeDate(matcher: java.util.regex.Matcher): Long? {
        val year = matcher.group(1)?.toIntOrNull() ?: return null
        val month = matcher.group(2)?.toIntOrNull() ?: return null
        val day = matcher.group(3)?.toIntOrNull() ?: return null
        
        return createDate(day, month, year)
    }
    
    /**
     * Parse day month year format (17 Jul 2025)
     */
    private fun parseDayMonthYear(matcher: java.util.regex.Matcher): Long? {
        val day = matcher.group(1)?.toIntOrNull() ?: return null
        val monthStr = matcher.group(2) ?: return null
        val yearStr = matcher.group(3) ?: return null
        val year = normalizeYear(yearStr.toIntOrNull() ?: return null)
        
        val month = parseMonthName(monthStr) ?: return null
        return createDate(day, month, year)
    }
    
    /**
     * Parse month day year format (Jul 17, 2025)
     */
    private fun parseMonthDayYear(matcher: java.util.regex.Matcher): Long? {
        val monthStr = matcher.group(1) ?: return null
        val day = matcher.group(2)?.toIntOrNull() ?: return null
        val yearStr = matcher.group(3) ?: return null
        val year = normalizeYear(yearStr.toIntOrNull() ?: return null)
        
        val month = parseMonthName(monthStr) ?: return null
        return createDate(day, month, year)
    }
    
    /**
     * Parse day-month-year dashed format (17-Jul-25)
     */
    private fun parseDayMonthYearDashed(matcher: java.util.regex.Matcher): Long? {
        val day = matcher.group(1)?.toIntOrNull() ?: return null
        val monthStr = matcher.group(2) ?: return null
        val yearStr = matcher.group(3) ?: return null
        val year = normalizeYear(yearStr.toIntOrNull() ?: return null)
        
        val month = parseMonthName(monthStr) ?: return null
        return createDate(day, month, year)
    }
    
    /**
     * Parse day full month year format (17 January 2025)
     */
    private fun parseDayFullMonthYear(matcher: java.util.regex.Matcher): Long? {
        val day = matcher.group(1)?.toIntOrNull() ?: return null
        val monthStr = matcher.group(2) ?: return null
        val yearStr = matcher.group(3) ?: return null
        val year = normalizeYear(yearStr.toIntOrNull() ?: return null)
        
        val month = parseFullMonthName(monthStr) ?: return null
        return createDate(day, month, year)
    }
    
    /**
     * Convert 2-digit year to 4-digit year intelligently
     */
    private fun normalizeYear(year: Int): Int {
        return when {
            year >= 1900 -> year // Already 4-digit
            year < CENTURY_THRESHOLD -> 2000 + year // 00-49 -> 2000-2049
            else -> 1900 + year // 50-99 -> 1950-1999
        }
    }
    
    /**
     * Parse abbreviated month name to month number (1-12)
     */
    private fun parseMonthName(monthStr: String): Int? {
        val monthMap = mapOf(
            "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4,
            "may" to 5, "jun" to 6, "jul" to 7, "aug" to 8,
            "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12
        )
        return monthMap[monthStr.lowercase()]
    }
    
    /**
     * Parse full month name to month number (1-12)
     */
    private fun parseFullMonthName(monthStr: String): Int? {
        val monthMap = mapOf(
            "january" to 1, "february" to 2, "march" to 3, "april" to 4,
            "may" to 5, "june" to 6, "july" to 7, "august" to 8,
            "september" to 9, "october" to 10, "november" to 11, "december" to 12
        )
        return monthMap[monthStr.lowercase()]
    }
    
    /**
     * Create date from day, month, year and return timestamp
     */
    private fun createDate(day: Int, month: Int, year: Int): Long? {
        return try {
            if (month < 1 || month > 12 || day < 1 || day > 31) {
                return null
            }
            
            val calendar = Calendar.getInstance()
            calendar.set(year, month - 1, day, 0, 0, 0) // Month is 0-based in Calendar
            calendar.set(Calendar.MILLISECOND, 0)
            
            // Validate the date by checking if it was auto-corrected
            if (calendar.get(Calendar.DAY_OF_MONTH) != day ||
                calendar.get(Calendar.MONTH) != month - 1 ||
                calendar.get(Calendar.YEAR) != year) {
                return null
            }
            
            calendar.timeInMillis
        } catch (e: Exception) {
            Log.w(TAG, "Failed to create date: $day/$month/$year", e)
            null
        }
    }
    
    /**
     * Validate if the date is reasonable for a receipt
     */
    private fun isValidDate(timestamp: Long): Boolean {
        val date = Date(timestamp)
        val now = Date()
        val oneYearAgo = Date(now.time - 365L * 24 * 60 * 60 * 1000)
        val oneYearFromNow = Date(now.time + 365L * 24 * 60 * 60 * 1000)
        
        // Receipt dates should be within a reasonable range
        return date.after(oneYearAgo) && date.before(oneYearFromNow)
    }
    
    /**
     * Format timestamp to readable date string
     */
    fun formatDate(timestamp: Long, pattern: String = "dd/MM/yyyy"): String {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            Log.w(TAG, "Failed to format date: $timestamp", e)
            "Unknown Date"
        }
    }
    
    /**
     * Get date parsing statistics for debugging
     */
    fun getParsingInfo(): DateParsingInfo {
        return DateParsingInfo(
            supportedPatterns = datePatterns.size,
            yearNormalizationThreshold = CENTURY_THRESHOLD,
            supportedFormats = datePatterns.map { it.description },
            exampleFormats = listOf(
                "17/07/25 -> 17/07/2025",
                "2025-07-17",
                "17 Jul 2025",
                "Jul 17, 2025",
                "17-Jul-25",
                "17 January 2025"
            )
        )
    }
}

/**
 * Data class for date pattern configuration
 */
private data class DatePattern(
    val pattern: Pattern,
    val format: String,
    val description: String
)

/**
 * Information about date parsing capabilities
 */
data class DateParsingInfo(
    val supportedPatterns: Int,
    val yearNormalizationThreshold: Int,
    val supportedFormats: List<String>,
    val exampleFormats: List<String>
)
