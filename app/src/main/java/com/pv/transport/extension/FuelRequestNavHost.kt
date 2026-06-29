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
import com.pv.transport.data.ExpenseData
import com.pv.transport.data.fuel.FuelRequestData
import com.pv.transport.presentation.AddFuelRequestScreen
import com.pv.transport.presentation.FuelRequestDetailScreen
import com.pv.transport.presentation.FuelRequestScreen
import com.pv.transport.presentation.UpdateOtherExpenseScreen

@Composable
fun FuelRequestNavHost(showTabs: MutableState<Boolean>, onRouteChanged: (String) -> Unit){
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.destination?.route?.let { route ->
            onRouteChanged(route)
        }
    }

    NavHost(navController = navController, startDestination = "fuel_request") {

        composable("fuel_request") {
            showTabs.value = true
            FuelRequestScreen(navController)
        }
        composable("add_fuel_request", enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500))
        },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500))
            }
            ) {
            showTabs.value = false
            AddFuelRequestScreen(navController)
        }
        composable("fuel_request_detail", enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500))
        }, exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right,tween(500))
        }) {
            val request = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<FuelRequestData>("fuel_request_detail")
            request?.let {
                FuelRequestDetailScreen(it,navController)
            }
            showTabs.value = false

        }
    }
}