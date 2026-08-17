package com.pv.transport.presentation

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.data.fuel.FuelRequest
import com.pv.transport.data.fuel.FuelType
import com.pv.transport.extension.CustomFuelTextField
import com.pv.transport.extension.CustomMultipleImagePicker
import com.pv.transport.extension.FuelTypeDropDown
import com.pv.transport.extension.findActivity
import com.pv.transport.ui.theme.FormPrimaryButton
import com.pv.transport.ui.theme.FormPrimaryButtonDefaults
import com.pv.transport.ui.theme.formScrollInsets
import com.pv.transport.ui.theme.FormFieldLabel
import com.pv.transport.ui.theme.FormSelect
import com.pv.transport.ui.theme.DotsLoading
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.textPrimary
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.FuelViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFuelRequestScreen(navController: NavController) {

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val fuelViewModel: FuelViewModel = if (activity != null) hiltViewModel(activity) else hiltViewModel()

    val fuelState by fuelViewModel.state.collectAsState()
    val fuelRequestState by fuelViewModel.requestState.collectAsState()
    val walletState by fuelViewModel.walletState.collectAsState()
    
    val category by fuelViewModel.addRequestCategory.collectAsState()
    val amount by fuelViewModel.addRequestAmount.collectAsState()
    val remark by fuelViewModel.addRequestRemark.collectAsState()
    val selectedFuelType by fuelViewModel.addRequestSelectedType.collectAsState()
    val selectedIndex by fuelViewModel.addRequestSelectedIndex.collectAsState()
    val selectedFiles by fuelViewModel.addRequestFiles.collectAsState()

    val fuelTypeList = remember { mutableStateListOf<FuelType>() }
    var isSaved by remember { mutableStateOf(false) }
    var isButtonClicked by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val totalDue = remember(walletState) {
        if (walletState is FuelViewModel.WalletState.Success) {
            val data = (walletState as FuelViewModel.WalletState.Success).response.data
            val cashDue = data.cash.due?.toDoubleOrNull() ?: 0.0
            val creditDue = data.credit.due?.toDoubleOrNull() ?: 0.0
            cashDue + creditDue
        } else 0.0
    }

    LaunchedEffect(Unit) {
        if (fuelState is FuelViewModel.FuelTypeState.Idle) {
            fuelViewModel.getFuelType()
        }
        fuelViewModel.getWalletBalance()
    }

    LaunchedEffect(fuelState) {
        if (fuelState is FuelViewModel.FuelTypeState.Success) {
            val records = (fuelState as FuelViewModel.FuelTypeState.Success).response.records
            fuelTypeList.clear()
            fuelTypeList.addAll(records)
            if (selectedFuelType.isEmpty() && fuelTypeList.isNotEmpty()) {
                fuelViewModel.addRequestSelectedType.value = fuelTypeList[0].name
                fuelViewModel.addRequestSelectedIndex.value = fuelTypeList[0].id
            }
        }
    }

    // The save state lives in the shared fuel view model, so only react to it while this
    // screen is the one waiting for its own save to finish.
    val isSaving = isButtonClicked && fuelRequestState is FuelViewModel.FuelRequestState.Loading

    LaunchedEffect(fuelRequestState) {
        if (!isButtonClicked) return@LaunchedEffect
        when (fuelRequestState) {
            is FuelViewModel.FuelRequestState.Success -> {
                isButtonClicked = false
                showSuccessDialog = true
                isSaved = true

            }
            is FuelViewModel.FuelRequestState.Error -> {
                isButtonClicked = false
                val error = (fuelRequestState as FuelViewModel.FuelRequestState.Error).message
                Toast.makeText(context, "Save failed: $error", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    val clearForm = {
        fuelViewModel.clearAddFuelRequest()
        if (fuelTypeList.isNotEmpty()) {
            fuelViewModel.addRequestSelectedType.value = fuelTypeList[0].name
            fuelViewModel.addRequestSelectedIndex.value = fuelTypeList[0].id
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.confirm_submission),
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to submit this ${if (category == "fuel_request") "Fuel Request" else "Due Request"} for ${amount} MMK?",
                    fontFamily = appFontFamily,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        isButtonClicked = true
                        val request = FuelRequest(
                            requestCategory = category,
                            amount = amount,
                            fuelTypeId = if (category == "fuel_request") selectedIndex.toString() else null,
                            remark = remark,
                            requestType = if (category == "fuel_request") "cash" else null
                        )
                        fuelViewModel.saveFundRequest(request, selectedFiles)
                    }
                ) {
                    Text("Confirm", color = colorPrimary, fontFamily = appFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = Color.Gray, fontFamily = appFontFamily)
                }
            },
            containerColor = white,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss */ },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                        fuelViewModel.resetFuelRequestState()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorPrimary),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Text("OK", color = white, fontFamily = appFontFamily)
                }
            },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFFE8F5E9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Success!",
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Text(
                    text = "Your ${if (category == "fuel_request") "fuel request" else "due request"} has been submitted successfully.",
                    fontFamily = appFontFamily,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            containerColor = white,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.add_fuel_request),
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
                        text = stringResource(R.string.clear),
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
                .formScrollInsets(innerPadding)
        ) {
            if (fuelState is FuelViewModel.FuelTypeState.Loading || (walletState is FuelViewModel.WalletState.Loading && category == "due_request")) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    DotsLoading()
                }
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    Spacer(modifier = Modifier.height(10.dp))

                    FormFieldLabel(text = stringResource(R.string.request_category), icon = Icons.Default.Category)
                    Spacer(modifier = Modifier.height(4.dp))

                    val categoryOptions = listOf("Fuel Request", "Due Request")
                    val categoryLabels = mapOf(
                        "fuel_request" to "Fuel Request",
                        "due_request" to "Due Request"
                    )
                    FormSelect(
                        selectedLabel = categoryLabels[category] ?: "Fuel Request",
                        options = categoryOptions,
                        onSelected = { _, label ->
                            fuelViewModel.addRequestCategory.value =
                                if (label == "Due Request") "due_request" else "fuel_request"
                        }
                    )

                    val amountNum = amount.toDoubleOrNull() ?: 0.0
                    val isExceedingDue = category == "due_request" && amountNum > totalDue

                    if (category == "due_request" && totalDue <= 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp)).padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            Text(
                                text = "You don't have any due amount to request.",
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontFamily = appFontFamily
                            )
                        }
                    } else if (category == "due_request") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Available Due: ${totalDue.toInt()} MMK",
                            color = if (isExceedingDue) Color.Red else colorPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        if (isExceedingDue) {
                            Text(
                                text = "Request amount cannot exceed available due.",
                                color = Color.Red,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(
                        visible = category == "fuel_request",
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
                        Column {
                            FormFieldLabel(text = stringResource(R.string.fuel_type), icon = Icons.Default.LocalGasStation)
                            Spacer(modifier = Modifier.height(4.dp))

                            FuelTypeDropDown(
                                types = fuelTypeList,
                                selectedType = selectedFuelType,
                                onTypeSelected = { index, type ->
                                    fuelViewModel.addRequestSelectedIndex.value = index
                                    fuelViewModel.addRequestSelectedType.value = type
                                },
                                modifier = Modifier
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    FormFieldLabel(text = stringResource(R.string.request_amount), icon = Icons.Default.AttachMoney)
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomFuelTextField(
                        value = amount,
                        hint = "Enter Amount",
                        onValueChange = { fuelViewModel.addRequestAmount.value = it },
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth(),
                        enableComma = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    FormFieldLabel(
                        text = stringResource(R.string.remark) + " (Optional)",
                        icon = Icons.Default.Edit
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomFuelTextField(
                        value = remark,
                        hint = "Remark",
                        onValueChange = { fuelViewModel.addRequestRemark.value = it },
                        keyboardType = KeyboardType.Text,
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth(),
                        enableComma = false
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(
                        visible = category == "due_request",
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
                        Column {
                            FormFieldLabel(text = "Proof (Optional)", icon = Icons.Default.AttachFile)
                            Spacer(modifier = Modifier.height(4.dp))
                            CustomMultipleImagePicker(
                                selectedUris = selectedFiles,
                                onImagesSelected = { fuelViewModel.addRequestFiles.value = it }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    val isFormValid = if (category == "due_request") {
                        amount.isNotEmpty() && totalDue > 0 && amountNum <= totalDue
                    } else {
                        amount.isNotEmpty()
                    }

                    FormPrimaryButton(
                        text = stringResource(R.string.save),
                        onClick = {
                            if (!isFormValid || isButtonClicked) return@FormPrimaryButton
                            showConfirmDialog = true
                        },
                        enabled = !isSaving && !isSaved && !isButtonClicked && isFormValid,
                        isLoading = isSaving
                    )
                    Spacer(modifier = Modifier.height(FormPrimaryButtonDefaults.SaveBottomSpace))
                }
            }
        }
    }
}
