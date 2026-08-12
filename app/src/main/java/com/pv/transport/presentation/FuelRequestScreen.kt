package com.pv.transport.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.data.fuel.FuelRequestData
import com.pv.transport.extension.CustomDatePicker
import com.pv.transport.extension.HandleBackPressWithDialog
import com.pv.transport.extension.withComma
import com.pv.transport.network.NetworkUtils
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.lightGreen
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.textSecondary
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.FuelViewModel
import java.time.LocalDate

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FuelRequestScreen(
    navController: NavController,
    fuelViewModel: FuelViewModel = hiltViewModel()
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


    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!NetworkUtils.isInternetAvailable(context)) {
                        Toast.makeText(context, "This action requires an active internet connection.", Toast.LENGTH_SHORT).show()
                    } else {
                        navController.navigate("add_fuel_request")
                    }
                },
                shape = CircleShape,
                containerColor = lightGreen,
                contentColor = colorPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        }
    ) {
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
                                    bgColor = colorSecondary
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.end_date), fontSize = 14.sp)
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

}

@Composable
fun FuelRequestCard(item: FuelRequestData,navController: NavController){
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

            Text(
                text =  "Request ID: ${item.id}",
                fontSize = 16.sp,
                fontFamily = appFontFamily ,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Fuel Type ID:  ${item.fuelTypeId}",
                fontFamily = appFontFamily ,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text =  "Fuel Amount: ${item.fuelAmount.withComma()} Ks",
                fontFamily = appFontFamily ,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Request Type: ${item.requestType}",
                fontFamily = appFontFamily ,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
        }
    }

}