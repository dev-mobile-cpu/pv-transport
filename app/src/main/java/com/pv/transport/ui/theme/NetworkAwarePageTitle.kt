package com.pv.transport.ui.theme

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pv.transport.R
import com.pv.transport.network.ConnectivityObserver
import kotlinx.coroutines.delay

enum class NetworkTitleMode {
    Title,
    WaitingForNetwork,
    Connecting
}

fun ConnectivityObserver.Status.toNetworkTitleMode(): NetworkTitleMode = when (this) {
    ConnectivityObserver.Status.Available -> NetworkTitleMode.Title
    ConnectivityObserver.Status.Losing -> NetworkTitleMode.Connecting
    ConnectivityObserver.Status.Lost,
    ConnectivityObserver.Status.Unavailable -> NetworkTitleMode.WaitingForNetwork
}

/**
 * Telegram-style header: page title slides with "Waiting for network…" / "Connecting…".
 * Offline text is debounced so brief Unavailable flashes on tab switch do not show.
 */
@Composable
fun NetworkAwarePageTitle(
    title: String,
    subtitle: String?,
    networkStatus: ConnectivityObserver.Status,
    modifier: Modifier = Modifier,
    titleColor: Color = textPrimary,
    subtitleColor: Color = Color.Gray,
    statusColor: Color = textSecondary
) {
    var mode by remember {
        mutableStateOf(
            if (networkStatus == ConnectivityObserver.Status.Available) {
                NetworkTitleMode.Title
            } else {
                NetworkTitleMode.Title // start with title; confirm offline after debounce
            }
        )
    }

    LaunchedEffect(networkStatus) {
        val target = networkStatus.toNetworkTitleMode()
        if (target == NetworkTitleMode.Title) {
            mode = NetworkTitleMode.Title
        } else {
            delay(450)
            if (networkStatus.toNetworkTitleMode() != NetworkTitleMode.Title) {
                mode = target
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        AnimatedContent(
            targetState = mode,
            transitionSpec = {
                (slideInVertically { fullHeight -> -fullHeight / 2 } + fadeIn()) togetherWith
                    (slideOutVertically { fullHeight -> fullHeight / 2 } + fadeOut())
            },
            label = "network_aware_title"
        ) { current ->
            when (current) {
                NetworkTitleMode.Title -> {
                    Column {
                        Text(
                            text = title,
                            color = titleColor,
                            fontSize = 20.sp,
                            fontFamily = appFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                text = subtitle,
                                color = subtitleColor,
                                fontSize = 14.sp,
                                fontFamily = appFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
                NetworkTitleMode.WaitingForNetwork -> {
                    NetworkStatusLabel(
                        text = stringResource(R.string.waiting_for_network),
                        color = statusColor
                    )
                }
                NetworkTitleMode.Connecting -> {
                    NetworkStatusLabel(
                        text = stringResource(R.string.connecting),
                        color = statusColor
                    )
                }
            }
        }
    }
}

/** Status text whose trailing dots animate instead of sitting still. */
@Composable
private fun NetworkStatusLabel(
    text: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text.trimEnd('…', '.', ' '),
            color = color,
            fontSize = 18.sp,
            fontFamily = appFontFamily,
            fontWeight = FontWeight.SemiBold
        )
        DotsLoading(
            modifier = Modifier.padding(start = 5.dp),
            color = color,
            dotSize = 4.dp
        )
    }
}
