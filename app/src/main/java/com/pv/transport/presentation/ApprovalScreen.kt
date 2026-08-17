package com.pv.transport.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.pv.transport.extension.safeNavigate
import com.pv.transport.network.ConnectivityObserver
import com.pv.transport.network.NetworkUtils
import com.pv.transport.ui.theme.*
import com.pv.transport.util.DebugLog
import com.pv.transport.viewmodels.ApproveDriverLogViewModel
import com.pv.transport.viewmodels.GenerateQRViewModel
import com.pv.transport.viewmodels.NetworkStatusViewModel
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ApprovalScreen(
    navController: NavController,
    viewModel: ApproveDriverLogViewModel = hiltViewModel(),
    generateQRViewModel: GenerateQRViewModel = hiltViewModel(),
    networkViewModel: NetworkStatusViewModel = hiltViewModel()
){
    val showExitDialog = remember { mutableStateOf(false) }
    val activity = LocalContext.current as Activity
    val networkStatus by networkViewModel.networkStatus.collectAsState()
    val isOffline = networkStatus != ConnectivityObserver.Status.Available

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

    LaunchedEffect(socketData) {
        socketData?.let {
            showQRDialog = false
            viewModel.getApprovalStatus(startDate.toString(), endDate.toString(), "")
            generateQRViewModel.clearSocketData()
        }
    }

    LaunchedEffect(approval) {
        selectedItems.clear()
    }

    LaunchedEffect(isOffline) {
        if (isOffline) {
            selectedItems.clear()
        }
    }

    LaunchedEffect(startDate, endDate) {
        viewModel.getApprovalStatus(startDate.toString(), endDate.toString(), "")
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && approval is ApproveDriverLogViewModel.ApprovalState.Success) {
            val successState = approval as ApproveDriverLogViewModel.ApprovalState.Success
            if (!successState.isLoadingMore && successState.currentPage < successState.lastPage) {
                viewModel.loadMoreLogs(startDate.toString(), endDate.toString(), "")
            }
        }
    }

    val chromeState = LocalCollapsibleChrome.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorSecondary)
    ) {
        CollapsibleTitleSlot(visible = chromeState?.titleVisible != false) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NetworkAwarePageTitle(
                    title = stringResource(R.string.approvals),
                    subtitle = stringResource(R.string.pending_reviewed),
                    networkStatus = networkStatus,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .collapsibleChromeScroll(chromeState),
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ){
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
                            CustomDatePicker(selectedDate = startDate, onDateSelected = { startDate = it }, bgColor = colorSecondary, readOnly = isOffline)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.end_date), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            CustomDatePicker(selectedDate = endDate, onDateSelected = { endDate = it }, bgColor = colorSecondary, readOnly = isOffline)
                        }
                    }
                }
            }
        }

        when (val currentApproval = approval) {
            is ApproveDriverLogViewModel.ApprovalState.Success -> {
                // ✅ ၁။ distinctBy { it.id } ဖြင့် Duplicate မပါအောင် UI Layer တွင် ခါထုတ်ပါသည်
                val approvalList = currentApproval.response.distinctBy { it.id }
                val filterList = approvalList.filter { it.status == "pending" }
                val allSelected = selectedItems.size == filterList.size && filterList.isNotEmpty()

                if (approvalList.isNotEmpty() && !isOffline){
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
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(stringResource(R.string.select_all), fontWeight = FontWeight.SemiBold, fontFamily = appFontFamily, fontSize = 14.sp)
                                }
                                Button(
                                    onClick = {
                                        if (!NetworkUtils.isInternetAvailable(activity)) {
                                            Toast.makeText(activity, "Active internet connection required.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            showDialog = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (anySelected) colorPrimary else Color.Gray
                                    ),
                                    shape = RoundedCornerShape(50.dp),
                                    enabled = anySelected,
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.generate_qr),
                                        fontSize = 13.sp,
                                        fontFamily = appFontFamily
                                    )
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
                    // ✅ ၂။ items(items = ..., key = { it.id }) ပုံစံဖြင့် တိုက်ရိုက် ပို့ပေးပါသည်
                    items(
                        items = approvalList,
                        key = { item -> item.id }
                    ) { item ->
                        Card(
                            onClick = {
                                navController.currentBackStackEntry?.savedStateHandle?.set("approval_detail", item)
                                navController.safeNavigate("approval_detail")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = white),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.align(Alignment.CenterStart), verticalAlignment = Alignment.Top){
                                        val itemId = item.id
                                        val checked = selectedItems.contains(itemId.toInt())
                                        if (item.status == "pending" && !isOffline) {
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
                                        Spacer(modifier = Modifier.width(if (item.status == "pending") 12.dp else 0.dp))
                                        Column {
                                            Text(item.type.replaceFirstChar { it.uppercase() }, fontFamily = appFontFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    StatusBadge(
                                        status = item.status,
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(text = item.reason, fontSize = 14.sp, color = textColorSecondary)
                                }
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(imageVector = Icons.Default.DateRange, contentDescription = "Date", modifier = Modifier.size(16.dp))
                                        Text(text = item.driverLog?.date ?: "", fontSize = 13.sp, fontFamily = appFontFamily)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(imageVector = Icons.Rounded.AccessTime, contentDescription = "Time", modifier = Modifier.size(16.dp))
                                        Text(text = "${item.startTime} - ${item.endTime}", fontSize = 13.sp, fontFamily = appFontFamily)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(stringResource(R.string.start_km), fontFamily = appFontFamily, color = textColorSecondary, fontSize = 12.sp)
                                        Text(item.startKm, fontFamily = appFontFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text(stringResource(R.string.end_km), fontFamily = appFontFamily, color = textColorSecondary, fontSize = 12.sp)
                                        Text("${item.endKm}", fontFamily = appFontFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    if (currentApproval.isLoadingMore) {
                        item { Box(modifier = Modifier.fillParentMaxWidth(), contentAlignment = Alignment.Center) { DotsLoading() } }
                    }
                }
            }
            is ApproveDriverLogViewModel.ApprovalState.Loading -> {
                item { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { DotsLoading() } }
            }
            is ApproveDriverLogViewModel.ApprovalState.Error -> {
                item { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text(text = currentApproval.message, color = textColorSecondary) } }
            }
            else -> {}
        }
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
                viewModel.resetState()
                generateQRViewModel.resetState()
                generateQRViewModel.clearSocketData()
                generateQRViewModel.generateQR(GenerateQR(ids,userId,userName))
            }
        )
    }

    LaunchedEffect(uiState) {
        when (val s = uiState) {
            is GenerateQRUiState.Success -> {
                qrData = s.qrResponse.qrUrl
                token = s.qrResponse.token
                showQRDialog = true
                generateQRViewModel.resetState()
            }
            is GenerateQRUiState.Error -> {
                Toast.makeText(activity, s.message, Toast.LENGTH_SHORT).show()
                generateQRViewModel.resetState()
            }
            else -> {}
        }
    }

    if (showQRDialog) {
        GenerateQRScreen(
            data = qrData,
            token = token,
            logViewModel = viewModel,
            startDate = startDate,
            endDate = endDate,
            onFinish = {
                showQRDialog = false
                generateQRViewModel.resetState()
                generateQRViewModel.clearSocketData()
                viewModel.resetState()
            },
            onDismiss = {
                showQRDialog = false
                generateQRViewModel.resetState()
                generateQRViewModel.clearSocketData()
                viewModel.resetState()
            }
        )
    }
}

@Composable
fun GenerateQRScreen(
    data: String,
    token: String,
    logViewModel: ApproveDriverLogViewModel,
    startDate: LocalDate,
    endDate: LocalDate,
    onFinish: () -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val state by logViewModel.state.collectAsState()
    val isSaving = state is ApproveDriverLogViewModel.ApproveDriverLogState.Loading

    var pinValue by remember { mutableStateOf("") }
    val signaturePaths = remember { mutableStateListOf<Path>() }
    var signatureCanvasSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(Unit) {
        logViewModel.resetState()
    }


    LaunchedEffect(state) {
        if (state is ApproveDriverLogViewModel.ApproveDriverLogState.Success) {
            val message = (state as ApproveDriverLogViewModel.ApproveDriverLogState.Success).response.message
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            logViewModel.getApprovalStatus(startDate.toString(), endDate.toString(), "")
            logViewModel.resetState()
            delay(150)
            onFinish()
        }
    }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false, dismissOnBackPress = true)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = white),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(text = stringResource(R.string.approval_required), fontFamily = appFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
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
                            Text(text = stringResource(R.string.scan_qr_approve), color = Color.Gray, fontSize = 13.sp, fontFamily = appFontFamily)
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

                            Box(modifier = Modifier.fillMaxWidth().height(120.dp).border(1.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFDFDFD)).onGloballyPositioned { signatureCanvasSize = it.size }) {
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
                                    try {
                                        val bitmap = createSignatureBitmap(signaturePaths, signatureCanvasSize.width, signatureCanvasSize.height)
                                        val signatureFile = saveSignatureToCache(context, bitmap)
                                        bitmap.recycle()
                                        logViewModel.approveDriverLog(token, pinValue, signatureFile)
                                    } catch (e: Exception) {
                                        DebugLog.w("UPLOAD_DEBUG", "Failed to build signature image", e)
                                        Toast.makeText(context, context.getString(R.string.image_process_failed), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorPrimary),
                            shape = RoundedCornerShape(50.dp),
                            enabled = !isSaving && pinValue.length == 4 && signaturePaths.isNotEmpty()
                        ) {
                            if (isSaving) DotsLoading(color = white, dotSize = 7.dp)
                            else Text("Approve", fontFamily = appFontFamily, color = white, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun PinInputField(
    pin: String,
    onPinChanged: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // ၁။ တကယ့် Input ကို လက်ခံမယ့် မမြင်ရတဲ့ TextField (Crash ဖြစ်ခြင်းမှ ရာနှုန်းပြည့် ကာကွယ်ပေးသည်)
        BasicTextField(
            value = pin,
            onValueChange = { newValue ->
                if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                    onPinChanged(newValue)
                }
            },
            modifier = Modifier
                .size(width = (54 * 4 + 12 * 3).dp, height = 54.dp) // Box ၄ ခုစာ အကျယ်ယူထားမယ်
                .focusRequester(focusRequester)
                .onFocusChanged { isFocused = it.isFocused },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            decorationBox = {
                // ၂။ ဒါကတော့ အပြင်ပန်း မြင်ရမယ့် UI Layout ဖြစ်ပါတယ်
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (i in 0 until 4) {
                        val char = pin.getOrNull(i)?.toString() ?: ""
                        // လက်ရှိ ရိုက်ရမယ့် အကွက် သို့မဟုတ် စာလုံးမပြည့်သေးရင် နောက်ဆုံးအကွက်ကို Focus ပြပေးမယ်
                        val isBoxFocused = isFocused && (i == pin.length || (i == 3 && pin.length == 4))

                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .border(
                                    width = if (isBoxFocused) 2.dp else 1.5.dp,
                                    color = if (isBoxFocused) colorPrimary else if (char.isNotEmpty()) colorPrimary.copy(alpha = 0.5f) else Color(0xFFE0E0E0),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isBoxFocused) Color(0xFFF9FFFA) else Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            if (char.isNotEmpty()) {
                                Text("●", fontSize = 18.sp, color = colorPrimary)
                            } else if (isBoxFocused) {
                                // ၃။ စာမရှိသေးဘဲ Focus ရောက်နေတဲ့ အကွက်မှာ Cursor အတုလေး ပြပေးထားမယ်
                                CursorVisual()
                            }
                        }
                    }
                }
            }
        )
    }

    // Auto-focus ပေးမည့်အပိုင်း
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

// မျက်တောင်ခတ်နေမည့် Cursor အတု Component
@Composable
fun CursorVisual() {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Box(
        modifier = Modifier
            .size(width = 2.dp, height = 24.dp)
            .background(colorPrimary.copy(alpha = alpha))
    )
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

fun createSignatureBitmap(paths: List<Path>, width: Int = 0, height: Int = 0): Bitmap {
    // Paths hold raw canvas pixel coordinates, so the bitmap has to match the on-screen
    // canvas size or the signature gets clipped.
    val bitmap = createBitmap(
        if (width > 0) width else 800,
        if (height > 0) height else 400,
        Bitmap.Config.ARGB_8888
    )
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

fun saveSignatureToCache(context: android.content.Context, bitmap: Bitmap): File {
    val file = File(context.cacheDir, "signature_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out) }
    return file
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

    val lightGrayBorder = FormSelectDefaults.BorderColor
    val labelColor = FormSelectDefaults.LabelColor
    val placeholderColor = FormSelectDefaults.PlaceholderColor

    Dialog(onDismissRequest = { /* Prevents closing on outside click */ }, properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(text = stringResource(R.string.select_approval_user), fontSize = 16.sp, fontFamily = appFontFamily, fontWeight = FontWeight.SemiBold, color = textColorPrimary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = stringResource(R.string.approval_user), fontFamily = appFontFamily, fontSize = 16.sp, color = labelColor)
                Spacer(modifier = Modifier.height(4.dp))
                FormSearchSelect(
                    value = searchText,
                    onValueChange = { searchText = it },
                    options = filteredUsers.map { it.name },
                    selectedLabel = userName,
                    placeholder = stringResource(R.string.search_hint),
                    onSelected = { index, label ->
                        val user = filteredUsers.getOrNull(index) ?: return@FormSearchSelect
                        searchText = TextFieldValue(text = label, selection = TextRange(label.length))
                        userName = user.name
                        selectedUserId = user.id
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(text = stringResource(R.string.actual_user), fontFamily = appFontFamily, fontSize = 16.sp, color = labelColor)
                Spacer(modifier = Modifier.height(4.dp))
                BasicTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    textStyle = TextStyle(fontFamily = appFontFamily, fontSize = 16.sp, color = FormSelectDefaults.TextColor),
                    singleLine = true,
                    cursorBrush = SolidColor(FormSelectDefaults.TextColor),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(FormSelectDefaults.Height)
                                .border(1.dp, lightGrayBorder, RoundedCornerShape(FormSelectDefaults.CornerRadius))
                                .clip(RoundedCornerShape(FormSelectDefaults.CornerRadius))
                                .background(Color.White)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                                if (userName.isEmpty()) Text(text = stringResource(R.string.enter_corporate_user_name), fontFamily = appFontFamily, color = placeholderColor, fontSize = 16.sp)
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
