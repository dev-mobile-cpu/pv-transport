package com.pv.transport.presentation

import android.app.Activity
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.data.fuel.FuelRequestData
import com.pv.transport.extension.CustomDatePicker
import com.pv.transport.extension.HandleBackPressWithDialog
import com.pv.transport.extension.activityHiltViewModel
import com.pv.transport.extension.withComma
import com.pv.transport.network.ConnectivityObserver
import com.pv.transport.ui.theme.StatusBadge
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.textSecondary
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.FuelViewModel
import com.pv.transport.viewmodels.NetworkStatusViewModel
import androidx.compose.material3.HorizontalDivider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate

@Composable
fun FuelRequestScreen(
    navController: NavController,
    fuelViewModel: FuelViewModel = activityHiltViewModel(),
    networkViewModel: NetworkStatusViewModel = activityHiltViewModel()
) {
    val showExitDialog = remember { mutableStateOf(false) }
    val activity = LocalContext.current as Activity

    HandleBackPressWithDialog(
        onBackConfirmed = {
            activity.finish()
        },
        showDialog = showExitDialog
    )
    var startDate by rememberSaveable {
        mutableStateOf(LocalDate.now())
    }
    var endDate by rememberSaveable {
        mutableStateOf(LocalDate.now())
    }

    val fuelRequest by fuelViewModel.allRequestState.collectAsState()
    val networkStatus by networkViewModel.networkStatus.collectAsStateWithLifecycle()
    val isOffline = networkStatus != ConnectivityObserver.Status.Available
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            println("Last visible item index: ${lastVisibleItem?.index}, Total items: ${listState.layoutInfo.totalItemsCount}")
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 5
        }
    }

    LaunchedEffect(startDate, endDate) {
        fuelViewModel.getFuelRequest(
            startDate.toString(),
            endDate.toString()
        )
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && fuelRequest is FuelViewModel.AllFuelRequestState.Success) {
            val successState = fuelRequest as FuelViewModel.AllFuelRequestState.Success
            if (!successState.isLoadingMore && successState.currentPage < successState.lastPage) {
                fuelViewModel.loadMoreRequest(startDate.toString(), endDate.toString())
            }
        }
    }

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
                        Text(stringResource(R.string.filters), fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.start_date), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            CustomDatePicker(
                                selectedDate = startDate,
                                onDateSelected = { startDate = it },
                                bgColor = colorSecondary,
                                readOnly = isOffline
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.end_date), fontSize = 14.sp)
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
        when (fuelRequest) {

            is FuelViewModel.AllFuelRequestState.Loading -> {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            is FuelViewModel.AllFuelRequestState.Success -> {
                val fuelResponse = fuelRequest as FuelViewModel.AllFuelRequestState.Success
                val fuelList = fuelResponse.response

                if (fuelList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.no_fuel_requests_found), color = Color.Gray)
                        }
                    }
                } else {
                    items(fuelList.size) { index ->
                        FuelRequestCard(item = fuelList[index],navController)

                    }
                    if (fuelResponse.isLoadingMore) {
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

            is FuelViewModel.AllFuelRequestState.Error -> {
                item {
                    val errorMessage = (fuelRequest as FuelViewModel.AllFuelRequestState.Error).message

                    if (errorMessage == "No Internet Connection") {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "No Internet",
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(errorMessage,
                                fontFamily = appFontFamily ,
                                fontWeight = FontWeight.Normal,
                                color = textSecondary)
                        }

                    }else{
                        Box(
                            modifier = Modifier.fillParentMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                errorMessage,
                                fontFamily = appFontFamily ,
                                fontWeight = FontWeight.Normal,
                                color = textSecondary)
                        }
                    }

                }
            }


            else -> {}
        }


    }

}

@Composable
fun FuelRequestCard(item: FuelRequestData, navController: NavController) {
    val dateLabel = item.date.orEmpty().ifBlank { item.dateTime.orEmpty() }.ifBlank { "—" }
    val amount = item.fuelAmount.orEmpty()
    val status = item.status.orEmpty()
    val fuelType = item.fuelType.orEmpty()
    val requestType = item.requestType.orEmpty()
    val plate = item.licensePlate.orEmpty()
    val code = item.code.orEmpty()

    Card(
        shape = RoundedCornerShape(16.dp),
        onClick = {
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("fuel_request_detail", item)
            navController.navigate("fuel_request_detail")
        },
        colors = CardDefaults.cardColors(white),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateLabel,
                    fontSize = 13.sp,
                    fontFamily = appFontFamily,
                    color = textSecondary
                )
                if (status.isNotBlank()) {
                    StatusBadge(status = status)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${amount.withComma()} Ks",
                fontSize = 22.sp,
                fontFamily = appFontFamily,
                fontWeight = FontWeight.Bold,
                color = colorPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = fuelType.ifBlank { "—" },
                fontSize = 14.sp,
                fontFamily = appFontFamily,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF495057)
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE8E8E8))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.request_type),
                        fontSize = 12.sp,
                        fontFamily = appFontFamily,
                        color = textSecondary
                    )
                    Text(
                        text = requestType.replace('_', ' ')
                            .replaceFirstChar { it.uppercase() }
                            .ifBlank { "—" },
                        fontSize = 14.sp,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.license_number),
                        fontSize = 12.sp,
                        fontFamily = appFontFamily,
                        color = textSecondary
                    )
                    Text(
                        text = plate.ifBlank { "—" },
                        fontSize = 14.sp,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (code.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${stringResource(R.string.code)}: $code",
                    fontSize = 12.sp,
                    fontFamily = appFontFamily,
                    color = textSecondary
                )
            }
        }
    }
}