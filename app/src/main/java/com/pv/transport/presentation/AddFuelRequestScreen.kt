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

    val isSaving = fuelRequestState is FuelViewModel.FuelRequestState.Loading

    LaunchedEffect(fuelRequestState) {
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
                    text = "Confirm Submission",
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
            if (fuelState is FuelViewModel.FuelTypeState.Loading || (walletState is FuelViewModel.WalletState.Loading && category == "due_request")) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorPrimary)
                }
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF495057))
                        Text("Request Category", fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = Color(0xFF495057))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    var categoryExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(white)
                                .clickable { categoryExpanded = true }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (category == "fuel_request") "Fuel Request" else "Due Request",
                                fontFamily = appFontFamily,
                                color = Color.Black
                            )
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
                        }
                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f).background(white)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Fuel Request", fontFamily = appFontFamily) },
                                onClick = {
                                    fuelViewModel.addRequestCategory.value = "fuel_request"
                                    categoryExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Due Request", fontFamily = appFontFamily) },
                                onClick = {
                                    fuelViewModel.addRequestCategory.value = "due_request"
                                    categoryExpanded = false
                                }
                            )
                        }
                    }

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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF495057))
                                Text(stringResource(R.string.fuel_type), fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = Color(0xFF495057))
                            }
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF495057))
                        Text(stringResource(R.string.request_amount), fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = Color(0xFF495057))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomFuelTextField(
                        value = amount,
                        hint = "Enter Amount",
                        onValueChange = { fuelViewModel.addRequestAmount.value = it },
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF495057))
                        Text(stringResource(R.string.remark) + " (Optional)", fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = Color(0xFF495057))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomFuelTextField(
                        value = remark,
                        hint = "Remark",
                        onValueChange = { fuelViewModel.addRequestRemark.value = it },
                        keyboardType = KeyboardType.Text,
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(
                        visible = category == "due_request",
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF495057))
                                Text("Proof (Optional)", fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = Color(0xFF495057))
                            }
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

                    Button(
                        onClick = {
                            if (!isFormValid || isButtonClicked) return@Button
                            showConfirmDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFormValid) colorPrimary else Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSaving && !isSaved && !isButtonClicked && isFormValid
                    ) {
                        if (isSaving) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Processing...", color = Color.White, fontFamily = appFontFamily, fontSize = 16.sp)
                            }
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.submit),
                                color = white,
                                fontFamily = appFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
