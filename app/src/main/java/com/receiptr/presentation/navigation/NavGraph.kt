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
import com.receiptr.presentation.receipts.ReceiptDetailScreen
import com.receiptr.presentation.scan.ScanScreen
import com.receiptr.presentation.scan.PhotoPreviewScreen
import com.receiptr.presentation.scan.ReviewReceiptScreen
import com.receiptr.presentation.settings.SettingsScreen
import com.receiptr.presentation.settings.ThemeSettingsScreen
import com.receiptr.presentation.email.EmailIntegrationScreen
import com.receiptr.presentation.profile.ChangePasswordScreen
import com.receiptr.presentation.profile.NotificationsScreen
import com.receiptr.presentation.profile.HelpCenterScreen
import com.receiptr.presentation.profile.ContactUsScreen
import android.net.Uri
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

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
            enterTransition = NavigationAnimationSpecs.bouncyScaleEnter(),
            exitTransition = NavigationAnimationSpecs.bouncyScaleExit(),
            popEnterTransition = NavigationAnimationSpecs.bouncyScaleEnter(),
            popExitTransition = NavigationAnimationSpecs.bouncyScaleExit()
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
            "receipt_detail/{receiptId}",
            enterTransition = NavigationAnimationSpecs.fastSlideEnter(),
            exitTransition = NavigationAnimationSpecs.fastSlideExit(),
            popEnterTransition = NavigationAnimationSpecs.backwardSlide(),
            popExitTransition = NavigationAnimationSpecs.backwardSlideExit()
        ) { backStackEntry ->
            val receiptId = backStackEntry.arguments?.getString("receiptId") ?: ""
            ReceiptDetailScreen(
                navController = navController,
                receiptId = receiptId
            )
        }
        
        composable(
            "photo_preview/{photoUri}",
            enterTransition = NavigationAnimationSpecs.forwardSlide(),
            exitTransition = NavigationAnimationSpecs.forwardSlideExit(),
            popEnterTransition = NavigationAnimationSpecs.backwardSlide(),
            popExitTransition = NavigationAnimationSpecs.backwardSlideExit()
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("photoUri") ?: ""
            val decodedUri = URLDecoder.decode(encodedUri, StandardCharsets.UTF_8.toString())
            PhotoPreviewScreen(
                navController = navController,
                photoUri = Uri.parse(decodedUri)
            )
        }
        
        composable(
            "review_receipt/{photoUri}",
            enterTransition = NavigationAnimationSpecs.forwardSlide(),
            exitTransition = NavigationAnimationSpecs.forwardSlideExit(),
            popEnterTransition = NavigationAnimationSpecs.backwardSlide(),
            popExitTransition = NavigationAnimationSpecs.backwardSlideExit()
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("photoUri") ?: ""
            val decodedUri = URLDecoder.decode(encodedUri, StandardCharsets.UTF_8.toString())
            ReviewReceiptScreen(
                navController = navController,
                photoUri = Uri.parse(decodedUri)
            )
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
            enterTransition = NavigationAnimationSpecs.crossFadeEnter(),
            exitTransition = NavigationAnimationSpecs.crossFadeExit(),
            popEnterTransition = NavigationAnimationSpecs.crossFadeEnter(),
            popExitTransition = NavigationAnimationSpecs.crossFadeExit()
        ) {
            ProfileScreen(navController = navController)
        }
        
        composable(
            "settings",
            enterTransition = NavigationAnimationSpecs.slideUpEnter(),
            exitTransition = NavigationAnimationSpecs.slideUpExit(),
            popEnterTransition = NavigationAnimationSpecs.slideUpEnter(),
            popExitTransition = NavigationAnimationSpecs.slideUpExit()
        ) {
            SettingsScreen(navController = navController)
        }
        
        composable(
            "theme_settings",
            enterTransition = NavigationAnimationSpecs.forwardSlide(),
            exitTransition = NavigationAnimationSpecs.forwardSlideExit(),
            popEnterTransition = NavigationAnimationSpecs.backwardSlide(),
            popExitTransition = NavigationAnimationSpecs.backwardSlideExit()
        ) {
            ThemeSettingsScreen(navController = navController)
        }
        
        composable(
            "email_integration",
            enterTransition = NavigationAnimationSpecs.forwardSlide(),
            exitTransition = NavigationAnimationSpecs.forwardSlideExit(),
            popEnterTransition = NavigationAnimationSpecs.backwardSlide(),
            popExitTransition = NavigationAnimationSpecs.backwardSlideExit()
        ) {
            EmailIntegrationScreen(navController = navController)
        }
        
        composable(
            "change_password",
            enterTransition = NavigationAnimationSpecs.forwardSlide(),
            exitTransition = NavigationAnimationSpecs.forwardSlideExit(),
            popEnterTransition = NavigationAnimationSpecs.backwardSlide(),
            popExitTransition = NavigationAnimationSpecs.backwardSlideExit()
        ) {
            ChangePasswordScreen(navController = navController)
        }
        
        composable(
            "notifications",
            enterTransition = NavigationAnimationSpecs.forwardSlide(),
            exitTransition = NavigationAnimationSpecs.forwardSlideExit(),
            popEnterTransition = NavigationAnimationSpecs.backwardSlide(),
            popExitTransition = NavigationAnimationSpecs.backwardSlideExit()
        ) {
            NotificationsScreen(navController = navController)
        }
        
        composable(
            "help_center",
            enterTransition = NavigationAnimationSpecs.forwardSlide(),
            exitTransition = NavigationAnimationSpecs.forwardSlideExit(),
            popEnterTransition = NavigationAnimationSpecs.backwardSlide(),
            popExitTransition = NavigationAnimationSpecs.backwardSlideExit()
        ) {
            HelpCenterScreen(navController = navController)
        }
        
        composable(
            "contact_us",
            enterTransition = NavigationAnimationSpecs.forwardSlide(),
            exitTransition = NavigationAnimationSpecs.forwardSlideExit(),
            popEnterTransition = NavigationAnimationSpecs.backwardSlide(),
            popExitTransition = NavigationAnimationSpecs.backwardSlideExit()
        ) {
            ContactUsScreen(navController = navController)
        }
    }
}
