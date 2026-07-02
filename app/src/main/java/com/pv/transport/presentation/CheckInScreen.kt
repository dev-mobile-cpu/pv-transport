package com.pv.transport.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.extension.CustomDatePicker
import com.pv.transport.network.NetworkUtils.isInternetAvailable
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.textPrimary
import com.pv.transport.viewmodels.ReasonViewModel
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.DriverLogViewModel
import com.pv.transport.viewmodels.TripTypeViewModel
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    
    // Persistence Fix: Scope ViewModels to Activity so data survives "Back" button
    val reasonViewModel: ReasonViewModel = if (activity != null) hiltViewModel(activity) else hiltViewModel()
    val tripTypeViewModel: TripTypeViewModel = if (activity != null) hiltViewModel(activity) else hiltViewModel()
    val driverLogViewModel: DriverLogViewModel = if (activity != null) hiltViewModel(activity) else hiltViewModel()

    val options = listOf("Daily", "Trip")
    var selectedOption by rememberSaveable { mutableStateOf(options[0]) }
    var expandedType by remember { mutableStateOf(false) }
    val date = remember { mutableStateOf(LocalDate.now())}
    var isNoInternet by remember { mutableStateOf(false) }
    var clearTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        isNoInternet = !isInternetAvailable(context)
    }

    val clearForm = {
        selectedOption = options[0]
        date.value = LocalDate.now()
        driverLogViewModel.clearDailyCheckIn()
        driverLogViewModel.clearTripCheckIn()
        clearTrigger++
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.add_daily_log),
                        fontFamily = appFontFamily,
                        fontSize = 18.sp,
                        color = textPrimary,
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
                actions = {
                    Text(
                        text = "Clear",
                        color = Color(0xFF007AFF),
                        fontSize = 13.sp,
                        fontFamily = appFontFamily,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { clearForm() }
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = white),
                windowInsets = WindowInsets(0) // UI Bug Fix: remove extra white space
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
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF495057)
                    )
                    Text(
                        stringResource(R.string.date),
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF495057)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                CustomDatePicker(
                    selectedDate = date.value,
                    onDateSelected = { date.value = it },
                    bgColor = white
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF495057)
                    )
                    Text(
                        stringResource(R.string.trip_type),
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF495057)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box{
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(white)
                            .clickable { expandedType = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedOption)
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }

                    DropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        options.forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(status)
                                        if (status == selectedOption) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedOption = status
                                    expandedType = false
                                }
                            )
                        }
                    }
                }
            }
            when (selectedOption) {
                "Daily" -> DailyCheckInScreen(navController,"Daily",date.value.toString(),reasonViewModel,driverLogViewModel, clearTrigger)
                "Trip" -> TripCheckInScreen(navController,"Trip",date.value.toString(),reasonViewModel,tripTypeViewModel,driverLogViewModel, clearTrigger)
            }
        }
    }
}
