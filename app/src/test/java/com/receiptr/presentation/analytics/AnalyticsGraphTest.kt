package com.receiptr.presentation.analytics

import com.receiptr.data.analytics.CategorySpending
import com.receiptr.data.analytics.MonthlySpending
import com.receiptr.data.analytics.SpendingAnalytics
import com.receiptr.data.ml.enhanced.ReceiptCategory
import com.receiptr.presentation.viewmodel.AnalyticsScreenData
import com.receiptr.presentation.viewmodel.AnalyticsViewModel
import com.receiptr.presentation.viewmodel.TimePeriod
import org.junit.Test
import org.junit.Assert.*

/**
 * Test class for Analytics Graph functionality
 */
class AnalyticsGraphTest {

    @Test
    fun testMonthlySpendingDataProcessing() {
        // Test data creation
        val monthlySpending = listOf(
            MonthlySpending("2024-01", 800.0),
            MonthlySpending("2024-02", 650.0),
            MonthlySpending("2024-03", 950.0),
            MonthlySpending("2024-04", 750.0),
            MonthlySpending("2024-05", 1200.0),
            MonthlySpending("2024-06", 1100.0),
            MonthlySpending("2024-07", 1234.0)
        )

        // Test maximum amount calculation
        val maxAmount = monthlySpending.maxOfOrNull { it.amount } ?: 1.0
        assertEquals(1234.0, maxAmount, 0.01)

        // Test percentage calculation for chart height
        val percentageForJuly = monthlySpending.last().amount / maxAmount
        assertEquals(1.0, percentageForJuly, 0.01)

        val percentageForFeb = monthlySpending[1].amount / maxAmount
        assertEquals(0.527, percentageForFeb, 0.01)

        // Test that we get the last 7 months for display
        val displayData = monthlySpending.takeLast(7)
        assertEquals(7, displayData.size)
        assertEquals("2024-01", displayData.first().month)
        assertEquals("2024-07", displayData.last().month)
    }

    @Test
    fun testCategorySpendingDataProcessing() {
        // Test data creation
        val categorySpending = listOf(
            CategorySpending(ReceiptCategory.GROCERIES, 450.0, 36.5),
            CategorySpending(ReceiptCategory.DINING, 315.0, 25.5),
            CategorySpending(ReceiptCategory.ENTERTAINMENT, 280.0, 22.7),
            CategorySpending(ReceiptCategory.TRANSPORTATION, 245.0, 19.9),
            CategorySpending(ReceiptCategory.CLOTHING, 144.0, 11.7)
        )

        // Test maximum amount calculation
        val maxAmount = categorySpending.maxOfOrNull { it.amount } ?: 1.0
        assertEquals(450.0, maxAmount, 0.01)

        // Test percentage calculation for bar width
        val percentageForGroceries = categorySpending.first().amount / maxAmount
        assertEquals(1.0, percentageForGroceries, 0.01)

        val percentageForClothing = categorySpending.last().amount / maxAmount
        assertEquals(0.32, percentageForClothing, 0.01)

        // Test that we only show top 5 categories
        val displayData = categorySpending.take(5)
        assertEquals(5, displayData.size)
        assertEquals(ReceiptCategory.GROCERIES, displayData.first().category)
        assertEquals(ReceiptCategory.CLOTHING, displayData.last().category)
    }

    @Test
    fun testEmptyDataHandling() {
        // Test empty monthly data
        val emptyMonthlyData = emptyList<MonthlySpending>()
        assertTrue(emptyMonthlyData.isEmpty())

        // Test empty category data
        val emptyCategoryData = emptyList<CategorySpending>()
        assertTrue(emptyCategoryData.isEmpty())

        // Test maxAmount calculation with empty data
        val maxAmount = emptyMonthlyData.maxOfOrNull { it.amount } ?: 1.0
        assertEquals(1.0, maxAmount, 0.01)
    }

    @Test
    fun testAnalyticsScreenDataCreation() {
        // Create test spending analytics
        val spendingAnalytics = SpendingAnalytics(
            totalSpending = 1234.0,
            spendingChange = "+12% from last month",
            categoryBreakdown = mapOf(
                ReceiptCategory.GROCERIES to 450.0,
                ReceiptCategory.DINING to 315.0
            ),
            topCategories = listOf(
                CategorySpending(ReceiptCategory.GROCERIES, 450.0, 36.5),
                CategorySpending(ReceiptCategory.DINING, 315.0, 25.5)
            ),
            dailySpending = emptyList(),
            weeklySpending = emptyList(),
            totalReceipts = 15,
            averageReceiptAmount = 82.27,
            lastUpdated = System.currentTimeMillis()
        )

        val monthlyTrends = listOf(
            MonthlySpending("2024-06", 1100.0),
            MonthlySpending("2024-07", 1234.0)
        )

        // Create analytics screen data
        val analyticsData = AnalyticsScreenData(
            spendingAnalytics = spendingAnalytics,
            monthlyTrends = monthlyTrends,
            categoryBreakdown = spendingAnalytics.topCategories,
            timePeriod = TimePeriod.MONTHLY
        )

        // Test analytics data
        assertEquals(1234.0, analyticsData.spendingAnalytics.totalSpending, 0.01)
        assertEquals("+12% from last month", analyticsData.spendingAnalytics.spendingChange)
        assertEquals(2, analyticsData.monthlyTrends.size)
        assertEquals(2, analyticsData.categoryBreakdown.size)
        assertEquals(TimePeriod.MONTHLY, analyticsData.timePeriod)
    }

    @Test
    fun testMonthLabelFormatting() {
        // Test month label extraction for display
        val monthlyData = listOf(
            MonthlySpending("2024-01", 800.0),
            MonthlySpending("2024-02", 650.0),
            MonthlySpending("2024-12", 950.0)
        )

        // Test that we extract the last 3 characters for display
        monthlyData.forEach { data ->
            val displayLabel = data.month.takeLast(3)
            when (data.month) {
                "2024-01" -> assertEquals("-01", displayLabel)
                "2024-02" -> assertEquals("-02", displayLabel)
                "2024-12" -> assertEquals("-12", displayLabel)
            }
        }
    }

    @Test
    fun testCategoryColorAssignment() {
        // Test that categories have colors assigned
        val categories = listOf(
            ReceiptCategory.GROCERIES,
            ReceiptCategory.DINING,
            ReceiptCategory.TRANSPORTATION,
            ReceiptCategory.ENTERTAINMENT,
            ReceiptCategory.CLOTHING
        )

        categories.forEach { category ->
            assertNotNull(category.color)
            assertTrue(category.color.startsWith("#"))
            assertEquals(7, category.color.length) // #RRGGBB format
        }
    }

    @Test
    fun testAmountFormatting() {
        // Test currency formatting
        val amounts = listOf(0.0, 123.45, 1000.0, 1234.567)
        
        amounts.forEach { amount ->
            val formatted = "$${String.format("%.2f", amount)}"
            when (amount) {
                0.0 -> assertEquals("$0.00", formatted)
                123.45 -> assertEquals("$123.45", formatted)
                1000.0 -> assertEquals("$1000.00", formatted)
                1234.567 -> assertEquals("$1234.57", formatted)
            }
        }
    }

    @Test
    fun testTimePeriodFunctionality() {
        // Test time period enum
        val periods = TimePeriod.values()
        assertEquals(3, periods.size)
        
        assertEquals("Weekly", TimePeriod.WEEKLY.displayName)
        assertEquals("Monthly", TimePeriod.MONTHLY.displayName)
        assertEquals("Yearly", TimePeriod.YEARLY.displayName)
    }

    @Test
    fun testPercentageCalculationEdgeCases() {
        // Test division by zero protection
        val zeroAmount = 0.0
        val maxAmount = 0.0
        
        val percentage = if (maxAmount > 0) (zeroAmount / maxAmount).toFloat() else 0f
        assertEquals(0f, percentage, 0.01f)
        
        // Test normal calculation
        val normalAmount = 500.0
        val normalMax = 1000.0
        val normalPercentage = if (normalMax > 0) (normalAmount / normalMax).toFloat() else 0f
        assertEquals(0.5f, normalPercentage, 0.01f)
    }

    @Test
    fun testCategoryDisplayNameFormatting() {
        // Test category display names
        val categories = mapOf(
            ReceiptCategory.GROCERIES to "Groceries",
            ReceiptCategory.DINING to "Dining & Restaurants",
            ReceiptCategory.TRANSPORTATION to "Transportation",
            ReceiptCategory.ENTERTAINMENT to "Entertainment",
            ReceiptCategory.HOME_GARDEN to "Home & Garden",
            ReceiptCategory.CLOTHING to "Clothing & Fashion"
        )

        categories.forEach { (category, expectedName) ->
            assertEquals(expectedName, category.displayName)
        }
    }
}
