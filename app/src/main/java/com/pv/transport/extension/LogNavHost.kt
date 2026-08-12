package com.pv.transport.extension

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pv.transport.data.log.Data
import com.pv.transport.presentation.CheckInScreen
import com.pv.transport.presentation.CheckOutScreen
import com.pv.transport.presentation.DriverLogDetailsScreen
import com.pv.transport.presentation.LogScreen

private const val TRANSITION_DURATION = 300

@Composable
fun LogNavHost(
    showTabs: MutableState<Boolean>,
    onRouteChanged: (String) -> Unit,
    createRequestId: Int = 0
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.destination?.route?.let { route ->
            onRouteChanged(route)
        }
    }

    LaunchedEffect(createRequestId) {
        if (createRequestId > 0) {
            navController.navigate("checkin")
        }
    }

    NavHost(navController = navController, startDestination = "log") {
        composable(
            "log",
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(TRANSITION_DURATION))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(TRANSITION_DURATION))
            }
        ) {
            showTabs.value = true
            LogScreen(navController)
        }

        composable(
            "checkin",
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(TRANSITION_DURATION))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(TRANSITION_DURATION))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(TRANSITION_DURATION))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(TRANSITION_DURATION))
            }
        ) {
            showTabs.value = false
            CheckInScreen(navController)
        }

        composable(
            "checkout",
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(TRANSITION_DURATION))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(TRANSITION_DURATION))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(TRANSITION_DURATION))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(TRANSITION_DURATION))
            }
        ) {
            val log = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Data>("checkout_log")

            log?.let {
                showTabs.value = false
                CheckOutScreen(it, navController)
            }
        }

        composable(
            "log_detail",
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(TRANSITION_DURATION))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(TRANSITION_DURATION))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(TRANSITION_DURATION))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(TRANSITION_DURATION))
            }
        ) {
            val log = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Data>("log")

            log?.let {
                showTabs.value = false
                DriverLogDetailsScreen(it, navController)
            }
        }
    }
}
