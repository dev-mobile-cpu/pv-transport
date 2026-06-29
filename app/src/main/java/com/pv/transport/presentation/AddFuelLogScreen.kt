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
import androidx.compose.ui.text.font.FontWeight
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
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.FuelViewModel
import com.pv.transport.viewmodels.OtherExpenseViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFuelLogScreen(
     navController: NavController,
     fuelViewModel: FuelViewModel = hiltViewModel(),
     otherExpenseViewModel: OtherExpenseViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val authPrefs = AuthPrefs(context)
    val carPlateNo = authPrefs.getLicensePlate()
    val fuelTypeId = authPrefs.getFuelTypeId()
    val fuel = fuelViewModel.state.collectAsState()
    val fuelLogState = fuelViewModel.fuelLogState.collectAsState()
    val fuelTypeList = remember { mutableStateListOf<FuelType>() }
    var selectedFuelTypeId by remember { mutableIntStateOf(0) }
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
    var expandedCompany by remember { mutableStateOf(false) }
    val fuelCompany = fuelViewModel.fuelCompaniesState.collectAsState()
    val fuelCompanyList = remember { mutableStateListOf<FuelCompany>() }
    var selectedFuelCompany by remember { mutableStateOf("") }
    var selectedCompanyIndex by remember { mutableIntStateOf(0) }
    var isButtonClicked by remember { mutableStateOf(false) }


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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is FuelViewModel.FuelTypeState.Success -> {
            fuelTypeList.clear()
            fuelTypeList.addAll(s.response.records)
            if (selectedFuelType.isEmpty() && fuelTypeList.isNotEmpty()) {
                if (fuelTypeId == null) {
                    selectedFuelTypeId = fuelTypeList[0].id
                    selectedFuelType = fuelTypeList[0].name

                } else {
                    val selectedItem = fuelTypeList.find {
                        it.id.toString() == fuelTypeId
                    }

                    selectedItem?.let {
                        selectedFuelTypeId = it.id
                        selectedFuelType = it.name
                    }
                }

                println(
                    "FuelType = $selectedFuelType, ID = $selectedFuelTypeId"
                )

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
                isButtonClicked = false
                amount = ""
                liter = ""
                currentKm = ""
                selectedFuelType = ""
                selectedFuelTypeId = 0
                Toast.makeText(context, "Save successful", Toast.LENGTH_SHORT).show()
                isSaved = true
                delay(350)
                navController.popBackStack()
            }
            is FuelViewModel.FuelLogState.Error -> {
                isButtonClicked = false
                if (state.message == "Error: null"){
                    Toast.makeText(
                        context,
                         "Insufficient wallet balance for this fuel amount.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                 else {
                    Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_SHORT).show()
                }
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
                Text(
                    stringResource(R.string.date),
                    fontFamily = appFontFamily ,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                CustomDatePicker(
                    selectedDate = date.value,
                    onDateSelected = { date.value = it },
                    bgColor = white

                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                   stringResource(R.string.fuel_type),
                    fontFamily = appFontFamily ,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(16.dp))
                FuelTypeDropDown(
                    types = fuelTypeList,
                    selectedType = selectedFuelType,
                    onTypeSelected = { index, type ->
                        selectedFuelTypeId = index
                        selectedFuelType = type
                    },
                    modifier = Modifier
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.fuel_station),
                    fontFamily = appFontFamily ,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(white)
                            .clickable { expandedCompany = true }
                            .padding(horizontal = 12.dp,10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            selectedFuelCompany,
                            fontFamily = appFontFamily ,
                            fontWeight = FontWeight.Normal
                        )

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
                                        if (company.name == selectedFuelCompany) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedFuelCompany = company.name
                                    selectedCompanyIndex = company.id
                                    expandedCompany = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "${stringResource(R.string.fuel_shop_name)} (Optional)",
                    fontFamily = appFontFamily ,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                CustomFuelTextField(
                    value = fuelShop,
                    hint = stringResource(R.string.enter_fuel_shop_name),
                    onValueChange = { fuelShop = it },
                    keyboardType = KeyboardType.Text,
                    modifier = Modifier.fillMaxWidth()
                )


                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.payment_type),
                    fontFamily = appFontFamily ,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(white)
                            .clickable { expanded = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
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
                Text(
                    stringResource(R.string.fuel_amount),
                    fontFamily = appFontFamily ,
                    fontWeight = FontWeight.Normal,
                )
                Spacer(modifier = Modifier.height(4.dp))
                CustomFuelTextField(
                    value = amount,
                    hint = "Enter Amount",
                    onValueChange = { amount = it },
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.fuel_liter),
                    fontFamily = appFontFamily ,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                CustomFuelTextField(
                    value = liter,
                    hint = "Enter Liter",
                    onValueChange = { liter = it },
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.current_km),
                    fontFamily = appFontFamily ,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                CustomFuelTextField(
                    value = currentKm,
                    hint = "Enter Current KM",
                    onValueChange = { currentKm = it },
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.current_km_image),
                    fontFamily = appFontFamily ,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                CustomImagePicker(
                    imageUri = currentUri,
                    onImagePicked = { currentUri = it }
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    stringResource(R.string.voucher_image),
                    fontFamily = appFontFamily ,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                CustomMultipleImagePicker(
                    selectedUris = uriList,
                    onImagesSelected = { uriList = it }
                )
                Spacer(modifier = Modifier.height(16.dp))

                println("currentKmPhoto = $currentUri, files = $uriList")
                if (amount.isEmpty() || liter.isEmpty() || currentKm.isEmpty() || currentUri == null || uriList.isEmpty()) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = false
                    ) {
                        Text(
                            stringResource(R.string.submit),
                            color = white,
                            fontFamily = appFontFamily ,
                            fontWeight = FontWeight.Normal
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            println("Submitting fuel log with: carPlateNo=$carPlateNo, date=${date.value}, fuelCompanyId=$selectedCompanyIndex, fuelShop=$selectedFuelCompany $fuelShop, fuelTypeId=$selectedFuelTypeId, fuelAmount=$amount, fuelLiter=$liter, files=$uriList, currentKm=$currentKm, currentKmPhoto=$currentUri, walletBucket=${selectedPayment.lowercase()}")
                            if (isButtonClicked) return@Button
                            isButtonClicked = true
                            if (!isSaving) {
                               fuelViewModel.saveFuelLog(
                                    carPlateNo = carPlateNo.toString(),
                                    date = date.value.toString(),
                                    fuelCompanyId = selectedCompanyIndex.toString(),
                                    fuelShop = "$selectedFuelCompany $fuelShop",
                                    fuelTypeId = selectedFuelTypeId.toString(),
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
                            Text(
                                stringResource(R.string.submit),
                                color = white,
                                fontFamily = appFontFamily ,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

            }
        }
    }
}