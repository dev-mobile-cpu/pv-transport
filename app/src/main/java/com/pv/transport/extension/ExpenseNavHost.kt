package com.pv.transport.extension

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pv.transport.data.ExpenseData
import com.pv.transport.presentation.AddOtherExpenseScreen
import com.pv.transport.presentation.ExpenseScreen
import com.pv.transport.presentation.UpdateOtherExpenseScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpenseNavHost() {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "other_expense") {

        composable("other_expense") {
            ExpenseScreen(navController)
        }
        composable("add_expense") {
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