package com.pv.transport.extension

import android.R.attr.progress
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.pv.transport.R
import com.pv.transport.data.CheckVersionResponse
import com.pv.transport.network.ApkDownloader
import androidx.core.net.toUri
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.network.ApkInstaller
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateVersionBottomSheet(
    onDismiss: () -> Unit,
    update: CheckVersionResponse
) {
    val context = LocalContext.current
    val authPrefs = AuthPrefs(context)

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    // prevent launching installer multiple times from this sheet
    var installLaunched by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            // Prevent hiding while forced update or while downloading
            if (isDownloading || update.forceUpdate) {
                sheetValue != SheetValue.Hidden
            } else {
                true
            }
        }
    )

    // Start download: set downloading flag and enqueue DownloadManager request via ApkDownloader.
    val startDownloadFlow = {
        installLaunched = false
        isDownloading = true
        downloadProgress = 0f
        ApkDownloader.download(context = context, url = update.downloadUrl)
    }

    // ActivityResult launcher for "unknown sources" permission settings screen
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // When returning from Settings, if permission granted then start download
        val activity = context as? Activity
        if (activity != null && activity.packageManager.canRequestPackageInstalls()) {
            startDownloadFlow()
        }
    }

    // Block back when forced update or currently downloading
    BackHandler(enabled = update.forceUpdate || isDownloading) {}

    // Collect download progress while isDownloading == true
    LaunchedEffect(isDownloading) {
        if (!isDownloading) return@LaunchedEffect
        try {
            ApkDownloader.downloadProgress(context).collect { progress ->
                // progress semantics:
                // -1f => error, 0..0.999 => in-progress, 1f => complete
                downloadProgress = progress

                when {
                    progress >= 1.0f -> {

                        isDownloading = false

                        if (!installLaunched) {
                            installLaunched = true
                            ApkInstaller.install(context)
                        }

                        onDismiss()
                    }

                    progress < 0f -> {
                        // Download failed: leave the sheet so "Update Now" acts as retry
                        isDownloading = false
                        Toast.makeText(
                            context,
                            context.getString(R.string.download_failed_try_again),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } catch (e: CancellationException) {


            // coroutine cancelled - ignore
            isDownloading = false
        } catch (t: Throwable) {
            // any other error - stop downloading and show error
            isDownloading = false
            downloadProgress = -1f
        }
    }

    // Reset installLaunched when the sheet is disposed (so next time it can run again)
    DisposableEffect(Unit) {
        onDispose {
            installLaunched = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!update.forceUpdate && !isDownloading) {
                onDismiss()
            }
        },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.update_available),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.new_version) + " ${update.latestVersionName}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.update_message) + " ${update.updateMessage}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (isDownloading) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val progressForCompose = when {
                        downloadProgress < 0f -> 0f
                        downloadProgress >= 1f -> 1f
                        else -> downloadProgress.coerceIn(0f, 1f)
                    }

                    LinearProgressIndicator(
                        progress = progressForCompose,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when {
                            downloadProgress < 0f -> stringResource(R.string.download_failed_try_again)
                            downloadProgress >= 1f -> stringResource(R.string.installing)
                            else -> "Downloading: ${(progressForCompose * 100).toInt()}%"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            } else {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val activity = context as? Activity
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                            activity != null &&
                            !activity.packageManager.canRequestPackageInstalls()
                        ) {
                            // Ask user to allow installs from this app in Settings.
                            val intent = android.content.Intent(
                                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                "package:${activity.packageName}".toUri()
                            )
                            launcher.launch(intent)
                        } else {
                            startDownloadFlow()
                        }
                    }
                ) {
                    Text(stringResource(R.string.update_now))
                }
            }

            if (!update.forceUpdate && !isDownloading) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    onClick = {
                        if (!update.forceUpdate) {
                            authPrefs.saveSkippedVersionCode(update.latestVersionCode)
                            onDismiss()
                        }
                    }
                ) {
                    Text(stringResource(R.string.later))
                }
            }
        }
    }
}