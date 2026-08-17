package com.pv.transport.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.pv.transport.data.fuel.FuelRequestData
import com.pv.transport.extension.withComma
import com.pv.transport.ui.theme.DetailItem
import com.pv.transport.ui.theme.DetailSectionCard
import com.pv.transport.ui.theme.StatusBadge
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.textSecondary
import com.pv.transport.ui.theme.white

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelRequestDetailScreen(data: FuelRequestData, navController: NavController) {
    val amount = data.fuelAmount.orEmpty()
    val status = data.status.orEmpty()
    val requestType = data.requestType.orEmpty()
    val typeLabel = requestType
        .replace('_', ' ')
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        .ifBlank { "—" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.fuel_request_detail),
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
                    text = typeLabel,
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
                DetailItem(label = stringResource(R.string.date), value = data.date.orEmpty())
                DetailItem(label = stringResource(R.string.code), value = data.code.orEmpty())
                DetailItem(label = stringResource(R.string.request_type), value = requestType)
                DetailItem(label = stringResource(R.string.fuel_type), value = data.fuelType.orEmpty())
                DetailItem(
                    label = stringResource(R.string.fuel_liter),
                    value = data.fuelLiter.orEmpty()
                )
                DetailItem(
                    label = stringResource(R.string.license_number),
                    value = data.licensePlate.orEmpty()
                )
                val remark = data.remark.orEmpty()
                if (remark.isNotBlank()) {
                    DetailItem(label = stringResource(R.string.remark), value = remark)
                }
                val approved = data.approvedDate.orEmpty()
                if (approved.isNotBlank()) {
                    DetailItem(label = stringResource(R.string.approved_date), value = approved)
                }
            }
        }
    }
}
