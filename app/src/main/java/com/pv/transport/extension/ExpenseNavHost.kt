package com.pv.transport.extension

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pv.transport.data.ExpenseData
import com.pv.transport.presentation.AddOtherExpenseScreen
import com.pv.transport.presentation.ExpenseScreen
import com.pv.transport.presentation.UpdateOtherExpenseScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpenseNavHost(
    onRouteChanged: (String) -> Unit
) {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.destination?.route?.let { route ->
            onRouteChanged(route)
        }
    }

    NavHost(navController = navController, startDestination = "other_expense") {

        composable("other_expense") {
            ExpenseScreen(navController)
        }
        composable("add_expense", enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500))
        },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500))
            }) {
            AddOtherExpenseScreen(navController)
        }

        composable("edit_expense") {
            val expense = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<ExpenseData>("edit_expense")

            expense?.let {
                UpdateOtherExpenseScreen(it,navController)
            }
        }
    }
}