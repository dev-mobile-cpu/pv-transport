package com.pv.transport.extension

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.pv.transport.ui.theme.white
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import kotlin.io.copyTo
import kotlin.io.outputStream
import kotlin.io.use
import kotlin.let
import androidx.core.graphics.scale
import com.pv.transport.R
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.util.DebugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomImagePicker(
    imageUri: Uri?,
    onImagePicked: (Uri) -> Unit,
    enableGallery: Boolean = false
){
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    // Saveable: the camera app can push this process out of memory on low-RAM devices.
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    // Gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let(onImagePicked)
        showBottomSheet = false
    }

    // Camera
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraUri?.let(onImagePicked)
        showBottomSheet = false
    }

    fun createImageUri(): Uri {
        val file = File(context.cacheDir, "IMG_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    // Image / Camera placeholder
    fun openCameraDirectly() {
        try {
            val uri = createImageUri()
            cameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.camera_open_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val cameraAccess = rememberCameraAccess { openCameraDirectly() }

    fun onPickerClick() {
        if (enableGallery) {
            showBottomSheet = true
            return
        }
        cameraAccess.request()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(white)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onPickerClick() },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            CachedAppImage(
                model = imageUri,
                cacheKey = imageUri.toString(),
                thumbDecode = true,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Surface(
                    color = Color(0xFF1B8E50),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = white,
                        modifier = Modifier.padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (enableGallery) "Camera or Gallery" else "Capture odometer",
                    color = Color(0xFF1B8E50),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontFamily = appFontFamily
                )
                Text(
                    text = if (enableGallery) "ဓာတ်ပုံရိုက် / ရွေးမည်" else "ကီလိုအားဓာတ်ပုံရိုက်မည်",
                    color = Color(0xFF1B8E50),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = appFontFamily
                )
            }
        }
    }

    cameraAccess.deniedText?.let { message ->
        Text(
            text = message,
            color = Color(0xFFD32F2F),
            fontSize = 12.sp,
            fontFamily = appFontFamily,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
    }

    if (showBottomSheet && enableGallery) {
        ImageSourceBottomSheet(
            onDismiss = { showBottomSheet = false },
            onCamera = {
                showBottomSheet = false
                cameraAccess.request()
            },
            onGallery = {
                showBottomSheet = false
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }
}


/** Thrown when a photo cannot be decoded/compressed. Message is user-facing. */
class ImageProcessingException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Memory-safe decode: reads bounds first, then decodes with inSampleSize so the
 * bitmap loaded into memory is already close to [maxDim]. Returns null on failure.
 */
internal fun decodeSampledBitmap(context: Context, uri: Uri, maxDim: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    // decodeStream always returns null while inJustDecodeBounds is set, so the stream itself
    // (not the decode result) is what tells us whether the uri could be opened at all.
    val opened = context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, bounds)
        true
    } ?: false
    if (!opened) return null

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (bounds.outWidth / (sampleSize * 2) >= maxDim ||
        bounds.outHeight / (sampleSize * 2) >= maxDim
    ) {
        sampleSize *= 2
    }

    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, options)
    }
}

internal fun applyExifRotation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
    val orientation = try {
        context.contentResolver.openInputStream(uri)?.use { ExifInterface(it) }
            ?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    } catch (e: Exception) {
        null
    }

    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        else -> return bitmap
    }

    val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    if (rotated != bitmap) bitmap.recycle()
    return rotated
}

internal fun newCacheImageFile(context: Context): File =
    File(context.cacheDir, "IMG_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(8)}.jpg")

suspend fun uriToFile(uri: Uri, context: Context): File = withContext(Dispatchers.IO) {
    try {
        val maxWidth = 1280

        val decoded = decodeSampledBitmap(context, uri, maxWidth)
            ?: run {
                DebugLog.w("UPLOAD_DEBUG", "Could not decode $uri")
                throw ImageProcessingException(context.getString(R.string.image_process_failed))
            }

        val fixedBitmap = applyExifRotation(context, uri, decoded)

        val resizedBitmap = if (fixedBitmap.width > maxWidth) {
            val ratio = maxWidth.toFloat() / fixedBitmap.width
            val scaled = fixedBitmap.scale(maxWidth, (fixedBitmap.height * ratio).toInt())
            if (scaled != fixedBitmap) fixedBitmap.recycle()
            scaled
        } else {
            fixedBitmap
        }

        val file = newCacheImageFile(context)
        FileOutputStream(file).use { out ->
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, out)
            out.flush()
        }
        resizedBitmap.recycle()

        DebugLog.d("UPLOAD_DEBUG", "Final size: ${file.length() / 1024} KB")
        file
    } catch (e: ImageProcessingException) {
        throw e
    } catch (e: OutOfMemoryError) {
        DebugLog.w("UPLOAD_DEBUG", "Out of memory converting $uri", e)
        throw ImageProcessingException(context.getString(R.string.image_process_failed), e)
    } catch (e: Exception) {
        DebugLog.w("UPLOAD_DEBUG", "Failed converting $uri", e)
        throw ImageProcessingException(context.getString(R.string.image_process_failed), e)
    }
}

suspend fun createMultipart(uri: Uri, name: String, context: Context): MultipartBody.Part {
    val file = uriToFile(uri, context)
    val requestBody = file.asRequestBody("image/*".toMediaType())
    return MultipartBody.Part.createFormData(name, file.name, requestBody)
}

/** For images the app generated itself, which need no decode/resize round-trip. */
fun createMultipartFromFile(file: File, name: String): MultipartBody.Part {
    val requestBody = file.asRequestBody("image/jpeg".toMediaType())
    return MultipartBody.Part.createFormData(name, file.name, requestBody)
}


fun toRequestBody(value: String) = value.toRequestBody("text/plain".toMediaType())

