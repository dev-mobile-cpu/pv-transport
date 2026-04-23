package com.pv.transport.presentation

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import coil.request.ImageRequest
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, context: Context){

    val alphaAnim = remember { Animatable(0f) }
    LaunchedEffect(true) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        delay(1000)
        alphaAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 500)
        )

        val sharedPreferences = context.getSharedPreferences("IMessagePreference", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        println("SplashScreen: isLoggedIn = $isLoggedIn")

        if (isLoggedIn) {
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true } // Remove splash from back stack
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true } // Remove splash from back stack
            }
        }

    }

    val imageUrl = "https://pvmyanmar-storage.s3.ap-southeast-1.amazonaws.com/other/pv_splash.png"

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun PreviewSplashScreen() {
//    // You can preview the SplashScreen composable in Android Studio.
//    // Replace 'NavController' and 'Context' with mock data for previewing purposes.
//    SplashScreen(navController = rememberNavController(), context = LocalContext.current)
//}