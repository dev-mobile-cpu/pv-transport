package com.pv.transport.presentation

import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.data.fuel.FuelRequest
import com.pv.transport.data.fuel.FuelType
import com.pv.transport.extension.CustomFuelTextField
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
    // Persistence Fix: Scope ViewModel to Activity so it survives "Back"
    val activity = remember(context) { context.findActivity() }
    val fuelViewModel: FuelViewModel = if (activity != null) hiltViewModel(activity) else hiltViewModel()

    val fuelState by fuelViewModel.state.collectAsState()
    val fuelRequestState by fuelViewModel.requestState.collectAsState()
    
    val amount by fuelViewModel.addRequestAmount.collectAsState()
    val remark by fuelViewModel.addRequestRemark.collectAsState()
    val selectedFuelType by fuelViewModel.addRequestSelectedType.collectAsState()
    val selectedIndex by fuelViewModel.addRequestSelectedIndex.collectAsState()

    val fuelTypeList = remember { mutableStateListOf<FuelType>() }
    var isSaved by remember { mutableStateOf(false) }
    var isButtonClicked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (fuelState is FuelViewModel.FuelTypeState.Idle) {
            fuelViewModel.getFuelType()
        }
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
                Toast.makeText(context, "Save successful", Toast.LENGTH_SHORT).show()
                isSaved = true
                delay(350)
                navController.popBackStack()
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
                // UI Bug Fix: Remove extra white space (double padding)
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
            if (fuelState is FuelViewModel.FuelTypeState.Loading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    Spacer(modifier = Modifier.height(10.dp))

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

                    if (amount.isEmpty()) {
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp),
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
                                    fuelViewModel.saveFundRequest(
                                        FuelRequest(amount, selectedIndex.toString(), remark, "cash")
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorPrimary
                            ),
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

// Helper to find Activity for scoping
fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
