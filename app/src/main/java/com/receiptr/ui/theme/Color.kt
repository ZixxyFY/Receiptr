package com.receiptr.ui.theme

import androidx.compose.ui.graphics.Color

// App Theme Color Palette - Updated Green Dark Theme

// Primary Colors
val Primary = Color(0xFF66BB6A)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFF2E7D32)
val OnPrimaryContainer = Color(0xFFFFFFFF)

// Secondary Colors
val Secondary = Color(0xFF81C784)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFF4CAF50)
val OnSecondaryContainer = Color(0xFFFFFFFF)

// Tertiary Colors
val Tertiary = Color(0xFF4CAF50)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFF1B5E20)
val OnTertiaryContainer = Color(0xFFFFFFFF)

// Background Colors
val Background = Color(0xFF121212)
val OnBackground = Color(0xFFE0E0E0)

// Surface Colors
val Surface = Color(0xFF1D1D1D)
val OnSurface = Color(0xFFE0E0E0)
val SurfaceVariant = Color(0xFF424242)
val OnSurfaceVariant = Color(0xFFBDBDBD)

// Error Colors
val Error = Color(0xFFCF6679)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFF880E4F)
val OnErrorContainer = Color(0xFFFFFFFF)

// Outline
val Outline = Color(0xFF616161)

// Light Theme Colors (for compatibility - creating lighter versions)
val ReceiptrBackgroundLight = Color(0xFFF1F8E9)  // Light green background
val ReceiptrDarkGreenLight = Color(0xFF1B5E20)   // Dark green for text
val ReceiptrPrimaryGreenLight = Color(0xFF4CAF50) // Primary green for buttons
val ReceiptrSecondaryGreenLight = Color(0xFF66BB6A) // Secondary green for icons
val ReceiptrBorderGreenLight = Color(0xFFDCEDC8)  // Border color

// Dark Theme Colors - Using new palette
val ReceiptrBackgroundDark = Background
val ReceiptrDarkGreenDark = OnBackground
val ReceiptrPrimaryGreenDark = Primary
val ReceiptrSecondaryGreenDark = Secondary
val ReceiptrBorderGreenDark = Outline

// Compatibility aliases (will be updated to use theme-aware colors)
val ReceiptrBackground = ReceiptrBackgroundLight
val ReceiptrDarkGreen = ReceiptrDarkGreenLight
val ReceiptrPrimaryGreen = ReceiptrPrimaryGreenLight
val ReceiptrSecondaryGreen = ReceiptrSecondaryGreenLight
val ReceiptrBorderGreen = ReceiptrBorderGreenLight

// Background Colors - Light Theme
val BackgroundLightGrey = Color(0xFFF1F8E9)
val BackgroundOffWhite = Color(0xFFFAFAFA)
val CardBackgroundWhite = Color(0xFFFFFFFF)
val CardBackgroundLight = Color(0xFFFDFDFD)

// Background Colors - Dark Theme
val BackgroundDarkGrey = Surface
val BackgroundDarkOffBlack = Background
val CardBackgroundDark = Surface
val CardBackgroundDarkLight = SurfaceVariant

// Text Colors
val PrimaryTextDark = Color(0xFF333333)
val PrimaryTextSoft = Color(0xFF4A4A4A)
val SecondaryText = Color(0xFF888888)
val OnPrimaryText = OnPrimary

// Status Colors
val ErrorRed = Error
val SuccessGreen = Primary
val WarningOrange = Color(0xFFFF9800)

// Navigation Colors
val NavSelectedBackground = PrimaryContainer
val NavSelectedText = OnPrimaryContainer

// Legacy colors (keeping for compatibility)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
