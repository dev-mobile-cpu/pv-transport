package com.pv.transport.extension

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import coil.size.Size
import java.io.File
import java.util.Collections

/**
 * Process-wide set of cache keys that have already been drawn this session.
 * Used to skip shimmer/reload flashes when a LazyColumn item recomposes.
 */
internal object ImageShowTracker {
    private val shown = Collections.synchronizedSet(mutableSetOf<String>())

    fun has(key: String): Boolean = key in shown

    fun mark(key: String) {
        shown.add(key)
    }
}

fun resolveImageModel(model: Any?): Any? {
    return when (model) {
        null -> null
        is File -> model.takeIf { it.exists() }
        is String -> {
            val value = model.trim()
            if (value.isEmpty()) {
                null
            } else if (
                value.startsWith("http://") ||
                value.startsWith("https://") ||
                value.startsWith("content://") ||
                value.startsWith("file://")
            ) {
                value
            } else {
                File(value).takeIf { it.exists() } ?: value
            }
        }
        else -> model
    }
}

fun imageCacheIdentity(model: Any?): String? {
    return when (val resolved = resolveImageModel(model)) {
        null -> null
        is File -> resolved.absolutePath
        else -> resolved.toString().trim().takeIf { it.isNotEmpty() }
    }
}

internal fun thumbMemoryKey(cacheKey: String) = "thumb:$cacheKey"

fun isImageInMemoryCache(context: Context, cacheKey: String): Boolean {
    if (cacheKey.isBlank()) return false
    val memory = context.applicationContext.imageLoader.memoryCache ?: return false
    return memory[MemoryCache.Key(thumbMemoryKey(cacheKey))] != null ||
        memory[MemoryCache.Key(cacheKey)] != null
}

/**
 * Warm Coil memory/disk cache so a list thumb can paint on the next frame
 * instead of decoding after navigation.
 */
fun preloadImageThumb(context: Context, source: Any?, cacheKey: String, sizePx: Int = 512) {
    val data = resolveImageModel(source) ?: return
    if (cacheKey.isBlank()) return
    val app = context.applicationContext
    val memoryKey = thumbMemoryKey(cacheKey)
    val request = ImageRequest.Builder(app)
        .data(data)
        .size(sizePx, sizePx)
        .precision(Precision.INEXACT)
        .scale(Scale.FILL)
        .memoryCacheKey(memoryKey)
        .diskCacheKey(memoryKey)
        .placeholderMemoryCacheKey(memoryKey)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .allowRgb565(true)
        .bitmapConfig(Bitmap.Config.RGB_565)
        .listener(onSuccess = { _, _ ->
            ImageShowTracker.mark(memoryKey)
            ImageShowTracker.mark(cacheKey)
        })
        .build()
    app.imageLoader.enqueue(request)
}

/**
 * Shared image loader for every photo surface. Always uses memory + disk cache,
 * a stable cache key, and a remembered request so recomposition does not refetch.
 */
@Composable
fun CachedAppImage(
    model: Any?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    cacheKey: String? = null,
    widthPx: Int? = null,
    heightPx: Int? = null,
    thumbDecode: Boolean = false,
    originalSize: Boolean = false,
    crossfade: Boolean = false,
    onLoading: (() -> Unit)? = null,
    onSuccess: (() -> Unit)? = null,
    onError: (() -> Unit)? = null
) {
    val resolved = remember(model) { resolveImageModel(model) }
    if (resolved == null) return

    val context = LocalContext.current
    val appContext = context.applicationContext
    val imageLoader = appContext.imageLoader
    val resolvedKey = cacheKey?.takeIf { it.isNotBlank() } ?: imageCacheIdentity(resolved)
    val namespacedKey = when {
        resolvedKey.isNullOrBlank() -> null
        originalSize -> "full:$resolvedKey"
        thumbDecode -> "thumb:$resolvedKey"
        else -> "preview:$resolvedKey"
    }
    val request = remember(resolved, namespacedKey, widthPx, heightPx, thumbDecode, originalSize, crossfade, appContext) {
        ImageRequest.Builder(appContext)
            .data(resolved)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .crossfade(crossfade)
            .apply {
                if (widthPx != null && heightPx != null && widthPx > 0 && heightPx > 0) {
                    size(widthPx, heightPx)
                    precision(Precision.INEXACT)
                    scale(Scale.FILL)
                } else if (originalSize) {
                    size(Size.ORIGINAL)
                }
                if (!namespacedKey.isNullOrBlank()) {
                    memoryCacheKey(namespacedKey)
                    diskCacheKey(namespacedKey)
                    placeholderMemoryCacheKey(namespacedKey)
                }
                if (thumbDecode) {
                    allowRgb565(true)
                    bitmapConfig(Bitmap.Config.RGB_565)
                }
            }
            .build()
    }

    AsyncImage(
        model = request,
        imageLoader = imageLoader,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        onLoading = { onLoading?.invoke() },
        onSuccess = {
            namespacedKey?.let { ImageShowTracker.mark(it) }
            resolvedKey?.let { ImageShowTracker.mark(it) }
            onSuccess?.invoke()
        },
        onError = { onError?.invoke() }
    )
}
