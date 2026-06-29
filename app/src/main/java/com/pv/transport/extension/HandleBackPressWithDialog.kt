package com.pv.transport.extension

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
fun HandleBackPressWithDialog(
    onBackConfirmed: () -> Unit,
    showDialog: MutableState<Boolean>
) {

    BackHandler {
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