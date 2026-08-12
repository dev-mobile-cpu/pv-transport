package com.pv.transport.presentation

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.data.log.ReasonListResponse
import com.pv.transport.data.log.TripType
import com.pv.transport.extension.CustomImagePicker
import com.pv.transport.extension.ReasonDropdown
import com.pv.transport.extension.StartKmTextField
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.textPrimary
import com.pv.transport.ui.theme.textSecondary
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.DriverLogViewModel
import com.pv.transport.viewmodels.ReasonViewModel
import com.pv.transport.viewmodels.TripTypeViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TripCheckInScreen(
    navController: NavController,
    type: String,
    date: String,
    reasonViewModel: ReasonViewModel = hiltViewModel(),
    tripTypeViewModel: TripTypeViewModel = hiltViewModel(),
    driverLogViewModel: DriverLogViewModel = hiltViewModel(),
    clearTrigger: Int = 0
) {

    val reasons = reasonViewModel.state.collectAsState()
    val tripType = tripTypeViewModel.state.collectAsState()
    val driverLogState = driverLogViewModel.state.collectAsState()

    // ViewModel-based states
    val startKm by driverLogViewModel.tripStartKm.collectAsState()
    val purpose by driverLogViewModel.tripPurpose.collectAsState()
    val startUri by driverLogViewModel.tripStartUri.collectAsState()
    val selectedReason by driverLogViewModel.tripSelectedReason.collectAsState()
    val selectedIndex by driverLogViewModel.tripSelectedIndex.collectAsState()
    val selectedTrip by driverLogViewModel.tripSelectedTrip.collectAsState()
    val tripTypeIndex by driverLogViewModel.tripTypeIndex.collectAsState()
    val from by driverLogViewModel.tripFrom.collectAsState()
    val to by driverLogViewModel.tripTo.collectAsState()

    val reasonList = remember { mutableStateListOf<ReasonListResponse>() }
    val tripTypeList = remember { mutableStateListOf<TripType>() }
    val context = LocalContext.current
    var isSaved by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var isButtonClicked by remember { mutableStateOf(false) }

    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.ENGLISH) }
    var currentTime by remember { mutableStateOf(timeFormatter.format(Date())) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTime = timeFormatter.format(now)
            delay(1000)
        }
    }

    LaunchedEffect(clearTrigger) {
        if (clearTrigger > 0) {
            driverLogViewModel.clearTripCheckIn()
            if (reasonList.isNotEmpty()) {
                driverLogViewModel.tripSelectedReason.value = reasonList[0].value
                driverLogViewModel.tripSelectedIndex.value = reasonList[0].id.toInt()
            }
            if (tripTypeList.isNotEmpty()) {
                driverLogViewModel.tripSelectedTrip.value = tripTypeList[0].value
                driverLogViewModel.tripTypeIndex.value = tripTypeList[0].id.toInt()
            }
        }
    }

    when (val s = reasons.value) {
        is ReasonViewModel.UiState.Idle -> {
            reasonViewModel.getReasons()
        }

        is ReasonViewModel.UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is ReasonViewModel.UiState.Success -> {
            reasonList.clear()
            reasonList.addAll(s.reasons.data)
            if (selectedReason.isEmpty() && reasonList.isNotEmpty()) {
                driverLogViewModel.tripSelectedReason.value = reasonList[0].value
                driverLogViewModel.tripSelectedIndex.value = reasonList[0].id.toInt()
            }
        }

        else -> {}
    }

    when (val s = tripType.value) {
        is TripTypeViewModel.UiState.Idle -> {
            tripTypeViewModel.getTripType()
        }

        is TripTypeViewModel.UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is TripTypeViewModel.UiState.Success -> {
            tripTypeList.clear()
            tripTypeList.addAll(s.tripType.data)
            if (selectedTrip.isEmpty() && tripTypeList.isNotEmpty()) {
                driverLogViewModel.tripSelectedTrip.value = tripTypeList[0].value
                driverLogViewModel.tripTypeIndex.value = tripTypeList[0].id.toInt()
            }
        }

        else -> {}
    }

    val isSaving = driverLogState.value is DriverLogViewModel.DriverLogState.Loading

    LaunchedEffect(key1 = driverLogState.value) {
        when (val state = driverLogState.value) {
            is DriverLogViewModel.DriverLogState.SavedOffline -> {
                isButtonClicked = false
                Toast.makeText(context, "Saved offline. Will sync when connected.", Toast.LENGTH_SHORT).show()
                isSaved = true
                delay(350)
                navController.popBackStack()
            }
            is DriverLogViewModel.DriverLogState.Success -> {
                isButtonClicked = false
                Toast.makeText(context, "Save successful", Toast.LENGTH_SHORT).show()
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
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)

    ) {
        Text(
            stringResource(R.string.trip_type),
            fontFamily = appFontFamily,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box{
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(white)
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selectedTrip)
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                tripTypeList.forEach { trip ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(trip.value)

                                if (trip.value == selectedTrip) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null
                                    )
                                }
                            }
                        },
                        onClick = {
                            driverLogViewModel.tripSelectedTrip.value = trip.value
                            driverLogViewModel.tripTypeIndex.value = trip.id.toInt()
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.from),
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(white),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = from,
                        onValueChange = { driverLogViewModel.tripFrom.value = it },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (from.isEmpty()) {
                                    Text(
                                        text = "Enter destination",
                                        color = textSecondary,
                                        fontSize = 16.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.to),
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(white),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = to,
                        onValueChange = { driverLogViewModel.tripTo.value = it },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (to.isEmpty()) {
                                    Text(
                                        text = "Enter destination",
                                        color = textSecondary,
                                        fontSize = 16.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
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
                driverLogViewModel.tripSelectedIndex.value = index
                driverLogViewModel.tripSelectedReason.value = reason
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
            onValueChange = { driverLogViewModel.tripStartKm.value = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.start_km_image))
        Spacer(modifier = Modifier.height(4.dp))
        CustomImagePicker(
            imageUri = startUri,
            onImagePicked = { driverLogViewModel.tripStartUri.value = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            stringResource(R.string.purpose_trip)+" (${stringResource(R.string.optional)})",
            fontFamily = appFontFamily,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(4.dp))

        BasicTextField(
            value = purpose,
            onValueChange = { driverLogViewModel.tripPurpose.value = it },
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
                        if (purpose.isEmpty()) {
                            Text(
                                text = stringResource(R.string.describe_purpose),
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        val canSave = startKm.isNotEmpty() && startUri != null && from.isNotEmpty() && to.isNotEmpty() &&  selectedReason.isNotEmpty()

        Button(
            onClick = {
                if (isSaving) return@Button

                driverLogViewModel.checkInTripDriverLog(
                    date = date,
                    type = type.lowercase(),
                    tripTypeId = tripTypeIndex.toString(),
                    from = from,
                    to = to,
                    purpose = purpose,
                    reasonId = selectedIndex.toString(),
                    startTime = currentTime,
                    startKm = startKm,
                    startPhoto = startUri!!,
                    context = context
                )
            },
            enabled = canSave && !isSaved && !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorPrimary,
                disabledContainerColor = Color.LightGray // ပိတ်ထားရင် မီးခိုးရောင်ဖြစ်မည်
            )
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = white,
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.save), fontFamily = appFontFamily, fontWeight = FontWeight.SemiBold, color = white)
            }
        }

    }
}
