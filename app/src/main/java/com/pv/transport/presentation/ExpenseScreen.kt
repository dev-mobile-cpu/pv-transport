package com.pv.transport.presentation

import android.annotation.SuppressLint
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.data.ExpenseData
import com.pv.transport.extension.CustomDatePicker
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.robotoFontFamily
import com.pv.transport.ui.theme.textPrimary
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.OtherExpenseViewModel
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(navController: NavController,otherExpenseViewModel: OtherExpenseViewModel = hiltViewModel()) {
    var startDate by rememberSaveable {
        mutableStateOf(LocalDate.now())
    }
    var endDate by rememberSaveable {
        mutableStateOf(LocalDate.now())
    }
    val expense by otherExpenseViewModel.allOtherExpense.collectAsState()
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.other_expense),
                        fontFamily = robotoFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(white),
                windowInsets = WindowInsets(0)
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_expense") }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        },
    ) {innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colorSecondary)
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

                    if (expensesList.isEmpty()) {
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
                            OtherExpenseCard(expensesList[index],navController)
                        }

                        if (expenses.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }

                }

                is OtherExpenseViewModel.AllOtherExpenseState.Error -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text((expense as OtherExpenseViewModel.AllOtherExpenseState.Error).message)
                        }
                    }

                }

                else -> {}
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
            Button(
                onClick = {
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("edit_expense", expenseData)

                    navController.navigate("edit_expense")
                },
                modifier = Modifier.align(Alignment.CenterEnd).padding(5.dp)
            ) {
                Text(stringResource(R.string.edit))
            }
        }
    }
}