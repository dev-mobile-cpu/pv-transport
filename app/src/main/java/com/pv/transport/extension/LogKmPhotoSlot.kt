package com.pv.transport.extension

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

private val PhotoSlotGray = Color(0xFFE6E7EC)
private val PhotoSlotShimmer = Color(0xFFF4F5F8)

/**
 * Log list km photo slot: always a gray container; image crossfades in when ready.
 * Empty / missing / error → gray only (no "Image Uploaded" text).
 */
@Composable
fun LogKmPhotoSlot(
    model: Any?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp
) {
    val resolvedModel = remember(model) { resolveImageModel(model) }
    val hasModel = resolvedModel != null

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(PhotoSlotGray)
    ) {
        if (hasModel) {
            val context = LocalContext.current
            var isLoading by remember(resolvedModel) { mutableStateOf(true) }

            if (isLoading) {
                PhotoShimmerOverlay(modifier = Modifier.fillMaxSize())
            }

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resolvedModel)
                    .crossfade(280)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onLoading = { isLoading = true },
                onSuccess = { isLoading = false },
                onError = { isLoading = false }
            )
        }
    }
}

private fun resolveImageModel(model: Any?): Any? {
    return when (model) {
        null -> null
        is String -> {
            val value = model.trim()
            if (value.isEmpty()) {
                null
            } else if (value.startsWith("http://") || value.startsWith("https://") || value.startsWith("content://") || value.startsWith("file://")) {
                value
            } else {
                // Local offline file path
                File(value).takeIf { it.exists() } ?: value
            }
        }
        else -> model
    }
}

@Composable
private fun PhotoShimmerOverlay(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "photo_shimmer")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "photo_shimmer_shift"
    )
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = listOf(PhotoSlotGray, PhotoSlotShimmer, PhotoSlotGray),
                start = Offset(shift - 400f, 0f),
                end = Offset(shift, 0f)
            )
        )
    )
}
