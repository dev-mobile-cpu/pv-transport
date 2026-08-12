package com.pv.transport.presentation

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
import com.pv.transport.data.log.Data
import com.pv.transport.extension.ImageUploadBox
import com.pv.transport.ui.theme.DetailItem
import com.pv.transport.ui.theme.DetailSectionCard
import com.pv.transport.ui.theme.StatusBadge
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.textSecondary
import com.pv.transport.ui.theme.white

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverLogDetailsScreen(
    log: Data,
    navController: NavController,
    titleRes: Int = R.string.driver_log_details,
    showApprovalMeta: Boolean = false
) {
    val savedStateHandle = navController.previousBackStackEntry?.savedStateHandle
    val originalIndex = savedStateHandle?.get<Int>("index_key")
    val driverLog = log.driverLog
    val type = (log.type.ifBlank { driverLog?.type.orEmpty() }).lowercase()
    val status = log.status.ifBlank { driverLog?.status.orEmpty() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(titleRes),
                        color = Color.Black,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = white),
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = type.replaceFirstChar { it.uppercase() },
                    fontSize = 13.sp,
                    fontFamily = appFontFamily,
                    color = textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.reason.ifBlank { "—" },
                    fontSize = 22.sp,
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = colorPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (status.isNotBlank()) {
                    StatusBadge(status = status)
                }
            }

            DetailSectionCard(title = "Log Info") {
                DetailItem(label = stringResource(R.string.date), value = driverLog?.date.orEmpty())
                DetailItem(label = stringResource(R.string.type), value = type.replaceFirstChar { it.uppercase() })
                DetailItem(label = stringResource(R.string.reason), value = log.reason)
                if (type == "trip") {
                    DetailItem(label = stringResource(R.string.trip_type), value = driverLog?.tripType.orEmpty())
                    DetailItem(label = stringResource(R.string.from), value = log.from ?: driverLog?.from.orEmpty())
                    DetailItem(label = stringResource(R.string.to), value = log.to ?: driverLog?.to.orEmpty())
                    DetailItem(
                        label = stringResource(R.string.purpose),
                        value = log.purpose ?: driverLog?.purpose.orEmpty()
                    )
                }
                if (type == "daily") {
                    DetailItem(label = stringResource(R.string.remark), value = log.remark.orEmpty())
                }
                DetailItem(label = stringResource(R.string.start_time), value = log.startTime)
                DetailItem(label = stringResource(R.string.end_time), value = log.endTime.orEmpty())
                DetailItem(label = stringResource(R.string.start_km), value = log.startKm)
                DetailItem(label = stringResource(R.string.end_km), value = log.endKm.orEmpty())
            }

            if (showApprovalMeta) {
                val actualUser = log.actualUser
                val corporateUser = log.corporateUser
                if (!actualUser.isNullOrEmpty() || corporateUser != null) {
                    DetailSectionCard(title = "Approval Info") {
                        if (!actualUser.isNullOrEmpty()) {
                            DetailItem(label = "Actual User", value = actualUser)
                        }
                        corporateUser?.let { user ->
                            DetailItem(label = "Corporate ID", value = user.corporateId.orEmpty())
                            DetailItem(label = "Name", value = user.name.orEmpty())
                            DetailItem(label = "Email", value = user.email.orEmpty())
                            DetailItem(label = "Phone", value = user.phone.orEmpty())
                        }
                    }
                }
            }

            DetailSectionCard(title = stringResource(R.string.attach_document)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ImageUploadBox(
                        stringResource(R.string.start_km_image),
                        log.documents,
                        imageFilePath = log.startImagePath
                    )
                    ImageUploadBox(
                        stringResource(R.string.end_km_image),
                        log.documents,
                        imageFilePath = log.endImagePath
                    )
                }
            }
        }
    }
}
