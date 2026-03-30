package com.pv.transport.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pv.transport.data.Data
import com.pv.transport.data.Document
import com.pv.transport.extension.CustomDatePicker
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.purple
import com.pv.transport.ui.theme.red
import com.pv.transport.ui.theme.textColor
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.DriverLogViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LogScreen(navController: NavController,logViewModel: DriverLogViewModel = hiltViewModel()){
    var startDate by rememberSaveable {
        mutableStateOf(LocalDate.now())
    }
    var endDate by rememberSaveable {
        mutableStateOf(LocalDate.now())
    }
    val logs by logViewModel.driverLogList.collectAsState()
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            println("Last visible item index: ${lastVisibleItem?.index}, Total items: ${listState.layoutInfo.totalItemsCount}")
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 5
        }
    }

    LaunchedEffect(startDate, endDate) {
        logViewModel.getDriverLogs(
            startDate.toString(),
            endDate.toString()
        )
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && logs is DriverLogViewModel.DriverLogListState.Success) {
            val successState = logs as DriverLogViewModel.DriverLogListState.Success
            if (!successState.isLoadingMore && successState.currentPage < successState.lastPage) {
                logViewModel.loadMoreLogs(startDate.toString(), endDate.toString())
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(colorSecondary),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("Daily Logs", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Track your daily trips", color = Color.Gray, fontSize = 14.sp)
                }

                Button(
                    onClick = { navController.navigate("checkin") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Log")
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(white),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FilterAlt, contentDescription = null)
                        Text("Filters", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Start Date", fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            CustomDatePicker(
                                selectedDate = startDate,
                                onDateSelected = { startDate = it }
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("End Date", fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            CustomDatePicker(
                                selectedDate = endDate,
                                onDateSelected = { endDate = it }
                            )
                        }
                    }
                }
            }
        }

        when (logs) {
            is DriverLogViewModel.DriverLogListState.Loading -> {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            is DriverLogViewModel.DriverLogListState.Success -> {
                val successState = logs as DriverLogViewModel.DriverLogListState.Success
                val logsList = successState.logs

                if (logsList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No logs found")
                        }
                    }
                } else {
                    items(logsList.size) { logItem ->
                        DriverLogCard(logsList[logItem], navController)
                    }

                    if (successState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }

            is DriverLogViewModel.DriverLogListState.Error -> {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error: ${(logs as DriverLogViewModel.DriverLogListState.Error).message}")
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun DriverLogCard(item: Data,navController: NavController){
    Card(
        shape = RoundedCornerShape(16.dp),
        onClick = {
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("log", item)

            navController.navigate("log_detail")

        },
        colors = CardDefaults.cardColors(white),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if(item.driverLog.type == "trip"){
                   Column(modifier = Modifier) {
                       Text("${item.driverLog.from} - ${item.driverLog.to}", color = Color.Black, fontWeight = FontWeight.Bold)
                       Spacer(modifier = Modifier.height(2.dp))
                       Text(item.driverLog.tripType)
                   }
                }else{
                    Text(item.driverLog.type.replaceFirstChar { it.uppercase() }, color = Color.Black)
                }


                if (item.isCheckout == "true"){
                    Button(
                        onClick = {
                            val recordId = item.id
                            val date = URLEncoder.encode(item.createdAt, StandardCharsets.UTF_8.toString())
                            val startTime = URLEncoder.encode(item.startTime, StandardCharsets.UTF_8.toString())
                            val startKm = item.startKm
                            val startPhoto = URLEncoder.encode(item.documents[0].documentUrl, StandardCharsets.UTF_8.toString())
                            val fileName = URLEncoder.encode(item.documents[0].fileName, StandardCharsets.UTF_8.toString())
                            navController.navigate( "checkout?recordId=$recordId&date=$date&startTime=$startTime&startKm=$startKm&startPhoto=$startPhoto&fileName=$fileName")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = red
                        ),
                        modifier = Modifier
                    ) {
                        Text(
                            text = "Check Out",
                            color = white,
                            fontSize = 12.sp,
                            modifier = Modifier
                        )
                    }
                }else{
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(purple)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {

                        Text(
                            text = item.status,
                            color = textColor,
                            fontSize = 12.sp
                        )
                    }

                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("${item.driverLog.date} ${item.startTime} ${item.endTime ?: "-"}", color = Color.Black)

            Spacer(modifier = Modifier.height(12.dp))

             Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("Start Km", color = Color.Gray)
                    Text(item.startKm, fontWeight = FontWeight.Bold)
                }

                Column {
                    Text("End Km", color = Color.Gray)
                    Text(item.endKm ?: "-", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                ImageUploadBox("Start Km Image",item.documents)
                ImageUploadBox("End Km Image",item.documents)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Reviewed by Manager on 2/10/2026",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ImageUploadBox(title: String,document: List<Document>) {

    val photo = document.firstOrNull {
        (it.kindOfDoc == "start-photo" && title == "Start Km Image") ||
                (it.kindOfDoc == "end-photo" && title == "End Km Image")
    }

    Column {
        Text(title, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(130.dp, 80.dp)
                .background(colorSecondary, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (photo != null) {
                AsyncImage(
                    model = photo.documentUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(130.dp, 80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = "Image Uploaded",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun PreviewProfile() {
//    // You can preview the ProfileScreen composable in Android Studio.
//    // Replace 'NavController' and 'Context' with mock data for previewing purposes.
//    LogScreen(navController = rememberNavController())
//}
