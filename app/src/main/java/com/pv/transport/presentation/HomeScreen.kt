package com.pv.transport.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.pv.transport.extension.ApprovalNavHost
import com.pv.transport.extension.ExpenseNavHost
import com.pv.transport.extension.MainBottomBar
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.DriverLogViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(navController: NavController,logViewModel: DriverLogViewModel){
    var currentRoute by rememberSaveable { mutableStateOf("logs") }
    val saveableStateHolder = rememberSaveableStateHolder()

    Scaffold(
        containerColor = white, // main screen background set to light grey
        bottomBar = {
            MainBottomBar(
                currentRoute = currentRoute,
                onItemClick = { route ->
                    currentRoute = route
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            saveableStateHolder.SaveableStateProvider(currentRoute) {
                when (currentRoute) {
                    "logs" -> LogScreen(navController,logViewModel)
                    "fuel" -> FuelScreen()
                    "approval" -> ApprovalNavHost()
                    "expense" -> ExpenseNavHost()
                    "profile" -> ProfileScreen({
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    })
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
   // HomeScreen(navController = rememberNavController())
}
