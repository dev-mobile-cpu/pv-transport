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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
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
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.data.ExpenseData
import com.pv.transport.extension.CustomDatePicker
import com.pv.transport.extension.HandleBackPressWithDialog
import com.pv.transport.local.data.OfflineOtherExpenseEntity
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.lightGreen
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.textPrimary
import com.pv.transport.ui.theme.textSecondary
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.DriverLogViewModel
import com.pv.transport.viewmodels.FuelViewModel
import com.pv.transport.viewmodels.OtherExpenseViewModel
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    navController: NavController,
    otherExpenseViewModel: OtherExpenseViewModel = hiltViewModel()
) {
    val showExitDialog = remember { mutableStateOf(false) }
    val activity = LocalContext.current as Activity

    HandleBackPressWithDialog(
        onBackConfirmed = {
            activity.finish()
        },
        showDialog = showExitDialog
    )
    var startDate by rememberSaveable {
        mutableStateOf(LocalDate.now())
    }
    var endDate by rememberSaveable {
        mutableStateOf(LocalDate.now())
    }
    val expense by otherExpenseViewModel.allOtherExpense.collectAsState()
    val pendingExpenses by otherExpenseViewModel.pendingExpenses.collectAsState()
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            println("Last visible item index: ${lastVisibleItem?.index}, Total items: ${listState.layoutInfo.totalItemsCount}")
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
            if (!successState.isLoadingMore && successState.currentPage < successState.lastPage) {
                otherExpenseViewModel.loadMoreExpense(startDate.toString(), endDate.toString())
            }
        }
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_expense") },
                shape = CircleShape,
                containerColor = lightGreen,
                contentColor = colorPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colorSecondary)
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = stringResource(R.string.other_expense),
                        color = textPrimary,
                        fontSize = 20.sp,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.track_your_expenses),
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
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
            when (expense) {
                is OtherExpenseViewModel.AllOtherExpenseState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.loading))
                        }
                    }
                }

                is OtherExpenseViewModel.AllOtherExpenseState.Success -> {
                    val expenses = (expense as OtherExpenseViewModel.AllOtherExpenseState.Success)
                    val expensesList = expenses.response

                    // Show pending offline items at the top
                    if (pendingExpenses.isNotEmpty()) {
                        items(pendingExpenses.size) { index ->
                            PendingExpenseCard(pendingExpenses[index])
                        }
                    }

                    if (expensesList.isEmpty() && pendingExpenses.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(R.string.no_expense_logs_found), color = Color.Gray)
                            }
                        }
                    } else {
                        items(expensesList.size) { index ->
                            OtherExpenseCard(expensesList[index], navController)
                        }
                        if (expenses.isLoadingMore) {
                            item {
                                Box(modifier = Modifier.fillParentMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }

                is OtherExpenseViewModel.AllOtherExpenseState.Error -> {
                    if (pendingExpenses.isNotEmpty()) {
                        items(pendingExpenses.size) { index ->
                            PendingExpenseCard(pendingExpenses[index])
                        }
                    }
                    item {
                        val errorMessage = (expense as OtherExpenseViewModel.AllOtherExpenseState.Error).message
                        if (errorMessage == "No Internet Connection") {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.WifiOff, contentDescription = "No Internet", tint = Color.Gray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(errorMessage, fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = textSecondary)
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillParentMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    errorMessage,
                                    fontFamily = appFontFamily ,
                                    fontWeight = FontWeight.Normal,
                                    color = textSecondary)
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(white),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(item.date, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(item.amount, fontSize = 14.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Pending sync",
                    tint = Color(0xFFEF6C00),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pending", fontSize = 12.sp, color = Color(0xFFEF6C00))
            }
        }
    }
}

@Composable
fun OtherExpenseCard(expenseData: ExpenseData, navController: NavController) {



    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(white),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            Column(modifier = Modifier.padding(16.dp).align(Alignment.CenterStart)) {
                Text(expenseData.date, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(expenseData.typeOfCost.name, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(expenseData.amount, fontSize = 14.sp)
            }
//            Button(
//                onClick = {
//                    navController.currentBackStackEntry
//                        ?.savedStateHandle
//                        ?.set("edit_expense", expenseData)
//
//                    navController.navigate("edit_expense")
//                },
//                modifier = Modifier.align(Alignment.CenterEnd).padding(5.dp)
//            ) {
//                Text(stringResource(R.string.edit))
//            }
        }
    }
}