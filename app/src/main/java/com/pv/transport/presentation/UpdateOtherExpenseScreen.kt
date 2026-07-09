package com.pv.transport.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.data.CostType
import com.pv.transport.data.ExpenseData
import com.pv.transport.data.log.ImageItem
import com.pv.transport.extension.EditMultipleImagePicker
import com.pv.transport.extension.TypeOfCostDropdown
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.OtherExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateOtherExpenseScreen(
    expenseData: ExpenseData,
    navController: NavController,
    otherExpenseViewModel: OtherExpenseViewModel = hiltViewModel()
) {
    val costs = otherExpenseViewModel.costState.collectAsState()
    val otherExpenseState = otherExpenseViewModel.otherExpenseState.collectAsState()
    val costList = remember { mutableStateListOf<CostType>() }
    var selectedCost by remember { mutableStateOf(expenseData.typeOfCost.name) }
    var selectedIndex by remember { mutableStateOf(expenseData.typeOfCost.id) }
    var amount by remember { mutableStateOf(TextFieldValue("")) }
    var imageList by remember { mutableStateOf<List<ImageItem>>(emptyList()) }
    var deletedIds by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(expenseData.amount) {
        amount = TextFieldValue(
            text = expenseData.amount,
            selection = TextRange(expenseData.amount.length)
        )

    }

    LaunchedEffect(expenseData.documents) {
        imageList = expenseData.documents.map {
            ImageItem(
                url = it.documentUrl,
                id = it.id
            )
        }
    }

    val isFormChanged by remember {
        derivedStateOf {
            val isCostChanged = selectedIndex != expenseData.typeOfCost.id
            val isAmountChanged = amount.text != expenseData.amount
            val hasNewImages = imageList.any { it.uri != null } // ပုံအသစ် ရွေးထားခြင်း ရှိမရှိ
            val hasDeletedImages = deletedIds.isNotEmpty()     // ပုံဟောင်း ဖြုတ်ထားခြင်း ရှိမရှိ

            isCostChanged || isAmountChanged || hasNewImages || hasDeletedImages
        }
    }

    LaunchedEffect(otherExpenseState.value) {
        when (otherExpenseState.value) {
            is OtherExpenseViewModel.OtherExpenseState.Success -> {
                navController.popBackStack()
            }
            is OtherExpenseViewModel.OtherExpenseState.Error -> {
            }
            else -> {}
        }
    }

    when (val s = costs.value) {
        is OtherExpenseViewModel.CostState.Idle -> {
            otherExpenseViewModel.getCostTypes()
            Text(text = "Loading costs...")
        }

        is OtherExpenseViewModel.CostState.Loading -> {
            CircularProgressIndicator()
        }

        is OtherExpenseViewModel.CostState.Success -> {
            costList.clear()
            costList.addAll(s.cost.data)
            if (selectedCost.isEmpty() && costList.isNotEmpty()) {
                selectedCost = costList[0].name
            }
        }

        is OtherExpenseViewModel.CostState.Error -> {
            Text(text = "Error: ${s.message}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.edit_other_expense), color = Color.Black)
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
    ){ innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.padding(16.dp)){
                Text(
                    stringResource(R.string.type_of_cost),
                    fontFamily = appFontFamily ,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))

                TypeOfCostDropdown(
                    reasons = costList,
                    selectedReason = selectedCost,
                    onReasonSelected = { index, cost ->
                        selectedCost = cost
                        selectedIndex = index.toString()
                    },
                    modifier = Modifier
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.expense_amount),
                    fontFamily = appFontFamily ,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                                if (amount.text.isEmpty()) {
                                    Text(
                                        text = amount.text,
                                        color = Color.Black
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    stringResource(R.string.expense_amount),
                    fontFamily = appFontFamily ,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))

                EditMultipleImagePicker(
                    selectedImages = imageList,
                    onImagesChanged = { imageList = it },
                    onImageDeleted = { id ->
                        deletedIds = deletedIds + id
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        otherExpenseViewModel.editOtherExpense(
                            recordId = expenseData.id,
                            date = expenseData.date,
                            typeOfCost = selectedIndex,
                            amount = amount.text.ifEmpty { expenseData.amount },
                            licensePlate = expenseData.licensePlate,
                            imageUris = imageList.mapNotNull { it.uri },
                            deletedIds = deletedIds
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorPrimary),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isFormChanged && otherExpenseState.value !is OtherExpenseViewModel.OtherExpenseState.Loading
                ) {
                    if (otherExpenseState.value is OtherExpenseViewModel.OtherExpenseState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text(stringResource(R.string.update), color = Color.White)
                    }
                }
            }
        }
    }
}
