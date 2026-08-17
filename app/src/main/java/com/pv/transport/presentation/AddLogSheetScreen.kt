package com.pv.transport.presentation

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.extension.CustomDatePicker
import com.pv.transport.extension.CustomImagePicker
import com.pv.transport.ui.theme.AppToast
import com.pv.transport.ui.theme.FormPrimaryButton
import com.pv.transport.ui.theme.FormPrimaryButtonDefaults
import com.pv.transport.ui.theme.formScrollInsets
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.textPrimary
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.DriverLogViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLogSheetScreen(
    navController: NavController,
    driverLogViewModel: DriverLogViewModel = hiltViewModel()
){
    val logSheetState by driverLogViewModel.logSheetState.collectAsState()
    var startUri by remember { mutableStateOf<Uri?>(null) }
    val date = remember { mutableStateOf(LocalDate.now())}
    val context = LocalContext.current
    var isSaved by remember { mutableStateOf(false) }
    var isButtonClicked by remember { mutableStateOf(false) }
    val isSaving = isButtonClicked && logSheetState is DriverLogViewModel.LogSheetState.Loading

    LaunchedEffect(logSheetState) {
        if (!isButtonClicked) return@LaunchedEffect
        when (val state = logSheetState) {
            is DriverLogViewModel.LogSheetState.Success -> {
                isButtonClicked = false
                AppToast.show(context, context.getString(R.string.log_saved))
                isSaved = true
                delay(350)
                navController.popBackStack()
            }
            is DriverLogViewModel.LogSheetState.Error -> {
                isButtonClicked = false
                AppToast.show(context, context.getString(R.string.save_failed, state.message))
            }
            else -> {}
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.add_log_sheet),
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
                            ) {
                                //clearForm()
                            }
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
        ){
            Column(modifier = Modifier.padding(20.dp)){

                CustomDatePicker(
                    selectedDate = date.value,
                    onDateSelected = { date.value = it },
                    bgColor = white,
                    readOnly = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomImagePicker(
                    imageUri = startUri,
                    onImagePicked = { startUri = it }
                )

                Spacer(modifier = Modifier.height(24.dp))


                val canSave =  startUri != null

                FormPrimaryButton(
                    text = stringResource(R.string.save),
                    onClick = {
                        if (isSaving || isButtonClicked) return@FormPrimaryButton
                        val photoUri = startUri ?: return@FormPrimaryButton
                        isButtonClicked = true
                        driverLogViewModel.saveLogSheet(date.value.toString(), photoUri, context)
                    },
                    enabled = canSave && !isSaving && !isSaved && !isButtonClicked,
                    isLoading = isSaving
                )
                Spacer(modifier = Modifier.height(FormPrimaryButtonDefaults.SaveBottomSpace))

            }

        }
    }
}