package com.pv.transport.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Status badge with fit-content width (no fixed width / fillMaxWidth).
 * Keeps "PENDING" on one line on small screens.
 */
@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val badgeStatus = status.uppercase()
    val backgroundColor = when (badgeStatus) {
        "OFFLINE" -> Color(0xFFF5F5F5)
        "SYNCING" -> Color(0xFFE3F2FD)
        "PENDING" -> backgroundColorPending
        "APPROVED" -> backgroundColorApproved
        "REJECTED" -> Color(0xFFFFEBEE)
        else -> backgroundColorApproved
    }
    val contentColor = when (badgeStatus) {
        "OFFLINE" -> Color(0xFF757575)
        "SYNCING" -> Color(0xFF1976D2)
        "PENDING" -> checkColorPending
        "APPROVED" -> checkColorApproved
        "REJECTED" -> Color(0xFFC62828)
        else -> checkColorApproved
    }

    Row(
        modifier = modifier
            .wrapContentWidth()
            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (badgeStatus == "SYNCING") {
            val infiniteTransition = rememberInfiniteTransition(label = "sync")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing)
                ),
                label = "sync_rot"
            )
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier
                    .size(12.dp)
                    .rotate(rotation)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = badgeStatus,
            fontSize = 11.sp,
            fontFamily = appFontFamily,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible
        )
    }
}
