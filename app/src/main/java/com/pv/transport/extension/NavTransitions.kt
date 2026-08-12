package com.pv.transport.extension

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry

/** Shared page transitions: enter R→L, leave/pop L→R. */
const val NAV_TRANSITION_MS = 320

private val navSlideSpec = tween<IntOffset>(
    durationMillis = NAV_TRANSITION_MS,
    easing = FastOutSlowInEasing
)

fun AnimatedContentTransitionScope<NavBackStackEntry>.navEnterSlide(): EnterTransition =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, navSlideSpec)

fun AnimatedContentTransitionScope<NavBackStackEntry>.navExitSlide(): ExitTransition =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, navSlideSpec)

fun AnimatedContentTransitionScope<NavBackStackEntry>.navPopEnterSlide(): EnterTransition =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, navSlideSpec)

fun AnimatedContentTransitionScope<NavBackStackEntry>.navPopExitSlide(): ExitTransition =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, navSlideSpec)
