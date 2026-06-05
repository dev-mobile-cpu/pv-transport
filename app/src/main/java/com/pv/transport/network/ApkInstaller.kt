package com.pv.transport.network

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File


object ApkInstaller {

    fun install(context: Context) {
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            ApkDownloader.APK_FILE_NAME
        )

        Log.d("  ", "STARTING_INSTALL_PROCESS")

        if (!file.exists() || file.length() == 0L) {
            Log.e("APK_INSTALL", "APK file invalid")
            return
        }

        // ⚠️ သင့် Manifest ထဲကအတိုင်း ".provider" ဟု သေချာပေါက် ထားပေးပါ
        val authorityName = "${context.packageName}.provider"

        val uri = try {
            FileProvider.getUriForFile(context, authorityName, file)
        } catch (e: Exception) {
            // ⚠️ တကယ်လို့ လမ်းကြောင်း လွဲနေရင် ဒီနေရာမှာ Log အနီရောင် ပြပါလိမ့်မယ်
            Log.e("APK_INSTALL", "FileProvider Error (လမ်းကြောင်းလွဲနေပါသည်): ${e.message}")
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
                Log.d("APK_INSTALL", "Intent Sent Successfully! Installer Screen ပွင့်လာရပါမည်။")
            } catch (e: Exception) {
                Log.e("APK_INSTALL", "Direct Install Failed: ${e.message}")
            }
        } else {
            Log.d("APK_INSTALL", "No Permission, opening Settings...")
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
                }
            }
        }
    }
}