package com.receiptr.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.receiptr.ui.theme.ReceiptrTheme
import com.receiptr.ui.theme.ReceiptrBackground
import com.receiptr.ui.theme.ReceiptrDarkGreen
import com.receiptr.ui.theme.ReceiptrPrimaryGreen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // Launch effect to navigate after 2 seconds
    LaunchedEffect(Unit) {
        delay(2000) // 2 second delay
        navController.navigate("welcome") {
            // Remove splash screen from back stack so user can't go back to it
            popUpTo("splash") { inclusive = true }
        }
    }

    // Splash screen UI with Figma design colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ReceiptrBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App icon
            Icon(
                imageVector = Icons.Filled.Receipt,
                contentDescription = "Receiptr Logo",
                modifier = Modifier.size(120.dp),
                tint = ReceiptrPrimaryGreen
            )
            
            // App name
            Text(
                text = "Receiptr",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = ReceiptrDarkGreen,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge
            )
            
            // Tagline
            Text(
                text = "AI-Powered Expense Tracking",
                fontSize = 16.sp,
                color = ReceiptrDarkGreen.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    ReceiptrTheme {
        SplashScreen(navController = rememberNavController())
    }
}
