package com.pv.transport.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pv.transport.data.Data
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.white

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverLogDetailsScreen(log: Data,navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                        Text(text = "Driver Log Detail", color = Color.Black)
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = white
                )
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
            ) {

                Column(
                    modifier = Modifier.padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Text("Date : ${log.driverLog.date}", fontSize = 12.sp)
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    Text("Type : ${log.driverLog.type}", fontSize = 12.sp)
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    if (log.driverLog.type == "trip"){
                        Text("Trip Type : ${log.driverLog.tripType}", fontSize = 12.sp)
                        HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                        Box(modifier = Modifier.fillMaxWidth()){
                            Text("From : ${log.driverLog.from}", modifier = Modifier.align(
                                Alignment.CenterStart), fontSize = 12.sp)
                            Text("To : ${log.driverLog.to}", modifier = Modifier.align(
                                Alignment.CenterEnd), fontSize = 12.sp)
                        }
                        HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                        Text("Purpose : ${log.driverLog.purpose}", fontSize = 12.sp)
                    }
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    Box(modifier = Modifier.fillMaxWidth()){
                        Text("Start Time : ${log.startTime}", modifier = Modifier.align(
                            Alignment.CenterStart), fontSize = 12.sp)
                        Text("End Time : ${log.endTime}", modifier = Modifier.align(
                            Alignment.CenterEnd), fontSize = 12.sp)
                    }
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    Box(modifier = Modifier.fillMaxWidth()){
                        Text("Start Km : ${log.startKm}", modifier = Modifier.align(
                            Alignment.CenterStart), fontSize = 12.sp)
                        Text("End Km : ${log.endKm}", modifier = Modifier.align(
                            Alignment.CenterEnd), fontSize = 12.sp)
                    }
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    if (log.driverLog.type == "daily"){
                        Text("Remark : ${log.remark}", fontSize = 12.sp)
                        HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    }

                    Text("Status : ${log.status}", fontSize = 12.sp)
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ImageUploadBox("Start Km Image",log.documents)
                        ImageUploadBox("End Km Image",log.documents)
                    }

                }

            }
        }

    }


}