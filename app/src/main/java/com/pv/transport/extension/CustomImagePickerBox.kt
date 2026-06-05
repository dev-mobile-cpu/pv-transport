package com.pv.transport.extension

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.white
import java.io.File
import kotlin.let

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomImagePickerBox(
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(colorSecondary)
            .dashedBorder(
                color = Color.LightGray,
                shape = RoundedCornerShape(12.dp),
                dashWidth = 8.dp,
                gapWidth = 8.dp
            )
            .clickable {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
                )  showBottomSheet = true
                else permissionLauncher.launch(Manifest.permission.CAMERA)
            },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null){
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = null,
                modifier = Modifier,
                contentScale = ContentScale.Crop
            )

        }else{
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                // The Green Plus Icon

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

                // Text in English and Myanmar
                Text(
                    text = "Capture odometer",
                    color = Color(0xFF1B8E50),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "(စက်ဖတ်ကိန်းရိုက်မည်)",
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

fun Modifier.dashedBorder(
    color: Color,
    shape: Shape,
    strokeWidth: Dp = 1.dp,
    dashWidth: Dp = 4.dp,
    gapWidth: Dp = 4.dp,
) = this.drawWithContent {
    drawContent()
    val outline = shape.createOutline(size, layoutDirection, this)
    val pathEffect = PathEffect.dashPathEffect(
        floatArrayOf(dashWidth.toPx(), gapWidth.toPx()),
        0f
    )
    drawOutline(
        outline = outline,
        color = color,
        style = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = pathEffect
        )
    )
}

