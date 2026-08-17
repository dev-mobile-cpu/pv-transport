package com.pv.transport.network

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.pv.transport.R
import com.pv.transport.util.DebugLog
import java.io.File


object ApkInstaller {

    private fun showInstallError(context: Context) {
        Toast.makeText(
            context,
            context.getString(R.string.install_failed_try_again),
            Toast.LENGTH_LONG
        ).show()
    }

    fun install(context: Context) {
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            ApkDownloader.APK_FILE_NAME
        )

        DebugLog.d("APK_INSTALL", "STARTING_INSTALL_PROCESS")

        if (!file.exists() || file.length() == 0L) {
            Log.e("APK_INSTALL", "APK file invalid")
            showInstallError(context)
            return
        }

        // Must match the ".provider" authority declared in the Manifest
        val authorityName = "${context.packageName}.provider"

        val uri = try {
            FileProvider.getUriForFile(context, authorityName, file)
        } catch (e: Exception) {
            Log.e("APK_INSTALL", "FileProvider Error: ${e.message}")
            showInstallError(context)
            return
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        // Unknown Sources Permission စစ်ဆေးပြီး Installer ကို ခေါ်ခြင်း
        val pm = context.packageManager
        if (pm.canRequestPackageInstalls()) {
            try {
                context.startActivity(intent)
                DebugLog.d("APK_INSTALL", "Installer screen launched")
            } catch (e: Exception) {
                Log.e("APK_INSTALL", "Direct Install Failed: ${e.message}")
                showInstallError(context)
            }
        } else {
            DebugLog.d("APK_INSTALL", "No Permission, opening Settings...")
            try {
                val manageIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = "package:${context.packageName}".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(manageIntent)
            } catch (e: Exception) {
                try {
                    context.startActivity(intent)
                } catch (ex: Exception) {
                    Log.e("APK_INSTALL", "All install attempts failed: ${ex.message}")
                    showInstallError(context)
                }
            }
        }
    }
}