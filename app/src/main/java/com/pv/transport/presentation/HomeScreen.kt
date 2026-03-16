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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.pv.transport.data.ApprovalNavHost
import com.pv.transport.data.ExpenseNavHost
import com.pv.transport.extension.MainBottomBar
import com.pv.transport.ui.theme.white

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(navController: NavController){
    var currentRoute by remember { mutableStateOf("logs") }

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

            when (currentRoute) {
                "logs" -> LogScreen(navController)
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

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    HomeScreen(navController = rememberNavController())
}
