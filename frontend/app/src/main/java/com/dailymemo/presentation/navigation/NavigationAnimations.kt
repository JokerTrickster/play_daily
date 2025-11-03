package com.dailymemo.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

/**
 * Standard navigation animations for the app
 * Provides consistent transition effects across all screens
 */
object NavigationAnimations {
    private const val ANIMATION_DURATION = 300

    /**
     * Slide in from right animation (for forward navigation)
     */
    val slideInFromRight = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(ANIMATION_DURATION)
    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))

    /**
     * Slide out to left animation (when navigating forward)
     */
    val slideOutToLeft = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(ANIMATION_DURATION)
    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))

    /**
     * Slide in from left animation (for back navigation)
     */
    val slideInFromLeft = slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(ANIMATION_DURATION)
    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))

    /**
     * Slide out to right animation (when navigating back)
     */
    val slideOutToRight = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(ANIMATION_DURATION)
    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))

    /**
     * Fade transition (for tab switching)
     */
    val fadeTransition = fadeIn(
        animationSpec = tween(ANIMATION_DURATION)
    ) to fadeOut(
        animationSpec = tween(ANIMATION_DURATION)
    )

    /**
     * Scale and fade (for modal dialogs)
     */
    val scaleAndFadeIn = scaleIn(
        initialScale = 0.9f,
        animationSpec = tween(ANIMATION_DURATION)
    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))

    val scaleAndFadeOut = scaleOut(
        targetScale = 0.9f,
        animationSpec = tween(ANIMATION_DURATION)
    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))

    /**
     * Slide up (for bottom sheets)
     */
    val slideUpTransition = slideInVertically(
        initialOffsetY = { fullHeight -> fullHeight },
        animationSpec = tween(ANIMATION_DURATION)
    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))

    val slideDownTransition = slideOutVertically(
        targetOffsetY = { fullHeight -> fullHeight },
        animationSpec = tween(ANIMATION_DURATION)
    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
}
