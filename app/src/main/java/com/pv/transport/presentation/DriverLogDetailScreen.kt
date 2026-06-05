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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.pv.transport.data.log.Data
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.white

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverLogDetailsScreen(log: Data,navController: NavController) {
    val savedStateHandle = navController.previousBackStackEntry?.savedStateHandle
    val originalIndex = savedStateHandle?.get<Int>("index_key")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.driver_log_details), color = Color.Black)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.previousBackStackEntry?.savedStateHandle?.set("clicked_index", originalIndex)
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
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(white)
            ) {

                Column(
                    modifier = Modifier.padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Text(stringResource(R.string.date)+" : ${log.driverLog.date}", fontSize = 12.sp)
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    Text(stringResource(R.string.type)+" : ${log.type}", fontSize = 12.sp)
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    Text(stringResource(R.string.reason)+" : ${log.reason}", fontSize = 12.sp)
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    if (log.type == "trip"){
                        Box(modifier = Modifier.fillMaxWidth()){
                            Text(stringResource(R.string.from)+" : ${log.from}", modifier = Modifier.align(
                                Alignment.CenterStart), fontSize = 12.sp)
                            Text(stringResource(R.string.to)+" : ${log.to}", modifier = Modifier.align(
                                Alignment.CenterEnd), fontSize = 12.sp)
                        }
                        HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                        Text(stringResource(R.string.purpose)+" : ${log.purpose}", fontSize = 12.sp)
                        HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    }
                    Box(modifier = Modifier.fillMaxWidth()){
                        Text(stringResource(R.string.start_time)+" : ${log.startTime}", modifier = Modifier.align(
                            Alignment.CenterStart), fontSize = 12.sp)
                        Text(stringResource(R.string.end_time)+" : ${log.endTime}", modifier = Modifier.align(
                            Alignment.CenterEnd), fontSize = 12.sp)
                    }
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    Box(modifier = Modifier.fillMaxWidth()){
                        Text(stringResource(R.string.start_km)+" : ${log.startKm}", modifier = Modifier.align(
                            Alignment.CenterStart), fontSize = 12.sp)
                        Text(stringResource(R.string.end_km)+" : ${log.endKm}", modifier = Modifier.align(
                            Alignment.CenterEnd), fontSize = 12.sp)
                    }
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    if (log.type == "daily"){
                        Text(stringResource(R.string.remark)+" : ${log.remark}", fontSize = 12.sp)
                        HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    }

                    Text(stringResource(R.string.status)+" : ${log.driverLog.status}", fontSize = 12.sp)
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ImageUploadBox(stringResource(R.string.start_km_image),log.documents)
                        ImageUploadBox(stringResource(R.string.end_km_image),log.documents)
                    }

                }

            }
        }

    }


}