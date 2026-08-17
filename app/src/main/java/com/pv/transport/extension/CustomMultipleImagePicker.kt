package com.pv.transport.extension

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.pv.transport.ui.theme.white
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import kotlin.let
import androidx.core.graphics.scale
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Surface
import androidx.compose.ui.text.style.TextAlign
import com.pv.transport.R
import com.pv.transport.util.DebugLog
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomMultipleImagePicker(
    selectedUris: List<Uri> = emptyList(),
    onImagesSelected: (List<Uri>) -> Unit,
    enableGallery: Boolean = false
){
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    // Saveable: the camera app can push this process out of memory on low-RAM devices.
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    // Gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        val newList = selectedUris + uris
        onImagesSelected(newList)
        showBottomSheet = false
    }
    // Camera
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { uri ->
                val newList = selectedUris + uri
                onImagesSelected(newList)
            }
        }
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

    // UI
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

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                Surface(
                    color = Color(0xFF1B8E50),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (enableGallery) "Camera or Gallery" else "Upload photos",
                    color = Color(0xFF1B8E50),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (enableGallery) "(ဓာတ်ပုံရိုက် / ရွေးမည်)" else "(ဓာတ်ပုံများကို အပ်လုဒ်လုပ်မည်)",
                    color = Color(0xFF1B8E50),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

        }

        cameraAccess.deniedText?.let { message ->
            Text(
                text = message,
                color = Color(0xFFD32F2F),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Display selected images
        if (selectedUris.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedUris.forEach { uri ->
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    ) {
                        CachedAppImage(
                            model = uri,
                            cacheKey = uri.toString(),
                            thumbDecode = true,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Remove button
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = Color.Red,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .clickable {
                                    val newList = selectedUris - uri
                                    onImagesSelected(newList)
                                }
                                .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(50))
                                .padding(4.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
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

suspend fun multipleUriToFile(uri: Uri, context: Context): File = withContext(Dispatchers.IO) {
    try {
        val maxWidth = 1024

        val decoded = decodeSampledBitmap(context, uri, maxWidth)
            ?: run {
                DebugLog.w("UPLOAD_DEBUG", "Could not decode $uri")
                throw ImageProcessingException(context.getString(R.string.image_process_failed))
            }

        val rotatedBitmap = applyExifRotation(context, uri, decoded)

        val finalBitmap = if (rotatedBitmap.width > maxWidth) {
            val ratio = maxWidth.toFloat() / rotatedBitmap.width
            val scaled = rotatedBitmap.scale(maxWidth, (rotatedBitmap.height * ratio).toInt())
            if (scaled != rotatedBitmap) rotatedBitmap.recycle()
            scaled
        } else {
            rotatedBitmap
        }

        // Compress under ~1MB, lowering quality stepwise
        var quality = 80
        var compressed: ByteArray
        do {
            val stream = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            compressed = stream.toByteArray()
            quality -= 10
        } while (compressed.size > 1_000_000 && quality > 30)

        finalBitmap.recycle()

        val file = newCacheImageFile(context)
        FileOutputStream(file).use {
            it.write(compressed)
            it.flush()
        }

        DebugLog.d("UPLOAD_DEBUG", "Final File size: ${file.length() / 1024} KB")
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

suspend fun createMultipleMultipart(
    uri: Uri, name: String,
    context: Context
): MultipartBody.Part {
    val file = multipleUriToFile(uri, context)
    val requestBody = file.asRequestBody("image/*".toMediaType())
    return MultipartBody.Part.createFormData(name, file.name, requestBody)
}

suspend fun createMultipartList(
    uriList: List<Uri>,
    name: String,
    context: Context
): List<MultipartBody.Part> {
    return uriList.map { uri ->
        createMultipleMultipart(uri, name, context)
    }
}

