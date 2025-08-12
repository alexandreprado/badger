package com.ducatti.badger.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

fun slideInFromRightAnimation(): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(SLIDE_TRANSITION_DURATION)
    )

fun slideOutToLeftAnimation(): ExitTransition =
    slideOutHorizontally(animationSpec = tween(SLIDE_TRANSITION_DURATION))

fun popSlideInFromLeftAnimation(): EnterTransition =
    slideInHorizontally(animationSpec = tween(SLIDE_TRANSITION_DURATION))

fun popSlideOutToRightAnimation(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(SLIDE_TRANSITION_DURATION)
    )

private const val SLIDE_TRANSITION_DURATION = 500
