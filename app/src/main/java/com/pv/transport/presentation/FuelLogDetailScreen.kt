package com.pv.transport.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.data.fuel.FuelLogData
import com.pv.transport.extension.withComma
import com.pv.transport.ui.theme.DetailItem
import com.pv.transport.ui.theme.DetailPhotoThumbnail
import com.pv.transport.ui.theme.DetailSectionCard
import com.pv.transport.ui.theme.StatusBadge
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.textSecondary
import com.pv.transport.ui.theme.white

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelLogDetailScreen(data: FuelLogData, navController: NavController) {
    val amount = data.fuelAmount.orEmpty()
    val status = data.status.orEmpty()
    val docs = data.documents.orEmpty()
    val odometerUrl = data.currentKmPhoto?.thumbnailUrl?.takeIf { it.isNotBlank() }
        ?: data.currentKmPhoto?.photoUrl?.takeIf { it.isNotBlank() }
        ?: data.currentKmPhoto?.fileName?.takeIf { it.isNotBlank() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.fuel_log_detail),
                        color = Color.Black,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.SemiBold
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = white),
                windowInsets = WindowInsets(0)
            )
        },
        containerColor = Color(0xFFF4F4F4)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.total_spent),
                    fontSize = 13.sp,
                    fontFamily = appFontFamily,
                    color = textSecondary
                )
                Text(
                    text = "${amount.withComma()} Ks",
                    fontSize = 28.sp,
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = colorPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (status.isNotBlank()) {
                    StatusBadge(status = status)
                }
            }

            DetailSectionCard(title = stringResource(R.string.transaction_info)) {
                DetailItem(label = stringResource(R.string.date_time), value = data.date.orEmpty())
                DetailItem(label = stringResource(R.string.fuel_type), value = data.fuelType.orEmpty())
                DetailItem(
                    label = stringResource(R.string.liters_filled),
                    value = "${data.fuelLiter.orEmpty()} L"
                )
                DetailItem(label = stringResource(R.string.station_shop), value = data.fuelShop.orEmpty())
                DetailItem(label = stringResource(R.string.customer), value = data.customer.orEmpty())
            }

            DetailSectionCard(title = stringResource(R.string.vehicle_mileage)) {
                DetailItem(
                    label = stringResource(R.string.car_plate_number),
                    value = data.carPlateNo.orEmpty()
                )
                DetailItem(
                    label = stringResource(R.string.current_km),
                    value = "${data.currentKm.orEmpty()} km"
                )
            }

            DetailSectionCard(title = stringResource(R.string.attach_document)) {
                // Row + horizontalScroll (NOT LazyRow) — LazyRow inside verticalScroll crashes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailPhotoThumbnail(
                        label = stringResource(R.string.odometer),
                        imageUrl = odometerUrl
                    )
                    docs.forEach { doc ->
                        DetailPhotoThumbnail(
                            label = stringResource(R.string.voucher_image),
                            imageUrl = doc.documentUrl
                        )
                    }
                }
            }
        }
    }
}
