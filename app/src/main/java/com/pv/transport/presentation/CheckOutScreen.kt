package com.pv.transport.presentation

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
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
import androidx.compose.material.icons.filled.Save
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
    // Persistence Fix: Scope ViewModel to Activity so it survives "Back"
    val driverLogViewModel: DriverLogViewModel = if (activity != null) hiltViewModel(activity) else hiltViewModel()

    val driverLogState by driverLogViewModel.state.collectAsState()
    
    // ViewModel-based states
    val endKm by driverLogViewModel.checkOutEndKm.collectAsState()
    val endUri by driverLogViewModel.checkOutEndUri.collectAsState()
    val remark by driverLogViewModel.checkOutRemark.collectAsState()
    val purpose by driverLogViewModel.checkOutPurpose.collectAsState()

    var currentTime by remember { mutableStateOf("") }
    var isSaved by remember { mutableStateOf(false) }
    var isButtonClicked by remember { mutableStateOf(false) }
    val date = remember { mutableStateOf(LocalDate.now())}

    // Initialize remark/purpose from data if currently empty in ViewModel
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
        // Reset state on entry to prevent immediate popBackStack if previous state was Success
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
                Toast.makeText(context, "Saved offline. Will sync when connected.", Toast.LENGTH_SHORT).show()
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
                // UI Bug Fix: Remove extra white space
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
                Text(stringResource(R.string.date), fontFamily = appFontFamily, fontWeight = FontWeight.Normal)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)).background(white).padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = date.value.toString(), fontSize = 16.sp, color = Color.Black)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                if (data.type == "trip") {
                    Text(stringResource(R.string.trip_type))
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)).background(white).padding(horizontal = 12.dp, vertical = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Prefer the human-readable trip type from server/cache, fall back to reason or empty
                        val display = data.driverLog!!.tripType ?: data.driverLog.tripTypeId.takeIf { it.isNotEmpty() } ?: data.reason ?: ""
                        Text(text = display, fontSize = 16.sp, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Text(stringResource(R.string.reason))
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
                            Text(text = stringResource(R.string.from), fontFamily = appFontFamily, fontWeight = FontWeight.Normal)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)).background(white).padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
                                Text(text = data.from ?: "", fontSize = 16.sp, color = Color.Black)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(R.string.to), fontFamily = appFontFamily, fontWeight = FontWeight.Normal)
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
                        Text(text = stringResource(R.string.start_km), fontFamily = appFontFamily, fontWeight = FontWeight.Normal)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)).background(white).padding(horizontal = 12.dp, vertical = 10.dp), contentAlignment = Alignment.CenterStart) {
                            Text(text = data.startKm, fontSize = 16.sp, color = Color.Black)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.end_km), fontFamily = appFontFamily, fontWeight = FontWeight.Normal)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)).background(white), contentAlignment = Alignment.CenterStart) {
                            BasicTextField(
                                value = endKm,
                                onValueChange = { driverLogViewModel.checkOutEndKm.value = it },
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
                    Text(text = stringResource(R.string.remark), fontFamily = appFontFamily, fontWeight = FontWeight.Normal)
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
                    Text(text = stringResource(R.string.purpose), fontFamily = appFontFamily, fontWeight = FontWeight.Normal)
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
                        Text(text = stringResource(R.string.start_km_image), fontSize = 14.sp, fontFamily = appFontFamily, fontWeight = FontWeight.Normal, modifier = Modifier.padding(bottom = 8.dp))
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
                        Text(text = stringResource(R.string.end_km_image), fontSize = 14.sp, fontFamily = appFontFamily, fontWeight = FontWeight.Normal, modifier = Modifier.padding(bottom = 8.dp))
                        CustomImagePickerBox(imageUri = endUri, onImagePicked = { driverLogViewModel.checkOutEndUri.value = it })
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                if (endKm.text.isEmpty() || endUri == null) {
                    Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray), modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(8.dp), enabled = false) {
                        Text(stringResource(R.string.save), color = white, fontFamily = appFontFamily, fontWeight = FontWeight.Normal)
                    }
                } else {
                    Button(
                        onClick = {
                            if (isButtonClicked) return@Button
                            isButtonClicked = true
                            if (!isSaving) {
                                driverLogViewModel.checkOutDriverLog(
                                    recordId = data.id,
                                    remark = if (data.type == "daily") remark.text else purpose.text,
                                    endTime = currentTime,
                                    endKm = endKm.text,
                                    endPhoto = endUri!!,
                                    context = context
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorPrimary),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSaving && !isSaved && !isButtonClicked
                    ) {
                        if (isSaving) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.saving), fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = white)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.save), fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = white)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}
