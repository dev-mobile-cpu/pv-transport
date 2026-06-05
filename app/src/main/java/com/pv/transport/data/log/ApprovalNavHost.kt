package com.pv.transport.data.log

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pv.transport.presentation.ApprovalDetailScreen
import com.pv.transport.presentation.ApprovalScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ApprovalNavHost() {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "approval_list") {

        composable("approval_list") {
            ApprovalScreen(navController)
        }

        composable("approval_detail") {
            val log = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Data>("approval_detail")

            log?.let {
                ApprovalDetailScreen(it, navController)
            }
        }
    }
}