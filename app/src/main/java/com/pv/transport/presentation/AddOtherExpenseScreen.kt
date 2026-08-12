package com.pv.transport.presentation

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.extension.CustomDatePicker
import com.pv.transport.extension.CustomMultipleImagePicker
import com.pv.transport.extension.TypeOfCostDropdown
import com.pv.transport.extension.findActivity
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.OtherExpenseViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.pv.transport.data.CostType
import com.pv.transport.extension.ThousandSeparatorTransformation
import com.pv.transport.ui.theme.FormPrimaryButton
import com.pv.transport.ui.theme.FormFieldLabel
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.textPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOtherExpenseScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val otherExpenseViewModel: OtherExpenseViewModel = if (activity != null) hiltViewModel(activity) else hiltViewModel()

    val costState by otherExpenseViewModel.costState.collectAsState()
    val otherExpenseState by otherExpenseViewModel.otherExpenseState.collectAsState()
    
    // ViewModel-based states
    val date by otherExpenseViewModel.addExpenseDate.collectAsState()
    val selectedCost by otherExpenseViewModel.addExpenseType.collectAsState()
    val selectedIndex by otherExpenseViewModel.addExpenseTypeId.collectAsState()
    val amount by otherExpenseViewModel.addExpenseAmount.collectAsState()
    val uriList by otherExpenseViewModel.addExpenseUriList.collectAsState()
    val selectedVehicle by otherExpenseViewModel.addExpenseVehicle.collectAsState()

    val costList = remember { mutableStateListOf<CostType>() }
    var isSaved by remember { mutableStateOf(false) }
    var isButtonClicked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (costState is OtherExpenseViewModel.CostState.Idle) {
            otherExpenseViewModel.getCostTypes()
        }
    }

    LaunchedEffect(costState) {
        if (costState is OtherExpenseViewModel.CostState.Success) {
            val costs = (costState as OtherExpenseViewModel.CostState.Success).cost.data
            costList.clear()
            costList.addAll(costs)
            if (selectedCost.isEmpty() && costList.isNotEmpty()) {
                otherExpenseViewModel.addExpenseType.value = costList[0].name
                otherExpenseViewModel.addExpenseTypeId.value = costList[0].id.toInt()
            }
        }
    }

    val isSaving = otherExpenseState is OtherExpenseViewModel.OtherExpenseState.Loading

    LaunchedEffect(otherExpenseState) {
        when (otherExpenseState) {
            is OtherExpenseViewModel.OtherExpenseState.Success -> {
                isButtonClicked = false
                Toast.makeText(context, "Save successful", Toast.LENGTH_SHORT).show()
                isSaved = true
                delay(350)
                navController.popBackStack()
                otherExpenseViewModel.resetOtherExpenseState()
            }
            is OtherExpenseViewModel.OtherExpenseState.SavedOffline -> {
                isButtonClicked = false
                Toast.makeText(context, "Saved offline.", Toast.LENGTH_SHORT).show()
                isSaved = true
                delay(350)
                navController.popBackStack()
                otherExpenseViewModel.resetOtherExpenseState()
            }
            is OtherExpenseViewModel.OtherExpenseState.Error -> {
                isButtonClicked = false
                val error = (otherExpenseState as OtherExpenseViewModel.OtherExpenseState.Error).message
                Toast.makeText(context, "Save failed: $error", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    val clearForm = {
        otherExpenseViewModel.clearAddExpense()
        if (costList.isNotEmpty()) {
            otherExpenseViewModel.addExpenseType.value = costList[0].name
            otherExpenseViewModel.addExpenseTypeId.value = costList[0].id.toInt()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.add_other_expense),
                        color = textPrimary,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
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
                            .padding(end = 8.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { clearForm() }
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = white),
                windowInsets = WindowInsets(0)
            )
        },
        containerColor = Color(0xFFF4F4F4),
        // UI Bug Fix
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ){
            Column(modifier = Modifier.padding(16.dp)){
                FormFieldLabel(text = stringResource(R.string.date), icon = Icons.Default.DateRange)
                Spacer(modifier = Modifier.height(4.dp))
                CustomDatePicker(
                    selectedDate = date,
                    onDateSelected = { otherExpenseViewModel.addExpenseDate.value = it },
                    bgColor = white
                )
                Spacer(modifier = Modifier.height(16.dp))
                FormFieldLabel(text = stringResource(R.string.type_of_cost), icon = Icons.Default.Category)
                Spacer(modifier = Modifier.height(4.dp))
                TypeOfCostDropdown(
                    reasons = costList,
                    selectedReason = selectedCost,
                    onReasonSelected = { index, cost ->
                        otherExpenseViewModel.addExpenseTypeId.value = index
                        otherExpenseViewModel.addExpenseType.value = cost
                    },
                    modifier = Modifier
                )

                Spacer(modifier = Modifier.height(16.dp))
                FormFieldLabel(text = stringResource(R.string.expense_amount), icon = Icons.Default.AttachMoney)
                Spacer(modifier = Modifier.height(4.dp))

                BasicTextField(
                    value = amount,
                    onValueChange = { otherExpenseViewModel.addExpenseAmount.value = it },
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = ThousandSeparatorTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    decorationBox = {innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                                if (amount.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.enter_amount),
                                        color = Color.Gray,
                                        fontSize = 16.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    }
                )


                Spacer(modifier = Modifier.height(16.dp))
                FormFieldLabel(text = stringResource(R.string.expense_proof), icon = Icons.Default.AttachFile)
                Spacer(modifier = Modifier.height(4.dp))

                CustomMultipleImagePicker(
                    selectedUris = uriList,
                    onImagesSelected = { otherExpenseViewModel.addExpenseUriList.value = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                val canSave = amount.isNotEmpty() && selectedCost != "Type Of Cost" && uriList.isNotEmpty()
                FormPrimaryButton(
                    text = stringResource(R.string.save),
                    onClick = {
                        if (isButtonClicked) return@FormPrimaryButton
                        isButtonClicked = true
                        if (!isSaving) {
                            otherExpenseViewModel.saveOtherExpense(
                                date = date.toString(),
                                typeOfCostId = selectedIndex.toString(),
                                typeOfCostOffline = selectedCost,
                                amount = amount,
                                licensePlate = selectedVehicle,
                                imageUris = uriList,
                                context = context
                            )
                        }
                    },
                    enabled = canSave && !isSaving && !isSaved && !isButtonClicked,
                    isLoading = isSaving
                )
            }
        }
    }
}
