package com.pv.transport.presentation

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.data.log.ReasonListResponse
import com.pv.transport.extension.CustomImagePicker
import com.pv.transport.extension.ReasonDropdown
import com.pv.transport.extension.StartKmTextField
import com.pv.transport.ui.theme.FormFieldLabel
import com.pv.transport.ui.theme.FormPrimaryButton
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
    val site by driverLogViewModel.dailySite.collectAsState()
    val purpose by driverLogViewModel.dailyPurpose.collectAsState()
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
            println("Reason List---- ${reasonList.size}")
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
                Toast.makeText(context, "Saved. Will sync when online.", Toast.LENGTH_SHORT).show()
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
        FormFieldLabel(text = stringResource(R.string.reason), icon = Icons.Default.Category)
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
        FormFieldLabel(text = stringResource(R.string.start_km), icon = Icons.Default.Speed)
        Spacer(modifier = Modifier.height(4.dp))
        StartKmTextField(
            value = startKm,
            hint = stringResource(R.string.enter_start_km),
            onValueChange = { driverLogViewModel.dailyStartKm.value = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        FormFieldLabel(text = stringResource(R.string.start_km_image), icon = Icons.Default.PhotoCamera)
        Spacer(modifier = Modifier.height(4.dp))
        CustomImagePicker(
            imageUri = startUri,
            onImagePicked = {
                println("Image picked = $it")
                driverLogViewModel.dailyStartUri.value = it
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        FormFieldLabel(
            text = stringResource(R.string.site) + " (${stringResource(R.string.optional)})",
            icon = Icons.Default.LocationOn
        )
        Spacer(modifier = Modifier.height(4.dp))

        BasicTextField(
            value = site,
            onValueChange = {driverLogViewModel.dailySite.value = it},
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
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

                    if (site.isEmpty()) {
                        Text(
                            text = stringResource(R.string.enter_site),
                            color = Color.Gray,
                            fontFamily = appFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp
                        )
                    }

                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        FormFieldLabel(
            text = stringResource(R.string.purpose) + " (${stringResource(R.string.optional)})",
            icon = Icons.Default.Flag
        )
        Spacer(modifier = Modifier.height(4.dp))

        BasicTextField(
            value = purpose,
            onValueChange = { driverLogViewModel.dailyPurpose.value = it },
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = Color.Black
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(white),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (purpose.isEmpty()) {
                            Text(
                                text = stringResource(R.string.enter_remark),
                                color = Color.Gray,
                                fontFamily = appFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        FormFieldLabel(
            text = stringResource(R.string.remark) + " (${stringResource(R.string.optional)})",
            icon = Icons.Default.Notes
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
                        .background(white),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (remark.isEmpty()) {
                            Text(
                                text = stringResource(R.string.describe_purpose),
                                color = Color.Gray,
                                fontFamily = appFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        println("Start Km: $startKm, Start Uri: $startUri, Reason: $selectedReason")

        val canSave = startKm.isNotEmpty() && startUri != null && selectedReason.isNotEmpty()

        FormPrimaryButton(
            text = stringResource(R.string.save),
            onClick = {
                if (isSaving) return@FormPrimaryButton
                driverLogViewModel.checkInDriverLog(
                    date = date,
                    type = type.lowercase(),
                    reasonId = selectedIndex.toString(),
                    site = site,
                    purpose = purpose,
                    remark = remark,
                    startTime = currentTime,
                    startKm = startKm,
                    startPhoto = startUri!!,
                    context = context
                )
            },
            enabled = canSave && !isSaving,
            isLoading = isSaving
        )

    }
}
