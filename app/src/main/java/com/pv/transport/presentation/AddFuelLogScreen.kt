package com.pv.transport.presentation

import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import com.pv.transport.data.fuel.FuelType
import com.pv.transport.extension.CustomDatePicker
import com.pv.transport.extension.CustomFuelTextField
import com.pv.transport.extension.CustomImagePicker
import com.pv.transport.extension.CustomMultipleImagePicker
import com.pv.transport.extension.FuelTypeDropDown
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.textPrimary
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.FuelViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFuelLogScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val fuelViewModel: FuelViewModel = if (activity != null) hiltViewModel(activity) else hiltViewModel()

    val authPrefs = AuthPrefs(context)
    val carPlateNo = authPrefs.getLicensePlate()
    val fuelTypeIdFromPrefs = authPrefs.getFuelTypeId()
    
    val fuelTypeState by fuelViewModel.state.collectAsState()
    val fuelLogState by fuelViewModel.fuelLogState.collectAsState()
    val fuelCompaniesState by fuelViewModel.fuelCompaniesState.collectAsState()

    // ViewModel-based states
    val amount by fuelViewModel.addLogAmount.collectAsState()
    val liter by fuelViewModel.addLogLiter.collectAsState()
    val currentKm by fuelViewModel.addLogCurrentKm.collectAsState()
    val fuelShop by fuelViewModel.addLogFuelShop.collectAsState()
    val selectedFuelType by fuelViewModel.addLogSelectedFuelType.collectAsState()
    val selectedFuelTypeId by fuelViewModel.addLogSelectedFuelTypeId.collectAsState()
    val date by fuelViewModel.addLogDate.collectAsState()
    val uriList by fuelViewModel.addLogUriList.collectAsState()
    val currentUri by fuelViewModel.addLogCurrentUri.collectAsState()
    val selectedPayment by fuelViewModel.addLogSelectedPayment.collectAsState()
    val selectedFuelCompany by fuelViewModel.addLogSelectedFuelCompany.collectAsState()
    val selectedCompanyIndex by fuelViewModel.addLogSelectedCompanyIndex.collectAsState()

    val fuelTypeList = remember { mutableStateListOf<FuelType>() }
    val fuelCompanyList = remember { mutableStateListOf<FuelCompany>() }
    
    var isSaved by remember { mutableStateOf(false) }
    val payments = listOf("Credit", "Cash")
    var expandedPayment by remember { mutableStateOf(false) }
    var expandedCompany by remember { mutableStateOf(false) }
    var isButtonClicked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (fuelCompaniesState is FuelViewModel.FuelCompaniesState.Idle) {
            fuelViewModel.getFuelCompanies()
        }
        if (fuelTypeState is FuelViewModel.FuelTypeState.Idle) {
            fuelViewModel.getFuelType()
        }
    }

    LaunchedEffect(fuelCompaniesState) {
        if (fuelCompaniesState is FuelViewModel.FuelCompaniesState.Success) {
            val companies = (fuelCompaniesState as FuelViewModel.FuelCompaniesState.Success).response.data
            fuelCompanyList.clear()
            fuelCompanyList.addAll(companies)
            if (selectedFuelCompany.isEmpty() && fuelCompanyList.isNotEmpty()) {
                fuelViewModel.addLogSelectedFuelCompany.value = fuelCompanyList[0].name
                fuelViewModel.addLogSelectedCompanyIndex.value = fuelCompanyList[0].id
            }
        }
    }

    LaunchedEffect(fuelTypeState) {
        if (fuelTypeState is FuelViewModel.FuelTypeState.Success) {
            val types = (fuelTypeState as FuelViewModel.FuelTypeState.Success).response.records
            fuelTypeList.clear()
            fuelTypeList.addAll(types)
            if (selectedFuelType.isEmpty() && fuelTypeList.isNotEmpty()) {
                if (fuelTypeIdFromPrefs == null) {
                    fuelViewModel.addLogSelectedFuelTypeId.value = fuelTypeList[0].id
                    fuelViewModel.addLogSelectedFuelType.value = fuelTypeList[0].name
                } else {
                    val selectedItem = fuelTypeList.find { it.id.toString() == fuelTypeIdFromPrefs }
                    selectedItem?.let {
                        fuelViewModel.addLogSelectedFuelTypeId.value = it.id
                        fuelViewModel.addLogSelectedFuelType.value = it.name
                    }
                }
            }
        }
    }

    val isSaving = fuelLogState is FuelViewModel.FuelLogState.Loading

    LaunchedEffect(fuelLogState) {
        when (val state = fuelLogState) {
            is FuelViewModel.FuelLogState.Success -> {
                isButtonClicked = false
                Toast.makeText(context, "Save successful", Toast.LENGTH_SHORT).show()
                isSaved = true
                delay(350)
                navController.popBackStack()
            }
            is FuelViewModel.FuelLogState.SavedOffline -> {
                isButtonClicked = false
                Toast.makeText(context, "Saved offline. Will sync when connected.", Toast.LENGTH_SHORT).show()
                isSaved = true
                delay(350)
                navController.popBackStack()
            }
            is FuelViewModel.FuelLogState.Error -> {
                isButtonClicked = false
                if (state.message == "Error: null") {
                    Toast.makeText(context, "Insufficient wallet balance for this fuel amount.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {}
        }
    }

    val clearForm = {
        fuelViewModel.clearAddFuelLog()
        if (fuelTypeList.isNotEmpty()) {
            fuelViewModel.addLogSelectedFuelType.value = fuelTypeList[0].name
            fuelViewModel.addLogSelectedFuelTypeId.value = fuelTypeList[0].id
        }
        if (fuelCompanyList.isNotEmpty()) {
            fuelViewModel.addLogSelectedFuelCompany.value = fuelCompanyList[0].name
            fuelViewModel.addLogSelectedCompanyIndex.value = fuelCompanyList[0].id
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Add Fuel Log",
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
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            if (fuelTypeState is FuelViewModel.FuelTypeState.Loading || fuelCompaniesState is FuelViewModel.FuelCompaniesState.Loading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF495057))
                        Text(stringResource(R.string.date), fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = Color(0xFF495057))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomDatePicker(
                        selectedDate = date,
                        onDateSelected = { fuelViewModel.addLogDate.value = it },
                        bgColor = white
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF495057))
                        Text(stringResource(R.string.fuel_type), fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = Color(0xFF495057))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FuelTypeDropDown(
                        types = fuelTypeList,
                        selectedType = selectedFuelType,
                        onTypeSelected = { index, type ->
                            fuelViewModel.addLogSelectedFuelTypeId.value = index
                            fuelViewModel.addLogSelectedFuelType.value = type
                        },
                        modifier = Modifier
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF495057))
                        Text(stringResource(R.string.fuel_station), fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = Color(0xFF495057))
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(white)
                                .clickable { expandedCompany = true }
                                .padding(horizontal = 12.dp, 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                selectedFuelCompany,
                                fontFamily = appFontFamily,
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
                            fuelCompanyList.forEach { company ->
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
                                        fuelViewModel.addLogSelectedFuelCompany.value = company.name
                                        fuelViewModel.addLogSelectedCompanyIndex.value = company.id
                                        expandedCompany = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Store, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF495057))
                        Text("${stringResource(R.string.fuel_shop_name)} (Optional)", fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = Color(0xFF495057))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomFuelTextField(
                        value = fuelShop,
                        hint = stringResource(R.string.enter_fuel_shop_name),
                        onValueChange = { fuelViewModel.addLogFuelShop.value = it },
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.fillMaxWidth()
                    )


                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF495057))
                        Text(stringResource(R.string.payment_type), fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = Color(0xFF495057))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(white)
                                .clickable { expandedPayment = true }
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
                            expanded = expandedPayment,
                            onDismissRequest = { expandedPayment = false },
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
                                        fuelViewModel.addLogSelectedPayment.value = status
                                        expandedPayment = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF495057))
                        Text(stringResource(R.string.fuel_amount), fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = Color(0xFF495057))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomFuelTextField(
                        value = amount,
                        hint = "Enter Amount",
                        onValueChange = { fuelViewModel.addLogAmount.value = it },
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF495057))
                        Text(stringResource(R.string.fuel_liter), fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = Color(0xFF495057))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomFuelTextField(
                        value = liter,
                        hint = "Enter Liter",
                        onValueChange = { fuelViewModel.addLogLiter.value = it },
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF495057))
                        Text(stringResource(R.string.current_km), fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = Color(0xFF495057))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomFuelTextField(
                        value = currentKm,
                        hint = "Enter Current KM",
                        onValueChange = { fuelViewModel.addLogCurrentKm.value = it },
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF495057))
                        Text(stringResource(R.string.current_km_image), fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = Color(0xFF495057))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomImagePicker(
                        imageUri = currentUri,
                        onImagePicked = { fuelViewModel.addLogCurrentUri.value = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF495057))
                        Text(stringResource(R.string.voucher_image), fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = Color(0xFF495057))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomMultipleImagePicker(
                        selectedUris = uriList,
                        onImagesSelected = { fuelViewModel.addLogUriList.value = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (amount.isEmpty() || liter.isEmpty() || currentKm.isEmpty() || currentUri == null || uriList.isEmpty()) {
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(50.dp),
                            enabled = false
                        ) {
                            Text(
                                stringResource(R.string.submit),
                                color = white,
                                fontFamily = appFontFamily,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                if (isButtonClicked) return@Button
                                isButtonClicked = true
                                if (!isSaving) {
                                   fuelViewModel.saveFuelLog(
                                        carPlateNo = carPlateNo.toString(),
                                        date = date.toString(),
                                        fuelCompanyId = selectedCompanyIndex.toString(),
                                        fuelShop = "$selectedFuelCompany $fuelShop",
                                        fuelTypeId = selectedFuelTypeId.toString(),
                                        fuelAmount = amount,
                                        fuelLiter = liter,
                                        files = uriList,
                                        currentKm = currentKm,
                                        currentKmPhoto = currentUri!!,
                                        walletBucket = selectedPayment.lowercase(),
                                        context = context
                                   )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorPrimary),
                            shape = RoundedCornerShape(50.dp),
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
                                    fontFamily = appFontFamily,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
