package com.pv.transport.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.data.log.ApprovalNavHost
import com.pv.transport.extension.ExpenseNavHost
import com.pv.transport.extension.LogNavHost
import com.pv.transport.extension.MainBottomBar
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.DriverLogViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(navController: NavController){
    val authPrefs = AuthPrefs(LocalContext.current)
    val driverType = authPrefs.getDriverType()
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

                if (driverType == "office"){
                    when (currentRoute) {
                        "logs" -> LogNavHost()
                        "fuel" -> FuelTabScreen()
                        "expense" -> ExpenseNavHost()
                        "profile" -> ProfileScreen(navToLogin = {
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }, navToLanguage = {
                            currentRoute = "language"
                        })
                        "language" -> LanguageScreen(onBack = {
                            currentRoute = "profile"
                        }, onLanguageChanged = {
                            currentRoute = "logs"
                        })
                    }
                }else{
                    when (currentRoute) {
                        "logs" -> LogNavHost()
                        "fuel" -> FuelTabScreen()
                        "approval" -> ApprovalNavHost()
                        "expense" -> ExpenseNavHost()
                        "profile" -> ProfileScreen(navToLogin = {
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }, navToLanguage = {
                            currentRoute = "language"
                        })
                        "language" -> LanguageScreen(onBack = {
                            currentRoute = "profile"
                        }, onLanguageChanged = {
                            currentRoute = "logs"
                        })
                    }
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