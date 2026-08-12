package com.pv.transport.extension

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel

fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/** Activity-scoped ViewModel so list data survives bottom-tab switches. */
@Composable
inline fun <reified VM : ViewModel> activityHiltViewModel(): VM {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    return if (activity != null) {
        hiltViewModel(activity)
    } else {
        hiltViewModel()
    }
}

@Composable
fun rememberActivityOrNull(): Activity? {
    val context = LocalContext.current
    return remember(context) { context.findActivity() }
}
