package com.pv.transport.extension

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pv.transport.data.log.Data
import com.pv.transport.presentation.AddLogSheetScreen
import com.pv.transport.presentation.CheckInScreen
import com.pv.transport.presentation.CheckOutScreen
import com.pv.transport.presentation.DriverLogDetailsScreen
import com.pv.transport.presentation.LogScreen
import com.pv.transport.presentation.LogSheetScreen
import com.pv.transport.presentation.LoginScreen
import com.pv.transport.viewmodels.DriverLogViewModel

private const val TRANSITION_DURATION = 300

@Composable
fun LogSheetNavHost(
    showTabs: MutableState<Boolean>, onRouteChanged: (String) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.destination?.route?.let { route ->
            onRouteChanged(route)
        }
    }

    NavHost(navController = navController, startDestination = "log_sheet") {
        composable(
            "log_sheet",
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(TRANSITION_DURATION))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(TRANSITION_DURATION))
            }
        ) {
            showTabs.value = true
            LogSheetScreen(navController)
        }

        composable(
            "add_log_sheet",
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
            AddLogSheetScreen(navController)
        }

        composable(
            "log_sheet_detail",
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
