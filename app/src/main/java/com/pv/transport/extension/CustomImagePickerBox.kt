package com.pv.transport.extension

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.core.content.FileProvider
import androidx.compose.ui.res.stringResource
import com.pv.transport.R
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
    // Saveable: the camera app can push this process out of memory on low-RAM devices.
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    // Camera
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraUri?.let(onImagePicked)
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

    Column(modifier = Modifier.fillMaxWidth()) {
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
            .clickable { cameraAccess.request() },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null){
            CachedAppImage(
                model = imageUri,
                cacheKey = imageUri.toString(),
                thumbDecode = true,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
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
                    text = stringResource(R.string.capture_odometer),
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

    cameraAccess.deniedText?.let { message ->
        Text(
            text = message,
            color = Color(0xFFD32F2F),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
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

