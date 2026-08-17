package com.pv.transport.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity

/**
 * Facebook-like chrome: list scroll hides/shows the page title and bottom nav.
 * Tab rows stay fixed outside this visibility flag.
 */
@Stable
class CollapsibleChromeState(
    private val hideThresholdPx: Float = 40f,
    private val showThresholdPx: Float = 24f
) {
    var titleVisible by mutableStateOf(true)
        private set

    private var accumulated by mutableFloatStateOf(0f)

    fun show() {
        titleVisible = true
        accumulated = 0f
    }

    fun hide() {
        titleVisible = false
        accumulated = 0f
    }

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            // Observe only; do not consume so lists keep their full scroll range.
            onScrollDelta(available.y)
            return Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity = Velocity.Zero
    }

    private fun onScrollDelta(dy: Float) {
        if (dy == 0f) return
        // dy < 0 → content moving up (user scrolled down) → hide chrome
        // dy > 0 → content moving down (user scrolled up) → show chrome
        if (dy < 0f) {
            if (!titleVisible) {
                accumulated = 0f
                return
            }
            accumulated += -dy
            if (accumulated >= hideThresholdPx) {
                titleVisible = false
                accumulated = 0f
            }
        } else {
            if (titleVisible) {
                accumulated = 0f
                return
            }
            accumulated += dy
            if (accumulated >= showThresholdPx) {
                titleVisible = true
                accumulated = 0f
            }
        }
    }
}

val LocalCollapsibleChrome = compositionLocalOf<CollapsibleChromeState?> { null }

@Composable
fun rememberCollapsibleChromeState(): CollapsibleChromeState {
    return remember { CollapsibleChromeState() }
}

fun Modifier.collapsibleChromeScroll(state: CollapsibleChromeState?): Modifier {
    return if (state != null) nestedScroll(state.nestedScrollConnection) else this
}

@Composable
fun CollapsibleTitleSlot(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(animationSpec = tween(220)) + fadeIn(animationSpec = tween(180)),
        exit = shrinkVertically(animationSpec = tween(220)) + fadeOut(animationSpec = tween(160))
    ) {
        content()
    }
}
