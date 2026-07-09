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
import com.pv.transport.extension.ImageUploadBox
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.white

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalDetailScreen(approvalData: Data, navController: NavController) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.approval_detail), color = Color.Black)
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
                    val driverLog = approvalData.driverLog
                    Text("Date : ${driverLog?.date ?: ""}", fontSize = 12.sp)
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    Text("Type : ${driverLog?.type ?: ""}", fontSize = 12.sp)
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    if (driverLog?.type == "trip"){
                        Text("Trip Type : ${driverLog.tripType ?: ""}", fontSize = 12.sp)
                        HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                        Box(modifier = Modifier.fillMaxWidth()){
                            Text("From : ${driverLog.from}", modifier = Modifier.align(
                                Alignment.CenterStart), fontSize = 12.sp)
                            Text("To : ${driverLog.to}", modifier = Modifier.align(
                                Alignment.CenterEnd), fontSize = 12.sp)
                        }
                        HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                        Text("Purpose : ${driverLog.purpose}", fontSize = 12.sp)
                    }
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    Box(modifier = Modifier.fillMaxWidth()){
                        Text("Start Time : ${approvalData.startTime}", modifier = Modifier.align(
                            Alignment.CenterStart), fontSize = 12.sp)
                        Text("End Time : ${approvalData.endTime ?: ""}", modifier = Modifier.align(
                            Alignment.CenterEnd), fontSize = 12.sp)
                    }
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    Box(modifier = Modifier.fillMaxWidth()){
                        Text("Start Km : ${approvalData.startKm}", modifier = Modifier.align(
                            Alignment.CenterStart), fontSize = 12.sp)
                        Text("End Km : ${approvalData.endKm ?: ""}", modifier = Modifier.align(
                            Alignment.CenterEnd), fontSize = 12.sp)
                    }
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    if (driverLog?.type == "daily"){
                        Text("Remark : ${approvalData.remark ?: ""}", fontSize = 12.sp)
                        HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    }
                    Text("Status : ${approvalData.status.replaceFirstChar {it.uppercase()}}", fontSize = 12.sp)
                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        ImageUploadBox("Start Km Image",approvalData.documents)
                        ImageUploadBox("End Km Image",approvalData.documents)
                    }

                    HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)

                    val actualUser = approvalData.actualUser
                    val corporateUser = approvalData.corporateUser
                    if (!actualUser.isNullOrEmpty() && corporateUser?.corporateId != null) {

                        Text("Actual User : $actualUser", fontSize = 12.sp)
                        HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                        Text(
                            "Corporate User ID : ${corporateUser.corporateId}",
                            fontSize = 12.sp
                        )
                        HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                        Text(
                            "Corporate User Name : ${corporateUser.name ?: ""}",
                            fontSize = 12.sp
                        )
                        HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                        Text(
                            "Corporate User Email : ${corporateUser.email ?: ""}",
                            fontSize = 12.sp
                        )
                        HorizontalDivider(Modifier, thickness = 1.dp, color = colorSecondary)
                        Text(
                            "Corporate User Phone : ${corporateUser.phone ?: ""}",
                            fontSize = 12.sp
                        )
                    }


                }

            }

        }

    }

}
