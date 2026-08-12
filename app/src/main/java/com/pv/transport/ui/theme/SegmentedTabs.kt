package com.pv.transport.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared segmented tab control used by Fuel and Daily Log tab screens.
 * Selected: white chip + brand green text. Inactive: muted label on soft track.
 */
object SegmentedTabsDefaults {
    val TrackColor: Color = Color(0xFFE9EAEF)
    val InactiveTextColor: Color = Color(0xFF8A8F98)
    val TrackCornerRadius: Dp = 16.dp
    val ChipCornerRadius: Dp = 12.dp
    val TrackPadding: Dp = 4.dp
    val ChipVerticalPadding: Dp = 10.dp
    val TextSize: TextUnit = 13.sp
}

@Composable
fun SegmentedTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SegmentedTabsDefaults.TrackCornerRadius))
            .background(SegmentedTabsDefaults.TrackColor)
            .padding(SegmentedTabsDefaults.TrackPadding)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, title ->
                val selected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(SegmentedTabsDefaults.ChipCornerRadius))
                        .background(if (selected) white else Color.Transparent)
                        .clickable { onTabSelected(index) }
                        .padding(vertical = SegmentedTabsDefaults.ChipVerticalPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = SegmentedTabsDefaults.TextSize,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selected) colorPrimary else SegmentedTabsDefaults.InactiveTextColor,
                        fontFamily = appFontFamily
                    )
                }
            }
        }
    }
}
