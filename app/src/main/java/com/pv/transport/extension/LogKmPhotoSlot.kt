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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pv.transport.data.log.Data
import java.io.File

private val PhotoSlotGray = Color(0xFFE6E7EC)
private val PhotoSlotShimmer = Color(0xFFF4F5F8)

/** List slot height — keep in sync with DriverLogCard photo row. */
private val ListPhotoHeight = 104.dp

/**
 * Prefer a local file when it still exists so list thumbs do not refetch after sync.
 * Returns a stable String (path/uri/url) so Compose can skip recomposition.
 */
fun preferredListPhoto(localPath: String?, remoteUrl: String?): String? {
    val local = localPath?.trim()?.takeIf { it.isNotEmpty() }
    if (local != null) {
        if (local.startsWith("content://") || local.startsWith("file://") ||
            local.startsWith("http://") || local.startsWith("https://")
        ) {
            return local
        }
        if (File(local).exists()) return local
    }
    return remoteUrl?.trim()?.takeIf { it.isNotEmpty() }
}

/** Same source the list uses: local check-in file if present, else start-photo URL. */
fun startKmPhotoModel(data: Data): String? {
    val remote = data.documents.firstOrNull { "start-photo".equals(it.kindOfDoc, ignoreCase = true) }
        ?.documentUrl?.takeUnless { it.isBlank() }
        ?: data.documents.firstOrNull()?.documentUrl?.takeUnless { it.isBlank() }
    return preferredListPhoto(data.startImagePath, remote)
}

/**
 * Log list km photo slot: always a gray container; image crossfades in when ready.
 * Empty / missing / error → gray only (no "Image Uploaded" text).
 */
@Composable
fun LogKmPhotoSlot(
    model: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    cacheKey: String? = null
) {
    val photoModel = remember(model) { model?.trim()?.takeIf { it.isNotEmpty() } }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(PhotoSlotGray)
    ) {
        if (photoModel != null) {
            val context = LocalContext.current
            val density = LocalDensity.current
            val screenWidthDp = LocalConfiguration.current.screenWidthDp
            val thumbHeightPx = remember(density) { with(density) { ListPhotoHeight.roundToPx().coerceAtLeast(1) } }
            val thumbWidthPx = remember(density, screenWidthDp) {
                with(density) { ((screenWidthDp.dp - 42.dp) / 2).roundToPx().coerceAtLeast(1) }
            }
            val slotKey = cacheKey?.takeIf { it.isNotBlank() } ?: photoModel
            val alreadyShown = remember(slotKey) {
                ImageShowTracker.has(slotKey) ||
                    ImageShowTracker.has(thumbMemoryKey(slotKey)) ||
                    isImageInMemoryCache(context, slotKey)
            }
            var isLoading by remember(slotKey) { mutableStateOf(!alreadyShown) }

            if (isLoading) {
                PhotoShimmerOverlay(modifier = Modifier.fillMaxSize())
            }

            key(slotKey) {
                CachedAppImage(
                    model = photoModel,
                    cacheKey = slotKey,
                    widthPx = thumbWidthPx,
                    heightPx = thumbHeightPx,
                    thumbDecode = true,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onLoading = { if (!alreadyShown) isLoading = true },
                    onSuccess = { isLoading = false },
                    onError = { isLoading = false }
                )
            }
        }
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
