package com.receiptr.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder

// Animation constants
const val ANIMATION_DURATION = 300
const val FAST_ANIMATION_DURATION = 200

// Navigation animation helper functions
@Composable
fun NavController.navigateWithAnimation(
    route: String,
    popUpToRoute: String? = null,
    inclusive: Boolean = false,
    restoreState: Boolean = false,
    launchSingleTop: Boolean = true
) {
    val navOptions = NavOptions.Builder()
        .setLaunchSingleTop(launchSingleTop)
        .setRestoreState(restoreState)
        .apply {
            popUpToRoute?.let { 
                setPopUpTo(it, inclusive)
            }
        }
        .build()
    
    this.navigate(route, navOptions)
}

// Enhanced navigation for bottom navigation with smooth transitions
@Composable
fun NavController.navigateToBottomNavDestination(
    route: String,
    currentRoute: String?
) {
    if (currentRoute != route) {
        navigateWithAnimation(
            route = route,
            popUpToRoute = "home",
            inclusive = false,
            restoreState = true,
            launchSingleTop = true
        )
    }
}

// Animation specs for different navigation types
object NavigationAnimationSpecs {
    
    // For forward navigation (like login flow)
    fun forwardSlide(): AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(ANIMATION_DURATION)
        ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
    }
    
    fun forwardSlideExit(): AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth },
            animationSpec = tween(ANIMATION_DURATION)
        ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
    }
    
    // For backward navigation (back button)
    fun backwardSlide(): AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { fullWidth -> -fullWidth },
            animationSpec = tween(ANIMATION_DURATION)
        ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
    }
    
    fun backwardSlideExit(): AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(ANIMATION_DURATION)
        ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
    }
    
    // For modal-like screens (scan, camera)
    fun modalEnter(): AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(ANIMATION_DURATION)
        ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
    }
    
    fun modalExit(): AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(ANIMATION_DURATION)
        ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
    }
    
    // For home screen and main destinations
    fun scaleEnter(): AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        scaleIn(
            initialScale = 0.9f,
            animationSpec = tween(ANIMATION_DURATION)
        ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
    }
    
    fun scaleExit(): AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        scaleOut(
            targetScale = 0.9f,
            animationSpec = tween(ANIMATION_DURATION)
        ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
    }
    
    // For fast bottom navigation transitions
    fun fastSlideEnter(): AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(FAST_ANIMATION_DURATION)
        ) + fadeIn(animationSpec = tween(FAST_ANIMATION_DURATION))
    }
    
    fun fastSlideExit(): AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth },
            animationSpec = tween(FAST_ANIMATION_DURATION)
        ) + fadeOut(animationSpec = tween(FAST_ANIMATION_DURATION))
    }
    
    // For splash and fade transitions
    fun fadeEnter(): AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(ANIMATION_DURATION))
    }
    
    fun fadeExit(): AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(ANIMATION_DURATION))
    }
    
    // For settings and detail screens with slide up
    fun slideUpEnter(): AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight / 4 },
            animationSpec = tween(ANIMATION_DURATION)
        ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
    }
    
    fun slideUpExit(): AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight / 4 },
            animationSpec = tween(ANIMATION_DURATION)
        ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
    }
    
    // For smooth cross-fade transitions
    fun crossFadeEnter(): AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        fadeIn(
            animationSpec = tween(ANIMATION_DURATION)
        ) + scaleIn(
            initialScale = 0.95f,
            animationSpec = tween(ANIMATION_DURATION)
        )
    }
    
    fun crossFadeExit(): AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        fadeOut(
            animationSpec = tween(ANIMATION_DURATION)
        ) + scaleOut(
            targetScale = 0.95f,
            animationSpec = tween(ANIMATION_DURATION)
        )
    }
    
    // For bouncy scale animations
    fun bouncyScaleEnter(): AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(
                durationMillis = ANIMATION_DURATION,
                easing = androidx.compose.animation.core.EaseOutBounce
            )
        ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
    }
    
    fun bouncyScaleExit(): AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(
                durationMillis = ANIMATION_DURATION,
                easing = androidx.compose.animation.core.EaseInBounce
            )
        ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
    }
}
