package com.receiptr.data.ml

import android.graphics.Point
import android.graphics.Rect
import com.receiptr.data.ml.*
import com.receiptr.data.ml.parser.DateParserService
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReceiptParserServiceTest {

    private lateinit var receiptParserService: ReceiptParserService
    private lateinit var mockDateParserService: DateParserService

    @Before
    fun setup() {
        mockDateParserService = mockk()
        receiptParserService = ReceiptParserService(mockDateParserService)
    }

    // Test data creation helpers
    private fun createTextBlock(
        text: String,
        top: Int = 0,
        left: Int = 0,
        right: Int = 100,
        bottom: Int = 20
    ): TextBlock {
        val boundingBox = Rect(left, top, right, bottom)
        val cornerPoints = listOf(
            Point(left, top),
            Point(right, top),
            Point(right, bottom),
            Point(left, bottom)
        )
        val textElement = TextElement(text, boundingBox, cornerPoints)
        val textLine = TextLine(text, boundingBox, cornerPoints, listOf(textElement))
        return TextBlock(text, boundingBox, cornerPoints, listOf(textLine))
    }

    private fun createTextRecognitionResult(textBlocks: List<TextBlock>): TextRecognitionResult {
        val fullText = textBlocks.joinToString("\n") { block ->
            block.lines.joinToString("\n") { it.text }
        }
        return TextRecognitionResult(
            fullText = fullText,
            textBlocks = textBlocks,
            isSuccess = true
        )
    }

    // ==================== DATE PARSING TESTS ====================

    @Test
    fun `test date extraction with DateParserService integration - DD-MM-YYYY format`() {
        // Given: Mock successful date parsing
        val dateString = "17/07/2025"
        val expectedTimestamp = 1753315200000L // July 17, 2025
        every { mockDateParserService.parseDate(dateString) } returns expectedTimestamp
        every { mockDateParserService.formatDate(expectedTimestamp) } returns "17/07/2025"

        val textBlocks = listOf(
            createTextBlock("SUPER MART", top = 0),
            createTextBlock("Date: $dateString", top = 20),
            createTextBlock("Total: Rs.150.00", top = 200)
        )
        val textResult = createTextRecognitionResult(textBlocks)

        // When
        val result = receiptParserService.parseReceipt(textResult)

        // Then
        assertEquals("17/07/2025", result.date)
        verify { mockDateParserService.parseDate(dateString) }
        verify { mockDateParserService.formatDate(expectedTimestamp) }
    }

    @Test
    fun `test date extraction with DateParserService integration - MM-DD-YYYY format`() {
        // Given
        val dateString = "07/17/2025"
        val expectedTimestamp = 1753315200000L
        every { mockDateParserService.parseDate(dateString) } returns expectedTimestamp
        every { mockDateParserService.formatDate(expectedTimestamp) } returns "17/07/2025"

        val textBlocks = listOf(
            createTextBlock("GROCERY STORE", top = 0),
            createTextBlock(dateString, top = 20),
            createTextBlock("Total: $25.50", top = 200)
        )
        val textResult = createTextRecognitionResult(textBlocks)

        // When
        val result = receiptParserService.parseReceipt(textResult)

        // Then
        assertEquals("17/07/2025", result.date)
        verify { mockDateParserService.parseDate(dateString) }
    }

    @Test
    fun `test date extraction with DateParserService integration - ISO format`() {
        // Given
        val dateString = "2025-07-17"
        val expectedTimestamp = 1753315200000L
        every { mockDateParserService.parseDate(dateString) } returns expectedTimestamp
        every { mockDateParserService.formatDate(expectedTimestamp) } returns "17/07/2025"

        val textBlocks = listOf(
            createTextBlock("TECH STORE", top = 0),
            createTextBlock("Transaction Date: $dateString", top = 20),
            createTextBlock("Amount: £45.99", top = 200)
        )
        val textResult = createTextRecognitionResult(textBlocks)

        // When
        val result = receiptParserService.parseReceipt(textResult)

        // Then
        assertEquals("17/07/2025", result.date)
        verify { mockDateParserService.parseDate(dateString) }
    }

    @Test
    fun `test date extraction with DateParserService integration - abbreviated month format`() {
        // Given
        val dateString = "17 Jul 2025"
        val expectedTimestamp = 1753315200000L
        every { mockDateParserService.parseDate(dateString) } returns expectedTimestamp
        every { mockDateParserService.formatDate(expectedTimestamp) } returns "17/07/2025"

        val textBlocks = listOf(
            createTextBlock("RESTAURANT ABC", top = 0),
            createTextBlock(dateString, top = 20),
            createTextBlock("Total: €32.75", top = 200)
        )
        val textResult = createTextRecognitionResult(textBlocks)

        // When
        val result = receiptParserService.parseReceipt(textResult)

        // Then
        assertEquals("17/07/2025", result.date)
        verify { mockDateParserService.parseDate(dateString) }
    }

    @Test
    fun `test date extraction with DateParserService integration - full month format`() {
        // Given
        val dateString = "17 January 2025"
        val expectedTimestamp = 1737072000000L // January 17, 2025
        every { mockDateParserService.parseDate(dateString) } returns expectedTimestamp
        every { mockDateParserService.formatDate(expectedTimestamp) } returns "17/01/2025"

        val textBlocks = listOf(
            createTextBlock("COFFEE SHOP", top = 0),
            createTextBlock("Date: $dateString", top = 20),
            createTextBlock("Total: $8.50", top = 200)
        )
        val textResult = createTextRecognitionResult(textBlocks)

        // When
        val result = receiptParserService.parseReceipt(textResult)

        // Then
        assertEquals("17/01/2025", result.date)
        verify { mockDateParserService.parseDate(dateString) }
    }

    @Test
    fun `test date extraction with DateParserService integration - American month day year format`() {
        // Given
        val dateString = "Jul 17, 2025"
        val expectedTimestamp = 1753315200000L
        every { mockDateParserService.parseDate(dateString) } returns expectedTimestamp
        every { mockDateParserService.formatDate(expectedTimestamp) } returns "17/07/2025"

        val textBlocks = listOf(
            createTextBlock("WALMART", top = 0),
            createTextBlock("Purchase Date: $dateString", top = 20),
            createTextBlock("Total: $125.43", top = 200)
        )
        val textResult = createTextRecognitionResult(textBlocks)

        // When
        val result = receiptParserService.parseReceipt(textResult)

        // Then
        assertEquals("17/07/2025", result.date)
        verify { mockDateParserService.parseDate(dateString) }
    }

    @Test
    fun `test date extraction with DateParserService integration - 2-digit year normalization`() {
        // Given
        val dateString = "17/07/25"
        val expectedTimestamp = 1753315200000L // Should be interpreted as 2025
        every { mockDateParserService.parseDate(dateString) } returns expectedTimestamp
        every { mockDateParserService.formatDate(expectedTimestamp) } returns "17/07/2025"

        val textBlocks = listOf(
            createTextBlock("QUICK MART", top = 0),
            createTextBlock(dateString, top = 20),
            createTextBlock("Total: Rs.75.00", top = 200)
        )
        val textResult = createTextRecognitionResult(textBlocks)

        // When
        val result = receiptParserService.parseReceipt(textResult)

        // Then
        assertEquals("17/07/2025", result.date)
        verify { mockDateParserService.parseDate(dateString) }
    }

    @Test
    fun `test date extraction with DateParserService integration - dashed format`() {
        // Given
        val dateString = "17-Jul-25"
        val expectedTimestamp = 1753315200000L
        every { mockDateParserService.parseDate(dateString) } returns expectedTimestamp
        every { mockDateParserService.formatDate(expectedTimestamp) } returns "17/07/2025"

        val textBlocks = listOf(
            createTextBlock("GAS STATION", top = 0),
            createTextBlock("Date: $dateString", top = 20),
            createTextBlock("Total: $45.20", top = 200)
        )
        val textResult = createTextRecognitionResult(textBlocks)

        // When
        val result = receiptParserService.parseReceipt(textResult)

        // Then
        assertEquals("17/07/2025", result.date)
        verify { mockDateParserService.parseDate(dateString) }
    }

    @Test
    fun `test date extraction fallback when DateParserService returns null`() {
        // Given: DateParserService returns null for all attempts
        every { mockDateParserService.parseDate(any()) } returns null

        val textBlocks = listOf(
            createTextBlock("STORE NAME", top = 0),
            createTextBlock("Invalid Date: 99/99/9999", top = 20),
            createTextBlock("Total: $50.00", top = 200)
        )
        val textResult = createTextRecognitionResult(textBlocks)

        // When
        val result = receiptParserService.parseReceipt(textResult)

        // Then
        assertNull(result.date)
        verify(atLeast = 1) { mockDateParserService.parseDate(any()) }
    }

    @Test
    fun `test date extraction with multiple date candidates - first valid wins`() {
        // Given: Multiple potential dates, first valid one should be selected
        val validDate = "17/07/2025"
        val invalidDate = "99/99/9999"
        val expectedTimestamp = 1753315200000L

        every { mockDateParserService.parseDate(invalidDate) } returns null
        every { mockDateParserService.parseDate(validDate) } returns expectedTimestamp
        every { mockDateParserService.formatDate(expectedTimestamp) } returns "17/07/2025"

        val textBlocks = listOf(
            createTextBlock("RETAIL STORE", top = 0),
            createTextBlock("Invalid: $invalidDate", top = 20),
            createTextBlock("Valid: $validDate", top = 40),
            createTextBlock("Total: €89.99", top = 200)
        )
        val textResult = createTextRecognitionResult(textBlocks)

        // When
        val result = receiptParserService.parseReceipt(textResult)

        // Then
        assertEquals("17/07/2025", result.date)
        verify { mockDateParserService.parseDate(invalidDate) }
        verify { mockDateParserService.parseDate(validDate) }
    }

    @Test
    fun `test date extraction with mixed content lines`() {
        // Given: Date embedded in a line with other content
        val mixedLine = "Transaction on 17 Jul 2025 at Store #123"
        val expectedTimestamp = 1753315200000L
        every { mockDateParserService.parseDate(mixedLine) } returns expectedTimestamp
        every { mockDateParserService.formatDate(expectedTimestamp) } returns "17/07/2025"

        val textBlocks = listOf(
            createTextBlock("BIG MART", top = 0),
            createTextBlock(mixedLine, top = 20),
            createTextBlock("Total: $67.43", top = 200)
        )
        val textResult = createTextRecognitionResult(textBlocks)

        // When
        val result = receiptParserService.parseReceipt(textResult)

        // Then
        assertEquals("17/07/2025", result.date)
        verify { mockDateParserService.parseDate(mixedLine) }
    }

    // ==================== COMPREHENSIVE RECEIPT PARSING TESTS ====================

    @Test
    fun `test complete receipt parsing with enhanced date integration`() {
        // Given: Complete receipt with date
        val dateString = "2025-07-17"
        val expectedTimestamp = 1753315200000L
        every { mockDateParserService.parseDate(dateString) } returns expectedTimestamp
        every { mockDateParserService.formatDate(expectedTimestamp) } returns "17/07/2025"

        val textBlocks = listOf(
            createTextBlock("WALMART SUPERCENTER", top = 0),
            createTextBlock("123 Main St, City, State 12345", top = 20),
            createTextBlock("Phone: (555) 123-4567", top = 40),
            createTextBlock("Date: $dateString", top = 60),
            createTextBlock("Time: 14:30:25", top = 80),
            createTextBlock("Milk 2% Gallon     $3.99", top = 120),
            createTextBlock("Bread Whole Wheat  $2.49", top = 140),
            createTextBlock("Apples Red 3lb     $4.99", top = 160),
            createTextBlock("Subtotal:         $11.47", top = 180),
            createTextBlock("Tax:              $0.92", top = 200),
            createTextBlock("Total:           $12.39", top = 220),
            createTextBlock("Credit Card Payment", top = 240)
        )
        val textResult = createTextRecognitionResult(textBlocks)

        // When
        val result = receiptParserService.parseReceipt(textResult)

        // Then
        assertEquals("WALMART SUPERCENTER", result.merchantName)
        assertEquals("17/07/2025", result.date)
        assertEquals("14:30:25", result.time)
        assertEquals("12.39", result.total)
        assertEquals("11.47", result.subtotal)
        assertEquals("0.92", result.tax)
        assertEquals("CREDIT", result.paymentMethod)
        assertTrue(result.items.isNotEmpty())
        verify { mockDateParserService.parseDate(dateString) }
        verify { mockDateParserService.formatDate(expectedTimestamp) }
    }

    @Test
    fun `test receipt parsing with Indian format and DateParserService`() {
        // Given: Indian receipt format
        val dateString = "17/07/25"
        val expectedTimestamp = 1753315200000L
        every { mockDateParserService.parseDate(dateString) } returns expectedTimestamp
        every { mockDateParserService.formatDate(expectedTimestamp) } returns "17/07/2025"

        val textBlocks = listOf(
            createTextBlock("RELIANCE FRESH", top = 0),
            createTextBlock("Mumbai, Maharashtra", top = 20),
            createTextBlock("Date: $dateString", top = 40),
            createTextBlock("Rice Basmati 1kg   Rs.120.00", top = 80),
            createTextBlock("Dal Moong 500g     Rs.85.50", top = 100),
            createTextBlock("Oil Sunflower 1L   Rs.95.00", top = 120),
            createTextBlock("Total Amount:      Rs.300.50", top = 160)
        )
        val textResult = createTextRecognitionResult(textBlocks)

        // When
        val result = receiptParserService.parseReceipt(textResult)

        // Then
        assertEquals("RELIANCE FRESH", result.merchantName)
        assertEquals("17/07/2025", result.date)
        assertEquals("300.50", result.total)
        verify { mockDateParserService.parseDate(dateString) }
    }

    @Test
    fun `test date extraction priority - top blocks searched first`() {
        // Given: Multiple dates with DateParserService called in order
        val topDate = "17/07/2025"
        val bottomDate = "18/07/2025"
        val topTimestamp = 1753315200000L
        val bottomTimestamp = 1753401600000L

        // Mock the service to return valid timestamps for both dates
        every { mockDateParserService.parseDate(topDate) } returns topTimestamp
        every { mockDateParserService.parseDate(bottomDate) } returns bottomTimestamp
        every { mockDateParserService.formatDate(topTimestamp) } returns "17/07/2025"
        every { mockDateParserService.formatDate(bottomTimestamp) } returns "18/07/2025"

        val textBlocks = listOf(
            createTextBlock("STORE NAME", top = 0),
            createTextBlock("Date: $topDate", top = 20), // This should be found first
            createTextBlock("Item 1: $10.00", top = 100),
            createTextBlock("Previous Date: $bottomDate", top = 200), // This comes later
            createTextBlock("Total: $10.00", top = 220)
        )
        val textResult = createTextRecognitionResult(textBlocks)

        // When
        val result = receiptParserService.parseReceipt(textResult)

        // Then
        assertEquals("17/07/2025", result.date) // Top date should win
        verify { mockDateParserService.parseDate(topDate) }
        verify { mockDateParserService.formatDate(topTimestamp) }
    }

    @Test
    fun `test date extraction with edge case formats handled by DateParserService`() {
        // Given: Various edge case date formats
        val edgeCaseDates = listOf(
            "1/1/25",      // Single digit month/day, 2-digit year
            "31-Dec-2024", // End of year
            "29 Feb 2024", // Leap year
            "Dec 31, 24"   // American format with 2-digit year
        )

        edgeCaseDates.forEachIndexed { index, dateString ->
            // Setup mock for each date
            val timestamp = 1735689600000L + (index * 86400000L) // Different timestamps
            every { mockDateParserService.parseDate(dateString) } returns timestamp
            every { mockDateParserService.formatDate(timestamp) } returns "31/12/2024"

            val textBlocks = listOf(
                createTextBlock("TEST STORE", top = 0),
                createTextBlock("Date: $dateString", top = 20),
                createTextBlock("Total: $100.00", top = 100)
            )
            val textResult = createTextRecognitionResult(textBlocks)

            // When
            val result = receiptParserService.parseReceipt(textResult)

            // Then
            assertEquals("31/12/2024", result.date)
            verify { mockDateParserService.parseDate(dateString) }

            // Clear invocations for next iteration
            clearMocks(mockDateParserService, answers = false)
        }
    }

    @Test
    fun `test date extraction with DateParserService exception handling`() {
        // Given: DateParserService throws exception
        every { mockDateParserService.parseDate(any()) } throws RuntimeException("Parsing error")

        val textBlocks = listOf(
            createTextBlock("STORE NAME", top = 0),
            createTextBlock("Date: 17/07/2025", top = 20),
            createTextBlock("Total: $50.00", top = 100)
        )
        val textResult = createTextRecognitionResult(textBlocks)

        // When & Then: Should not crash and return null date
        val result = receiptParserService.parseReceipt(textResult)
        assertNull(result.date)
    }

    @Test
    fun `test receipt parsing without date - should not crash`() {
        // Given: Receipt with no date information
        every { mockDateParserService.parseDate(any()) } returns null

        val textBlocks = listOf(
            createTextBlock("STORE NAME", top = 0),
            createTextBlock("Item 1: $10.00", top = 40),
            createTextBlock("Total: $10.00", top = 80)
        )
        val textResult = createTextRecognitionResult(textBlocks)

        // When
        val result = receiptParserService.parseReceipt(textResult)

        // Then
        assertNull(result.date)
        assertEquals("STORE NAME", result.merchantName)
        assertEquals("10.00", result.total)
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    fun `test real world receipt parsing with multiple date formats`() {
        // Given: Realistic receipt with DateParserService handling different formats
        val scenarios = listOf(
            Triple("McDonald's Receipt", "07/17/2025", 1753315200000L),
            Triple("Gas Station", "2025-07-17", 1753315200000L),
            Triple("Grocery Store", "17 Jul 25", 1753315200000L),
            Triple("Restaurant", "July 17, 2025", 1753315200000L)
        )

        scenarios.forEach { (storeName, dateStr, timestamp) ->
            // Setup
            every { mockDateParserService.parseDate(dateStr) } returns timestamp
            every { mockDateParserService.formatDate(timestamp) } returns "17/07/2025"

            val textBlocks = listOf(
                createTextBlock(storeName, top = 0),
                createTextBlock("Date: $dateStr", top = 20),
                createTextBlock("Item 1       $5.99", top = 60),
                createTextBlock("Tax          $0.48", top = 80),
                createTextBlock("Total:       $6.47", top = 100)
            )
            val textResult = createTextRecognitionResult(textBlocks)

            // When
            val result = receiptParserService.parseReceipt(textResult)

            // Then
            assertEquals("17/07/2025", result.date)
            assertEquals(storeName, result.merchantName)
            assertEquals("6.47", result.total)

            // Clear for next iteration
            clearMocks(mockDateParserService, answers = false)
        }
    }

    @Test
    fun `test date parsing performance with multiple text blocks`() {
        // Given: Large receipt with many blocks, date should be found efficiently
        val dateString = "17/07/2025"
        val timestamp = 1753315200000L
        every { mockDateParserService.parseDate(dateString) } returns timestamp
        every { mockDateParserService.formatDate(timestamp) } returns "17/07/2025"
        every { mockDateParserService.parseDate(neq(dateString)) } returns null

        val textBlocks = mutableListOf<TextBlock>()
        // Add many blocks without dates
        repeat(50) { index ->
            textBlocks.add(createTextBlock("Item $index: $${index + 1}.00", top = index * 20))
        }
        // Add date block at position 5
        textBlocks.add(5, createTextBlock("Date: $dateString", top = 100))

        val textResult = createTextRecognitionResult(textBlocks)

        // When
        val result = receiptParserService.parseReceipt(textResult)

        // Then
        assertEquals("17/07/2025", result.date)
        // Verify DateParserService was called for the date string
        verify { mockDateParserService.parseDate(dateString) }
    }
}
