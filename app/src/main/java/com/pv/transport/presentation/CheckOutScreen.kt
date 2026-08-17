package com.pv.transport.presentation

import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.platform.LocalDensity
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
import com.pv.transport.R
import com.pv.transport.data.log.Data
import com.pv.transport.data.log.stableKey
import com.pv.transport.extension.CachedAppImage
import com.pv.transport.extension.CustomImagePickerBox
import com.pv.transport.extension.startKmPhotoModel
import com.pv.transport.ui.theme.FormFieldLabel
import com.pv.transport.ui.theme.FormPrimaryButton
import com.pv.transport.ui.theme.FormPrimaryButtonDefaults
import com.pv.transport.ui.theme.AppToast
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.formScrollInsets
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
    val driverLogListState by driverLogViewModel.driverLogList.collectAsState()

    val endKm by driverLogViewModel.checkOutEndKm.collectAsState()
    val endUri by driverLogViewModel.checkOutEndUri.collectAsState()
    val remark by driverLogViewModel.checkOutRemark.collectAsState()
    val site by driverLogViewModel.checkOutSite.collectAsState()
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
        if (data.type == "daily") {
            if (remark.text.isEmpty() && !data.remark.isNullOrEmpty()) {
                driverLogViewModel.checkOutRemark.value = TextFieldValue(
                    text = data.remark,
                    selection = TextRange(data.remark.length)
                )
            }
            if (site.text.isEmpty() && !data.site.isNullOrEmpty()) {
                driverLogViewModel.checkOutSite.value = TextFieldValue(
                    text = data.site,
                    selection = TextRange(data.site.length)
                )
            }
            if (purpose.text.isEmpty() && !data.purpose.isNullOrEmpty()) {
                driverLogViewModel.checkOutPurpose.value = TextFieldValue(
                    text = data.purpose,
                    selection = TextRange(data.purpose.length)
                )
            }
        } else if (purpose.text.isEmpty() && !data.purpose.isNullOrEmpty()) {
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

    // The save state is shared with the other log forms, so only react to it while this
    // screen is the one waiting for its own save to finish.
    val isSaving = isButtonClicked && driverLogState is DriverLogViewModel.DriverLogState.Loading

    LaunchedEffect(driverLogState) {
        if (!isButtonClicked) return@LaunchedEffect
        when (driverLogState) {
            is DriverLogViewModel.DriverLogState.Success -> {
                isButtonClicked = false
                AppToast.show(context, context.getString(R.string.checkout_complete))
                isSaved = true
                delay(350)
                navController.popBackStack()
            }
            is DriverLogViewModel.DriverLogState.SavedOffline -> {
                isButtonClicked = false
                AppToast.show(context, context.getString(R.string.checkout_complete))
                isSaved = true
                delay(350)
                navController.popBackStack()
            }
            is DriverLogViewModel.DriverLogState.Error -> {
                isButtonClicked = false
                val error = (driverLogState as DriverLogViewModel.DriverLogState.Error).message
                AppToast.show(context, context.getString(R.string.save_failed, error))
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
                        text = stringResource(R.string.clear),
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
                .formScrollInsets(innerPadding)
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
                                onValueChange = { new ->
                                    val digits = new.text.filter { it.isDigit() }.take(7)
                                    driverLogViewModel.checkOutEndKm.value =
                                        new.copy(text = digits, selection = TextRange(digits.length))
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
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel(
                            text = stringResource(R.string.start_km_image),
                            icon = Icons.Default.PhotoCamera,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(white).clip(RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            val liveRecord = (driverLogListState as? DriverLogViewModel.DriverLogListState.Success)
                                ?.logs
                                ?.firstOrNull { it.stableKey == data.stableKey || it.id == data.id }
                            val displayStartImage = startKmPhotoModel(liveRecord ?: data)
                                ?: startKmPhotoModel(data)
                            if (displayStartImage != null) {
                                val density = LocalDensity.current
                                val heightPx = remember(density) { with(density) { 150.dp.roundToPx().coerceAtLeast(1) } }
                                CachedAppImage(
                                    model = displayStartImage,
                                    cacheKey = "${(liveRecord ?: data).stableKey}-start",
                                    heightPx = heightPx,
                                    widthPx = heightPx,
                                    thumbDecode = true,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
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

                Spacer(modifier = Modifier.height(16.dp))
                if (data.type == "daily") {
                    FormFieldLabel(
                        text = stringResource(R.string.site) + " (${stringResource(R.string.optional)})",
                        icon = Icons.Default.LocationOn
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)).background(white), contentAlignment = Alignment.CenterStart) {
                        BasicTextField(
                            value = site,
                            onValueChange = { driverLogViewModel.checkOutSite.value = it },
                            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                                    if (site.text.isEmpty()) Text(stringResource(R.string.enter_site), color = Color.Gray, fontSize = 16.sp)
                                    inner()
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FormFieldLabel(
                        text = stringResource(R.string.purpose) + " (${stringResource(R.string.optional)})",
                        icon = Icons.Default.Flag
                    )
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
                    Spacer(modifier = Modifier.height(16.dp))
                    FormFieldLabel(
                        text = stringResource(R.string.remark) + " (${stringResource(R.string.optional)})",
                        icon = Icons.Default.Notes
                    )
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
                    FormFieldLabel(
                        text = stringResource(R.string.purpose_trip) + " (${stringResource(R.string.optional)})",
                        icon = Icons.Default.Edit
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(65.dp).clip(RoundedCornerShape(8.dp)).background(white), contentAlignment = Alignment.CenterStart) {
                        BasicTextField(
                            value = purpose,
                            onValueChange = { driverLogViewModel.checkOutPurpose.value = it },
                            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                                    if (purpose.text.isEmpty()) Text(stringResource(R.string.describe_purpose), color = Color.Gray, fontSize = 16.sp)
                                    inner()
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                val canSave = endKm.text.trim().isNotEmpty() && endUri != null

                val startKmValue = data.startKm.filter { it.isDigit() }.toLongOrNull()
                val endKmValue = endKm.text.trim().toLongOrNull()
                val kmWarning = startKmValue != null && endKmValue != null && endKmValue < startKmValue
                if (kmWarning) {
                    Text(
                        text = stringResource(R.string.end_km_less_than_start),
                        color = Color(0xFFE65100),
                        fontSize = 12.sp,
                        fontFamily = appFontFamily,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                FormPrimaryButton(
                    text = stringResource(R.string.save),
                    onClick = {
                        if (isSaving || isButtonClicked) return@FormPrimaryButton
                        val photoUri = endUri ?: return@FormPrimaryButton
                        isButtonClicked = true
                        driverLogViewModel.checkOutDriverLog(
                            recordId = data.id,
                            remark = if (data.type == "daily") remark.text else purpose.text,
                            endTime = currentTime,
                            endKm = endKm.text,
                            endPhoto = photoUri,
                            context = context,
                            localCheckInUuid = data.clientUuid,
                            site = if (data.type == "daily") site.text else "",
                            purpose = purpose.text
                        )
                    },
                    enabled = canSave && !isSaving && !isSaved && !isButtonClicked,
                    isLoading = isSaving
                )
                Spacer(modifier = Modifier.height(FormPrimaryButtonDefaults.SaveBottomSpace))
            }
        }
    }
}

private fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}
