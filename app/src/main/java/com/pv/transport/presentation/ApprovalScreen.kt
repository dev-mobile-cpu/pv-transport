package com.pv.transport.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.pv.transport.R
import com.pv.transport.data.log.CorporateUsersResponse
import com.pv.transport.data.log.GenerateQR
import com.pv.transport.data.log.GenerateQRUiState
import com.pv.transport.extension.CustomDatePicker
import com.pv.transport.extension.HandleBackPressWithDialog
import com.pv.transport.network.NetworkUtils
import com.pv.transport.ui.theme.*
import com.pv.transport.viewmodels.ApproveDriverLogViewModel
import com.pv.transport.viewmodels.GenerateQRViewModel
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

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
        onBackConfirmed = { activity.finish() },
        showDialog = showExitDialog
    )
    var startDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var endDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    val approval by viewModel.approval.collectAsState()
    val uiState by generateQRViewModel.uiState.collectAsState()

    val selectedItems = remember { mutableStateListOf<Int>() }
    val anySelected by remember { derivedStateOf { selectedItems.isNotEmpty() } }
    var showDialog by remember { mutableStateOf(false) }
    var showQRDialog by remember { mutableStateOf(false) }
    var qrData by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 5
        }
    }

    val socketData by generateQRViewModel.socketState.collectAsState()
    var isFirstSocketEmission by remember { mutableStateOf(true) }

    LaunchedEffect(socketData) {
        if (isFirstSocketEmission) {
            isFirstSocketEmission = false
            return@LaunchedEffect
        }
        socketData?.let {
            showQRDialog = false
            viewModel.getApprovalStatus(startDate.toString(),endDate.toString(),"")
        }
    }

    LaunchedEffect(approval) {
        selectedItems.clear()
    }

    LaunchedEffect(startDate,endDate) {
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
        modifier = Modifier.fillMaxSize().background(colorSecondary),
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ){
        item {
            Column {
                Text(text = stringResource(R.string.approvals), color = textPrimary, fontSize = 20.sp, fontFamily = appFontFamily, fontWeight = FontWeight.SemiBold)
                Text(text = stringResource(R.string.pending_reviewed), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }

        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterAlt, contentDescription = null)
                        Text(stringResource(R.string.filters), fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.start_date), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            CustomDatePicker(selectedDate = startDate, onDateSelected = { startDate = it }, bgColor = colorSecondary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.end_date), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            CustomDatePicker(selectedDate = endDate, onDateSelected = { endDate = it }, bgColor = colorSecondary)
                        }
                    }
                }
            }
        }

        when (val currentApproval = approval) {
            is ApproveDriverLogViewModel.ApprovalState.Success -> {
                val approvalList = currentApproval.response
                val filterList = approvalList.filter { it.status == "pending" }
                val allSelected = selectedItems.size == filterList.size && filterList.isNotEmpty()
               if (approvalList.isNotEmpty()){
                   item {
                       Spacer(modifier = Modifier.height(8.dp))
                       Card(
                           shape = RoundedCornerShape(12.dp),
                           colors = CardDefaults.cardColors(containerColor = white),
                           elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                       ) {
                           Row(
                               modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp),
                               verticalAlignment = Alignment.CenterVertically,
                               horizontalArrangement = Arrangement.SpaceBetween
                           ) {
                               Row(verticalAlignment = Alignment.CenterVertically) {
                                   CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                                       Checkbox(
                                           checked = allSelected,
                                           onCheckedChange = { isChecked ->
                                               selectedItems.clear()
                                               if (isChecked) selectedItems.addAll(filterList.map { it.id.toInt() })
                                           },
                                           colors = CheckboxDefaults.colors(
                                               checkedColor = colorPrimary,
                                               uncheckedColor = Color.Gray
                                           )
                                       )
                                   }
                                   Spacer(modifier = Modifier.width(4.dp))
                                   Text(stringResource(R.string.select_all), fontWeight = FontWeight.SemiBold, fontFamily = appFontFamily, fontSize = 14.sp)
                               }
                               Button(
                                   onClick = {
                                       if (!NetworkUtils.isInternetAvailable(activity)) Toast.makeText(activity, "Active internet connection required.", Toast.LENGTH_SHORT).show()
                                       else showDialog = true
                                   },
                                   colors = ButtonDefaults.buttonColors(containerColor = if (anySelected) colorPrimary else Color.Gray),
                                   shape = RoundedCornerShape(50.dp),
                                   enabled = anySelected,
                                   contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                               ) {
                                   Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                                   Spacer(modifier = Modifier.width(8.dp))
                                   Text(text = stringResource(R.string.generate_qr), fontSize = 13.sp, fontFamily = appFontFamily)
                               }
                           }
                       }
                   }
               }
                if (approvalList.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.no_approval_found), fontFamily = appFontFamily, fontWeight = FontWeight.Normal, color = textSecondary)
                        }
                    }
                } else {
                    items(approvalList.size, key = { index -> approvalList[index].id }) { index ->
                        Card(
                            onClick = {
                                navController.currentBackStackEntry?.savedStateHandle?.set("approval_detail", approvalList[index])
                                navController.navigate("approval_detail")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = white),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                      Row(modifier = Modifier.align(Alignment.CenterStart), verticalAlignment = Alignment.Top){
                                          val itemId  = approvalList[index].id
                                          val checked = selectedItems.contains(itemId.toInt())
                                          if (approvalList[index].status == "pending") {
                                              CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                                                  Checkbox(
                                                      checked = checked,
                                                      onCheckedChange = { isChecked ->
                                                          if (isChecked) selectedItems.add(itemId.toInt()) else selectedItems.remove(itemId.toInt())
                                                      },
                                                      modifier = Modifier.size(20.dp).offset(y = (7).dp),
                                                      colors = CheckboxDefaults.colors(checkedColor = colorPrimary, uncheckedColor = Color.Gray)
                                                  )
                                              }
                                          }
                                          Spacer(modifier = Modifier.width(if (approvalList[index].status == "pending") 12.dp else 0.dp))
                                          Column {
                                              Text(approvalList[index].type.replaceFirstChar { it.uppercase() }, fontFamily = appFontFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                          }
                                      }
                                    Box(
                                        modifier = Modifier.align(Alignment.TopEnd).width(80.dp)
                                            .background(if (approvalList[index].status == "pending") backgroundColorPending else backgroundColorApproved, shape = RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(text = approvalList[index].status.uppercase(), color = if (approvalList[index].status == "pending") checkColorPending else checkColorApproved, fontFamily = appFontFamily, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(text = approvalList[index].reason, fontSize = 14.sp, color = textColorSecondary)
                                }
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(imageVector = Icons.Default.DateRange, contentDescription = "Date", modifier = Modifier.size(16.dp))
                                        Text(text = approvalList[index].driverLog!!.date, fontSize = 13.sp, fontFamily = appFontFamily)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(imageVector = Icons.Rounded.AccessTime, contentDescription = "Time", modifier = Modifier.size(16.dp))
                                        Text(text = "${approvalList[index].startTime} - ${approvalList[index].endTime}", fontSize = 13.sp, fontFamily = appFontFamily)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(stringResource(R.string.start_km), fontFamily = appFontFamily, color = textColorSecondary, fontSize = 12.sp)
                                        Text(approvalList[index].startKm, fontFamily = appFontFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text(stringResource(R.string.end_km), fontFamily = appFontFamily, color = textColorSecondary, fontSize = 12.sp)
                                        Text("${approvalList[index].endKm}", fontFamily = appFontFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    if (currentApproval.isLoadingMore) {
                        item { Box(modifier = Modifier.fillParentMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                    }
                }
            }
            is ApproveDriverLogViewModel.ApprovalState.Loading -> {
                item { Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            }
            is ApproveDriverLogViewModel.ApprovalState.Error -> {
                item { Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text(text = currentApproval.message, color = Color.Red) } }
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
            onConfirm = { userId: String, userName: String, ids: List<Int> ->
                showDialog = false
                generateQRViewModel.generateQR(GenerateQR(ids,userId,userName))
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
        GenerateQRScreen(data = qrData, token = token ,viewModel,startDate,endDate, onFinish = {showQRDialog = false}, onDismiss = {showQRDialog = false} )
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
    var selectedTabIndex by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val viewModel: ApproveDriverLogViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val isSaving = state is ApproveDriverLogViewModel.ApproveDriverLogState.Loading

    var pinValue by remember { mutableStateOf("") }
    val signaturePaths = remember { mutableStateListOf<Path>() }

    LaunchedEffect(state) {
        if (state is ApproveDriverLogViewModel.ApproveDriverLogState.Success) {
            Toast.makeText(context, (state as ApproveDriverLogViewModel.ApproveDriverLogState.Success).response.message, Toast.LENGTH_SHORT).show()
            logViewModel.getApprovalStatus(startDate.toString(), endDate.toString(), "")
            onFinish()
        }
    }

    Dialog(
        onDismissRequest = { /* Do nothing to prevent accidental dismissal */ },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false, dismissOnBackPress = true)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = white),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(text = "Approval Required", fontFamily = appFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(14.dp))

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = colorPrimary,
                    indicator = { tabPositions -> TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]), color = colorPrimary) },
                    divider = {}
                ) {
                    Tab(selected = selectedTabIndex == 0, onClick = { if (!isSaving) selectedTabIndex = 0 }, text = { Text("QR Scan", fontFamily = appFontFamily, fontSize = 14.sp) }, icon = { Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(20.dp)) })
                    Tab(selected = selectedTabIndex == 1, onClick = { if (!isSaving) selectedTabIndex = 1 }, text = { Text("PIN & Sign", fontFamily = appFontFamily, fontSize = 14.sp) }, icon = { Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(20.dp)) })
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth().height(if (selectedTabIndex == 0) 200.dp else 300.dp)) {
                    if (selectedTabIndex == 0) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                            Image(bitmap = generateQrBitmap(data).asImageBitmap(), contentDescription = "QR Code", modifier = Modifier.size(165.dp).padding(4.dp))
                            Text(text = "Scan this QR to approve", color = Color.Gray, fontSize = 13.sp, fontFamily = appFontFamily)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                            Text("Enter 4-Digit PIN", fontFamily = appFontFamily, fontSize = 14.sp, color = textPrimary, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(10.dp))
                            PinInputField(pin = pinValue, onPinChanged = { pinValue = it })

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Digital Signature", fontFamily = appFontFamily, fontSize = 14.sp, color = textPrimary, fontWeight = FontWeight.SemiBold)
                                if (signaturePaths.isNotEmpty()) {
                                    Text("Clear", color = Color.Red, fontSize = 12.sp, modifier = Modifier.clickable { signaturePaths.clear() }, fontFamily = appFontFamily, fontWeight = FontWeight.Medium)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            Box(modifier = Modifier.fillMaxWidth().height(120.dp).border(1.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFDFDFD))) {
                                InlineSignatureCanvas(paths = signaturePaths, modifier = Modifier.fillMaxSize())
                                if (signaturePaths.isEmpty()) {
                                    Text("Sign here", color = Color.LightGray, fontSize = 14.sp, modifier = Modifier.align(Alignment.Center), fontFamily = appFontFamily)
                                }
                            }

                            if (state is ApproveDriverLogViewModel.ApproveDriverLogState.Error) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = (state as ApproveDriverLogViewModel.ApproveDriverLogState.Error).message, color = Color.Red, fontSize = 12.sp, fontFamily = appFontFamily, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onDismiss() },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = textPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(50.dp),
                        enabled = !isSaving
                    ) { Text("Close", fontFamily = appFontFamily, fontSize = 15.sp) }

                    if (selectedTabIndex == 1) {
                        Button(
                            onClick = {
                                if (pinValue.length == 4 && signaturePaths.isNotEmpty()) {
                                    val bitmap = createSignatureBitmap(signaturePaths)
                                    val uri = saveSignatureToCache(context, bitmap)
                                    viewModel.approveDriverLog(token, pinValue, uri)
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorPrimary),
                            shape = RoundedCornerShape(50.dp),
                            enabled = !isSaving && pinValue.length == 4 && signaturePaths.isNotEmpty()
                        ) {
                            if (isSaving) CircularProgressIndicator(color = white, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Text("Approve", fontFamily = appFontFamily, color = white, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PinInputField(pin: String, onPinChanged: (String) -> Unit) {
    val focusRequesters = remember { List(4) { FocusRequester() } }
    var focusedIndex by remember { mutableIntStateOf(-1) }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        for (i in 0 until 4) {
            val char = pin.getOrNull(i)?.toString() ?: ""
            val isFocused = focusedIndex == i

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .border(
                        width = if (isFocused) 2.dp else 1.5.dp,
                        color = if (isFocused) colorPrimary else if (char.isNotEmpty()) colorPrimary.copy(alpha = 0.5f) else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isFocused) Color(0xFFF9FFFA) else white),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = char,
                    onValueChange = { newValue ->
                        if (newValue.length <= 1) {
                            val newPin = if (i < pin.length) pin.replaceRange(i, i + 1, newValue) else pin + newValue
                            if (newPin.length <= 4) {
                                onPinChanged(newPin)
                                if (newValue.isNotEmpty() && i < 3) focusRequesters[i + 1].requestFocus()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequesters[i])
                        .onFocusChanged { if (it.isFocused) focusedIndex = i }
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.key == Key.Backspace && char.isEmpty() && i > 0) {
                                focusRequesters[i - 1].requestFocus()
                                true
                            } else false
                        },
                    textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Color.Transparent), // Hide text, show dot
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    cursorBrush = SolidColor(colorPrimary),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.Center) {
                            if (char.isEmpty() && isFocused) {
                                // Show cursor even when empty
                                innerTextField()
                            } else if (char.isNotEmpty()) {
                                Text("●", fontSize = 18.sp, color = colorPrimary)
                            }
                            if (char.isNotEmpty() && isFocused) innerTextField()
                        }
                    }
                )
            }
        }
    }

    // Auto-focus first empty field
    LaunchedEffect(Unit) {
        val nextFocus = pin.length.coerceAtMost(3)
        focusRequesters[nextFocus].requestFocus()
    }
}

@Composable
fun InlineSignatureCanvas(paths: MutableList<Path>, modifier: Modifier) {
    var currentPath by remember { mutableStateOf<Path?>(null) }
    Canvas(modifier = modifier.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = { offset ->
                currentPath = Path().apply { moveTo(offset.x, offset.y) }
                currentPath?.let { paths.add(it) }
            },
            onDrag = { change, _ ->
                currentPath?.lineTo(change.position.x, change.position.y)
                if (paths.isNotEmpty()) {
                    val last = paths.removeAt(paths.size - 1)
                    paths.add(last)
                }
            }
        )
    }) {
        paths.forEach { path ->
            drawPath(path = path, color = Color.Black, style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

fun createSignatureBitmap(paths: List<Path>): Bitmap {
    val bitmap = createBitmap(800, 400, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.BLACK
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
        isAntiAlias = true
    }
    paths.forEach { path -> canvas.drawPath(path.asAndroidPath(), paint) }
    return bitmap
}

fun saveSignatureToCache(context: android.content.Context, bitmap: Bitmap): Uri {
    val file = File(context.cacheDir, "signature_${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

@SuppressLint("UseKtx")
fun generateQrBitmap(text: String): Bitmap {
    val size = 512
    val bits = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
    val bitmap = createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
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

    val corporateState by viewModel.corporateUsers.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    val userList = remember { mutableStateListOf<CorporateUsersResponse>() }
    var selectedUser by remember { mutableStateOf("") }
    var selectedUserId by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(Unit) {
        viewModel.getCorporateUsers()
    }

    LaunchedEffect(corporateState) {
        val s = corporateState
        if (s is ApproveDriverLogViewModel.CorporateUsersState.Success) {
            userList.clear()
            userList.addAll(s.response)
            if (selectedUser.isEmpty() && userList.isNotEmpty()) {
                selectedUser = userList[0].name
                selectedUserId = userList[0].id
            }
        }
    }

    val filteredUsers by remember {
        derivedStateOf {
            userList.filter { it.name.contains(searchText.text, ignoreCase = true) }
        }
    }

    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "")
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val density = LocalDensity.current
    val lightGrayBorder = Color(0xFFE0E0E0)
    val labelColor = Color(0xFF757575)
    val placeholderColor = Color(0xFFBDBDBD)

    Dialog(onDismissRequest = { /* Prevents closing on outside click */ }, properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(text = stringResource(R.string.select_approval_user), fontSize = 16.sp, fontFamily = appFontFamily, fontWeight = FontWeight.SemiBold, color = textColorPrimary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = stringResource(R.string.approval_user), fontFamily = appFontFamily, fontSize = 16.sp, color = labelColor)
                Box(modifier = Modifier.fillMaxWidth().onGloballyPositioned { coordinates -> textFieldSize = coordinates.size.toSize() }) {
                    BasicTextField(
                        value = searchText,
                        onValueChange = {
                            searchText = it
                            expanded = it.text.isNotEmpty() && filteredUsers.isNotEmpty()
                        },
                        textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                        singleLine = true,
                        cursorBrush = SolidColor(Color.Black),
                        decorationBox = { innerTextField ->
                            Row(modifier = Modifier.fillMaxWidth().height(50.dp).border(1.dp, lightGrayBorder, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.weight(1.0f), contentAlignment = Alignment.CenterStart) {
                                    if (searchText.text.isEmpty()) Text(text = "Search...", color = placeholderColor, fontFamily = appFontFamily, fontSize = 15.sp)
                                    innerTextField()
                                }
                                Icon(imageVector = Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = labelColor, modifier = Modifier.rotate(rotation).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { expanded = !expanded }.size(24.dp))
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = expanded && filteredUsers.isNotEmpty(), onDismissRequest = { expanded = false }, properties = PopupProperties(focusable = false), modifier = Modifier.width(with(density) { textFieldSize.width.toDp() })) {
                        filteredUsers.forEach { user ->
                            DropdownMenuItem(text = { Text(user.name, fontFamily = appFontFamily, fontSize = 15.sp) }, onClick = {
                                searchText = TextFieldValue(text = user.name, selection = TextRange(user.name.length))
                                userName = user.name
                                selectedUserId = user.id
                                expanded = false
                            })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(text = stringResource(R.string.corporate_user_name), fontFamily = appFontFamily, fontSize = 16.sp, color = labelColor)
                BasicTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    decorationBox = { innerTextField ->
                        Row(modifier = Modifier.fillMaxWidth().height(50.dp).border(1.dp, lightGrayBorder, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                                if (userName.isEmpty()) Text(text = stringResource(R.string.enter_corporate_user_name), fontFamily = appFontFamily, color = placeholderColor, fontSize = 15.sp)
                                innerTextField()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, lightGrayBorder)
                    ) { Text("Cancel", fontFamily = appFontFamily) }

                    if (userName.isEmpty()) {
                        Button(
                            onClick = {
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(Color.Gray),
                            enabled = userName.isNotEmpty() || selectedUser.isNotEmpty()
                        ) { Text(stringResource(R.string.text_continue), fontFamily = appFontFamily, fontWeight = FontWeight.Bold) }

                    }else{
                        Button(
                            onClick = {
                                val finalName = userName.ifEmpty { selectedUser }
                                    onConfirm(selectedUserId, finalName, selectedIds)
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(colorPrimary),
                            enabled = userName.isNotEmpty() || selectedUser.isNotEmpty()
                        ) { Text(stringResource(R.string.text_continue), fontFamily = appFontFamily, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}
