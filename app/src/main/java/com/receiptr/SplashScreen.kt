package com.receiptr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.receiptr.ui.theme.ReceiptrTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // Launch effect to navigate after 2 seconds
    LaunchedEffect(Unit) {
        delay(2000) // 2 second delay
        navController.navigate("login") {
            // Remove splash screen from back stack so user can't go back to it
            popUpTo("splash") { inclusive = true }
        }
    }

    // Splash screen UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App logo or icon (you can replace this with your app's actual logo)
            Image(
                painter = painterResource(id = android.R.drawable.sym_def_app_icon),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp)
            )
            
            // App name
            Text(
                text = "Receiptr",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            // Tagline or subtitle
            Text(
                text = "Track your receipts with ease",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
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
