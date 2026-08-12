package com.pv.transport.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.data.SessionEvents

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val context = LocalContext.current
    val authPrefs = remember { AuthPrefs(context) }

    LaunchedEffect(Unit) {
        SessionEvents.logoutEvent.collect {
            if (navController.currentDestination?.route != "login") {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        composable("splash") {
            SplashScreen(navController, context, authPrefs)
        }

        composable(
            route = "login",
            exitTransition = {
                fadeOut(animationSpec = tween(280))
            }
        ) {
            LoginScreen(navController, context)
        }

        composable(
            route = "home",
            enterTransition = {
                // Login success: main screen slides up from bottom
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(420)
                ) + fadeIn(animationSpec = tween(320))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(220))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(220))
            },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(320)
                ) + fadeOut(animationSpec = tween(220))
            }
        ) {
            HomeScreen(navController)
        }
    }
}
