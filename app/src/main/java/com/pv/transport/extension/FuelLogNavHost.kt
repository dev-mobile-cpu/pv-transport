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
import com.pv.transport.data.fuel.FuelLogData
import com.pv.transport.data.fuel.FuelRequestData
import com.pv.transport.presentation.AddFuelLogScreen
import com.pv.transport.presentation.FuelLogDetailScreen
import com.pv.transport.presentation.FuelLogScreen
import com.pv.transport.presentation.FuelRequestDetailScreen

@Composable
fun FuelLogNavHost(showTabs: MutableState<Boolean>,onRouteChanged: (String) -> Unit){
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.destination?.route?.let { route ->
            onRouteChanged(route)
        }
    }

    NavHost(navController = navController, startDestination = "fuel_log") {

        composable("fuel_log") {
            showTabs.value = true
            FuelLogScreen(navController)
        }
        composable("add_fuel_log", enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500))
        },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500))
            }
        ) {
            showTabs.value = false
            AddFuelLogScreen(navController)
        }

        composable("fuel_log_detail", enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500))
        },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500))
            }
        ) {
            val log = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<FuelLogData>("fuel_log_detail")
            log?.let {
                FuelLogDetailScreen(it,navController)
            }
            showTabs.value = false
        }
    }
}