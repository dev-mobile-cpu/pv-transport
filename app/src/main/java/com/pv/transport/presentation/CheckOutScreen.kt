package com.pv.transport.presentation

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.pv.transport.extension.CustomImagePicker
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.DriverLogViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckOutScreen(
    navController: NavController,
    record: String,
    date: String,
    startTime: String,
    startKm: String,
    startPhoto: String,
    driverLogViewModel: DriverLogViewModel = hiltViewModel()
) {

    var endKm by remember { mutableStateOf("") }
    var endUri by remember { mutableStateOf<Uri?>(null) }
    var currentTime by remember { mutableStateOf("") }
    var isSaved by remember { mutableStateOf(false) }
    val driverLogState = driverLogViewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat(
                "HH:mm:ss",
                Locale.getDefault()
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
                endKm = ""
                endUri = null
                Toast.makeText(context, "Updated successful", Toast.LENGTH_SHORT).show()
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


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Add Daily Log", color = Color.Black)
                        Text(
                            text = "Record your trip details",
                            color = Color.Black,
                            fontSize = 12.sp
                        )
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
                )
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
                Text("Driver Id")
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(white)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {

                    BasicTextField(
                        value = record,
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

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("Start Km")
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier =  Modifier.width(150.dp).height(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(white)
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {

                            BasicTextField(
                                value = startKm,
                                onValueChange = { },
                                readOnly = true,
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    color = Color.Black
                                ),
                                modifier = Modifier
                            )
                        }
                    }

                    Column(modifier = Modifier,
                        horizontalAlignment = Alignment.End
                    ) {
                        // Start & End KM

                        Text("End Km", modifier = Modifier.align(Alignment.Start))
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier =  Modifier.width(150.dp).height(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(white)
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = endKm,
                                onValueChange = { endKm = it },

                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    color = Color.Black
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            ) { innerTextField ->

                                if (endKm.isEmpty()) {
                                    Text(
                                        text = "Enter End Km",
                                        color = Color.Gray
                                    )
                                }
                                innerTextField()

                            }
                        }

                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Start KM Image
                Text("Start Km Image")
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(white)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {

                        }
                ){
                    AsyncImage(
                        model = startPhoto,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop

                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                    // Start KM Image
                    Text("End Km Image")
                    Spacer(modifier = Modifier.height(4.dp))

                    CustomImagePicker(
                        imageUri = endUri,
                        onImagePicked = { endUri = it }

                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save Button
                    if (endKm.isEmpty() || endUri == null) {
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false
                        ) {
                            Text("Checkout", color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = {
                                // Prevent double-click while saving

                                println("Saving Driver Log with: $$currentTime, $endKm, ${endUri.toString()}")
                                if (!isSaving) {
                                    driverLogViewModel.checkOutDriverLog(
                                        record, currentTime, endKm,
                                        endUri!!
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
                                Icon(
                                    Icons.Default.Save,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Checkout", color = Color.White)
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
