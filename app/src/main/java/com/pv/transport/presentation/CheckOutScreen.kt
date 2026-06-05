package com.pv.transport.presentation

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
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
import com.pv.transport.extension.CustomDatePicker
import com.pv.transport.extension.CustomImagePickerBox
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.robotoFontFamily
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.DriverLogViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckOutScreen(
    data: Data,
    navController: NavController,
    driverLogViewModel: DriverLogViewModel = hiltViewModel()
) {

    println("CheckOutScreen received data: $data")

    var endKm by remember { mutableStateOf("") }
    var endUri by remember { mutableStateOf<Uri?>(null) }
    var currentTime by remember { mutableStateOf("") }
    var isSaved by remember { mutableStateOf(false) }
    val driverLogState = driverLogViewModel.state.collectAsState()
    val context = LocalContext.current
    val date = remember { mutableStateOf(LocalDate.now())}
    var remark by remember { mutableStateOf(TextFieldValue(data.remark?: "")) }
    var purpose by remember { mutableStateOf(TextFieldValue(data.purpose ?: "")) }
    var isButtonClicked by remember { mutableStateOf(false) }


    if (data.type == "daily") {
        LaunchedEffect(data.remark) {
            remark = TextFieldValue(
                text = data.remark ?: "",
                selection = TextRange((data.remark ?: "").length)
            )
        }
    } else {
        LaunchedEffect(data.purpose) {
            purpose = TextFieldValue(
                text = data.purpose ?: "",
                selection = TextRange((data.purpose ?: "").length)
            )
        }
    }



    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat(
                "HH:mm:ss",
                Locale.ENGLISH
            ).format(Date())

            delay(1000)
        }
    }
    val isSaving = when (driverLogState.value) {
        is DriverLogViewModel.DriverLogState.Loading -> true
        else -> false
    }

    LaunchedEffect(key1 = driverLogState.value) {
        when (val state = driverLogState.value) {
            is DriverLogViewModel.DriverLogState.Success -> {
                isButtonClicked = false
                endKm = ""
                endUri = null
                Toast.makeText(context, "Updated successful", Toast.LENGTH_SHORT).show()
                isSaved = true
                // small delay so user sees toast, then go back to logs screen
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


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(R.string.checkout_daily_log), color = Color.Black)
                    }
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = white
                ),
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

            // Card Content
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {

                // Date
                Text(
                    stringResource(R.string.date),
                    fontFamily = robotoFontFamily,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(white)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    BasicTextField(
                        value = date.value.toString(),
                        onValueChange = { },
                        readOnly = true,
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.trip_type))
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(white)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    BasicTextField(
                        value = data.driverLog.type,
                        onValueChange = { },
                        readOnly = true,
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.reason))
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(white)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    BasicTextField(
                        value = data.reason,
                        onValueChange = { },
                        readOnly = true,
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (data.type == "trip"){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = stringResource(R.string.from),
                                fontFamily = robotoFontFamily,
                                fontWeight = FontWeight.Normal,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(45.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(white)
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = data.from ?: "",
                                    onValueChange = { },
                                    readOnly = true,
                                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                                )
                            }
                        }

                        // Right Column
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = stringResource(R.string.to),
                                fontFamily = robotoFontFamily,
                                fontWeight = FontWeight.Normal,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(45.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(white)
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = data.to ?: "",
                                    onValueChange = { },
                                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                    }

                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = stringResource(R.string.start_km),
                            fontFamily = robotoFontFamily,
                            fontWeight = FontWeight.Normal,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(45.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(white)
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = data.startKm,
                                onValueChange = { },
                                readOnly = true,
                                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                            )
                        }
                    }

                    // Right Column
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = stringResource(R.string.end_km),
                            fontFamily = robotoFontFamily,
                            fontWeight = FontWeight.Normal,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(45.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(white)
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = endKm,
                                onValueChange = { endKm = it },
                                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            ) { innerTextField ->
                                if (endKm.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.enter_end_km),
                                        color = Color.Gray,
                                        fontSize = 16.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (data.type == "daily"){
                    // Remark
                    Text(
                        text = stringResource(R.string.remark),
                        fontFamily = robotoFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(white)
                            .padding(horizontal = 24.dp, vertical = 22.dp)
                    ) {
                        BasicTextField(
                            value = remark,
                            onValueChange = { remark = it },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                color = Color.Black
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) { innerTextField ->

                            if (remark.text.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.enter_remark),
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }

                            innerTextField()
                        }
                    }
                }else{
                    // Purpose
                    Text(
                        text = stringResource(R.string.purpose),
                        fontFamily = robotoFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(white)
                            .padding(horizontal = 24.dp, vertical = 22.dp)
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

                            if (purpose.text.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.enter_purpose),
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }

                            innerTextField()
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Start Km Image
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.start_km_image),
                            fontSize = 14.sp,
                            fontFamily = robotoFontFamily,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(white)
                                .clip(RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {

                            AsyncImage(
                                model = data.documents[0].documentUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop

                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // End Km Image
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.end_km_image),
                            fontSize = 14.sp,
                            fontFamily = robotoFontFamily,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        CustomImagePickerBox(
                            imageUri = endUri,
                            onImagePicked = { endUri = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                if (endKm.isEmpty() || endUri == null) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = false
                    ) {
                        Text("Checkout",
                            color = white,
                            fontFamily = robotoFontFamily,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            if (isButtonClicked) return@Button
                            isButtonClicked = true
                            println("Saving Driver Log with: $$currentTime, $endKm, ${endUri.toString()}")
                            if (!isSaving) {
                                driverLogViewModel.checkOutDriverLog(
                                    data.id,remark.text, currentTime, endKm,
                                    endUri!!
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSaving && !isSaved && !isButtonClicked
                    ) {
                        if (isSaving) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.saving),
                                    fontFamily = robotoFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = white
                                )
                            }
                        } else {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.check_out),
                                fontFamily = robotoFontFamily,
                                fontWeight = FontWeight.Normal,
                                color = white
                            )
                        }
                    }

                }
            }

        }
    }

}



//@RequiresApi(Build.VERSION_CODES.O)
//@Preview(showBackground = true)
//@Composable
//fun PreviewProfile() {
//    // You can preview the ProfileScreen composable in Android Studio.
//    // Replace 'NavController' and 'Context' with mock data for previewing purposes.
//   CheckOutScreen(navController = rememberNavController(), record = "12345", date = "2024-06-01", startTime = "08:00:00", startKm = "1000", startPhoto = "")
//}
