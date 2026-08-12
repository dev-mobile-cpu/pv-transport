package com.pv.transport.extension

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavController

/**
 * Reads a payload handed over by the calling screen and caches it for the whole destination.
 *
 * [NavController.previousBackStackEntry] already points at the new top of the stack while a pop
 * animation is still running, so reading it on every recomposition returns null halfway through
 * the back gesture and blanks the screen.
 */
@Composable
inline fun <reified T : Parcelable> NavController.rememberNavPayload(key: String): T? {
    val source = previousBackStackEntry
    val cached = rememberSaveable(key) {
        mutableStateOf(source?.savedStateHandle?.get<T>(key))
    }
    return cached.value
}
