package com.receiptr.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = Outline,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = ReceiptrPrimaryGreenLight,
    onPrimary = OnPrimaryText,
    primaryContainer = ReceiptrSecondaryGreenLight,
    onPrimaryContainer = ReceiptrDarkGreenLight,
    secondary = ReceiptrSecondaryGreenLight,
    onSecondary = OnPrimaryText,
    secondaryContainer = CardBackgroundLight,
    onSecondaryContainer = ReceiptrDarkGreenLight,
    tertiary = ReceiptrPrimaryGreenLight,
    onTertiary = OnPrimaryText,
    tertiaryContainer = Color(0xFFDCEDC8),
    onTertiaryContainer = ReceiptrDarkGreenLight,
    background = ReceiptrBackgroundLight,
    onBackground = ReceiptrDarkGreenLight,
    surface = CardBackgroundWhite,
    onSurface = ReceiptrDarkGreenLight,
    surfaceVariant = CardBackgroundLight,
    onSurfaceVariant = ReceiptrDarkGreenLight,
    outline = ReceiptrBorderGreenLight,
    outlineVariant = ReceiptrBorderGreenLight,
    error = ErrorRed,
    onError = OnPrimaryText,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFD32F2F)
)

@Composable
fun ReceiptrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ but disabled by default to use custom theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}