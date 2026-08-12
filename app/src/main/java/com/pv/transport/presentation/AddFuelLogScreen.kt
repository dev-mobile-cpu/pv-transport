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
import com.pv.transport.extension.findActivity
import com.pv.transport.ui.theme.FormPrimaryButton
import com.pv.transport.ui.theme.FormFieldLabel
import com.pv.transport.ui.theme.FormSelect
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.textPrimary
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.FuelViewModel
import kotlinx.coroutines.delay
import kotlin.text.isNotEmpty

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
                fuelViewModel.resetFuelLogState()
            }
            is FuelViewModel.FuelLogState.SavedOffline -> {
                isButtonClicked = false
                Toast.makeText(context, "Saved offline. Will sync when connected.", Toast.LENGTH_SHORT).show()
                isSaved = true
                delay(350)
                navController.popBackStack()
                fuelViewModel.resetFuelLogState()
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
                    FormFieldLabel(text = stringResource(R.string.date), icon = Icons.Default.DateRange)
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomDatePicker(
                        selectedDate = date,
                        onDateSelected = { fuelViewModel.addLogDate.value = it },
                        bgColor = white
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FormFieldLabel(text = stringResource(R.string.fuel_type), icon = Icons.Default.LocalGasStation)
                    Spacer(modifier = Modifier.height(4.dp))
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
                    FormFieldLabel(text = stringResource(R.string.fuel_station), icon = Icons.Default.LocationOn)
                    Spacer(modifier = Modifier.height(4.dp))

                    FormSelect(
                        selectedLabel = selectedFuelCompany,
                        options = fuelCompanyList.map { it.name },
                        onSelected = { index, _ ->
                            val company = fuelCompanyList.getOrNull(index) ?: return@FormSelect
                            fuelViewModel.addLogSelectedFuelCompany.value = company.name
                            fuelViewModel.addLogSelectedCompanyIndex.value = company.id
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FormFieldLabel(
                        text = "${stringResource(R.string.fuel_shop_name)} (Optional)",
                        icon = Icons.Default.Store
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomFuelTextField(
                        value = fuelShop,
                        hint = stringResource(R.string.enter_fuel_shop_name),
                        onValueChange = { fuelViewModel.addLogFuelShop.value = it },
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.fillMaxWidth()
                    )


                    Spacer(modifier = Modifier.height(16.dp))
                    FormFieldLabel(text = stringResource(R.string.payment_type), icon = Icons.Default.CreditCard)
                    Spacer(modifier = Modifier.height(4.dp))
                    FormSelect(
                        selectedLabel = selectedPayment,
                        options = payments,
                        onSelected = { _, status ->
                            fuelViewModel.addLogSelectedPayment.value = status
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FormFieldLabel(text = stringResource(R.string.fuel_amount), icon = Icons.Default.AttachMoney)
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomFuelTextField(
                        value = amount,
                        hint = "Enter Amount",
                        onValueChange = { fuelViewModel.addLogAmount.value = it },
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth(),
                        enableComma = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FormFieldLabel(text = stringResource(R.string.fuel_liter), icon = Icons.Default.LocalGasStation)
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomFuelTextField(
                        value = liter,
                        hint = "Enter Liter",
                        onValueChange = { fuelViewModel.addLogLiter.value = it },
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth(),
                        enableComma = false
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FormFieldLabel(text = stringResource(R.string.current_km), icon = Icons.Default.Speed)
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomFuelTextField(
                        value = currentKm,
                        hint = "Enter Current KM",
                        onValueChange = { fuelViewModel.addLogCurrentKm.value = it },
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth(),
                        enableComma = false
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FormFieldLabel(text = stringResource(R.string.current_km_image), icon = Icons.Default.PhotoCamera)
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomImagePicker(
                        imageUri = currentUri,
                        onImagePicked = { fuelViewModel.addLogCurrentUri.value = it },
                        enableGallery = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    FormFieldLabel(text = stringResource(R.string.voucher_image), icon = Icons.Default.Receipt)
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomMultipleImagePicker(
                        selectedUris = uriList,
                        onImagesSelected = { fuelViewModel.addLogUriList.value = it },
                        enableGallery = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val canSave = amount.isNotEmpty() && liter.isNotEmpty() && currentKm.isNotEmpty() && currentUri != null && uriList.isNotEmpty()

                    FormPrimaryButton(
                        text = stringResource(R.string.save),
                        onClick = {
                            if (isSaving) return@FormPrimaryButton
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
                        },
                        enabled = canSave && !isSaving,
                        isLoading = isSaving
                    )

                }
            }
        }
    }
}
