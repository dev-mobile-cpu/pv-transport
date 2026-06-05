package com.pv.transport.extension

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pv.transport.data.log.Data
import com.pv.transport.presentation.CheckInScreen
import com.pv.transport.presentation.CheckOutScreen
import com.pv.transport.presentation.DriverLogDetailsScreen
import com.pv.transport.presentation.LogScreen
import com.pv.transport.viewmodels.DriverLogViewModel

@Composable
fun LogNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "log") {
        composable("log") {
            LogScreen(navController)
        }
        composable("checkin", enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500))
        },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500)
                )
            }
        ) {
            CheckInScreen(navController)
        }

        composable("checkout",
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500))
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500)
                )
            }
        ) {
            val log = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Data>("checkout_log")

            log?.let {
                CheckOutScreen(it, navController)
            }

        }

        composable("log_detail") {
            val log = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Data>("log")

            log?.let {
                DriverLogDetailsScreen(it, navController)
            }
        }

    }

}