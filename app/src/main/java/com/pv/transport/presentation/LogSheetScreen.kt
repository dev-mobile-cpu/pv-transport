package com.pv.transport.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pv.transport.BuildConfig
import com.pv.transport.R
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.data.CheckVersionResponse
import com.pv.transport.data.log.Data
import com.pv.transport.data.log.stableKey
import com.pv.transport.extension.CustomDatePicker
import com.pv.transport.extension.HandleBackPressWithDialog
import com.pv.transport.extension.LogKmPhotoSlot
import com.pv.transport.extension.preferredListPhoto
import com.pv.transport.extension.UpdateVersionBottomSheet
import com.pv.transport.extension.safeNavigate
import com.pv.transport.network.ConnectivityObserver
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.DotsLoading
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.backgroundColorApproved
import com.pv.transport.ui.theme.backgroundColorPending
import com.pv.transport.ui.theme.black
import com.pv.transport.ui.theme.checkColor
import com.pv.transport.ui.theme.checkColorApproved
import com.pv.transport.ui.theme.checkColorPending
import com.pv.transport.ui.theme.textColorPrimary
import com.pv.transport.ui.theme.textColorSecondary
import com.pv.transport.ui.theme.textPrimary
import com.pv.transport.ui.theme.textSecondary
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.CheckVersionViewModel
import com.pv.transport.viewmodels.DriverLogViewModel
import java.time.LocalDate
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.Scaffold
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@SuppressLint("ResourceType", "UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LogSheetScreen(
    navController: NavController,
    logViewModel: DriverLogViewModel = hiltViewModel(),
    versionModel: CheckVersionViewModel = hiltViewModel()
){
    val authPrefs = AuthPrefs(LocalContext.current)
    val skippedVersionCode = authPrefs.getSkippedVersionCode()
    var showUpdateSheet by remember { mutableStateOf(true) }
    var startDate by rememberSaveable {
        mutableStateOf(LocalDate.now())
    }
    var endDate by rememberSaveable {
        mutableStateOf(LocalDate.now())
    }
    val logs by logViewModel.driverLogList.collectAsState()
    val networkStatus by logViewModel.networkStatus.collectAsStateWithLifecycle()
    val versionState by versionModel.version.collectAsState()
    var versionInfo by remember { mutableStateOf<CheckVersionResponse?>(null) }
    val listState = rememberLazyListState()

    val isOffline = networkStatus != ConnectivityObserver.Status.Available


    LaunchedEffect(Unit) {
        versionModel.getCheckVersion()
    }

    LaunchedEffect(versionState) {
        if (versionState is CheckVersionViewModel.CheckVersionState.Success) {
            val data = (versionState as CheckVersionViewModel.CheckVersionState.Success).message
            if (data.latestVersionCode > BuildConfig.VERSION_CODE) {
                versionInfo = data
                showUpdateSheet = if (data.forceUpdate) true else data.latestVersionCode != skippedVersionCode
            }
        }
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
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
            if (!successState.isLoadingMore && successState.currentPage < successState.lastPage && !successState.isOffline) {
                logViewModel.loadMoreLogs(startDate.toString(), endDate.toString()) 
            }
        }
    }

    val showExitDialog = remember { mutableStateOf(false) }
    val activity = LocalContext.current as Activity

    HandleBackPressWithDialog(
        onBackConfirmed = {
            activity.finish()
        },
        showDialog = showExitDialog
    )

    Scaffold { _ ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colorSecondary),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

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
                                fontFamily = appFontFamily ,
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
                                    fontFamily = appFontFamily ,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                CustomDatePicker(
                                    selectedDate = startDate,
                                    onDateSelected = { startDate = it },
                                    bgColor = colorSecondary,
                                    readOnly = isOffline
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.end_date),
                                    color = textPrimary,
                                    fontFamily = appFontFamily ,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                CustomDatePicker(
                                    selectedDate = endDate,
                                    onDateSelected = { endDate = it },
                                    bgColor = colorSecondary,
                                    readOnly = isOffline
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
                            DotsLoading()
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
                                    fontFamily = appFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = textSecondary)
                            }
                        }
                    } else {
                        items(
                            logsList,
                            key = { it.stableKey },
                            contentType = { "driver_log" }
                        ) { logItem ->
                            DriverLogCard(logItem, navController)
                        }

                        if (successState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    DotsLoading()
                                }
                            }
                        }
                    }
                }

                is DriverLogViewModel.DriverLogListState.Error -> {
                    item {
                        val errorMessage = (logs as DriverLogViewModel.DriverLogListState.Error).message
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.WifiOff, contentDescription = "No Internet", tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(errorMessage, fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = textSecondary)
                        }
                    }
                }
                else -> {}
            }
        }
    }



    versionInfo?.let { info ->
        if (showUpdateSheet) {
            UpdateVersionBottomSheet(
                update = info,
                onDismiss = {
                    if (!info.forceUpdate) showUpdateSheet = false
                }
            )
        }
    }
}


@Composable
fun DriverLogSheetCard(
    item: Data,
    navController: NavController
) {
    val authPrefs = AuthPrefs(LocalContext.current)
    val driverType = authPrefs.getDriverType()
    val cachePrefix = item.stableKey
    val startImageUrl = remember(item.documents) {
        item.documents.firstOrNull { it.kindOfDoc == "start-photo" }?.documentUrl
            ?: item.documents.firstOrNull()?.documentUrl
    }
    val endImageUrl = remember(item.documents) {
        item.documents.firstOrNull { it.kindOfDoc == "end-photo" }?.documentUrl
            ?: item.documents.getOrNull(1)?.documentUrl
    }
    val displayStartImage = remember(cachePrefix, item.startImagePath, startImageUrl) {
        preferredListPhoto(item.startImagePath, startImageUrl)
    }
    val displayEndImage = remember(cachePrefix, item.endImagePath, endImageUrl) {
        preferredListPhoto(item.endImagePath, endImageUrl)
    }
    val isOffline = item.status == "OFFLINE" || item.status == "SYNCING"
    var checkoutOpened by remember(item.id) { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = white),
        onClick = {
            if (!isOffline) {
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("log", item)
                navController.safeNavigate("log_detail")
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 16.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.align(Alignment.CenterStart)) {
                    Text(
                        text = item.type.replaceFirstChar { it.uppercase() },
                        fontSize = 16.sp,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = textColorPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Show reason for Daily logs, show trip-type string for Trip logs (fall back to reason)
                    val subtitle = if (item.type == "trip") item.driverLog!!.tripType ?: item.reason else item.reason
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        fontFamily = appFontFamily,
                        color = textColorSecondary
                    )
                }

                // Status Badge Logic
                val badgeStatus = item.status.uppercase()
                val isCorporate = driverType == "corporate"
                
                if (badgeStatus == "OFFLINE" || badgeStatus == "SYNCING" || isCorporate) {
                    val backgroundColor = when (badgeStatus) {
                        "OFFLINE" -> Color(0xFFF5F5F5)
                        "SYNCING" -> Color(0xFFE3F2FD)
                        "PENDING" -> backgroundColorPending
                        else -> backgroundColorApproved
                    }
                    val contentColor = when (badgeStatus) {
                        "OFFLINE" -> Color(0xFF757575)
                        "SYNCING" -> Color(0xFF1976D2)
                        "PENDING" -> checkColorPending
                        else -> checkColorApproved
                    }

                    Box(
                        modifier = Modifier.align(Alignment.TopEnd)
                            .wrapContentWidth()
                            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (badgeStatus == "SYNCING") {
                                val infiniteTransition = rememberInfiniteTransition(label = "")
                                val rotation by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 360f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000, easing = LinearEasing)
                                    ), label = ""
                                )
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = contentColor,
                                    modifier = Modifier.size(12.dp).rotate(rotation)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = badgeStatus,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = contentColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = black, modifier = Modifier.size(16.dp).padding(bottom = 3.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = item.driverLog!!.date, color = textColorPrimary, fontFamily = appFontFamily, fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AccessTime, tint = black, contentDescription = null, modifier = Modifier.size(16.dp).padding(bottom = 3.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if(item.endTime == null) item.startTime else "${item.startTime} - ${item.endTime}", color = textColorPrimary, fontFamily = appFontFamily, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = stringResource(R.string.start_km), fontFamily = appFontFamily, color = textColorSecondary, fontSize = 12.sp)
                    Text(text = item.startKm, fontWeight = FontWeight.Bold, color = textColorPrimary, fontSize = 18.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = stringResource(R.string.end_km), fontFamily = appFontFamily, color = textColorSecondary, fontSize = 12.sp)
                    Text(text = item.endKm ?: "-", fontFamily = appFontFamily, color = textColorPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LogKmPhotoSlot(
                    model = displayStartImage,
                    cacheKey = "$cachePrefix-start",
                    modifier = Modifier.weight(1f).height(104.dp)
                )
                LogKmPhotoSlot(
                    model = displayEndImage,
                    cacheKey = "$cachePrefix-end",
                    modifier = Modifier.weight(1f).height(104.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (item.isCheckout == "true"){
                Button(
                    onClick = {
                        if (checkoutOpened) return@Button
                        checkoutOpened = true
                        navController.currentBackStackEntry?.savedStateHandle?.set("checkout_log", item)
                        navController.safeNavigate("checkout")
                    },
                    enabled = !checkoutOpened,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = checkColor)
                ) {
                    Text(text = stringResource(R.string.check_out), color = Color.White, fontSize = 15.sp)
                }
            }
        }
    }
}
