package com.pv.transport.extension

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import coil.compose.rememberAsyncImagePainter
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
import com.pv.transport.ui.theme.robotoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomImagePicker(
    imageUri: Uri?,
    onImagePicked: (Uri) -> Unit
){
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

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

    // Image / Camera placeholder

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(white)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                // Opens BottomSheet
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
                )  showBottomSheet = true
                else permissionLauncher.launch(Manifest.permission.CAMERA)

            },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
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
                    text = "Capture odometer",
                    color = Color(0xFF1B8E50),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "ကီလိုအားဓာတ်ပုံရိုက်မည်",
                    color = Color(0xFF1B8E50),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
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

//                PickerItem("🖼 Pick from Gallery") {
//                    galleryLauncher.launch(
//                        PickVisualMediaRequest(
//                            ActivityResultContracts.PickVisualMedia.ImageOnly)
//                    )
//                }
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

fun uriToFile(uri: Uri, context: Context): File {

    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw IllegalArgumentException("Cannot open URI")

    val originalBitmap = BitmapFactory.decodeStream(inputStream)
    inputStream.close()

    val exif = context.contentResolver.openInputStream(uri)?.use {
        ExifInterface(it)
    }

    val orientation = exif?.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )

    val matrix = Matrix()

    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
    }
    val fixedBitmap = Bitmap.createBitmap(
        originalBitmap,
        0,
        0,
        originalBitmap.width,
        originalBitmap.height,
        matrix,
        true
    )
    var finalBitmap = fixedBitmap

    if (fixedBitmap.width > 2000) {

        val maxWidth = 2000
        val ratio = maxWidth.toFloat() / fixedBitmap.width
        val newHeight = (fixedBitmap.height * ratio).toInt()

        finalBitmap = fixedBitmap.scale(maxWidth, newHeight)
    }
    val file = File(context.cacheDir, "IMG_${System.currentTimeMillis()}.jpg")

    val outputStream = FileOutputStream(file)
    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

    outputStream.flush()
    outputStream.close()

    Log.d("UPLOAD_DEBUG", "File size: ${file.length() / 1024} KB")

    return file
}

fun createMultipart(uri: Uri, name: String, context: Context): MultipartBody.Part {
    val file = uriToFile(uri, context)
    val requestBody = file.asRequestBody("image/*".toMediaType())
    return MultipartBody.Part.createFormData(name, file.name, requestBody)
}


fun toRequestBody(value: String) = value.toRequestBody("text/plain".toMediaType())

