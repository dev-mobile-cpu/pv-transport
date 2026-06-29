package com.pv.transport.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.pv.transport.R
import com.pv.transport.data.log.ApproveDriverLogRequest
import com.pv.transport.data.log.CorporateUsersResponse
import com.pv.transport.data.log.GenerateQR
import com.pv.transport.data.log.GenerateQRUiState
import com.pv.transport.extension.CustomDatePicker
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.purple
import com.pv.transport.ui.theme.white
import com.pv.transport.ui.theme.yellow
import com.pv.transport.viewmodels.ApproveDriverLogViewModel
import com.pv.transport.viewmodels.GenerateQRViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import androidx.core.graphics.createBitmap
import com.pv.transport.extension.HandleBackPressWithDialog
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.backgroundColorApproved
import com.pv.transport.ui.theme.backgroundColorPending
import com.pv.transport.ui.theme.black
import com.pv.transport.ui.theme.checkColorApproved
import com.pv.transport.ui.theme.checkColorPending
import com.pv.transport.ui.theme.textColorPrimary
import com.pv.transport.ui.theme.textColorSecondary
import com.pv.transport.ui.theme.textSecondary

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ApprovalScreen(
    navController: NavController,
    viewModel: ApproveDriverLogViewModel = hiltViewModel(),
    generateQRViewModel: GenerateQRViewModel = hiltViewModel()
){
    val showExitDialog = remember { mutableStateOf(false) }
    val activity = LocalContext.current as Activity

    HandleBackPressWithDialog(
        onBackConfirmed = {
            activity.finish()
        },
        showDialog = showExitDialog
    )
    var startDate by rememberSaveable {
        mutableStateOf(LocalDate.now())
    }
    var endDate by rememberSaveable {
        mutableStateOf(LocalDate.now())
    }
    val approval by viewModel.approval.collectAsState()
    val uiState by generateQRViewModel.uiState.collectAsState()

    // Track selection state for each approval item
    val selectedItems = remember { mutableStateListOf<Int>() }
    val anySelected by remember {
        derivedStateOf { selectedItems.isNotEmpty() }
    }
    var showDialog by remember { mutableStateOf(false) }
    var showQRDialog by remember { mutableStateOf(false) }
    var qrData by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            println("Last visible item index: ${lastVisibleItem?.index}, Total items: ${listState.layoutInfo.totalItemsCount}")
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 5
        }
    }

    val socketData by generateQRViewModel.socketState.collectAsState()
    var isFirstSocketEmission by remember { mutableStateOf(true) }

    LaunchedEffect(socketData) {
        Log.d("ApprovalScreen", "Socket Effect Called: $socketData")
        if (isFirstSocketEmission) {
            isFirstSocketEmission = false
            return@LaunchedEffect
        }
        socketData?.let {
            Log.d("TOKEN", it.token)
            Log.d("DRIVER_ID", it.corporateDriverId.toString())
            showQRDialog = false
            viewModel.getApprovalStatus(startDate.toString(),endDate.toString(),"")
        }
    }

    LaunchedEffect(approval) {
        if (approval is ApproveDriverLogViewModel.ApprovalState.Success) {
            selectedItems.clear()
        } else {
            selectedItems.clear()
        }
    }

    LaunchedEffect(startDate,endDate,"") {
        Log.d("ApprovalScreen", "Date Effect Called")
        viewModel.getApprovalStatus(startDate.toString(),endDate.toString(),"")
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && approval is ApproveDriverLogViewModel.ApprovalState.Success) {
            val successState = approval as ApproveDriverLogViewModel.ApprovalState.Success
            if (!successState.isLoadingMore && successState.currentPage < successState.lastPage) {
                viewModel.loadMoreLogs(startDate.toString(), endDate.toString(),"")
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorSecondary),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ){
        item {
            Column(modifier = Modifier) {
                Text(
                    text = stringResource(R.string.approvals),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.pending_reviewed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FilterAlt, contentDescription = null)
                        Text(stringResource(R.string.filters), fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.start_date), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            CustomDatePicker(
                                selectedDate = startDate,
                                onDateSelected = { startDate = it },
                                bgColor = colorSecondary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.end_date), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            CustomDatePicker(
                                selectedDate = endDate,
                                onDateSelected = { endDate = it },
                                bgColor = colorSecondary
                            )
                        }
                    }
                }
            }
        }

        when (approval) {
            is ApproveDriverLogViewModel.ApprovalState.Success -> {
                val successState = approval as ApproveDriverLogViewModel.ApprovalState.Success
                val approvalList = successState.response
                val filterList = successState.response.filter { it.status == "pending" }

                val allSelected = selectedItems.size == filterList.size && filterList.isNotEmpty()
               if (approvalList.isNotEmpty()){
                   item {
                       Spacer(modifier = Modifier.height(20.dp))
                       Box(
                           modifier = Modifier
                               .fillMaxWidth()
                       ){
                           Row(
                               modifier = Modifier.align(Alignment.CenterStart),
                               verticalAlignment = Alignment.CenterVertically
                           ) {
                               Checkbox(
                                   checked = allSelected,
                                   onCheckedChange = { isChecked ->
                                       selectedItems.clear()
                                       if (isChecked) {
                                           selectedItems.addAll(
                                               approvalList
                                                   .filter { it.status == "pending" }
                                                   .map { it.id.toInt() }
                                           )
                                       }
                                       println("Selected all: ${selectedItems.size} / ${filterList.size}")
                                   }
                               )
                               Text(stringResource(R.string.select_all), fontWeight = FontWeight.SemiBold)
                           }
                           Button(
                               onClick = { showDialog = true },
                               colors = ButtonDefaults.buttonColors(containerColor = if (anySelected) colorPrimary else Color.Gray),
                               modifier = Modifier.align(Alignment.CenterEnd),
                               enabled = anySelected
                           ) { Text(text = stringResource(R.string.generate_qr)) }
                       }
                       Spacer(modifier = Modifier.height(16.dp))

                   }
               }
                if (approvalList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.no_approval_found),
                                fontFamily = appFontFamily ,
                                fontWeight = FontWeight.Normal,
                                color = textSecondary
                            )
                        }
                    }
                } else {
                    items(approvalList.size) { index ->

                        Card(
                            onClick = {
                                navController.currentBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("approval_detail", approvalList[index])

                                navController.navigate("approval_detail")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = white),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()
                                .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 18.dp,
                                bottom = 16.dp
                            )) {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {


                                      Row(
                                          modifier = Modifier.align(Alignment.CenterStart),
                                          verticalAlignment = Alignment.CenterVertically
                                      ){
                                          val itemId  = approvalList[index].id
                                          val checked = selectedItems.contains(itemId.toInt())

                                          println("Item ID: $itemId, Checked: $checked, Status: ${approvalList[index].status}")
                                          Box(
                                              modifier = Modifier.wrapContentSize()
                                          ) {

                                              if (approvalList[index].status == "pending") {
                                                  CompositionLocalProvider(
                                                      LocalMinimumInteractiveComponentSize provides 0.dp
                                                  ) {
                                                      Checkbox(
                                                          checked = checked,
                                                          onCheckedChange = { isChecked ->
                                                              if (isChecked) {
                                                                  selectedItems.add(itemId.toInt())
                                                                  println("Selected items add: ${selectedItems.size} / ${approvalList.size} /  $selectedItems")
                                                              } else {
                                                                  selectedItems.remove(itemId.toInt())
                                                                  println("Selected items remove: ${selectedItems.size} / ${approvalList.size}")
                                                              }
                                                          },
                                                          modifier = Modifier
                                                              .align(Alignment.TopStart)
                                                              .offset(y = 4.dp)
                                                              .size(20.dp),
                                                      )
                                                  }
                                              } else {
                                                  Spacer(modifier = Modifier.width(0.dp))
                                              }

                                              Spacer(modifier = Modifier.width(6.dp))

                                              Column(
                                                  modifier = Modifier.padding(start = 0.dp)
                                              ) {
                                                  Text(
                                                      approvalList[index].type.replaceFirstChar { it.uppercase() },
                                                      fontFamily = appFontFamily,
                                                      fontSize = 16.sp,
                                                      fontWeight = FontWeight.SemiBold,
                                                      modifier = Modifier.padding(start = if (approvalList[index].status == "pending") 32.dp else 0.dp)
                                                  )
                                                  Spacer(modifier = Modifier.height(4.dp))
                                                  Text(
                                                      text = approvalList[index].reason,
                                                      fontSize = 14.sp,
                                                      color = textColorSecondary
                                                  )
                                              }
                                          }


                                      }


                                    Box(
                                        modifier = Modifier.align(Alignment.TopEnd)
                                            .clip(RoundedCornerShape(8))
                                            .width(80.dp)
                                            .background(if (approvalList[index].status == "pending") backgroundColorPending else backgroundColorApproved)
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = approvalList[index].status.uppercase(),
                                            color = if (approvalList[index].status == "pending") checkColorPending else checkColorApproved,
                                            fontFamily = appFontFamily,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                val tightTextStyle = LocalTextStyle.current.copy(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Date",
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp).padding(bottom = 3.dp)
                                        )
                                        Text(
                                            text = approvalList[index].driverLog.date,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Normal,
                                            fontFamily = appFontFamily,
                                            color = Color.Black,
                                            style = tightTextStyle
                                        )
                                    }


                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {

                                        Icon(
                                            imageVector = Icons.Rounded.AccessTime,
                                            contentDescription = "Time",
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp).padding(bottom = 3.dp)
                                        )
                                        Text(
                                            text = "${approvalList[index].startTime} - ${approvalList[index].endTime}",
                                            fontSize = 13.sp,
                                            fontFamily = appFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.Black,
                                            style = tightTextStyle
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            stringResource(R.string.start_km),
                                            fontFamily = appFontFamily,
                                            color = textColorSecondary,
                                            fontSize = 12.sp
                                        )
                                        Text(approvalList[index].startKm,
                                            fontFamily = appFontFamily,
                                            fontSize = 18.sp,
                                            color = textColorPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column {
                                        Text(
                                            stringResource(R.string.end_km),
                                            fontFamily = appFontFamily,
                                            color = textColorSecondary,
                                            fontSize = 12.sp
                                        )
                                        Text("${approvalList[index].endKm}",
                                            fontFamily = appFontFamily,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColorPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }


                    if (successState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
            is ApproveDriverLogViewModel.ApprovalState.Loading -> {
            }
            is ApproveDriverLogViewModel.ApprovalState.Error -> {
            }

            else -> {}
        }
    }

    if (showDialog){
        GenerateQrDialog(
            show = true,
            viewModel = viewModel,
            selectedIds = selectedItems.toList(),
            onDismiss = { showDialog = false },
            onConfirm = { userId, userName, ids ->
                showDialog = false
                println("Selected IDs: $ids $userId $userName $ids")
                generateQRViewModel.generateQR(
                    GenerateQR(ids,userId,userName)
                )

            }
        )
    }

    LaunchedEffect(uiState) {
        if (uiState is GenerateQRUiState.Success) {
            qrData = (uiState as GenerateQRUiState.Success).qrResponse.qrUrl
            token = (uiState as GenerateQRUiState.Success).qrResponse.token
            showQRDialog = true
            generateQRViewModel.resetState()
        }
    }

    if (showQRDialog) {
        GenerateQRScreen(data = qrData, token = token ,viewModel,startDate,endDate, onFinish = {showQRDialog = false}, onDismiss = {showDialog = false} )
    }

}


@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateQrDialog(
    show: Boolean,
    viewModel: ApproveDriverLogViewModel = hiltViewModel(),
    selectedIds: List<Int>,
    onDismiss: () -> Unit,
    onConfirm: (userId: String, userName: String, selectedCount: List<Int>) -> Unit
) {
    if (!show) return

    val corporate = viewModel.corporateUsers.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    val userList = remember { mutableStateListOf<CorporateUsersResponse>() }
    var selectedUser by remember { mutableStateOf("") }
    var selectedUserId by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var searchText by remember {
        mutableStateOf(TextFieldValue(""))
    }
    LaunchedEffect(Unit) {
        viewModel.getCorporateUsers()
    }

    when (val s = corporate.value) {
        is ApproveDriverLogViewModel.CorporateUsersState.Loading -> {
            // CircularProgressIndicator()
        }

        is ApproveDriverLogViewModel.CorporateUsersState.Success -> {
            userList.clear()
            userList.addAll(s.response)
            if (selectedUser.isEmpty() && userList.isNotEmpty()) {
                selectedUser = userList[0].name
                selectedUserId = userList[0].id
            }

        }

        is ApproveDriverLogViewModel.CorporateUsersState.Error -> {
            Text(text = "Error: ${s.message}")
        }
    }

    val filteredUsers by remember {
        derivedStateOf {
            userList.filter {
                it.name.contains(searchText.text, ignoreCase = true)
            }
        }
    }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = ""
    )

    var textFieldSize by remember {
        mutableStateOf(Size.Zero)
    }

    val density = LocalDensity.current
    val lightGrayBorder = Color(0xFFE0E0E0) // Light border from photo
    val labelColor = Color(0xFF757575) // Gray for labels
    val placeholderColor = Color(0xFFBDBDBD) // Lighter gray for placeholders

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp), // Matched softer, broader card radius from photo
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp,16.dp, 16.dp,16.dp)
            ) {

                   Text(
                       text = stringResource(R.string.select_approval_user),
                       fontSize = 16.sp,
                       fontFamily = appFontFamily,
                       fontWeight = FontWeight.SemiBold,
                       color = textColorPrimary,
                       modifier = Modifier.fillMaxWidth(),
                       textAlign = TextAlign.Center
                   )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.approval_user),
                    fontFamily = appFontFamily,
                    fontSize = 16.sp,
                    color = labelColor
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            textFieldSize = coordinates.size.toSize()
                        }
                ) {
                    BasicTextField(
                        value = searchText,
                        onValueChange = {
                            searchText = it
                            expanded = it.text.isNotEmpty() && filteredUsers.isNotEmpty()
                        },
                        textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp) // More professional height matching photo
                                    .border(1.dp, lightGrayBorder, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1.0f)) {
                                    if (searchText.text.isEmpty()) {
                                        Text(
                                            text = "Search...",
                                            color = placeholderColor,
                                            fontFamily = appFontFamily,
                                            fontSize = 15.sp
                                        )
                                    }
                                    innerTextField()
                                }

                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = labelColor, // Standard grey
                                    modifier = Modifier
                                        .rotate(rotation)
                                        .clickable { expanded = !expanded }
                                        .size(24.dp)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = expanded && filteredUsers.isNotEmpty(),
                        onDismissRequest = { expanded = false },
                        properties = PopupProperties(focusable = true),
                        modifier = Modifier.width(with(density) { textFieldSize.width.toDp() })
                    ) {
                        filteredUsers.forEach { user ->
                            DropdownMenuItem(
                                text = { Text(user.name, fontFamily = appFontFamily, fontSize = 15.sp) },
                                onClick = {
                                    searchText = TextFieldValue(
                                        text = user.name,
                                        selection = TextRange(user.name.length)
                                    )
                                    userName = user.name
                                    selectedUserId = user.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp)) // Specific spacing from photo

                Text(
                    text = stringResource(R.string.corporate_user_name),
                    fontFamily = appFontFamily,
                    fontSize = 16.sp,
                    color = labelColor
                )
                BasicTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .border(1.dp, lightGrayBorder, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (userName.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.enter_corporate_user_name),
                                        fontFamily = appFontFamily,
                                        color = placeholderColor,
                                        fontSize = 15.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                if(userName.isEmpty()){
                    Button(
                        onClick = {
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text(
                            stringResource(R.string.text_continue),
                            fontFamily = appFontFamily ,
                            fontWeight = FontWeight.Normal
                        )
                    }

                }else{
                    println("$userName $selectedUserId $selectedIds ")
                    Button(
                        onClick = {
                            if (userName.isEmpty()){
                                userName = selectedUser
                            }
                            onConfirm(selectedUserId, userName, selectedIds)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorPrimary)
                    ) {
                        Text(
                            stringResource(R.string.text_continue),
                            fontFamily = appFontFamily ,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun GenerateQRScreen(
    data: String,
    token: String,
    logViewModel: ApproveDriverLogViewModel = hiltViewModel(),
    startDate: LocalDate,
    endDate: LocalDate,
    onFinish: () -> Unit,
    onDismiss: () -> Unit,
) {

    val textColor = Color.Black
    val grayTextColor = Color(0xFF808080)
    val inputBorderColor = Color(0xFFE0E0E0)
    val inputTextColor = Color(0xFFBDBDBD) // placeholder color
    val activeInputTextColor = Color.Black
    val buttonBgColor = Color(0xFFE0E0E0)
    val closeButtonBorderColor = Color(0xFFE0E0E0)
    val disabledTextColor = Color(0xFFB0B0B0)

    println("QR data: $data")

    val bitmap = remember(data) {
        generateQrBitmap(data)
    }
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val viewModel: ApproveDriverLogViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState()
    var isSaved by remember { mutableStateOf(false) }
    val isSaving = when (state.value) {
        is  ApproveDriverLogViewModel.ApproveDriverLogState.Loading -> true
        else -> false
    }
    LaunchedEffect(key1 = state.value) {
        when (val state = state.value) {
            is ApproveDriverLogViewModel.ApproveDriverLogState.Success -> {
                password = ""
                Toast.makeText(context, state.response.message, Toast.LENGTH_SHORT).show()
                isSaved = true
                delay(350)
                logViewModel.getApprovalStatus(startDate.toString(),endDate.toString(),"")
                onFinish()
            }
            is ApproveDriverLogViewModel.ApproveDriverLogState.Error -> {
                Toast.makeText(context, "Save failed: ${state.message}", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(16.dp), // Matched softer, broader card radius from photo
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp,16.dp, 16.dp,16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.generated_qr), fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Generated QR Code",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.scan_qr_approve),
                    fontFamily = appFontFamily,
                    style = TextStyle(
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.or),
                        style = TextStyle(
                            color = Color.Gray,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.enter_password_approve),
                    fontFamily = appFontFamily,
                    fontSize = 15.sp,
                    color = grayTextColor
                )

                Spacer(modifier = Modifier.height(14.dp))

                BasicTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp) // Set standard height
                        .border(width = 1.dp, color = inputBorderColor, shape = RoundedCornerShape(12.dp))
                        .background(color = Color.Transparent, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp),
                    textStyle = TextStyle(
                        color = activeInputTextColor,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(activeInputTextColor),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (password.isEmpty()) {
                                    Text(
                                        text = "Password",
                                        style = TextStyle(
                                            color = inputTextColor,
                                            fontSize = 16.sp
                                        )
                                    )
                                }
                                innerTextField()
                            }
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = grayTextColor,
                                modifier = Modifier
                                    .clickable { passwordVisible = !passwordVisible }
                                    .padding(start = 8.dp)
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))
                val finalButtonColor = if ( password.isNotEmpty()) grayTextColor else disabledTextColor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Button(
                        onClick = {onFinish()},
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = white, contentColor = textColor),
                        shape = RoundedCornerShape(12.dp),
                        elevation = null,
                        border = BorderStroke(width = 1.dp, color = closeButtonBorderColor)

                    ) {Text(stringResource(R.string.close)) }

                    if (password.isEmpty() ) {
                        Button(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonBgColor, contentColor = finalButtonColor),
                            shape = RoundedCornerShape(12.dp),
                            elevation = null ,
                            enabled = false
                        ) {
                            Text(stringResource(R.string.approve),
                                color = Color.White,
                                fontFamily = appFontFamily,
                                fontSize = 15.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                if (!isSaving) {
                                    viewModel.approveDriverLog(
                                        token = token,
                                        password = ApproveDriverLogRequest(password),
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorPrimary
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
                                    Text(
                                        stringResource(R.string.saving),
                                        color = Color.White,
                                        fontFamily = appFontFamily,
                                        fontSize = 15.sp
                                    )
                                }
                            } else {
                                Text(
                                    stringResource(R.string.approve),
                                    color = Color.White,
                                    fontFamily = appFontFamily,
                                    fontSize = 15.sp
                                )
                            }
                        }

                    }
                }
            }
        }
    }
}



@SuppressLint("UseKtx")
fun generateQrBitmap(text: String): Bitmap {

    val size = 1024
    val bits = MultiFormatWriter()
        .encode(text, BarcodeFormat.QR_CODE, size, size)

    val bitmap = createBitmap(size, size, Bitmap.Config.RGB_565)

    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(
                x,
                y,
                if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            )

        }
    }
    return bitmap
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewSplashScreen() {
    // You can preview the SplashScreen composable in Android Studio.
    // Replace 'NavController' and 'Context' with mock data for previewing purposes.
    ApprovalScreen(navController = rememberNavController())
}