package com.pv.transport.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.data.ExpenseData
import com.pv.transport.extension.CustomDatePicker
import com.pv.transport.extension.HandleBackPressWithDialog
import com.pv.transport.extension.activityHiltViewModel
import com.pv.transport.extension.withComma
import com.pv.transport.local.data.OfflineOtherExpenseEntity
import com.pv.transport.network.ConnectivityObserver
import com.pv.transport.ui.theme.AddActionButton
import com.pv.transport.ui.theme.NetworkAwarePageTitle
import com.pv.transport.ui.theme.StatusBadge
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.textPrimary
import com.pv.transport.ui.theme.textSecondary
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.NetworkStatusViewModel
import com.pv.transport.viewmodels.OtherExpenseViewModel
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    navController: NavController,
    otherExpenseViewModel: OtherExpenseViewModel = activityHiltViewModel(),
    networkViewModel: NetworkStatusViewModel = activityHiltViewModel()
) {
    // Shared network status (same source as Fuel header) — debounced in title
    val networkStatus by networkViewModel.networkStatus.collectAsStateWithLifecycle()
    val isOffline = networkStatus != ConnectivityObserver.Status.Available
    val showExitDialog = remember { mutableStateOf(false) }
    val activity = LocalContext.current as Activity

    HandleBackPressWithDialog(
        onBackConfirmed = { activity.finish() },
        showDialog = showExitDialog
    )

    var startDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var endDate by rememberSaveable { mutableStateOf(LocalDate.now()) }

    // 🌟 2. Unified Expense Logs (Cache + API + Pending Logs)
    val expense by otherExpenseViewModel.unifiedOtherExpenseLogs.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 5
        }
    }

    LaunchedEffect(startDate, endDate) {
        otherExpenseViewModel.getAllOtherExpenses(
            startDate.toString(),
            endDate.toString()
        )
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && expense is OtherExpenseViewModel.AllOtherExpenseState.Success) {
            val successState = expense as OtherExpenseViewModel.AllOtherExpenseState.Success
            if (!successState.isLoadingMore && successState.currentPage < successState.lastPage && !successState.isOffline) {
                otherExpenseViewModel.loadMoreExpense(startDate.toString(), endDate.toString())
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(colorSecondary)
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item(key = "header_title") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NetworkAwarePageTitle(
                        title = stringResource(R.string.other_expense),
                        subtitle = stringResource(R.string.track_your_expenses),
                        networkStatus = networkStatus,
                        modifier = Modifier.weight(1f)
                    )
                    AddActionButton(
                        text = stringResource(R.string.add_expense),
                        onClick = { navController.navigate("add_expense") }
                    )
                }
            }

            // Date Filters
            item(key = "filter_card") {
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
                                    bgColor = colorSecondary,
                                    readOnly = isOffline
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.end_date), fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                CustomDatePicker(
                                    selectedDate = endDate,
                                    onDateSelected = { endDate = it },
                                    bgColor = colorSecondary,
                                    readOnly = isOffline
                                )
                            }
                        }
                    }
                }
            }

            // Main State Content
            when (val state = expense) {
                is OtherExpenseViewModel.AllOtherExpenseState.Loading -> {
                    item(key = "loading_indicator") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colorPrimary)
                        }
                    }
                }

                is OtherExpenseViewModel.AllOtherExpenseState.Success -> {
                    val expensesList = state.response

                    if (expensesList.isEmpty()) {
                        item(key = "empty_state") {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .padding(top = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    stringResource(R.string.no_expense_logs_found),
                                    color = Color.Gray,
                                    fontFamily = appFontFamily
                                )
                            }
                        }
                    } else {
                        // 🌟 3. Items များတွင် Unique Key တိုင်ပေးထားသဖြင့် Scroll Position မလွဲတော့ပါ
                        items(
                            items = expensesList,
                            key = { item -> item.uuid ?: item.id }
                        ) { item ->
                            OtherExpenseCard(item, navController)
                        }

                        if (state.isLoadingMore) {
                            item(key = "load_more_progress") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }

                is OtherExpenseViewModel.AllOtherExpenseState.Error -> {
                    item(key = "error_state") {
                        val errorMessage = state.message
                        if (errorMessage == "No Internet Connection") {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = "No Internet",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    errorMessage,
                                    fontFamily = appFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = textSecondary
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .padding(top = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    errorMessage,
                                    fontFamily = appFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = textSecondary
                                )
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
fun PendingExpenseCard(item: OfflineOtherExpenseEntity) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(white),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.date,
                    fontSize = 13.sp,
                    fontFamily = appFontFamily,
                    color = textSecondary
                )
                StatusBadge(status = if (item.isSyncing) "SYNCING" else "OFFLINE")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${item.amount.withComma()} Ks",
                fontSize = 22.sp,
                fontFamily = appFontFamily,
                fontWeight = FontWeight.Bold,
                color = colorPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.typeOfCost.ifBlank { "—" },
                fontSize = 14.sp,
                fontFamily = appFontFamily,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF495057)
            )
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE8E8E8))
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = textSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.licensePlate.ifBlank { "—" },
                    fontSize = 13.sp,
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
            }
        }
    }
}

@Composable
fun OtherExpenseCard(expenseData: ExpenseData, navController: NavController) {
    Card(
        shape = RoundedCornerShape(16.dp),
        onClick = {
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("expense_detail", expenseData)
            navController.navigate("expense_detail")
        },
        colors = CardDefaults.cardColors(white),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expenseData.date,
                    fontSize = 13.sp,
                    fontFamily = appFontFamily,
                    color = textSecondary
                )
                if (!expenseData.isSynced) {
                    StatusBadge(status = "OFFLINE")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${expenseData.amount.withComma()} Ks",
                fontSize = 22.sp,
                fontFamily = appFontFamily,
                fontWeight = FontWeight.Bold,
                color = colorPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = expenseData.typeOfCost.name.ifBlank { "—" },
                fontSize = 14.sp,
                fontFamily = appFontFamily,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF495057)
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE8E8E8))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = textSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = expenseData.licensePlate.ifBlank { "—" },
                        fontSize = 13.sp,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = textSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = expenseData.typeOfCost.name,
                        fontSize = 13.sp,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                }
            }
        }
    }
}