package com.pv.transport.presentation

import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pv.transport.R
import com.pv.transport.data.log.Data
import com.pv.transport.extension.CustomImagePickerBox
import com.pv.transport.ui.theme.FormFieldLabel
import com.pv.transport.ui.theme.FormPrimaryButton
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.textPrimary
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.DriverLogViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckOutScreen(
    data: Data,
    navController: NavController
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findComponentActivity() }
    val driverLogViewModel: DriverLogViewModel = if (activity != null) hiltViewModel(activity) else hiltViewModel()

    val driverLogState by driverLogViewModel.state.collectAsState()

    val endKm by driverLogViewModel.checkOutEndKm.collectAsState()
    val endUri by driverLogViewModel.checkOutEndUri.collectAsState()
    val remark by driverLogViewModel.checkOutRemark.collectAsState()
    val purpose by driverLogViewModel.checkOutPurpose.collectAsState()

    var currentTime by remember { mutableStateOf("") }
    var isSaved by remember { mutableStateOf(false) }
    var isButtonClicked by remember { mutableStateOf(false) }
    val date = remember { mutableStateOf(LocalDate.now())}

    LaunchedEffect(data.id) {
        if (driverLogViewModel.currentCheckOutId != data.id) {
            driverLogViewModel.clearCheckOut()
            driverLogViewModel.setCurrentCheckOutId(data.id)
        }
    }

    LaunchedEffect(data) {
        if (data.type == "daily" && remark.text.isEmpty() && !data.remark.isNullOrEmpty()) {
            driverLogViewModel.checkOutRemark.value = TextFieldValue(
                text = data.remark,
                selection = TextRange(data.remark.length)
            )
        } else if (data.type != "daily" && purpose.text.isEmpty() && !data.purpose.isNullOrEmpty()) {
            driverLogViewModel.checkOutPurpose.value = TextFieldValue(
                text = data.purpose,
                selection = TextRange(data.purpose.length)
            )
        }
    }

    LaunchedEffect(Unit) {
        isSaved = false
        isButtonClicked = false
        val currentS = driverLogViewModel.state.value
        if (currentS is DriverLogViewModel.DriverLogState.Success || 
            currentS is DriverLogViewModel.DriverLogState.SavedOffline) {
            driverLogViewModel.resetState()
        }

        while (true) {
            currentTime = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(Date())
            delay(1000)
        }
    }

    val isSaving = driverLogState is DriverLogViewModel.DriverLogState.Loading

    LaunchedEffect(driverLogState) {
        when (driverLogState) {
            is DriverLogViewModel.DriverLogState.Success -> {
                isButtonClicked = false
                Toast.makeText(context, "Update successful", Toast.LENGTH_SHORT).show()
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
                val error = (driverLogState as DriverLogViewModel.DriverLogState.Error).message
                Toast.makeText(context, "Save failed: $error", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(R.string.checkout_daily_log),
                        color = textPrimary,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            modifier = Modifier.size(20.dp),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    Text(
                        text = "Clear",
                        color = Color(0xFF007AFF),
                        fontSize = 13.sp,
                        fontFamily = appFontFamily,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { driverLogViewModel.clearCheckOut() }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = white),
                windowInsets = WindowInsets(0)
            )
        },
        containerColor = Color(0xFFF4F4F4)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Date
                FormFieldLabel(text = stringResource(R.string.date), icon = Icons.Default.DateRange)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)).background(white).padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = date.value.toString(), fontSize = 16.sp, color = Color.Black)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                if (data.type == "trip") {
                    FormFieldLabel(text = stringResource(R.string.trip_type), icon = Icons.Default.DirectionsCar)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)).background(white).padding(horizontal = 12.dp, vertical = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Prefer the human-readable trip type from server/cache, fall back to reason or empty
                        val display = data.driverLog?.tripType ?: data.driverLog?.tripTypeId?.takeIf { it.isNotEmpty() } ?: data.reason
                        Text(text = display, fontSize = 16.sp, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    FormFieldLabel(text = stringResource(R.string.reason), icon = Icons.Default.Category)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)).background(white).padding(horizontal = 12.dp, vertical = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(text = data.reason, fontSize = 16.sp, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                if (data.type == "trip") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormFieldLabel(text = stringResource(R.string.from), icon = Icons.Default.Place)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)).background(white).padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
                                Text(text = data.from ?: "", fontSize = 16.sp, color = Color.Black)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormFieldLabel(text = stringResource(R.string.to), icon = Icons.Default.Flag)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)).background(white).padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
                                Text(text = data.to ?: "", fontSize = 16.sp, color = Color.Black)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel(text = stringResource(R.string.start_km), icon = Icons.Default.Speed)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)).background(white).padding(horizontal = 12.dp, vertical = 10.dp), contentAlignment = Alignment.CenterStart) {
                            Text(text = data.startKm, fontSize = 16.sp, color = Color.Black)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel(text = stringResource(R.string.end_km), icon = Icons.Default.Speed)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)).background(white), contentAlignment = Alignment.CenterStart) {
                            BasicTextField(
                                value = endKm,
                                onValueChange = {
                                    println("INPUT END KM = ${it.text}")
                                    println("INPUT END KM = $endKm")
                                    driverLogViewModel.checkOutEndKm.value = it
                                   },
                                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                decorationBox = { inner ->
                                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                                        if (endKm.text.isEmpty()) Text(stringResource(R.string.enter_end_km), color = Color.Gray, fontSize = 16.sp)
                                        inner()
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                if (data.type == "daily") {
                    FormFieldLabel(text = stringResource(R.string.remark), icon = Icons.Default.Notes)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)).background(white), contentAlignment = Alignment.CenterStart) {
                        BasicTextField(
                            value = remark,
                            onValueChange = { driverLogViewModel.checkOutRemark.value = it },
                            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                                    if (remark.text.isEmpty()) Text(stringResource(R.string.enter_remark), color = Color.Gray, fontSize = 16.sp)
                                    inner()
                                }
                            }
                        )
                    }
                } else {
                    FormFieldLabel(text = stringResource(R.string.purpose), icon = Icons.Default.Edit)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)).background(white), contentAlignment = Alignment.CenterStart) {
                        BasicTextField(
                            value = purpose,
                            onValueChange = { driverLogViewModel.checkOutPurpose.value = it },
                            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                                    if (purpose.text.isEmpty()) Text(stringResource(R.string.enter_purpose), color = Color.Gray, fontSize = 16.sp)
                                    inner()
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel(
                            text = stringResource(R.string.start_km_image),
                            icon = Icons.Default.PhotoCamera,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(white).clip(RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            // Display start image - use file path for offline, URL for online
                            val displayStartImage = if (data.status == "OFFLINE" || data.status == "SYNCING") {
                                data.startImagePath ?: data.documents.getOrNull(0)?.documentUrl
                            } else {
                                data.documents.getOrNull(0)?.documentUrl
                            }
                            AsyncImage(model = displayStartImage, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel(
                            text = stringResource(R.string.end_km_image),
                            icon = Icons.Default.PhotoCamera,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        CustomImagePickerBox(imageUri = endUri, onImagePicked = { driverLogViewModel.checkOutEndUri.value = it })
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                val canSave = endKm.text.trim().isNotEmpty() && endUri != null

                FormPrimaryButton(
                    text = stringResource(R.string.save),
                    onClick = {
                        if (isSaving) return@FormPrimaryButton

                        driverLogViewModel.checkOutDriverLog(
                            recordId = data.id,
                            remark = if (data.type == "daily") remark.text else purpose.text,
                            endTime = currentTime,
                            endKm = endKm.text,
                            endPhoto = endUri!!,
                            context = context
                        )
                    },
                    enabled = canSave && !isSaving,
                    isLoading = isSaving
                )
            }
        }
    }
}

private fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}
