package com.pv.transport.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object FormFieldLabelDefaults {
    val IconSize: Dp = 18.dp
    val Spacing: Dp = 6.dp
    val TextSize: TextUnit = 14.sp
    val LineHeight: TextUnit = 18.sp
    val ContentColor: Color = Color(0xFF495057)
}

/**
 * Form field label with icon + text, vertically center-aligned.
 */
@Composable
fun FormFieldLabel(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = FormFieldLabelDefaults.ContentColor
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FormFieldLabelDefaults.Spacing)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(FormFieldLabelDefaults.IconSize),
            tint = color
        )
        Text(
            text = text,
            color = color,
            fontSize = FormFieldLabelDefaults.TextSize,
            lineHeight = FormFieldLabelDefaults.LineHeight,
            fontFamily = appFontFamily,
            fontWeight = FontWeight.Normal,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both
                )
            )
        )
    }
}
