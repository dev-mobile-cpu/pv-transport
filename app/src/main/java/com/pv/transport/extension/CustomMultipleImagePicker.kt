package com.pv.transport.extension

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import coil.compose.rememberAsyncImagePainter
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
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomMultipleImagePicker(
    selectedUris: List<Uri> = emptyList(),
    onImagesSelected: (List<Uri>) -> Unit
){
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

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

    // Camera permission
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) showBottomSheet = true
        else Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
    }

    fun createImageUri(): Uri {
        val file = File(context.cacheDir, "IMG_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    // UI
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(white)
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED
                    ) showBottomSheet = true
                    else permissionLauncher.launch(Manifest.permission.CAMERA)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = null,
                tint = Color(0xFF1B5E20),
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp)
            )

            Text(
                text = "Upload photos\n(ဓာတ်ပုံများကို အပ်လုဒ်လုပ်မည်)",
                color = Color(0xFF1B5E20),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Center)
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
                        Image(
                            painter = rememberAsyncImagePainter(uri),
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

    // Bottom sheet
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PickerItem("📷 Take Photo") {
                    cameraUri = createImageUri()
                    cameraLauncher.launch(cameraUri!!)
                }

                PickerItem("🖼 Pick from Gallery") {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 16.sp)
    }
}

fun multipleUriToFile(uri: Uri, context: Context): File {

    val originalBitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BitmapFactory.decodeStream(inputStream)
    } ?: throw IllegalArgumentException("Cannot decode bitmap")

    val maxWidth = 1024

    val finalBitmap = if (originalBitmap.width > maxWidth) {
        val ratio = maxWidth.toFloat() / originalBitmap.width
        val newHeight = (originalBitmap.height * ratio).toInt()
        originalBitmap.scale(maxWidth, newHeight)
    } else {
        originalBitmap
    }

    // recycle original if resized
    if (finalBitmap != originalBitmap) {
        originalBitmap.recycle()
    }

    val file = File(context.cacheDir, "IMG_${System.currentTimeMillis()}.jpg")

    var quality = 80
    var compressed: ByteArray

    do {
        val stream = ByteArrayOutputStream()
        stream.use {
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, it)
            compressed = it.toByteArray()
        }
        quality -= 10
    } while (compressed.size > 1_000_000 && quality > 30)

    FileOutputStream(file).use {
        it.write(compressed)
        it.flush()
    }

    // optional: free bitmap memory
    finalBitmap.recycle()

    Log.d("UPLOAD_DEBUG", "Final File size: ${file.length() / 1024} KB")

    return file
}

//fun multipleUriToFile(uri: Uri, context: Context): File {
//
//    val inputStream = context.contentResolver.openInputStream(uri)
//        ?: throw IllegalArgumentException("Cannot open URI")
//
//    val originalBitmap = BitmapFactory.decodeStream(inputStream)
//
//    var finalBitmap = originalBitmap
//    if (originalBitmap.width > 2000) {
//        val maxWidth = 2000
//        val ratio = maxWidth.toFloat() / originalBitmap.width
//        val newHeight = (originalBitmap.height * ratio).toInt()
//
//        finalBitmap = originalBitmap.scale(maxWidth, newHeight)
//    }
//    val file = File(context.cacheDir, "IMG_${System.currentTimeMillis()}.jpg")
//    val outputStream = FileOutputStream(file)
//    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
//    outputStream.flush()
//    outputStream.close()
//    Log.d(
//        "UPLOAD_DEBUG",
//        "High Quality File size: ${file.length() / 1024} KB"
//    )
//
//    return file
//}

fun createMultipleMultipart(
    uri: Uri, name: String,
    context: Context
): MultipartBody.Part {
    val file = multipleUriToFile(uri, context)
    val requestBody = file.asRequestBody("image/*".toMediaType())
    return MultipartBody.Part.createFormData(name, file.name, requestBody)
}

fun createMultipartList(
    uriList: List<Uri>,
    name: String,
    context: Context
): List<MultipartBody.Part> {
    return uriList.map { uri ->
        createMultipleMultipart(uri, name, context)
    }
}

