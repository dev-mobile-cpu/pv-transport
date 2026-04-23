package com.pv.transport.presentation

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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.data.fuel.FuelRequest
import com.pv.transport.data.fuel.FuelType
import com.pv.transport.extension.CustomFuelTextField
import com.pv.transport.extension.FuelTypeDropDown
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.FuelViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFuelRequestScreen(navController: NavController,fuelViewModel: FuelViewModel = hiltViewModel()){

    val context = LocalContext.current
    val fuel = fuelViewModel.state.collectAsState()
    val fuelRequestState = fuelViewModel.requestState.collectAsState()
    var amount by remember { mutableStateOf("") }
    val fuelTypeList = remember { mutableStateListOf<FuelType>() }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var selectedFuelType by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }
    var isSaved by remember { mutableStateOf(false) }
    val payments = listOf("Credit", "Cash")
    var expanded by remember { mutableStateOf(false) }
    var selectedPayment by remember { mutableStateOf(payments[0]) }

    when (val s = fuel.value) {
        is FuelViewModel.FuelTypeState.Idle -> {
            fuelViewModel.getFuelType()
            Text(text = "Loading reasons...")
        }

        is FuelViewModel.FuelTypeState.Loading -> {
            CircularProgressIndicator()
        }

        is FuelViewModel.FuelTypeState.Success -> {
            fuelTypeList.clear()
            fuelTypeList.addAll(s.response.records)
            if (selectedFuelType.isEmpty() && fuelTypeList.isNotEmpty()) {
                selectedFuelType = fuelTypeList[0].name
                selectedIndex = fuelTypeList[0].id
            }
        }

        is FuelViewModel.FuelTypeState.Error -> {
            Text(text = "Error: ${s.message}")
        }
    }

    val isSaving = when (fuelRequestState.value) {
        is FuelViewModel.FuelRequestState.Loading -> true
        else -> false
    }

    LaunchedEffect(key1 = fuelRequestState.value) {
        when (val state = fuelRequestState.value) {
            is FuelViewModel.FuelRequestState.Success -> {
                amount = ""
                remark = ""
                selectedFuelType = ""
                selectedIndex = 0
                Toast.makeText(context, "Save successful", Toast.LENGTH_SHORT).show()
                isSaved = true
                delay(350)
                navController.popBackStack()
            }
            is FuelViewModel.FuelRequestState.Error -> {
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
                        Text(text = "Add Fuel Request", color = Color.Black)
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
        ) {

            Column(modifier = Modifier.padding(16.dp)){
                Spacer(modifier = Modifier.height(10.dp))

                Box(modifier = Modifier) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(5.dp))
                            .background(white)
                            .clickable { expanded = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedPayment)

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        payments.forEach { status ->

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(status)

                                        if (status == selectedPayment) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedPayment = status
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                CustomFuelTextField(
                    value = amount,
                    hint = "Enter Amount",
                    onValueChange = { amount = it },
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                FuelTypeDropDown(
                    types = fuelTypeList,
                    selectedType = selectedFuelType,
                    onTypeSelected = { index, type ->
                        selectedIndex = index
                        selectedFuelType = type
                    },
                    modifier = Modifier
                )
                Spacer(modifier = Modifier.height(16.dp))

                CustomFuelTextField(
                    value = remark,
                    hint = "Remark (optional)",
                    onValueChange = { remark = it },
                    keyboardType = KeyboardType.Text,
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()

                )
                Spacer(modifier = Modifier.height(16.dp))

                if (amount.isEmpty() || remark.isEmpty() || remark.isEmpty()) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        enabled = false
                    ) {
                        Text("Submit", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = {
                            if (!isSaving) {
                                fuelViewModel.saveFundRequest(
                                    FuelRequest(amount,selectedIndex.toString(),remark,selectedPayment.lowercase())
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSaving && !isSaved
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
                            Text("Submit", color = Color.White)
                        }
                    }
                }
            }
        }
    }

}