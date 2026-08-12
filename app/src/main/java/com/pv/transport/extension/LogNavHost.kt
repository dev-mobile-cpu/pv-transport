package com.pv.transport.extension

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
            exitTransition = { navExitSlide() },
            popEnterTransition = { navPopEnterSlide() }
        ) {
            showTabs.value = true
            LogScreen(navController)
        }

        composable(
            "checkin",
            enterTransition = { navEnterSlide() },
            exitTransition = { navExitSlide() },
            popEnterTransition = { navPopEnterSlide() },
            popExitTransition = { navPopExitSlide() }
        ) {
            showTabs.value = false
            CheckInScreen(navController)
        }

        composable(
            "checkout",
            enterTransition = { navEnterSlide() },
            exitTransition = { navExitSlide() },
            popEnterTransition = { navPopEnterSlide() },
            popExitTransition = { navPopExitSlide() }
        ) {
            val log = navController.rememberNavPayload<Data>("checkout_log")

            log?.let {
                showTabs.value = false
                CheckOutScreen(it, navController)
            }
        }

        composable(
            "log_detail",
            enterTransition = { navEnterSlide() },
            exitTransition = { navExitSlide() },
            popEnterTransition = { navPopEnterSlide() },
            popExitTransition = { navPopExitSlide() }
        ) {
            val log = navController.rememberNavPayload<Data>("log")

            log?.let {
                showTabs.value = false
                DriverLogDetailsScreen(it, navController)
            }
        }
    }
}
