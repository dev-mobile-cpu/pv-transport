package com.pv.transport.extension

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConfirmExitDialog(
    showDialog: Boolean,
    title: String = "Exit Screen",
    message: String = "Do you want to leave this screen?",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Stay")
                }
            }
        )
    }
}



