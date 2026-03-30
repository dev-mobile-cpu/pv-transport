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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pv.transport.data.ReasonListResponse
import com.pv.transport.data.TripType
import com.pv.transport.extension.CustomDatePicker
import com.pv.transport.extension.CustomImagePicker
import com.pv.transport.extension.ReasonDropdown
import com.pv.transport.extension.StartKmTextField
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.DriverLogViewModel
import com.pv.transport.viewmodels.ReasonViewModel
import com.pv.transport.viewmodels.TripTypeViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import kotlin.toString

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TripCheckInScreen(
    navController: NavController,
    type: String,
    reasonViewModel: ReasonViewModel = hiltViewModel(),
    tripTypeViewModel: TripTypeViewModel = hiltViewModel(),
    driverLogViewModel: DriverLogViewModel = hiltViewModel()
) {

    val reasons = reasonViewModel.state.collectAsState()
    val tripType = tripTypeViewModel.state.collectAsState()
    val driverLogState = driverLogViewModel.state.collectAsState()
    var startKm by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var startUri by remember { mutableStateOf<Uri?>(null) }
    val date = remember { mutableStateOf(LocalDate.now())}
    val reasonList = remember { mutableStateListOf<ReasonListResponse>() }
    var selectedReason by remember { mutableStateOf("") }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var currentTime by remember { mutableStateOf("") }
    val context = LocalContext.current
    var isSaved by remember { mutableStateOf(false) }

    val tripTypeList = remember { mutableStateListOf<TripType>() }
    var selectedTrip by remember { mutableStateOf("") }
    var tripTypeIndex by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }

    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }


    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat(
                "HH:mm:ss",
                Locale.getDefault()
            ).format(Date())

            delay(1000)
        }
    }

    when (val s = reasons.value) {
        is ReasonViewModel.UiState.Idle -> {
            reasonViewModel.getReasons()
            Text(text = "Loading reasons...")
        }

        is ReasonViewModel.UiState.Loading -> {
            CircularProgressIndicator()
        }

        is ReasonViewModel.UiState.Success -> {
            reasonList.clear()
            reasonList.addAll(s.reasons.data)
            if (selectedReason.isEmpty() && reasonList.isNotEmpty()) {
                selectedReason = reasonList[0].value
                selectedIndex = reasonList[0].id.toInt()
            }
        }

        is ReasonViewModel.UiState.Error -> {
            Text(text = "Error: ${s.message}")
        }
    }

    when (val s = tripType.value) {
        is TripTypeViewModel.UiState.Idle -> {
            tripTypeViewModel.getTripType()
            Text(text = "Loading reasons...")
        }

        is TripTypeViewModel.UiState.Loading -> {
            CircularProgressIndicator()
        }

        is TripTypeViewModel.UiState.Success -> {
            tripTypeList.clear()
            tripTypeList.addAll(s.tripType.data)
            if (selectedTrip.isEmpty() && tripTypeList.isNotEmpty()) {
                selectedTrip = tripTypeList[0].value
                tripTypeIndex = tripTypeList[0].id.toInt()
            }
        }

        is TripTypeViewModel.UiState.Error -> {
            Text(text = "Error: ${s.message}")
        }
    }


    val isSaving = when (driverLogState.value) {
        is DriverLogViewModel.DriverLogState.Loading -> true
        else -> false
    }

    LaunchedEffect(key1 = driverLogState.value) {
        when (val state = driverLogState.value) {
            is DriverLogViewModel.DriverLogState.Success -> {
                startKm = ""
                purpose = ""
                startUri = null
                selectedReason = ""
                selectedIndex = 0
                Toast.makeText(context, "Save successful", Toast.LENGTH_SHORT).show()
                isSaved = true
                // small delay so user sees toast, then go back to logs screen
                delay(350)
                navController.popBackStack()
            }
            is DriverLogViewModel.DriverLogState.Error -> {
                Toast.makeText(context, "Save failed: ${state.message}", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // Date
        Text("Date")
        Spacer(modifier = Modifier.height(4.dp))
        CustomDatePicker(
            selectedDate = date.value,
            onDateSelected = { date.value = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Trip Type")
        Spacer(modifier = Modifier.height(4.dp))
        Box{
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(5.dp))
                    .background(white)
                    .clickable { expanded = true }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
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
                            selectedTrip = trip.value
                            tripTypeIndex = trip.id.toInt()
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier,
                horizontalAlignment = Alignment.Start
            ) {
                Text("From")
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier =  Modifier.width(150.dp).height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(white)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {

                    BasicTextField(
                        value = from,
                        onValueChange = { from = it},
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    ){ innerTextField ->

                        if (from.isEmpty()) {
                            Text(
                                text = "Enter destination",
                                color = Color.Gray
                            )
                        }
                        innerTextField()

                    }
                }
            }

            Column(modifier = Modifier,
                horizontalAlignment = Alignment.End
            ) {
                Text("To", modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier =  Modifier.width(150.dp).height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(white)
                        .padding(horizontal = 10.dp, vertical =8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {

                    BasicTextField(
                        value = to,
                        onValueChange = { to = it },

                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    ) { innerTextField ->

                        if (to.isEmpty()) {
                            Text(
                                text = "Enter destination",
                                color = Color.Gray
                            )
                        }
                        innerTextField()

                    }
                }

            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Reason")
        Spacer(modifier = Modifier.height(4.dp))
        ReasonDropdown(
            reasons = reasonList,
            selectedReason = selectedReason,
            onReasonSelected = { index, reason ->
                selectedIndex = index
                selectedReason = reason
            },
            modifier = Modifier
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Start Km")
        Spacer(modifier = Modifier.height(4.dp))
        StartKmTextField(
            value = startKm,
            hint = "Enter Start Km",
            onValueChange = { startKm = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Start KM Image
        Text("Start Km Image")
        Spacer(modifier = Modifier.height(4.dp))
        CustomImagePicker(
            imageUri = startUri,
            onImagePicked = { startUri = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Remark
        Text("Purpose of Trip")
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(white)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            BasicTextField(
                value = purpose,
                onValueChange = { purpose = it },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Black
                ),
                modifier = Modifier.fillMaxWidth()
            ) { innerTextField ->
                if (purpose.isEmpty()) {
                    Text(
                        text = "Describe the purpose of this trip...",
                        color = Color.Gray
                    )
                }
                innerTextField()
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        // Save Button
        if (startKm.isEmpty() || purpose.isEmpty() || startUri == null || from.isEmpty() || to.isEmpty()) {
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            ) {
                Text("Check In", color = Color.White)
            }
        } else {
            Button(
                onClick = {
                    println("Saving Driver Log with: ${date.value}, $selectedIndex, $purpose, $currentTime, $startKm, ${startUri.toString()}")
                    if (!isSaving) {
                        driverLogViewModel.checkInTripDriverLog(
                            date = date.value.toString(),
                            type = type,
                            tripTypeId = tripTypeIndex.toString(),
                            from = from,
                            to = to,
                            purpose = purpose,
                            reason = selectedIndex.toString(),
                            startTime = currentTime,
                            startKm = startKm,
                            startPhoto = startUri!!
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving && !isSaved
            ) {
                if (isSaving) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...", color = Color.White)
                    }
                } else {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Check In", color = Color.White)
                }
            }

        }
    }

}

