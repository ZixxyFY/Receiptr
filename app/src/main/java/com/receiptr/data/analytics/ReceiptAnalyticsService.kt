package com.receiptr.data.analytics

import com.receiptr.data.ml.enhanced.ReceiptCategory
import com.receiptr.domain.model.Receipt
import com.receiptr.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for analyzing receipt data and providing insights
 */
@Singleton
class ReceiptAnalyticsService @Inject constructor(
    private val receiptRepository: ReceiptRepository
) {
    
    /**
     * Gets spending analytics for a user
     */
    suspend fun getSpendingAnalytics(userId: String): Flow<SpendingAnalytics> = flow {
        val receipts = receiptRepository.getAllReceipts(userId).first()
        
        if (receipts.isEmpty()) {
            emit(SpendingAnalytics.empty())
            return@flow
        }
        
        val currentMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val lastMonth = Calendar.getInstance().apply {
            time = currentMonth.time
            add(Calendar.MONTH, -1)
        }
        
        val currentMonthReceipts = receipts.filter { it.date >= currentMonth.timeInMillis }
        val lastMonthReceipts = receipts.filter { 
            it.date >= lastMonth.timeInMillis && it.date < currentMonth.timeInMillis 
        }
        
        val currentMonthSpending = currentMonthReceipts.sumOf { it.totalAmount }
        val lastMonthSpending = lastMonthReceipts.sumOf { it.totalAmount }
        
        val spendingChange = if (lastMonthSpending > 0) {
            val percentChange = ((currentMonthSpending - lastMonthSpending) / lastMonthSpending) * 100
            if (percentChange > 0) "+${percentChange.toInt()}%" else "${percentChange.toInt()}%"
        } else {
            "N/A"
        }
        
        val categoryBreakdown = getCategoryBreakdown(currentMonthReceipts)
        val topCategories = getTopCategories(currentMonthReceipts)
        val dailySpending = getDailySpending(currentMonthReceipts)
        val weeklySpending = getWeeklySpending(currentMonthReceipts)
        
        emit(SpendingAnalytics(
            totalSpending = currentMonthSpending,
            spendingChange = "$spendingChange from last month",
            categoryBreakdown = categoryBreakdown,
            topCategories = topCategories,
            dailySpending = dailySpending,
            weeklySpending = weeklySpending,
            totalReceipts = currentMonthReceipts.size,
            averageReceiptAmount = if (currentMonthReceipts.isNotEmpty()) 
                currentMonthSpending / currentMonthReceipts.size else 0.0,
            lastUpdated = System.currentTimeMillis()
        ))
    }
    
    /**
     * Gets recent receipts with enhanced categorization
     */
    suspend fun getRecentReceipts(userId: String, limit: Int = 10): Flow<List<EnhancedReceiptItem>> = flow {
        val receipts = receiptRepository.getAllReceipts(userId).first()
        
        val recentReceipts = receipts
            .sortedByDescending { it.date }
            .take(limit)
            .map { receipt ->
                EnhancedReceiptItem(
                    id = receipt.id,
                    merchantName = receipt.merchantName,
                    totalAmount = receipt.totalAmount,
                    category = ReceiptCategory.fromString(receipt.category),
                    date = Date(receipt.date),
                    isProcessed = receipt.isProcessed,
                    confidence = calculateReceiptConfidence(receipt)
                )
            }
        
        emit(recentReceipts)
    }
    
    /**
     * Gets category breakdown for visualization
     */
    private fun getCategoryBreakdown(receipts: List<Receipt>): Map<ReceiptCategory, Double> {
        val categoryTotals = mutableMapOf<ReceiptCategory, Double>()
        
        receipts.forEach { receipt ->
            val category = ReceiptCategory.fromString(receipt.category)
            categoryTotals[category] = (categoryTotals[category] ?: 0.0) + receipt.totalAmount
        }
        
        return categoryTotals.toMap()
    }
    
    /**
     * Gets top spending categories
     */
    private fun getTopCategories(receipts: List<Receipt>): List<CategorySpending> {
        val categoryTotals = getCategoryBreakdown(receipts)
        
        return categoryTotals.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { (category, amount) ->
                CategorySpending(
                    category = category,
                    amount = amount,
                    percentage = if (receipts.isNotEmpty()) {
                        (amount / receipts.sumOf { it.totalAmount }) * 100
                    } else 0.0
                )
            }
    }
    
    /**
     * Gets daily spending for the current month
     */
    private fun getDailySpending(receipts: List<Receipt>): List<DailySpending> {
        val dailyTotals = mutableMapOf<String, Double>()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        receipts.forEach { receipt ->
            val dateKey = dateFormat.format(Date(receipt.date))
            dailyTotals[dateKey] = (dailyTotals[dateKey] ?: 0.0) + receipt.totalAmount
        }
        
        return dailyTotals.entries
            .sortedBy { it.key }
            .map { (date, amount) ->
                DailySpending(
                    date = date,
                    amount = amount
                )
            }
    }
    
    /**
     * Gets weekly spending for the current month
     */
    private fun getWeeklySpending(receipts: List<Receipt>): List<WeeklySpending> {
        val weeklyTotals = mutableMapOf<Int, Double>()
        val calendar = Calendar.getInstance()
        
        receipts.forEach { receipt ->
            calendar.timeInMillis = receipt.date
            val week = calendar.get(Calendar.WEEK_OF_YEAR)
            weeklyTotals[week] = (weeklyTotals[week] ?: 0.0) + receipt.totalAmount
        }
        
        return weeklyTotals.entries
            .sortedBy { it.key }
            .map { (week, amount) ->
                WeeklySpending(
                    week = week,
                    amount = amount
                )
            }
    }
    
    /**
     * Calculates confidence score for a receipt
     */
    private fun calculateReceiptConfidence(receipt: Receipt): Float {
        var confidence = 0.0f
        
        // Check if merchant name is extracted
        if (receipt.merchantName.isNotEmpty()) confidence += 0.3f
        
        // Check if total amount is reasonable
        if (receipt.totalAmount > 0) confidence += 0.3f
        
        // Check if category is assigned
        if (receipt.category.isNotEmpty()) confidence += 0.2f
        
        // Check if processed
        if (receipt.isProcessed) confidence += 0.2f
        
        return confidence.coerceIn(0.0f, 1.0f)
    }
    
    /**
     * Gets spending trends over time
     */
    suspend fun getSpendingTrends(userId: String, months: Int = 6): Flow<List<MonthlySpending>> = flow {
        val receipts = receiptRepository.getAllReceipts(userId).first()
        val monthlyTotals = mutableMapOf<String, Double>()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM", Locale.getDefault())
        
        receipts.forEach { receipt ->
            val monthKey = dateFormat.format(Date(receipt.date))
            monthlyTotals[monthKey] = (monthlyTotals[monthKey] ?: 0.0) + receipt.totalAmount
        }
        
        val trends = monthlyTotals.entries
            .sortedBy { it.key }
            .takeLast(months)
            .map { (month, amount) ->
                MonthlySpending(
                    month = month,
                    amount = amount
                )
            }
        
        emit(trends)
    }
}

/**
 * Data classes for analytics
 */
data class SpendingAnalytics(
    val totalSpending: Double,
    val spendingChange: String,
    val categoryBreakdown: Map<ReceiptCategory, Double>,
    val topCategories: List<CategorySpending>,
    val dailySpending: List<DailySpending>,
    val weeklySpending: List<WeeklySpending>,
    val totalReceipts: Int,
    val averageReceiptAmount: Double,
    val lastUpdated: Long
) {
    companion object {
        fun empty() = SpendingAnalytics(
            totalSpending = 0.0,
            spendingChange = "No data available",
            categoryBreakdown = emptyMap(),
            topCategories = emptyList(),
            dailySpending = emptyList(),
            weeklySpending = emptyList(),
            totalReceipts = 0,
            averageReceiptAmount = 0.0,
            lastUpdated = System.currentTimeMillis()
        )
    }
}

data class EnhancedReceiptItem(
    val id: String,
    val merchantName: String,
    val totalAmount: Double,
    val category: ReceiptCategory,
    val date: Date,
    val isProcessed: Boolean,
    val confidence: Float
)

data class CategorySpending(
    val category: ReceiptCategory,
    val amount: Double,
    val percentage: Double
)

data class DailySpending(
    val date: String,
    val amount: Double
)

data class WeeklySpending(
    val week: Int,
    val amount: Double
)

data class MonthlySpending(
    val month: String,
    val amount: Double
)
