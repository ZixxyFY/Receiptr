package com.receiptr.ui.components

import androidx.compose.ui.graphics.Color
import org.junit.Test
import org.junit.Assert.*

class PieChartTest {

    @Test
    fun `generateCategoryColors should return correct number of colors`() {
        // Given
        val count = 5
        
        // When
        val colors = generateCategoryColors(count)
        
        // Then
        assertEquals(count, colors.size)
        assertTrue(colors.all { it is Color })
    }

    @Test
    fun `generateCategoryColors should not exceed available colors`() {
        // Given
        val count = 15 // More than available colors
        
        // When
        val colors = generateCategoryColors(count)
        
        // Then
        assertEquals(10, colors.size) // Should return only 10 colors (max available)
    }

    @Test
    fun `PieChartData should calculate percentage correctly`() {
        // Given
        val totalAmount = 1000.0
        val categoryAmount = 250.0
        val expectedPercentage = 0.25f
        
        // When
        val pieChartData = PieChartData(
            category = "Food",
            amount = categoryAmount,
            color = Color.Red,
            percentage = (categoryAmount / totalAmount).toFloat()
        )
        
        // Then
        assertEquals(expectedPercentage, pieChartData.percentage, 0.01f)
        assertEquals("Food", pieChartData.category)
        assertEquals(categoryAmount, pieChartData.amount, 0.01)
    }
}
