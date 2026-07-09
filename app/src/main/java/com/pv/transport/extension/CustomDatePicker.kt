package com.pv.transport.extension

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.white
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun CustomDatePicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    bgColor: Color,
    readOnly: Boolean = false
){

    var showDialog by remember { mutableStateOf(false) }
    val today = LocalDate.now()

    // datePickerState must be created when the dialog is shown so it picks up the
    // latest selectedDate and ensures the displayed month/focus matches the
    // currently selected date (or today by default).

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .then(if (!readOnly) Modifier.clickable { showDialog = true } else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp)

    ) {
        val displayDate = selectedDate.format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        ) ?: today.format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        )

        Text(
            text = displayDate,
            fontFamily = appFontFamily ,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.align(Alignment.CenterStart),
            color = if (readOnly) Color.Gray else Color.Black
        )

        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.align(Alignment.CenterEnd),
            tint = if (readOnly) Color.Gray else Color.Black
        )
    }

    // Date Picker Dialog
    if (showDialog && !readOnly) {
        // Create a fresh DatePickerState when dialog opens so the picker focuses
        // on `selectedDate` (which defaults to today on first render) and the
        // selected/focused day will remain in sync with the UI.
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli(),
            initialDisplayedMonthMillis = selectedDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()

                            onDateSelected(localDate)
                        }
                        showDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

}