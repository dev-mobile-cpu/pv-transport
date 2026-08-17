package com.pv.transport.extension

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pv.transport.R

/**
 * Shared camera permission flow:
 * granted -> onGranted; first deny -> rationale dialog + re-request;
 * permanent deny -> "Open Settings" dialog + inline error text ([deniedText]).
 */
class CameraAccess internal constructor(
    val request: () -> Unit,
    private val deniedTextState: State<String?>
) {
    val deniedText: String? get() = deniedTextState.value
}

@Composable
fun rememberCameraAccess(onGranted: () -> Unit): CameraAccess {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val currentOnGranted by rememberUpdatedState(onGranted)

    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val deniedText = remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            deniedText.value = null
            currentOnGranted()
        } else {
            val canAskAgain = activity != null && ActivityCompat
                .shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
            if (canAskAgain) {
                deniedText.value = context.getString(R.string.camera_permission_required)
            } else {
                deniedText.value = context.getString(R.string.camera_permission_blocked)
                showSettingsDialog = true
            }
        }
    }

    val request: () -> Unit = {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> {
                deniedText.value = null
                currentOnGranted()
            }

            activity != null && ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.CAMERA
            ) -> showRationaleDialog = true

            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text(stringResource(R.string.camera_permission_title)) },
            text = { Text(stringResource(R.string.camera_permission_rationale)) },
            confirmButton = {
                TextButton(onClick = {
                    showRationaleDialog = false
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }) { Text(stringResource(R.string.allow)) }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text(stringResource(R.string.camera_permission_title)) },
            text = { Text(stringResource(R.string.camera_permission_blocked)) },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    runCatching {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                }) { Text(stringResource(R.string.open_settings)) }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    return CameraAccess(request = request, deniedTextState = deniedText)
}
