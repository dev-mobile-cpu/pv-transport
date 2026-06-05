package com.pv.transport.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pv.transport.data.ExpenseData
import com.pv.transport.data.fuel.FuelRequestData
import com.pv.transport.presentation.AddFuelRequestScreen
import com.pv.transport.presentation.FuelRequestDetailScreen
import com.pv.transport.presentation.FuelRequestScreen
import com.pv.transport.presentation.UpdateOtherExpenseScreen

@Composable
fun FuelRequestNavHost(showTabs: MutableState<Boolean>){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "fuel_request") {

        composable("fuel_request") {
            showTabs.value = true
            FuelRequestScreen(navController)
        }
        composable("add_fuel_request") {
            showTabs.value = false
            AddFuelRequestScreen(navController)
        }
        composable("fuel_request_detail") {
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