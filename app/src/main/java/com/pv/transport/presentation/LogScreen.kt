package com.pv.transport.presentation

import android.R.attr.resource
import android.annotation.SuppressLint
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pv.transport.BuildConfig
import com.pv.transport.R
import com.pv.transport.data.CheckVersionResponse
import com.pv.transport.data.log.Data
import com.pv.transport.data.log.Document
import com.pv.transport.extension.CustomDatePicker
import com.pv.transport.extension.UpdateVersionBottomSheet
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.purple
import com.pv.transport.ui.theme.red
import com.pv.transport.ui.theme.robotoFontFamily
import com.pv.transport.ui.theme.textColor
import com.pv.transport.ui.theme.textPrimary
import com.pv.transport.ui.theme.textSecondary
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.CheckVersionViewModel
import com.pv.transport.viewmodels.DriverLogViewModel
import java.time.LocalDate

@SuppressLint("ResourceType")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LogScreen(
    navController: NavController,
    logViewModel: DriverLogViewModel = hiltViewModel(),
    versionModel: CheckVersionViewModel = hiltViewModel()
){
    var showUpdateSheet by remember { mutableStateOf(true) }
    var startDate by rememberSaveable {
        mutableStateOf(LocalDate.now())
    }
    var endDate by rememberSaveable {
        mutableStateOf(LocalDate.now())
    }
    val logs by logViewModel.driverLogList.collectAsState()
    val versionState by versionModel.version.collectAsState()
    var versionInfo by remember { mutableStateOf<CheckVersionResponse?>(null) }
    val listState = rememberLazyListState()


    LaunchedEffect(Unit) {
        versionModel.getCheckVersion()
    }

    LaunchedEffect(versionState) {
        if (versionState is CheckVersionViewModel.CheckVersionState.Success) {
            val data = (versionState as CheckVersionViewModel.CheckVersionState.Success).message
            println("Latest version code from API: ${data.latestVersionCode}, Current app version code: ${BuildConfig.VERSION_CODE}")
            println("Latest version name from API: ${data.latestVersionName}, Current app version name: ${BuildConfig.VERSION_NAME}")
            if (data.latestVersionCode > BuildConfig.VERSION_CODE) {
                versionInfo = data
                showUpdateSheet = true
            }
        }
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            println("Last visible item index: ${lastVisibleItem}, Total items: ${listState.layoutInfo.totalItemsCount}")
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
                logViewModel.loadMoreLogs(startDate.toString(), endDate.toString()) }
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
                    Text(
                        stringResource(R.string.daily_logs),
                        color = textPrimary,
                        fontSize = 24.sp,
                        fontFamily = robotoFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        stringResource(R.string.track_your_daily_trips),
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontFamily = robotoFontFamily,
                        fontWeight = FontWeight.Normal)
                }
                Button(
                    onClick = {
                        navController.navigate("checkin")
                     },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.add_log))
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
                        Text(
                            stringResource(R.string.filters),
                            fontFamily = robotoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )

                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.start_date),
                                color = textPrimary,
                                fontFamily = robotoFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            CustomDatePicker(
                                selectedDate = startDate,
                                onDateSelected = { startDate = it },
                                bgColor = colorSecondary
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.end_date),
                                color = textPrimary,
                                fontFamily = robotoFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            CustomDatePicker(
                                selectedDate = endDate,
                                onDateSelected = { endDate = it },
                                bgColor = colorSecondary
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
                            Text(
                                stringResource(R.string.no_logs_found),
                                fontFamily = robotoFontFamily,
                                fontWeight = FontWeight.Normal,
                                color = textSecondary)
                        }
                    }
                } else {
                    items(logsList.size) {logItem ->
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


    versionInfo?.let { info ->

        if (showUpdateSheet) {
            UpdateVersionBottomSheet(
                update = info,
                onDismiss = {
                    showUpdateSheet = false
                }
            )
        }
    }
}


@Composable
fun DriverLogCard(
    item: Data,
    navController: NavController
) {


    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = white),
        onClick = {

            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("log", item)

            navController.navigate("log_detail")
        }
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

             //Top Section
              Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if(item.type == "trip"){
                    Column(modifier = Modifier) {
                        Text(
                            item.type.replaceFirstChar { it.uppercase()},
                            fontFamily = robotoFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "${item.from} - ${item.to}",
                            color = textPrimary,
                            fontFamily = robotoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }else{
                    Text(
                        item.type.replaceFirstChar { it.uppercase() },
                        fontFamily = robotoFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = textPrimary)
                }
                if (item.isCheckout == "true"){
                    Button(
                        onClick = {
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("checkout_log", item)
                            navController.navigate( "checkout")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = red
                        ),
                        modifier = Modifier
                    ) {
                        Text(
                            text = stringResource(R.string.check_out),
                            color = white,
                            fontFamily = robotoFontFamily,
                            fontWeight = FontWeight.Medium,
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
                            fontFamily = robotoFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }

                }
            }

            Spacer(modifier = Modifier.height(10.dp))

           Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
           ) {
               Text(
                   "${item.driverLog.date} ",
                   fontSize = 12.sp,
                   fontFamily = robotoFontFamily,
                   fontWeight = FontWeight.Normal,
                   color = textPrimary
               )
               Text(
                   "${item.startTime} ~ ${item.endTime ?: "N/A"}",
                    fontFamily = robotoFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = textPrimary
               )
           }
            Spacer(modifier = Modifier.height(14.dp))

            // KM Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = stringResource(R.string.start_km),
                        fontFamily = robotoFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = textSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${item.startKm} Km",
                        fontWeight = FontWeight.Bold,
                        fontFamily = robotoFontFamily,
                        fontSize = 18.sp,
                        color = textPrimary
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {

                    Text(
                        text = stringResource(R.string.end_km),
                        fontFamily = robotoFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = textSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.endKm?.let { "$it Km" } ?: "N/A",
                        fontFamily = robotoFontFamily,
                        fontWeight = if (item.endKm != null) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 18.sp,
                        color = if (item.endKm != null) textPrimary else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Image Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Start KM Image
                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = stringResource(R.string.start_km_image),
                        fontFamily = robotoFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = textSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val startImageUrl = item.documents.firstOrNull()?.documentUrl

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorSecondary),
                        contentAlignment = Alignment.Center
                    ) {

                        if (!startImageUrl.isNullOrEmpty()) {

                            AsyncImage(
                                model = startImageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                        } else {

                            Text(
                                text = stringResource(R.string.image_uploaded),
                                fontFamily = robotoFontFamily,
                                fontWeight = FontWeight.Normal,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // End KM Image
                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = stringResource(R.string.end_km_image),
                        fontFamily = robotoFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = textSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val endImageUrl = item.documents.getOrNull(1)?.documentUrl

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorSecondary),
                        contentAlignment = Alignment.Center
                    ) {

                        if (!endImageUrl.isNullOrEmpty()) {

                            AsyncImage(
                                model = endImageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                        } else {

                            Text(
                                text = stringResource(R.string.image_uploaded),
                                color = Color.Gray,
                                fontFamily = robotoFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

//@Composable
//fun DriverLogCard(item: Data,navController: NavController){
//
//    Card(
//        shape = RoundedCornerShape(16.dp),
//        onClick = {
//            navController.currentBackStackEntry
//                ?.savedStateHandle
//                ?.set("log", item)
//
//            navController.navigate("log_detail")
//
//        },
//        colors = CardDefaults.cardColors(white),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//
//            Row(
//                horizontalArrangement = Arrangement.SpaceBetween,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                if(item.driverLog.type == "trip"){
//                    Column(modifier = Modifier) {
//                        Text("${item.driverLog.from} - ${item.driverLog.to}", color = Color.Black, fontWeight = FontWeight.Bold)
//                        Spacer(modifier = Modifier.height(2.dp))
//                        Text(item.driverLog.tripType)
//                    }
//                }else{
//                    Text(item.driverLog.type.replaceFirstChar { it.uppercase() }, color = Color.Black)
//                }
//
//
//                if (item.isCheckout == "true"){
//                    Button(
//                        onClick = {
//                            navController.currentBackStackEntry
//                                ?.savedStateHandle
//                                ?.set("checkout_log", item)
//                            navController.navigate( "checkout")
//                        },
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = red
//                        ),
//                        modifier = Modifier
//                    ) {
//                        Text(
//                            text = stringResource(R.string.check_out),
//                            color = white,
//                            fontSize = 12.sp,
//                            modifier = Modifier
//                        )
//                    }
//                }else{
//                    Box(
//                        modifier = Modifier
//                            .clip(RoundedCornerShape(50))
//                            .background(purple)
//                            .padding(horizontal = 10.dp, vertical = 4.dp)
//                    ) {
//
//                        Text(
//                            text = item.status,
//                            color = textColor,
//                            fontSize = 12.sp
//                        )
//                    }
//
//                }
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//           Row(
//                horizontalArrangement = Arrangement.SpaceBetween,
//                modifier = Modifier.fillMaxWidth()
//           ) {
//               Text("${item.driverLog.date} ", color = Color.Black)
//               Text("${item.startTime} ${item.endTime ?: "-"}", color = Color.Black)
//           }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            Row(
//                horizontalArrangement = Arrangement.SpaceBetween,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Column(horizontalAlignment = Alignment.Start) {
//                    Text(stringResource(R.string.start_km), color = textSecondary)
//                    Text(item.startKm, fontWeight = FontWeight.Bold, color = textPrimary)
//                }
//
//                Column(horizontalAlignment = Alignment.End) {
//                    Text(stringResource(R.string.end_km), color = textSecondary)
//                    Text(item.endKm ?: "-", fontWeight = FontWeight.Bold, color = textPrimary)
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            Row(
//                horizontalArrangement = Arrangement.SpaceBetween,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                ImageUploadBox(stringResource(R.string.start_km_image),item.documents)
//                ImageUploadBox(stringResource(R.string.end_km_image),item.documents)
//            }
//
////            Spacer(modifier = Modifier.height(12.dp))
////
////            Text(
////                text = "Reviewed by Manager on 2/10/2026",
////                fontSize = 12.sp,
////                color = Color.Gray
////            )
//        }
//    }
//}

@Composable
fun ImageUploadBox(
    title: String,
    document: List<Document>
) {


    val photo = document.firstOrNull {
        (it.kindOfDoc == "start-photo" && title == "Start Km Image") ||
                (it.kindOfDoc == "end-photo" && title == "End Km Image")
    }

    Column {
        Text(title, fontSize = 12.sp, fontFamily = robotoFontFamily, fontWeight = FontWeight.Normal, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(100.dp, 90.dp)
                .background(colorSecondary, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (photo != null) {
                println("Documents in ImageUploadBox: ${photo.documentUrl}")
                AsyncImage(
                    model = photo.documentUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp, 90.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

            } else {
                Text(
                    text = stringResource(R.string.image_uploaded),
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