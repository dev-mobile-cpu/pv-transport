package com.pv.transport.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.data.fuel.FuelRequestData
import com.pv.transport.extension.withComma
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.white

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelRequestDetailScreen(data: FuelRequestData,navController: NavController){

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.fuel_request_detail), color = Color.Black)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
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
                .padding(innerPadding)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(white)
            ){

                Column(
                    modifier = Modifier.padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Text(stringResource(R.string.date)+" : ${data.date}", fontSize = 12.sp)
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    Text(stringResource(R.string.license_number)+" : ${data.licensePlate}", fontSize = 12.sp)
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    Text(stringResource(R.string.request_type)+" : ${data.requestType}", fontSize = 12.sp)
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    Text(stringResource(R.string.fuel_type)+" : ${data.fuelType}", fontSize = 12.sp)
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    Text(stringResource(R.string.fuel_type_id)+" : ${data.fuelTypeId}", fontSize = 12.sp)
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    Text(stringResource(R.string.fuel_amount)+" : ${data.fuelAmount.withComma()} Ks", fontSize = 12.sp)
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    Text(stringResource(R.string.fuel_liter)+" : ${data.fuelLiter}", fontSize = 12.sp)
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    Text(stringResource(R.string.code)+" : ${data.code}", fontSize = 12.sp)
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    Text(stringResource(R.string.status)+" : ${data.status}", fontSize = 12.sp)

                }

            }

        }
    }
}