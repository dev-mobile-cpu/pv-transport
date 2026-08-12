package com.pv.transport.extension

import android.os.Build
import androidx.annotation.RequiresApi
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
import com.pv.transport.presentation.OtherExpenseDetailScreen
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
        composable(
            "other_expense",
            exitTransition = { navExitSlide() },
            popEnterTransition = { navPopEnterSlide() }
        ) {
            ExpenseScreen(navController)
        }

        composable(
            "add_expense",
            enterTransition = { navEnterSlide() },
            exitTransition = { navExitSlide() },
            popEnterTransition = { navPopEnterSlide() },
            popExitTransition = { navPopExitSlide() }
        ) {
            AddOtherExpenseScreen(navController)
        }

        composable(
            "expense_detail",
            enterTransition = { navEnterSlide() },
            exitTransition = { navExitSlide() },
            popEnterTransition = { navPopEnterSlide() },
            popExitTransition = { navPopExitSlide() }
        ) {
            val expense = navController.rememberNavPayload<ExpenseData>("expense_detail")
            if (expense != null) {
                OtherExpenseDetailScreen(expense, navController)
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack("expense_detail", inclusive = true)
                }
            }
        }

        composable(
            "edit_expense",
            enterTransition = { navEnterSlide() },
            exitTransition = { navExitSlide() },
            popEnterTransition = { navPopEnterSlide() },
            popExitTransition = { navPopExitSlide() }
        ) {
            val expense = navController.rememberNavPayload<ExpenseData>("edit_expense")

            expense?.let {
                UpdateOtherExpenseScreen(it, navController)
            }
        }
    }
}
