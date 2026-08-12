package com.pv.transport.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pv.transport.R
import com.pv.transport.data.log.Data
import com.pv.transport.presentation.ApprovalScreen
import com.pv.transport.presentation.DriverLogDetailsScreen

@Composable
fun ApprovalNavHost(
    onRouteChanged: (String) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.destination?.route?.let { route ->
            onRouteChanged(route)
        }
    }

    NavHost(navController = navController, startDestination = "approval_list") {
        composable("approval_list") {
            ApprovalScreen(navController)
        }

        composable("approval_detail") {
            val log = navController.rememberNavPayload<Data>("approval_detail")

            log?.let {
                DriverLogDetailsScreen(
                    log = it,
                    navController = navController,
                    titleRes = R.string.approval_detail,
                    showApprovalMeta = true
                )
            }
        }
    }
}
