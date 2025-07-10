package com.receiptr.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.receiptr.presentation.analytics.AnalyticsScreen
import com.receiptr.presentation.auth.*
import com.receiptr.presentation.home.HomeScreen
import com.receiptr.presentation.onboarding.WelcomeScreen
import com.receiptr.presentation.profile.ProfileScreen
import com.receiptr.presentation.receipts.ReceiptsScreen
import com.receiptr.presentation.scan.ScanScreen

@Composable
fun NavGraph(navController: NavHostController) {
    val currentDestination by navController.currentBackStackEntryAsState()
    
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable(
            "splash",
            enterTransition = NavigationAnimationSpecs.fadeEnter(),
            exitTransition = NavigationAnimationSpecs.fadeExit()
        ) {
            SplashScreen(navController = navController)
        }
        
        composable(
            "welcome",
            enterTransition = NavigationAnimationSpecs.forwardSlide(),
            exitTransition = NavigationAnimationSpecs.forwardSlideExit(),
            popEnterTransition = NavigationAnimationSpecs.backwardSlide(),
            popExitTransition = NavigationAnimationSpecs.backwardSlideExit()
        ) {
            WelcomeScreen(navController = navController)
        }
        
        composable(
            "login",
            enterTransition = NavigationAnimationSpecs.forwardSlide(),
            exitTransition = NavigationAnimationSpecs.forwardSlideExit(),
            popEnterTransition = NavigationAnimationSpecs.backwardSlide(),
            popExitTransition = NavigationAnimationSpecs.backwardSlideExit()
        ) {
            LoginScreen(navController = navController)
        }
        
        composable(
            "email_auth",
            enterTransition = NavigationAnimationSpecs.forwardSlide(),
            exitTransition = NavigationAnimationSpecs.forwardSlideExit(),
            popEnterTransition = NavigationAnimationSpecs.backwardSlide(),
            popExitTransition = NavigationAnimationSpecs.backwardSlideExit()
        ) {
            EmailAuthScreen(navController = navController)
        }
        
        composable(
            "signup",
            enterTransition = NavigationAnimationSpecs.forwardSlide(),
            exitTransition = NavigationAnimationSpecs.forwardSlideExit(),
            popEnterTransition = NavigationAnimationSpecs.backwardSlide(),
            popExitTransition = NavigationAnimationSpecs.backwardSlideExit()
        ) {
            SignupScreen(navController = navController)
        }
        
        composable(
            "registration",
            enterTransition = NavigationAnimationSpecs.forwardSlide(),
            exitTransition = NavigationAnimationSpecs.forwardSlideExit(),
            popEnterTransition = NavigationAnimationSpecs.backwardSlide(),
            popExitTransition = NavigationAnimationSpecs.backwardSlideExit()
        ) {
            RegistrationScreen(navController = navController)
        }
        
        composable(
            "phone_auth",
            enterTransition = NavigationAnimationSpecs.forwardSlide(),
            exitTransition = NavigationAnimationSpecs.forwardSlideExit(),
            popEnterTransition = NavigationAnimationSpecs.backwardSlide(),
            popExitTransition = NavigationAnimationSpecs.backwardSlideExit()
        ) {
            PhoneAuthScreen(navController = navController)
        }
        
        composable(
            "home",
            enterTransition = NavigationAnimationSpecs.scaleEnter(),
            exitTransition = NavigationAnimationSpecs.scaleExit(),
            popEnterTransition = NavigationAnimationSpecs.scaleEnter(),
            popExitTransition = NavigationAnimationSpecs.scaleExit()
        ) {
            HomeScreen(navController = navController)
        }
        
        composable(
            "receipts",
            enterTransition = NavigationAnimationSpecs.fastSlideEnter(),
            exitTransition = NavigationAnimationSpecs.fastSlideExit(),
            popEnterTransition = NavigationAnimationSpecs.backwardSlide(),
            popExitTransition = NavigationAnimationSpecs.backwardSlideExit()
        ) {
            ReceiptsScreen(navController = navController)
        }
        
        composable(
            "scan",
            enterTransition = NavigationAnimationSpecs.modalEnter(),
            exitTransition = NavigationAnimationSpecs.modalExit(),
            popEnterTransition = NavigationAnimationSpecs.modalEnter(),
            popExitTransition = NavigationAnimationSpecs.modalExit()
        ) {
            ScanScreen(navController = navController)
        }
        
        composable(
            "analytics",
            enterTransition = NavigationAnimationSpecs.fastSlideEnter(),
            exitTransition = NavigationAnimationSpecs.fastSlideExit(),
            popEnterTransition = NavigationAnimationSpecs.backwardSlide(),
            popExitTransition = NavigationAnimationSpecs.backwardSlideExit()
        ) {
            AnalyticsScreen(navController = navController)
        }
        
        composable(
            "profile",
            enterTransition = NavigationAnimationSpecs.fastSlideEnter(),
            exitTransition = NavigationAnimationSpecs.fastSlideExit(),
            popEnterTransition = NavigationAnimationSpecs.backwardSlide(),
            popExitTransition = NavigationAnimationSpecs.backwardSlideExit()
        ) {
            ProfileScreen(navController = navController)
        }
    }
}
