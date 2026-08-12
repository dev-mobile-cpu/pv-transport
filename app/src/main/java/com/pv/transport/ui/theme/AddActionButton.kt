package com.pv.transport.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared Create/Add action button styles.
 * Use for: Daily Log, Logsheet, Fuel Log, Fuel Request, Other Expenses.
 */
object AddActionButtonDefaults {
    val BorderColor: Color = Color(0xFF169A5A)
    val ContentColor: Color = Color(0xFF169A5A)
    val BackgroundColor: Color = Color(0x1A169A5A) // #169A5A1A
    val CornerRadius: Dp = 12.dp
    val BorderWidth: Dp = 1.dp
    val HorizontalPadding: Dp = 14.dp
    val VerticalPadding: Dp = 8.dp
    val IconSize: Dp = 18.dp
    val TextSize: TextUnit = 13.sp
}

@Composable
fun AddActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Add,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(AddActionButtonDefaults.CornerRadius),
        border = BorderStroke(
            width = AddActionButtonDefaults.BorderWidth,
            color = AddActionButtonDefaults.BorderColor
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AddActionButtonDefaults.BackgroundColor,
            contentColor = AddActionButtonDefaults.ContentColor,
            disabledContainerColor = AddActionButtonDefaults.BackgroundColor.copy(alpha = 0.5f),
            disabledContentColor = AddActionButtonDefaults.ContentColor.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(
            horizontal = AddActionButtonDefaults.HorizontalPadding,
            vertical = AddActionButtonDefaults.VerticalPadding
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(AddActionButtonDefaults.IconSize),
            tint = AddActionButtonDefaults.ContentColor
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = AddActionButtonDefaults.TextSize,
            fontFamily = appFontFamily,
            fontWeight = FontWeight.SemiBold,
            color = AddActionButtonDefaults.ContentColor
        )
    }
}
