package com.receiptr.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Typography Usage Guide for Poppins Font Implementation
 * 
 * This file demonstrates how to use the new Poppins typography system
 * throughout your Receiptr app according to the specifications provided.
 */

@Composable
fun TypographyShowcase() {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // App Title/Main Screen Heading (H1): Poppins SemiBold (600) or Bold (700)
        Text(
            text = "Welcome to Receiptr",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Track Your Expenses",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Section Headings (H2, H3): Poppins Medium (500) or SemiBold (600)
        Text(
            text = "Recent Receipts",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Monthly Summary",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Categories",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Main Content Text/Paragraphs: Poppins Regular (400)
        Text(
            text = "This is your main content text for paragraphs and general information throughout the app.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Medium sized body text for secondary information and descriptions.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Small body text for additional details and fine print.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Slightly Emphasized Body Text/Subheadings: Poppins Medium (500)
        Text(
            text = "Emphasized subheading text",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Primary Action Buttons: Poppins SemiBold (600)
        Text(
            text = "ADD RECEIPT",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Secondary/Tertiary Buttons & Navigation: Poppins Medium (500)
        Text(
            text = "Cancel",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Skip",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Form Labels, Captions, Helper Text: Poppins Regular (400) or Light (300)
        Text(
            text = "Email Address",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Text(
            text = "Enter your email address",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Text(
            text = "Required field",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Display of Stored Data - Key numerical values: Poppins Medium (500) or SemiBold (600)
        Text(
            text = "$1,234.56",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Display of Stored Data - Regular values: Poppins Regular (400)
        Text(
            text = "Transaction on 2024-01-15",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Navigation Bar/Tab Labels: Poppins Medium (500) or SemiBold (600)
        Text(
            text = "Home",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // In-app Links/Menu Items: Poppins Regular (400) or Medium (500)
        Text(
            text = "View All Receipts",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Text within Input Fields: Poppins Regular (400)
        Text(
            text = "Enter receipt amount...",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

/**
 * Quick reference for typography usage throughout the app:
 * 
 * App Title/Main Screen Heading (H1): 
 * - MaterialTheme.typography.displayLarge (Bold)
 * - MaterialTheme.typography.displayMedium (SemiBold)
 * 
 * Section Headings (H2, H3):
 * - MaterialTheme.typography.headlineLarge (SemiBold)
 * - MaterialTheme.typography.headlineMedium (Medium)
 * - MaterialTheme.typography.headlineSmall (Medium)
 * 
 * Main Content Text/Paragraphs:
 * - MaterialTheme.typography.bodyLarge (Regular)
 * - MaterialTheme.typography.bodyMedium (Regular)
 * - MaterialTheme.typography.bodySmall (Regular)
 * 
 * Slightly Emphasized Body Text/Subheadings:
 * - MaterialTheme.typography.bodyLarge (Medium)
 * 
 * Primary Action Buttons:
 * - MaterialTheme.typography.titleLarge (SemiBold)
 * 
 * Secondary/Tertiary Buttons:
 * - MaterialTheme.typography.titleMedium (Medium)
 * - MaterialTheme.typography.titleSmall (Medium)
 * 
 * Form Labels, Captions, Helper Text:
 * - MaterialTheme.typography.labelLarge (Regular)
 * - MaterialTheme.typography.labelMedium (Regular)
 * - MaterialTheme.typography.labelSmall (Light)
 * 
 * Navigation Bar/Tab Labels:
 * - MaterialTheme.typography.labelLarge (Medium)
 * 
 * In-app Links/Menu Items:
 * - MaterialTheme.typography.bodyLarge (Regular)
 * 
 * Text within Input Fields:
 * - MaterialTheme.typography.bodyMedium (Regular)
 * 
 * Display of Stored Data:
 * - MaterialTheme.typography.headlineSmall (SemiBold for key values)
 * - MaterialTheme.typography.bodyMedium (Regular for most values)
 */
