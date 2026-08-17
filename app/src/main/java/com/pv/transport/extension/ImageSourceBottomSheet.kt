package com.pv.transport.extension

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pv.transport.R
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.textPrimary
import com.pv.transport.ui.theme.textSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSourceBottomSheet(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.select_image_source),
                fontFamily = appFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = textPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            ImageSourceOption(
                icon = Icons.Default.CameraAlt,
                title = stringResource(R.string.camera),
                subtitle = stringResource(R.string.take_a_new_photo),
                onClick = onCamera
            )
            HorizontalDivider(color = Color(0xFFE8E8E8), thickness = 0.5.dp)
            ImageSourceOption(
                icon = Icons.Default.PhotoLibrary,
                title = stringResource(R.string.gallery),
                subtitle = stringResource(R.string.choose_from_photos),
                onClick = onGallery
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ImageSourceOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colorPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                fontFamily = appFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = textPrimary
            )
            Text(
                text = subtitle,
                fontFamily = appFontFamily,
                fontSize = 12.sp,
                color = textSecondary
            )
        }
    }
}
