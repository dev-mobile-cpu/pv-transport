package com.pv.transport.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared solid primary button for form Save / submit actions.
 * Active: #169A5A bg, white icon+text. Disabled: muted. No border.
 * Do not use for Log list Checkout CTA.
 */
object FormPrimaryButtonDefaults {
    val BackgroundColor: Color = Color(0xFF169A5A)
    val ContentColor: Color = Color.White
    val DisabledBackgroundColor: Color = Color(0xFFBDBDBD)
    val DisabledContentColor: Color = Color.White
    val CornerRadius: Dp = 12.dp
    val Height: Dp = 50.dp
    val IconSize: Dp = 20.dp
    val TextSize: TextUnit = 15.sp
}

@Composable
fun FormPrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Save,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(FormPrimaryButtonDefaults.Height),
        shape = RoundedCornerShape(FormPrimaryButtonDefaults.CornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = FormPrimaryButtonDefaults.BackgroundColor,
            contentColor = FormPrimaryButtonDefaults.ContentColor,
            disabledContainerColor = FormPrimaryButtonDefaults.DisabledBackgroundColor,
            disabledContentColor = FormPrimaryButtonDefaults.DisabledContentColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        if (isLoading) {
            DotsLoading(
                color = FormPrimaryButtonDefaults.ContentColor,
                dotSize = 7.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(FormPrimaryButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    fontSize = FormPrimaryButtonDefaults.TextSize,
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
