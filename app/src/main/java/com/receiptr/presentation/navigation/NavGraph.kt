package com.receiptr.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.receiptr.presentation.analytics.AnalyticsScreen
import com.receiptr.presentation.auth.*
import com.receiptr.presentation.home.HomeScreen
import com.receiptr.presentation.onboarding.WelcomeScreen
import com.receiptr.presentation.profile.ProfileScreen
import com.receiptr.presentation.receipts.ReceiptsScreen
import com.receiptr.presentation.scan.ScanScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController = navController)
        }
        
        composable("welcome") {
            WelcomeScreen(navController = navController)
        }
        
        composable("login") {
            LoginScreen(navController = navController)
        }
        
        composable("email_auth") {
            EmailAuthScreen(navController = navController)
        }
        
        composable("signup") {
            SignupScreen(navController = navController)
        }
        
        composable("phone_auth") {
            PhoneAuthScreen(navController = navController)
        }
        
        composable("home") {
            HomeScreen(navController = navController)
        }
        
        composable("receipts") {
            ReceiptsScreen(navController = navController)
        }
        
        composable("scan") {
            ScanScreen(navController = navController)
        }
        
        composable("analytics") {
            AnalyticsScreen(navController = navController)
        }
        
        composable("profile") {
            ProfileScreen(navController = navController)
        }
    }
}
