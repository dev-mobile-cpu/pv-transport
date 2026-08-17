package com.pv.transport.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.extension.ApprovalNavHost
import com.pv.transport.extension.ExpenseNavHost
import com.pv.transport.extension.MainBottomBar
import com.pv.transport.ui.theme.LocalCollapsibleChrome
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.rememberCollapsibleChromeState
import com.pv.transport.ui.theme.white

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(navController: NavController){
    val authPrefs = AuthPrefs(LocalContext.current)
    val driverType = authPrefs.getDriverType()
    var currentRoute by rememberSaveable { mutableStateOf("logs") }
    val saveableStateHolder = rememberSaveableStateHolder()
    val chromeState = rememberCollapsibleChromeState()

    var logRoute by rememberSaveable { mutableStateOf("log") }
    var fuelRoute by rememberSaveable { mutableStateOf("fuel_tabs") }
    var approvalRoute by rememberSaveable { mutableStateOf("approval_list") }
    var expenseRoute by rememberSaveable { mutableStateOf("expense_list") }
    var resetLogTab by remember {
        mutableStateOf(false)
    }


    val hideBottomBar =
        (currentRoute == "logs" &&
                logRoute in listOf("checkin", "checkout", "log_detail")) ||

                (currentRoute == "fuel" &&
                        fuelRoute in listOf("add_fuel_request", "fuel_request_detail","add_fuel_log","fuel_log_detail")) ||

                (currentRoute == "approval" &&
                        approvalRoute in listOf("approval_detail")) ||

                (currentRoute == "expense" &&
                        expenseRoute in listOf("add_expense", "expense_detail"))

    // Detail routes and tab switches should always restore chrome.
    LaunchedEffect(currentRoute, hideBottomBar) {
        chromeState.show()
    }

    CompositionLocalProvider(LocalCollapsibleChrome provides chromeState) {
        Scaffold(
            containerColor = if (hideBottomBar) white else colorSecondary,
            bottomBar = {
                AnimatedVisibility(
                    visible = !hideBottomBar && chromeState.titleVisible,
                    enter = expandVertically(animationSpec = tween(220)) + fadeIn(animationSpec = tween(180)),
                    exit = shrinkVertically(animationSpec = tween(220)) + fadeOut(animationSpec = tween(160))
                ) {
                    MainBottomBar(
                        currentRoute = currentRoute,
                        onItemClick = { route ->
                            if (route == "logs") {
                                resetLogTab = !resetLogTab
                            }
                            chromeState.show()
                            currentRoute = route
                        }
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                saveableStateHolder.SaveableStateProvider(currentRoute) {

                    if (driverType == "office"){
                        when (currentRoute) {
                            "logs" -> LogTabScreen (
                                onRouteChanged = {route ->
                                    logRoute = route

                                }
                            )
                            "fuel" -> FuelTabScreen(
                                onRouteChanged = {route ->
                                    fuelRoute = route
                                }
                            )
                            "expense" -> ExpenseNavHost(
                                onRouteChanged = {route ->
                                    expenseRoute = route
                                }
                            )
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
                            "logs" -> LogTabScreen(
                                onRouteChanged = {route ->
                                    logRoute = route

                                },
                                resetTab = resetLogTab
                            )
                            "fuel" -> FuelTabScreen(
                                onRouteChanged = {route ->
                                    fuelRoute = route
                                }
                            )
                            "approval" -> ApprovalNavHost(
                                onRouteChanged = {route ->
                                    approvalRoute = route
                                }
                            )
                            "expense" -> ExpenseNavHost(
                                onRouteChanged = {route ->
                                    expenseRoute = route
                                }
                            )
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
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    // HomeScreen(navController = rememberNavController())
}
