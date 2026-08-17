package com.pv.transport.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.PopupProperties

object FormSelectDefaults {
    val Height = 50.dp
    val CornerRadius = 12.dp
    val BorderColor = Color(0xFFE0E0E0)
    val LabelColor = Color(0xFF757575)
    val SelectedItemBg = Color(0x14169A5A) // soft green tint matching theme
    val TextColor = Color.Black
    val PlaceholderColor = Color(0xFFBDBDBD)
}

/**
 * Simple clean select matching Approval User dialog style.
 * White field, light gray border, chevron, soft selected highlight.
 */
@Composable
fun FormSelect(
    selectedLabel: String,
    options: List<String>,
    onSelected: (index: Int, label: String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    var fieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    val density = LocalDensity.current
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "formSelectChevron"
    )
    val displayText = selectedLabel.ifBlank { placeholder }
    val isPlaceholder = selectedLabel.isBlank() && placeholder.isNotEmpty()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { fieldSize = it.size.toSize() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(FormSelectDefaults.Height)
                .border(
                    width = 1.dp,
                    color = FormSelectDefaults.BorderColor,
                    shape = RoundedCornerShape(FormSelectDefaults.CornerRadius)
                )
                .clip(RoundedCornerShape(FormSelectDefaults.CornerRadius))
                .background(Color.White)
                .clickable(
                    enabled = enabled,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { expanded = true }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayText,
                fontFamily = appFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = if (isPlaceholder) FormSelectDefaults.PlaceholderColor
                else FormSelectDefaults.TextColor,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = FormSelectDefaults.LabelColor,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotation)
            )
        }

        DropdownMenu(
            expanded = expanded && options.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(density) { fieldSize.width.toDp() })
                .background(Color.White)
        ) {
            FormSelectOptions(
                options = options,
                selectedLabel = selectedLabel,
                onSelected = { index, label ->
                    onSelected(index, label)
                    expanded = false
                }
            )
        }
    }
}

/**
 * Searchable variant of [FormSelect]: the field accepts typing and filters [options],
 * but keeps the same field height, border, chevron and menu styling.
 */
@Composable
fun FormSearchSelect(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    options: List<String>,
    onSelected: (index: Int, label: String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    selectedLabel: String = ""
) {
    var expanded by remember { mutableStateOf(false) }
    var fieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    val density = LocalDensity.current
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "formSearchSelectChevron"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { fieldSize = it.size.toSize() }
    ) {
        BasicTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = appFontFamily,
                fontSize = 16.sp,
                color = FormSelectDefaults.TextColor
            ),
            cursorBrush = SolidColor(FormSelectDefaults.TextColor),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(FormSelectDefaults.Height)
                        .border(
                            width = 1.dp,
                            color = FormSelectDefaults.BorderColor,
                            shape = RoundedCornerShape(FormSelectDefaults.CornerRadius)
                        )
                        .clip(RoundedCornerShape(FormSelectDefaults.CornerRadius))
                        .background(Color.White)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (value.text.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                fontFamily = appFontFamily,
                                fontSize = 16.sp,
                                color = FormSelectDefaults.PlaceholderColor
                            )
                        }
                        innerTextField()
                    }
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = FormSelectDefaults.LabelColor,
                        modifier = Modifier
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { expanded = !expanded }
                            .size(24.dp)
                            .rotate(rotation)
                    )
                }
            }
        )

        DropdownMenu(
            expanded = expanded && options.isNotEmpty(),
            onDismissRequest = { expanded = false },
            properties = PopupProperties(focusable = false),
            modifier = Modifier
                .width(with(density) { fieldSize.width.toDp() })
                .background(Color.White)
        ) {
            FormSelectOptions(
                options = options,
                selectedLabel = selectedLabel,
                onSelected = { index, label ->
                    onSelected(index, label)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun FormSelectOptions(
    options: List<String>,
    selectedLabel: String,
    onSelected: (index: Int, label: String) -> Unit
) {
    options.forEachIndexed { index, label ->
        val isSelected = label == selectedLabel
        DropdownMenuItem(
            text = {
                Text(
                    text = label,
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = FormSelectDefaults.TextColor
                )
            },
            onClick = { onSelected(index, label) },
            modifier = if (isSelected) {
                Modifier.background(FormSelectDefaults.SelectedItemBg)
            } else {
                Modifier
            }
        )
    }
}
