package com.pv.transport.ui.theme

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val DOT_COUNT = 3
private const val DIM_ALPHA = 0.2f
private const val FADE_IN_MILLIS = 250
private const val STAGGER_MILLIS = 200
private const val HOLD_UNTIL_MILLIS = 950
private const val FADED_OUT_AT_MILLIS = 1200
private const val CYCLE_MILLIS = 1450

/**
 * Three dots that light up one after another, fade out together and start over for as long
 * as the app is waiting, matching the pace of the Telegram typing indicator.
 */
@Composable
fun DotsLoading(
    modifier: Modifier = Modifier,
    color: Color = colorPrimary,
    dotSize: Dp = 8.dp
) {
    val transition = rememberInfiniteTransition(label = "dotsLoading")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dotSize / 2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(DOT_COUNT) { index ->
            val litFrom = index * STAGGER_MILLIS
            val alpha by transition.animateFloat(
                initialValue = DIM_ALPHA,
                targetValue = DIM_ALPHA,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = CYCLE_MILLIS
                        if (litFrom > 0) DIM_ALPHA at 0
                        DIM_ALPHA at litFrom
                        1f at litFrom + FADE_IN_MILLIS
                        1f at HOLD_UNTIL_MILLIS
                        DIM_ALPHA at FADED_OUT_AT_MILLIS
                    }
                ),
                label = "dotAlpha$index"
            )

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .alpha(alpha)
                    .background(color, CircleShape)
            )
        }
    }
}
