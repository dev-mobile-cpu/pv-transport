package com.pv.transport.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pv.transport.presentation.AddFuelLogScreen
import com.pv.transport.presentation.FuelLogScreen

@Composable
fun FuelLogNavHost(showTabs: MutableState<Boolean>){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "fuel_log") {

        composable("fuel_log") {
            showTabs.value = true
            FuelLogScreen(navController)
        }
        composable("add_fuel_log") {
            showTabs.value = false
            AddFuelLogScreen(navController)
        }
    }
}