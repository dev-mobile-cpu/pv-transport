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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.data.fuel.FuelCompany
import com.pv.transport.data.fuel.FuelRequest
import com.pv.transport.data.fuel.FuelType
import com.pv.transport.data.log.AssignedVehicle
import com.pv.transport.extension.CustomDatePicker
import com.pv.transport.extension.CustomFuelTextField
import com.pv.transport.extension.CustomImagePicker
import com.pv.transport.extension.CustomMultipleImagePicker
import com.pv.transport.extension.FuelTypeDropDown
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.FuelViewModel
import com.pv.transport.viewmodels.OtherExpenseViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFuelLogScreen(navController: NavController,fuelViewModel: FuelViewModel = hiltViewModel(),otherExpenseViewModel: OtherExpenseViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val fuel = fuelViewModel.state.collectAsState()
    val fuelLogState = fuelViewModel.fuelLogState.collectAsState()
    val fuelTypeList = remember { mutableStateListOf<FuelType>() }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var selectedFuelType by remember { mutableStateOf("") }
    val date = remember { mutableStateOf(LocalDate.now())}
    var amount by remember { mutableStateOf("") }
    var liter by remember { mutableStateOf("") }
    var currentKm by remember { mutableStateOf("") }
    var fuelShop by remember { mutableStateOf("") }
    var uriList by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var currentUri by remember { mutableStateOf<Uri?>(null) }
    var isSaved by remember { mutableStateOf(false) }
    val payments = listOf("Credit", "Cash")
    var expanded by remember { mutableStateOf(false) }
    var selectedPayment by remember { mutableStateOf(payments[0]) }

    var expandedVehicle by remember { mutableStateOf(false) }
    val vehicles = otherExpenseViewModel.assignedVehicle.collectAsState()
    val vehicleList = remember { mutableStateListOf<AssignedVehicle>() }
    var selectedVehicle by remember { mutableStateOf("") }

    var expandedCompany by remember { mutableStateOf(false) }
    val fuelCompany = fuelViewModel.fuelCompaniesState.collectAsState()
    val fuelCompanyList = remember { mutableStateListOf<FuelCompany>() }
    var selectedFuelCompany by remember { mutableStateOf("") }
    var selectedCompanyIndex by remember { mutableIntStateOf(0) }


    when(val v = vehicles.value) {
        is OtherExpenseViewModel.AssignedVehicleState.Idle -> {
            otherExpenseViewModel.getAssignedVehicle()
            Text(text = "Loading vehicles...")
        }
        is OtherExpenseViewModel.AssignedVehicleState.Loading -> {
            CircularProgressIndicator()
        }
        is OtherExpenseViewModel.AssignedVehicleState.Success -> {
            vehicleList.clear()
            vehicleList.addAll(v.response.data)
            if (selectedVehicle.isEmpty() && vehicleList.isNotEmpty()) {
                selectedVehicle = vehicleList[0].licensePlate
            }
        }
        is OtherExpenseViewModel.AssignedVehicleState.Error -> {
            Text(text = "Error: ${v.message}")
        }
    }

    when(val v = fuelCompany.value) {
        is FuelViewModel.FuelCompaniesState.Idle -> {
            fuelViewModel.getFuelCompanies()
            Text(text = "Loading vehicles...")
        }
        is FuelViewModel.FuelCompaniesState.Loading -> {
            CircularProgressIndicator()
        }
        is FuelViewModel.FuelCompaniesState.Success -> {
            fuelCompanyList.clear()
            fuelCompanyList.addAll(v.response.data)
            if (selectedFuelCompany.isEmpty() && fuelCompanyList.isNotEmpty()) {
                selectedFuelCompany = fuelCompanyList[0].name
                selectedCompanyIndex = fuelCompanyList[0].id
            }
        }
        is FuelViewModel.FuelCompaniesState.Error -> {
            Text(text = "Error: ${v.message}")
        }
    }
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
    val isSaving = when (fuelLogState.value) {
        is FuelViewModel.FuelLogState.Loading -> true
        else -> false
    }

    LaunchedEffect(key1 = fuelLogState.value) {
        when (val state = fuelLogState.value) {
            is FuelViewModel.FuelLogState.Success -> {
                amount = ""
                liter = ""
                currentKm = ""
                selectedFuelType = ""
                selectedIndex = 0
                Toast.makeText(context, "Save successful", Toast.LENGTH_SHORT).show()
                isSaved = true
                delay(350)
                navController.popBackStack()
            }
            is FuelViewModel.FuelLogState.Error -> {
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
                        Text(text = "Add Fuel Log", color = Color.Black)
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

            Column(modifier = Modifier.padding(16.dp)) {
                Text("Date")
                Spacer(modifier = Modifier.height(4.dp))
                CustomDatePicker(
                    selectedDate = date.value,
                    onDateSelected = { date.value = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                // car plate no dropdown
                Box(modifier = Modifier) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(5.dp))
                            .background(white)
                            .clickable { expandedVehicle = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedVehicle)
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }

                    DropdownMenu(
                        expanded = expandedVehicle,
                        onDismissRequest = { expandedVehicle = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        vehicleList.forEach { vehicle ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(vehicle.licensePlate)

                                        if (vehicle.licensePlate == selectedVehicle) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedVehicle = vehicle.licensePlate
                                    expandedVehicle = false
                                }
                            )
                        }
                    }
                }
                // end of car plate no dropdown
                Spacer(modifier = Modifier.height(16.dp))
                Text("Fuel Type Id")
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
                Text("Fuel Shop")
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(5.dp))
                                .background(white)
                                .clickable { expandedCompany = true }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedFuelCompany)

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }

                        DropdownMenu(
                            expanded = expandedCompany,
                            onDismissRequest = { expandedCompany = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            fuelCompanyList.forEach {company ->

                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(company.name)
                                            if (company.name == selectedVehicle) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedVehicle = company.name
                                        selectedCompanyIndex = company.id
                                        expandedCompany = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    CustomFuelTextField(
                        value = fuelShop,
                        hint = "Enter Fuel Shop",
                        onValueChange = { fuelShop = it },
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
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
                Text("Fuel Amount")
                Spacer(modifier = Modifier.height(4.dp))
                CustomFuelTextField(
                    value = amount,
                    hint = "Enter Amount",
                    onValueChange = { amount = it },
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Fuel Liter")
                Spacer(modifier = Modifier.height(4.dp))
                CustomFuelTextField(
                    value = liter,
                    hint = "Enter Liter",
                    onValueChange = { liter = it },
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Current KM")
                Spacer(modifier = Modifier.height(4.dp))
                CustomFuelTextField(
                    value = currentKm,
                    hint = "Enter Current KM",
                    onValueChange = { currentKm = it },
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.start_km_image))
                Spacer(modifier = Modifier.height(4.dp))
                CustomImagePicker(
                    imageUri = currentUri,
                    onImagePicked = { currentUri = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                CustomMultipleImagePicker(
                    selectedUris = uriList,
                    onImagesSelected = { uriList = it }
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (amount.isEmpty() || liter.isEmpty() || currentKm.isEmpty()) {
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
                               fuelViewModel.saveFuelLog(
                                    carPlateNo = selectedVehicle,
                                    date = date.value.toString(),
                                    fuelCompanyId = selectedCompanyIndex.toString(),
                                    fuelShop = fuelShop,
                                    fuelTypeId = selectedIndex.toString(),
                                    fuelAmount = amount,
                                    fuelLiter = liter,
                                    files = uriList,
                                    currentKm = currentKm,
                                    currentKmPhoto = currentUri!!,
                                    walletBucket = selectedPayment.lowercase()
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