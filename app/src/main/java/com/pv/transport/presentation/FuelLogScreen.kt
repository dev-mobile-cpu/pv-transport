package com.pv.transport.presentation

import android.annotation.SuppressLint
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.data.fuel.FuelLogData
import com.pv.transport.data.fuel.FuelRequestData
import com.pv.transport.extension.CustomDatePicker
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.FuelViewModel
import java.time.LocalDate

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FuelLogScreen(navController: NavController,fuelViewModel: FuelViewModel = hiltViewModel()){
    var startDate by rememberSaveable {
        mutableStateOf(LocalDate.now())
    }
    var endDate by rememberSaveable {
        mutableStateOf(LocalDate.now())
    }
    val fuelLog by fuelViewModel.allFuelLogState.collectAsState()
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            println("Last visible item index: ${lastVisibleItem?.index}, Total items: ${listState.layoutInfo.totalItemsCount}")
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 5
        }
    }

    LaunchedEffect(startDate, endDate) {
        fuelViewModel.getFuelLog(
            startDate.toString(),
            endDate.toString()
        )
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && fuelLog is FuelViewModel.AllFuelLogState.Success) {
            val successState = fuelLog as FuelViewModel.AllFuelLogState.Success
            if (!successState.isLoadingMore && successState.currentPage < successState.lastPage) {
                fuelViewModel.loadMoreFuelLog(startDate.toString(), endDate.toString())
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_fuel_log") }
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
            when (fuelLog) {
                is FuelViewModel.AllFuelLogState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is FuelViewModel.AllFuelLogState.Success -> {
                    val fuelLogResponse = fuelLog as FuelViewModel.AllFuelLogState.Success
                    val fuelLogList = fuelLogResponse.response

                    if (fuelLogList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(R.string.no_fuel_logs_found), color = Color.Gray)
                            }
                        }
                    } else {
                        items(fuelLogList.size) { index ->
                            FuelLogCard(item = fuelLogList[index],navController)

                        }
                        if (fuelLogResponse.isLoadingMore) {
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
                is FuelViewModel.AllFuelLogState.Error -> {
                    item {
                        Text("Error: ${(fuelLog as FuelViewModel.AllFuelLogState.Error).message}")
                    }
                }

                else -> {}
            }

        }


    }

}

@Composable
fun FuelLogCard(item: FuelLogData,navController: NavController){
    Card(
        shape = RoundedCornerShape(16.dp),
        onClick = {
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("fuel_log_detail", item)

            navController.navigate("fuel_log_detail")

        },
        colors = CardDefaults.cardColors(white),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Date and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusChip(status = item.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Info: Fuel Amount & Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = item.fuelAmount,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = colorPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${item.fuelLiter}L)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Text(
                text = item.fuelType,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            // Details Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow(icon = Icons.Default.DirectionsCar, label = "Plate", value = item.carPlateNo)
                InfoRow(icon = Icons.Default.Store, label = "Shop", value = item.fuelShop)
                InfoRow(icon = Icons.Default.Speed, label = "Odometer", value = "${item.currentKm} km")
            }

        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun StatusChip(status: String) {
    val containerColor = when (status.lowercase()) {
        "approved" -> Color(0xFFE8F5E9)
        "pending" -> Color(0xFFFFF3E0)
        else -> Color(0xFFF5F5F5)
    }
    val contentColor = when (status.lowercase()) {
        "approved" -> Color(0xFF2E7D32)
        "pending" -> Color(0xFFEF6C00)
        else -> Color(0xFF616161)
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = contentColor
        )
    }
}