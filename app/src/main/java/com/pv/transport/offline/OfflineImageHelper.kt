package com.pv.transport.offline

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

object OfflineImageHelper {

    private val gson = Gson()

    fun copyUriToInternalStorage(context: Context, uri: Uri, prefix: String): String? {
        return try {
            val dir = File(context.filesDir, "offline_images")
            dir.mkdirs()
            val file = File(dir, "${prefix}_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun copyUrisToInternalStorage(context: Context, uris: List<Uri>, prefix: String): List<String> {
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
