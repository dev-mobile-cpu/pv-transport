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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.OtherExpenseViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.data.CostType
import com.pv.transport.data.log.AssignedVehicle
import com.pv.transport.data.log.AssignedVehicleResponse
import com.pv.transport.data.log.ReasonListResponse
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.robotoFontFamily
import com.pv.transport.ui.theme.textPrimary
import com.pv.transport.viewmodels.ReasonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOtherExpenseScreen(navController: NavController,otherExpenseViewModel: OtherExpenseViewModel = hiltViewModel()) {
    val costs = otherExpenseViewModel.costState.collectAsState()
    val otherExpenseState = otherExpenseViewModel.otherExpenseState.collectAsState()
    val date = remember { mutableStateOf(LocalDate.now())}
    val costList = remember { mutableStateListOf<CostType>() }
    var selectedCost by remember { mutableStateOf("") }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var amount by remember { mutableStateOf("") }
    var uriList by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isSaved by remember { mutableStateOf(false) }
    val  context = LocalContext.current
    var selectedVehicle by remember { mutableStateOf("") }
    var isButtonClicked by remember { mutableStateOf(false) }

    when (val s = costs.value) {
        is OtherExpenseViewModel.CostState.Idle -> {
            otherExpenseViewModel.getCostTypes()
            Text(text = "Loading costs...")
        }

        is OtherExpenseViewModel.CostState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is OtherExpenseViewModel.CostState.Success -> {
            costList.clear()
            costList.addAll(s.cost.data)
            if (selectedCost.isEmpty() && costList.isNotEmpty()) {
                selectedCost = costList[0].name
                selectedIndex = costList[0].id.toInt()
            }
        }

        is OtherExpenseViewModel.CostState.Error -> {
            Text(text = "Error: ${s.message}")
        }
    }

    val isSaving = when (otherExpenseState.value) {
        is  OtherExpenseViewModel.OtherExpenseState.Loading -> true
        else -> false
    }

    LaunchedEffect(key1 = otherExpenseState.value) {
        when (val state = otherExpenseState.value) {
            is OtherExpenseViewModel.OtherExpenseState.Success -> {
                isButtonClicked = false
                amount = ""
                uriList = emptyList()
                selectedCost = ""
                selectedIndex = 0
                Toast.makeText(context, "Save successful", Toast.LENGTH_SHORT).show()
                isSaved = true
                // small delay so user sees toast, then go back to logs screen
                delay(350)
                navController.popBackStack()
            }
            is OtherExpenseViewModel.OtherExpenseState.Error -> {
                isButtonClicked = false
                Toast.makeText(context, "Save failed: ${state.message}", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.add_other_expense),
                            fontFamily = robotoFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = textPrimary
                        )

                    }
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
        containerColor = Color(0xFFF4F4F4),
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ){
            Column(modifier = Modifier.padding(16.dp)){
                Text(stringResource(R.string.date), fontFamily = robotoFontFamily, fontWeight = FontWeight.Normal)
                Spacer(modifier = Modifier.height(4.dp))
                CustomDatePicker(
                    selectedDate = date.value,
                    onDateSelected = { date.value = it },
                    bgColor = white
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.type_of_cost),
                    fontFamily = robotoFontFamily,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                TypeOfCostDropdown(
                    reasons = costList,
                    selectedReason = selectedCost,
                    onReasonSelected = { index, cost ->
                        selectedIndex = index
                        selectedCost = cost
                    },
                    modifier = Modifier
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.expense_amount),
                    fontFamily = robotoFontFamily,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
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
                        )
                    ) { innerTextField ->

                        if (amount.isEmpty()) {
                            Text(
                                text = stringResource(R.string.enter_amount),
                                color = Color.Gray
                            )
                        }
                        innerTextField()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                CustomMultipleImagePicker(
                    selectedUris = uriList,
                    onImagesSelected = { uriList = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (amount.isEmpty() || selectedCost == "Type Of Cost" || uriList.isEmpty()) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = false
                    ) {
                        Text(stringResource(R.string.other_expense), color = Color.White)
                    }
                } else {
                    Button(
                        onClick = {
                            if (isButtonClicked) return@Button
                            isButtonClicked = true
                            println("Saving Driver Log with: ${date.value}, $amount, $selectedCost,$selectedIndex, $uriList")
                            if (!isSaving) {
                                otherExpenseViewModel.saveOtherExpense(
                                    date = date.value.toString(),
                                    typeOfCost = selectedIndex.toString(),
                                    amount = amount,
                                    licensePlate = selectedVehicle,
                                    imageUris = uriList
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSaving && !isSaved && !isButtonClicked
                    ) {
                        if (isSaving) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.saving), color = Color.White)
                            }
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save", color = Color.White)
                        }
                    }

                }
            }
        }
    }

}