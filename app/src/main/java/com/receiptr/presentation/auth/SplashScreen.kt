package com.receiptr.presentation.auth

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.receiptr.R
import com.receiptr.domain.model.AuthState
import com.receiptr.presentation.viewmodel.AuthViewModel
import com.receiptr.ui.theme.ReceiptrTheme
import com.receiptr.ui.theme.ReceiptrBackground
import com.receiptr.ui.theme.ReceiptrDarkGreen
import com.receiptr.ui.theme.ReceiptrPrimaryGreen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    
    // Launch effect to check authentication state and navigate accordingly
    LaunchedEffect(authState) {
        delay(2000) // 2 second delay to show splash screen
        
        when (authState) {
            is AuthState.Authenticated -> {
                // User is authenticated, navigate to home
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            is AuthState.Unauthenticated -> {
                // User is not authenticated, navigate to welcome/onboarding
                navController.navigate("welcome") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            is AuthState.Loading -> {
                // Still loading, wait for state to change
                // The LaunchedEffect will be triggered again when state changes
            }
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
            // App logo
            Image(
                painter = painterResource(id = R.drawable.receiptr_logo),
                contentDescription = "Receiptr Logo",
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Crop
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
