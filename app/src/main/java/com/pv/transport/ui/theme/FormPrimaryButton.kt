package com.pv.transport.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
 * Form Save / submit — same outline language as Add Log / Add Request.
 * [Danger] is the same shape in red (logout).
 */
object FormPrimaryButtonDefaults {
    val BackgroundColor: Color = Color(0x1A169A5A)
    val ContentColor: Color = Color(0xFF169A5A)
    val BorderColor: Color = Color(0xFF169A5A)
    val DangerBackgroundColor: Color = Color(0x1AD32F2F)
    val DangerContentColor: Color = Color(0xFFD32F2F)
    val DangerBorderColor: Color = Color(0xFFD32F2F)
    val DisabledBackgroundColor: Color = Color(0xFFF1F2F6)
    val DisabledContentColor: Color = Color(0xFFBDBDBD)
    val DisabledBorderColor: Color = Color(0xFFD0D0D0)
    val CornerRadius: Dp = 16.dp
    val BorderWidth: Dp = 1.dp
    val Height: Dp = 50.dp
    val IconSize: Dp = 20.dp
    val TextSize: TextUnit = 15.sp
    val SaveBottomSpace: Dp = 16.dp
}

enum class FormButtonTone {
    Primary,
    Danger
}

fun Modifier.formScrollInsets(innerPadding: PaddingValues): Modifier =
    this
        .padding(top = innerPadding.calculateTopPadding())
        .navigationBarsPadding()
        .imePadding()

@Composable
fun FormPrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Save,
    isLoading: Boolean = false,
    tone: FormButtonTone = FormButtonTone.Primary
) {
    val content = when (tone) {
        FormButtonTone.Primary -> FormPrimaryButtonDefaults.ContentColor
        FormButtonTone.Danger -> FormPrimaryButtonDefaults.DangerContentColor
    }
    val background = when (tone) {
        FormButtonTone.Primary -> FormPrimaryButtonDefaults.BackgroundColor
        FormButtonTone.Danger -> FormPrimaryButtonDefaults.DangerBackgroundColor
    }
    val border = when (tone) {
        FormButtonTone.Primary -> FormPrimaryButtonDefaults.BorderColor
        FormButtonTone.Danger -> FormPrimaryButtonDefaults.DangerBorderColor
    }

    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(FormPrimaryButtonDefaults.Height),
        shape = RoundedCornerShape(FormPrimaryButtonDefaults.CornerRadius),
        border = BorderStroke(
            FormPrimaryButtonDefaults.BorderWidth,
            if (enabled && !isLoading) border else FormPrimaryButtonDefaults.DisabledBorderColor
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = background,
            contentColor = content,
            disabledContainerColor = FormPrimaryButtonDefaults.DisabledBackgroundColor,
            disabledContentColor = FormPrimaryButtonDefaults.DisabledContentColor
        )
    ) {
        if (isLoading) {
            DotsLoading(
                color = content,
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
