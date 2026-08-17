package com.pv.transport.extension

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.core.content.FileProvider
import androidx.compose.ui.res.stringResource
import com.pv.transport.R
import com.pv.transport.ui.theme.white
import java.io.File
import kotlin.let
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Surface
import androidx.compose.ui.text.style.TextAlign
import com.pv.transport.data.log.ImageItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMultipleImagePicker(
    selectedImages: List<ImageItem>,
    onImagesChanged: (List<ImageItem>) -> Unit,
    onImageDeleted: (String) -> Unit
){
    val context = LocalContext.current
    // Saveable: the camera app can push this process out of memory on low-RAM devices.
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    // Camera
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { uri ->
                val newItem = ImageItem(uri = uri)
                val newList = selectedImages + newItem
                onImagesChanged(newList)
            }
        }
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

    // UI
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(white)
                .clip(RoundedCornerShape(8.dp))
                .clickable { cameraAccess.request() },
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
                    text = stringResource(R.string.upload_photos),
                    color = Color(0xFF1B8E50),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "(ဓာတ်ပုံများကို အပ်လုဒ်လုပ်မည်)",
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
        if (selectedImages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedImages.forEach { image ->
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    ) {

                        val model = image.uri ?: image.url
                        CachedAppImage(
                            model = model,
                            cacheKey = model.toString(),
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
                                    image.id?.let { onImageDeleted(it) }
                                    val newList = selectedImages - image
                                    onImagesChanged(newList)
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

}
