package com.pv.transport.extension

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState

@Composable
fun HandleBackPressWithDialog(
    onBackConfirmed: () -> Unit,
    showDialog: MutableState<Boolean>
) {
    // While a detail screen is on top (or sliding away) this screen's back stack entry is not
    // resumed yet, so the pop that brings it back must not also trigger the exit prompt.
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()

    BackHandler(enabled = lifecycleState == Lifecycle.State.RESUMED) {
        showDialog.value = true
    }

    ConfirmExitDialog(
        showDialog = showDialog.value,
        onConfirm = {
            showDialog.value = false
            onBackConfirmed()
        },
        onDismiss = {
            showDialog.value = false
        }
    )
}
