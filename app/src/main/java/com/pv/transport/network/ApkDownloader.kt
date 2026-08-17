package com.pv.transport.network

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import com.pv.transport.util.DebugLog
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import okhttp3.internal.platform.android.AndroidLogHandler.close
import java.io.File

object ApkDownloader {
    var downloadId: Long = -1L
    const val APK_FILE_NAME = "driver_app_update.apk"

    fun download(context: Context, url: String): Long {

        val request = DownloadManager.Request(url.toUri())
            .setTitle("PV Transport Update")
            .setDescription("Downloading latest version...")
            .setAllowedOverMetered(true)
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE
            )
            .setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                APK_FILE_NAME
            )

        val manager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        downloadId = manager.enqueue(request)

        return downloadId
    }
    fun getId() = downloadId

    fun downloadProgress(context: Context): Flow<Float> = callbackFlow {
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // keep local reference of current download id
        val id = getId()
        if (id == -1L) {
            trySend(-1f)
            close()
            return@callbackFlow
        }

        try {
            var keepPolling = true
            while (isActive && keepPolling) {
                val query = DownloadManager.Query().setFilterById(id)
                val cursor = manager.query(query)

                if (cursor != null && cursor.moveToFirst()) {
                    try {
                        val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        when (status) {
                            DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PAUSED -> {
                                val bytes = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                                val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                                val fraction = if (total > 0L) {
                                    bytes.toFloat() / total.toFloat()
                                } else {
                                    // total unknown: use 0..0.99 to indicate progress but not complete
                                    if (bytes > 0) 0.5f else 0f
                                }
                                trySend(fraction.coerceIn(0f, 0.999f))
                            }
                            DownloadManager.STATUS_SUCCESSFUL -> {

                                val file = File(
                                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                                    ApkDownloader.APK_FILE_NAME
                                )

                                DebugLog.d("APK_DOWNLOAD", "DONE")
                                DebugLog.d("APK_PATH", file.absolutePath)
                                DebugLog.d("APK_SIZE", file.length().toString())

                                if (file.exists() && file.length() > 0) {
                                    trySend(1f)
                                } else {
                                    Log.e("APK_DOWNLOAD", "FILE INVALID")
                                    trySend(-1f)
                                }

                                keepPolling = false
                            }
                            DownloadManager.STATUS_FAILED -> {
                                trySend(-1f)
                                keepPolling = false
                            }
                            else -> {
                                // other statuses - just poll again
                            }
                        }
                    } finally {
                        cursor.close()
                    }
                } else {
                    // no cursor -> consider it finished or failed
                    trySend(-1f)
                    keepPolling = false
                }
                // reasonable polling interval
                delay(500)
            }
        } catch (t: Throwable) {
            trySend(-1f)
        } finally {
            // clean up
            close()
        }

        awaitClose { /* no extra cleanup required here */ }
    }
}


