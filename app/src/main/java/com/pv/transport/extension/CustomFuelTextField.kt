package com.pv.transport.extension

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pv.transport.R
import com.pv.transport.ui.theme.white

@Composable
fun CustomFuelTextField(
    value: String,
    hint: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    modifier: Modifier,
    enableComma: Boolean = false
) {

    BasicTextField(
        value = value,
        onValueChange = { new ->
            if (keyboardType == KeyboardType.Number) {
                // Numeric fields: digits + at most one decimal point, capped length
                val filtered = new.filter { it.isDigit() || it == '.' }
                val firstDot = filtered.indexOf('.')
                val cleaned = if (firstDot >= 0) {
                    filtered.filterIndexed { i, c -> c != '.' || i == firstDot }
                } else {
                    filtered
                }
                onValueChange(cleaned.take(12))
            } else {
                onValueChange(new.take(100))
            }
        },
        visualTransformation = if (enableComma) ThousandSeparatorTransformation() else VisualTransformation.None,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = Color.Black
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            ),
        decorationBox = { innerTextField ->

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {

                if (value.isEmpty()) {
                    Text(
                        text = hint,
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }

                innerTextField()
            }
        }
    )
}