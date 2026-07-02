package com.pv.transport.extension

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pv.transport.R
import com.pv.transport.data.log.Document
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.colorSecondary

@Composable
fun ImageUploadBox(
    title: String,
    document: List<Document>
) {
    val photo = document.firstOrNull {
        (it.kindOfDoc == "start-photo" && title == "Start Km Image") ||
                (it.kindOfDoc == "end-photo" && title == "End Km Image")
    }

    Column {
        Text(
            text = title,
            fontSize = 12.sp,
            fontFamily = appFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(100.dp, 90.dp)
                .background(colorSecondary, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (photo != null) {
                AsyncImage(
                    model = photo.documentUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp, 90.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = stringResource(R.string.image_uploaded),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}
