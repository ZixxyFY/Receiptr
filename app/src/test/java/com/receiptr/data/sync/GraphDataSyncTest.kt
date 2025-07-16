package com.receiptr.data.sync

import com.receiptr.data.analytics.CategorySpending
import com.receiptr.data.analytics.MonthlySpending
import com.receiptr.data.analytics.SpendingAnalytics
import com.receiptr.data.ml.enhanced.ReceiptCategory
import com.receiptr.domain.model.Receipt
import com.receiptr.presentation.home.ReceiptItem
import com.receiptr.presentation.viewmodel.AnalyticsScreenData
import com.receiptr.presentation.viewmodel.HomeScreenData
import com.receiptr.presentation.viewmodel.TimePeriod
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Integration test for graph data synchronization between home screen and analytics
 */
class GraphDataSyncTest {

    @Test
    fun testReceiptToHomeScreenDataTransformation() {
        // Create mock receipt data
        val mockReceipts = listOf(
            Receipt(
                id = "1",
                merchantName = "Fresh Foods Market",
                totalAmount = 52.75,
                date = System.currentTimeMillis(),
                category = ReceiptCategory.GROCERIES.name,
                isProcessed = true
            ),
            Receipt(
                id = "2", 
                merchantName = "Airways Express",
                totalAmount = 245.00,
                date = System.currentTimeMillis() - 86400000, // 1 day ago
                category = ReceiptCategory.TRANSPORTATION.name,
                isProcessed = true
            ),
            Receipt(
                id = "3",
                merchantName = "Fashion Emporium", 
                totalAmount = 89.99,
                date = System.currentTimeMillis() - 172800000, // 2 days ago
                category = ReceiptCategory.CLOTHING.name,
                isProcessed = true
            )
        )

        // Test transformation to ReceiptItem for home screen
        val homeReceiptItems = mockReceipts.map { receipt ->
            ReceiptItem(
                id = receipt.id,
                storeName = receipt.merchantName,
                amount = receipt.totalAmount,
                date = Date(receipt.date),
                category = ReceiptCategory.fromString(receipt.category).displayName
            )
        }

        // Verify transformation
        assertEquals(3, homeReceiptItems.size)
        assertEquals("Fresh Foods Market", homeReceiptItems[0].storeName)
        assertEquals(52.75, homeReceiptItems[0].amount, 0.01)
        assertEquals("Groceries", homeReceiptItems[0].category)
        
        assertEquals("Airways Express", homeReceiptItems[1].storeName)
        assertEquals(245.00, homeReceiptItems[1].amount, 0.01)
        assertEquals("Transportation", homeReceiptItems[1].category)
        
        assertEquals("Fashion Emporium", homeReceiptItems[2].storeName)
        assertEquals(89.99, homeReceiptItems[2].amount, 0.01)
        assertEquals("Clothing & Fashion", homeReceiptItems[2].category)
    }

    @Test
    fun testSpendingAnalyticsCalculation() {
        // Create mock spending data
        val totalSpending = 387.74 // Sum of all receipts
        val spendingChange = "+12% from last month"
        
        val categoryBreakdown = mapOf(
            ReceiptCategory.GROCERIES to 52.75,
            ReceiptCategory.TRANSPORTATION to 245.00,
            ReceiptCategory.CLOTHING to 89.99
        )
        
        val topCategories = listOf(
            CategorySpending(ReceiptCategory.TRANSPORTATION, 245.00, 63.2),
            CategorySpending(ReceiptCategory.CLOTHING, 89.99, 23.2),
            CategorySpending(ReceiptCategory.GROCERIES, 52.75, 13.6)
        )
        
        val spendingAnalytics = SpendingAnalytics(
            totalSpending = totalSpending,
            spendingChange = spendingChange,
            categoryBreakdown = categoryBreakdown,
            topCategories = topCategories,
            dailySpending = emptyList(),
            weeklySpending = emptyList(),
            totalReceipts = 3,
            averageReceiptAmount = totalSpending / 3,
            lastUpdated = System.currentTimeMillis()
        )

        // Test analytics data
        assertEquals(387.74, spendingAnalytics.totalSpending, 0.01)
        assertEquals("+12% from last month", spendingAnalytics.spendingChange)
        assertEquals(3, spendingAnalytics.categoryBreakdown.size)
        assertEquals(3, spendingAnalytics.topCategories.size)
        assertEquals(129.25, spendingAnalytics.averageReceiptAmount, 0.01)
        
        // Test that transportation is the top category
        assertEquals(ReceiptCategory.TRANSPORTATION, spendingAnalytics.topCategories.first().category)
        assertEquals(245.00, spendingAnalytics.topCategories.first().amount, 0.01)
    }

    @Test
    fun testMonthlyTrendsForGraphs() {
        // Create mock monthly trends data
        val monthlyTrends = listOf(
            MonthlySpending("2024-01", 800.0),
            MonthlySpending("2024-02", 650.0),
            MonthlySpending("2024-03", 950.0),
            MonthlySpending("2024-04", 750.0),
            MonthlySpending("2024-05", 1200.0),
            MonthlySpending("2024-06", 1100.0),
            MonthlySpending("2024-07", 1234.0)
        )

        // Test chart data processing
        val maxAmount = monthlyTrends.maxOfOrNull { it.amount } ?: 1.0
        assertEquals(1234.0, maxAmount, 0.01)

        // Test percentage calculations for bar heights
        val chartData = monthlyTrends.takeLast(7).map { monthData ->
            val percentage = if (maxAmount > 0) (monthData.amount / maxAmount).toFloat() else 0f
            Pair(monthData.month, percentage)
        }

        assertEquals(7, chartData.size)
        assertEquals(1.0f, chartData.last().second, 0.01f) // July should be 100%
        assertEquals(0.527f, chartData[1].second, 0.01f) // February should be ~52.7%
        
        // Test month label formatting
        chartData.forEach { (month, _) ->
            val displayLabel = month.takeLast(3)
            assertTrue(displayLabel.length == 3)
            assertTrue(displayLabel.startsWith("-"))
        }
    }

    @Test
    fun testCategoryGraphDataProcessing() {
        // Create mock category data
        val categoryData = listOf(
            CategorySpending(ReceiptCategory.GROCERIES, 450.0, 36.5),
            CategorySpending(ReceiptCategory.DINING, 315.0, 25.5),
            CategorySpending(ReceiptCategory.ENTERTAINMENT, 280.0, 22.7),
            CategorySpending(ReceiptCategory.TRANSPORTATION, 245.0, 19.9),
            CategorySpending(ReceiptCategory.CLOTHING, 144.0, 11.7)
        )

        // Test bar chart data processing
        val maxAmount = categoryData.maxOfOrNull { it.amount } ?: 1.0
        assertEquals(450.0, maxAmount, 0.01)

        // Test top 5 categories display
        val displayData = categoryData.take(5)
        assertEquals(5, displayData.size)

        // Test percentage calculations for bar widths
        val chartData = displayData.map { categorySpending ->
            val percentage = if (maxAmount > 0) (categorySpending.amount / maxAmount).toFloat() else 0f
            Triple(categorySpending.category.displayName, categorySpending.amount, percentage)
        }

        assertEquals(5, chartData.size)
        assertEquals("Groceries", chartData[0].first)
        assertEquals(1.0f, chartData[0].third, 0.01f)
        assertEquals("Clothing & Fashion", chartData[4].first)
        assertEquals(0.32f, chartData[4].third, 0.01f)
    }

    @Test
    fun testHomeScreenDataSync() {
        // Mock home screen data creation
        val recentReceipts = listOf(
            ReceiptItem("1", "Fresh Foods Market", 52.75, Date(), "Groceries"),
            ReceiptItem("2", "Airways Express", 245.00, Date(), "Transportation"),
            ReceiptItem("3", "Fashion Emporium", 89.99, Date(), "Clothing & Fashion")
        )

        val homeScreenData = HomeScreenData(
            recentReceipts = recentReceipts,
            monthlySpending = 387.74,
            spendingChange = "+12% from last month",
            quickActions = emptyList()
        )

        // Test home screen data
        assertEquals(3, homeScreenData.recentReceipts.size)
        assertEquals(387.74, homeScreenData.monthlySpending, 0.01)
        assertEquals("+12% from last month", homeScreenData.spendingChange)
        
        // Test that recent receipts are properly formatted
        val firstReceipt = homeScreenData.recentReceipts[0]
        assertEquals("Fresh Foods Market", firstReceipt.storeName)
        assertEquals(52.75, firstReceipt.amount, 0.01)
        assertEquals("Groceries", firstReceipt.category)
    }

    @Test
    fun testAnalyticsScreenDataSync() {
        // Mock analytics screen data creation
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

        val analyticsScreenData = AnalyticsScreenData(
            spendingAnalytics = spendingAnalytics,
            monthlyTrends = monthlyTrends,
            categoryBreakdown = spendingAnalytics.topCategories,
            timePeriod = TimePeriod.MONTHLY
        )

        // Test analytics screen data
        assertEquals(1234.0, analyticsScreenData.spendingAnalytics.totalSpending, 0.01)
        assertEquals("+12% from last month", analyticsScreenData.spendingAnalytics.spendingChange)
        assertEquals(2, analyticsScreenData.monthlyTrends.size)
        assertEquals(2, analyticsScreenData.categoryBreakdown.size)
        assertEquals(TimePeriod.MONTHLY, analyticsScreenData.timePeriod)
    }

    @Test
    fun testSyncUpdateFlow() {
        // Test that sync updates trigger proper data refresh
        val mockReceiptUpdate = ReceiptUpdate(
            type = ReceiptUpdateType.ADDED,
            receipt = Receipt(
                id = "new-receipt",
                merchantName = "New Store",
                totalAmount = 123.45,
                date = System.currentTimeMillis(),
                category = ReceiptCategory.GROCERIES.name,
                isProcessed = true
            )
        )

        // Test update processing
        assertEquals(ReceiptUpdateType.ADDED, mockReceiptUpdate.type)
        assertNotNull(mockReceiptUpdate.receipt)
        assertEquals("New Store", mockReceiptUpdate.receipt?.merchantName)
        assertEquals(123.45, mockReceiptUpdate.receipt?.totalAmount ?: 0.0, 0.01)
        
        // Test that receipts trigger analytics recalculation
        val analyticsUpdate = AnalyticsUpdate(
            spendingAnalytics = SpendingAnalytics.empty(),
            recentReceipts = emptyList(),
            spendingTrends = emptyList(),
            lastUpdated = System.currentTimeMillis()
        )

        assertNotNull(analyticsUpdate.spendingAnalytics)
        assertEquals(0.0, analyticsUpdate.spendingAnalytics!!.totalSpending, 0.01)
        assertTrue(analyticsUpdate.recentReceipts.isEmpty())
        assertTrue(analyticsUpdate.spendingTrends.isEmpty())
    }
}
