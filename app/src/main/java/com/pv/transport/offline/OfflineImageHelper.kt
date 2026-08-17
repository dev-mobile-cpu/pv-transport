package com.pv.transport.offline

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pv.transport.extension.uriToFile
import com.pv.transport.util.DebugLog
import java.io.File
import java.util.UUID

object OfflineImageHelper {

    private val gson = Gson()

    /**
     * Persist a photo under filesDir, downsampled the same way as online upload
     * so list thumbs decode quickly instead of reading a full camera JPEG.
     */
    suspend fun copyUriToInternalStorage(context: Context, uri: Uri, prefix: String): String? {
        return try {
            val compressed = uriToFile(uri, context)
            val dir = File(context.filesDir, "offline_images")
            dir.mkdirs()
            val dest = File(
                dir,
                "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
            )
            if (compressed.absolutePath != dest.absolutePath) {
                compressed.copyTo(dest, overwrite = true)
                compressed.delete()
            }
            dest.absolutePath
        } catch (e: Exception) {
            DebugLog.w("OFFLINE_IMAGE", "Failed to copy $uri", e)
            null
        }
    }

    suspend fun copyUrisToInternalStorage(context: Context, uris: List<Uri>, prefix: String): List<String> {
        return uris.mapNotNull { uri ->
            copyUriToInternalStorage(context, uri, prefix)
        }
    }

    fun pathsToJson(paths: List<String>): String = gson.toJson(paths)

    fun jsonToPaths(json: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun fileFromPath(path: String): File? {
        val file = File(path)
        return if (file.exists()) file else null
    }
}
