package com.pv.transport.presentation

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.data.log.ReasonListResponse
import com.pv.transport.extension.CustomImagePicker
import com.pv.transport.extension.ReasonDropdown
import com.pv.transport.extension.StartKmTextField
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.DriverLogViewModel
import com.pv.transport.viewmodels.ReasonViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyCheckInScreen(
    navController: NavController,
    type: String,
    date: String,
    reasonViewModel: ReasonViewModel,
    driverLogViewModel: DriverLogViewModel,
    clearTrigger: Int = 0
) {
    val reasonsState by reasonViewModel.state.collectAsState()
    val driverLogState by driverLogViewModel.state.collectAsState()
    
    val startKm by driverLogViewModel.dailyStartKm.collectAsState()
    val remark by driverLogViewModel.dailyRemark.collectAsState()
    val startUri by driverLogViewModel.dailyStartUri.collectAsState()
    val selectedReason by driverLogViewModel.dailySelectedReason.collectAsState()
    val selectedIndex by driverLogViewModel.dailySelectedIndex.collectAsState()
    
    val reasonList = remember { mutableStateListOf<ReasonListResponse>() }
    val context = LocalContext.current
    var isSaved by remember { mutableStateOf(false) }
    var isButtonClicked by remember { mutableStateOf(false) }

    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.ENGLISH) }
    var currentTime by remember { mutableStateOf(timeFormatter.format(Date())) }

    LaunchedEffect(Unit) {
        // Reset state only if it was Success to allow retrying Errors without losing data
        val currentS = driverLogViewModel.state.value
        if (currentS is DriverLogViewModel.DriverLogState.Success || 
            currentS is DriverLogViewModel.DriverLogState.SavedOffline) {
            driverLogViewModel.resetState()
        }
        
        while (true) {
            currentTime = timeFormatter.format(Date())
            delay(1000)
        }
    }

    LaunchedEffect(clearTrigger) {
        if (clearTrigger > 0) {
            driverLogViewModel.clearDailyCheckIn()
            if (reasonList.isNotEmpty()) {
                driverLogViewModel.dailySelectedReason.value = reasonList[0].value
                driverLogViewModel.dailySelectedIndex.value = reasonList[0].id.toInt()
            }
        }
    }

    LaunchedEffect(reasonsState) {
        val s = reasonsState
        if (s is ReasonViewModel.UiState.Success) {
            reasonList.clear()
            reasonList.addAll(s.reasons.data)
            if (selectedReason.isEmpty() && reasonList.isNotEmpty()) {
                driverLogViewModel.dailySelectedReason.value = reasonList[0].value
                driverLogViewModel.dailySelectedIndex.value = reasonList[0].id.toInt()
            }
        } else if (s is ReasonViewModel.UiState.Idle) {
            reasonViewModel.getReasons()
        }
    }

    LaunchedEffect(driverLogState) {
        val state = driverLogState
        when (state) {
            is DriverLogViewModel.DriverLogState.Success -> {
                isButtonClicked = false
                Toast.makeText(context, "Save successful", Toast.LENGTH_SHORT).show()
                isSaved = true
                delay(350)
                navController.popBackStack()
            }
            is DriverLogViewModel.DriverLogState.SavedOffline -> {
                isButtonClicked = false
                Toast.makeText(context, "Saved offline.", Toast.LENGTH_SHORT).show()
                isSaved = true
                delay(350)
                navController.popBackStack()
            }
            is DriverLogViewModel.DriverLogState.Error -> {
                isButtonClicked = false
                Toast.makeText(context, "Save failed: ${state.message}", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    val isSaving = driverLogState is DriverLogViewModel.DriverLogState.Loading

    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            stringResource(R.string.reason),
            fontFamily = appFontFamily,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(4.dp))
        ReasonDropdown(
            reasons = reasonList,
            selectedReason = selectedReason,
            onReasonSelected = { index, reason ->
                driverLogViewModel.dailySelectedIndex.value = index
                driverLogViewModel.dailySelectedReason.value = reason
            },
            modifier = Modifier
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.start_km),
            fontFamily = appFontFamily,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(4.dp))
        StartKmTextField(
            value = startKm,
            hint = stringResource(R.string.enter_start_km),
            onValueChange = { driverLogViewModel.dailyStartKm.value = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.start_km_image),
            fontFamily = appFontFamily,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(4.dp))
        CustomImagePicker(
            imageUri = startUri,
            onImagePicked = { driverLogViewModel.dailyStartUri.value = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.remark)+" (${stringResource(R.string.optional)})",
            fontFamily = appFontFamily,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(4.dp))

        BasicTextField(
            value = remark,
            onValueChange = { driverLogViewModel.dailyRemark.value = it },
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = Color.Black
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(white)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (remark.isEmpty()) {
                        Text(
                            text = stringResource(R.string.describe_purpose),
                            color = Color.Gray,
                            fontFamily = appFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        if (reasonsState is ReasonViewModel.UiState.Loading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val canSave = startKm.isNotEmpty() && startUri != null
            Button(
                onClick = {
                    if (isButtonClicked) return@Button
                    isButtonClicked = true
                    if (!isSaving) {
                        driverLogViewModel.checkInDriverLog(
                            date = date,
                            type = type.lowercase(),
                            reason = selectedIndex.toString(),
                            remark = remark,
                            startTime = currentTime,
                            startKm = startKm,
                            startPhoto = startUri!!,
                            context = context
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (canSave) colorPrimary else Color.Gray),
                shape = RoundedCornerShape(8.dp),
                enabled = canSave && !isSaving && !isSaved && !isButtonClicked
            ) {
                if (isSaving) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.saving), color = Color.White)
                    }
                } else {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.save),
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color.White)
                }
            }
        }
    }
}
