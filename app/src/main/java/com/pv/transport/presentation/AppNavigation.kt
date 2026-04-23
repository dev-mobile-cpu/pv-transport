package com.pv.transport.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pv.transport.data.log.Data
import com.pv.transport.viewmodels.DriverLogViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(){ // allow overriding start destination
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash")
        {
            SplashScreen(navController, context)
        }
        composable("login") {
            LoginScreen(navController, context)
        }
        composable("home") { backStackEntry ->
            val logViewModel: DriverLogViewModel = hiltViewModel(backStackEntry)
            HomeScreen(navController, logViewModel)
        }
        composable("log") { backStackEntry ->
            val logViewModel: DriverLogViewModel = hiltViewModel(backStackEntry)
            LogScreen(navController, logViewModel)
        }
        composable("checkin", enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500))
        },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500)
                )
            }
        ) {
            CheckInScreen(navController)
        }


        composable("checkout?recordId={recordId}&date={date}&startTime={startTime}&startKm={startKm}&startPhoto={startPhoto}",listOf(
            navArgument("recordId") { type = NavType.StringType; defaultValue = "" },
            navArgument("date") { type = NavType.StringType; defaultValue = "" },
            navArgument("startTime") { type = NavType.StringType; defaultValue = "" },
            navArgument("startKm") { type = NavType.StringType; defaultValue = "" },
            navArgument("startPhoto") { type = NavType.StringType; defaultValue = "" },
            navArgument("fileName") { type = NavType.StringType; defaultValue = "" }
        ),
            // Enter from right-to-left and exit to left
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500))
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500)
                )
            }
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getString("recordId") ?: ""
            val startKm = backStackEntry.arguments?.getString("startKm") ?: ""
            val startPhoto = backStackEntry.arguments?.getString("startPhoto") ?: ""
            val startTime = backStackEntry.arguments?.getString("startTime") ?: ""
            val date = backStackEntry.arguments?.getString("date") ?: ""
            CheckOutScreen(navController, recordId, date, startTime, startKm, startPhoto)
        }

        composable("log_detail") {

            val log = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Data>("log")

            log?.let {
                DriverLogDetailsScreen(it, navController)
            }
        }

        composable("full_image?url={url}&recordId={recordId}&date={date}&startTime={startTime}&startKm={startKm}&startPhoto={startPhoto}",listOf(
            navArgument("url") { type = NavType.StringType; defaultValue = "" },
            navArgument("recordId") { type = NavType.StringType; defaultValue = "" },
            navArgument("date") { type = NavType.StringType; defaultValue = "" },
            navArgument("startTime") { type = NavType.StringType; defaultValue = "" },
            navArgument("startKm") { type = NavType.StringType; defaultValue = "" },
            navArgument("startPhoto") { type = NavType.StringType; defaultValue = "" }
        ),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500))
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500)
                )
            }
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            FullImageScreen(url, navController)
        }
    }


}