package com.pv.transport.presentation

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
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
import com.pv.transport.ui.theme.robotoFontFamily
import com.pv.transport.ui.theme.textSecondary

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ApprovalScreen(
    navController: NavController,
    viewModel: ApproveDriverLogViewModel = hiltViewModel(),
    generateQRViewModel: GenerateQRViewModel = hiltViewModel()
){
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

    LaunchedEffect(socketData) {
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
                        Text("Filters", fontWeight = FontWeight.SemiBold)
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
                                fontFamily = robotoFontFamily,
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
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, yellow),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                        ) {

                            Column(modifier = Modifier.padding(16.dp)) {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    ) {
                                        val itemId  = approvalList[index].id
                                        val checked = selectedItems.contains(itemId.toInt())

                                        println("Item ID: $itemId, Checked: $checked, Status: ${approvalList[index].status}")

                                        if(approvalList[index].status == "pending"){
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
                                                }
                                            )
                                        }

                                        Text(
                                            stringResource(R.string.personal_errand),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    StatusBadge(
                                        approvalList[index].status,
                                        Color(0xFFFFF3CD),
                                        Color(0xFF856404),
                                        modifier = Modifier.align(Alignment.CenterEnd)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("${approvalList[index].driverLog.date}  •  ${approvalList[index].startTime} - ${approvalList[index].endTime}", color = Color.Gray, modifier = Modifier.align(
                                        Alignment.CenterStart))
                                    StatusBadge("personal", purple, Color(0xFF6A1B9A), modifier = Modifier.align(Alignment.CenterEnd))
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(stringResource(R.string.start_km), fontSize = 12.sp, color = Color.Gray)
                                        Text(approvalList[index].startKm, fontWeight = FontWeight.Bold)
                                    }

                                    Column {
                                        Text(stringResource(R.string.end_km), fontSize = 12.sp, color = Color.Gray)
                                        Text("${approvalList[index].endKm}", fontWeight = FontWeight.Bold)
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

@Composable
fun StatusBadge(text: String, bgColor: Color, textColor: Color,modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp
        )
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

//    val filteredUsers = userList.filter {
//        it.name.contains(searchText, ignoreCase = true)
//    }

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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.generate_qr),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Selected count: ${selectedIds.joinToString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(20.dp))
                Box {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(35.dp)
                            .onGloballyPositioned { coordinates ->
                                textFieldSize = coordinates.size.toSize()
                            }
                            .clip(RoundedCornerShape(5.dp))
                            .background(white)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {

                        BasicTextField(
                            value = searchText,
                            onValueChange = {
                                searchText = it
                                expanded = it.text.isNotEmpty() && filteredUsers.isNotEmpty()
                            },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                color = Color.Black
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (searchText.text.isEmpty()) {
                                        Text("Search...", color = Color.Gray)
                                    }
                                    innerTextField()

                                    IconButton(
                                        onClick = { expanded = !expanded },
                                        modifier = Modifier.align(Alignment.CenterEnd)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.KeyboardArrowDown,
                                            contentDescription = null,
                                            modifier = Modifier.rotate(rotation)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    DropdownMenu(
                        expanded = expanded && filteredUsers.isNotEmpty(),
                        onDismissRequest = { expanded = false },
                        properties = PopupProperties(
                            focusable = false
                        ),
                        modifier = Modifier
                            .width( with(density) {
                                textFieldSize.width.toDp()
                            })
                    ) {
                        filteredUsers.forEach { user ->
                            DropdownMenuItem(
                                text = { Text(user.name) },
                                onClick = {
                                    searchText = TextFieldValue(
                                        text = user.name,
                                        selection = TextRange(user.name.length) // cursor at end
                                    )

                                    userName = user.name
                                    selectedUserId = user.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // TextField
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(white)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    BasicTextField(
                        value = userName,
                        onValueChange = {
                            userName = it
                        },

                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (userName.isEmpty()) {
                                    Text(
                                        stringResource(R.string.corporate_user_name),
                                        color = Color.Gray,
                                        fontFamily = robotoFontFamily,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if(userName.isEmpty()){
                    Button(
                        onClick = {
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text(
                            stringResource(R.string.text_continue),
                            fontFamily = robotoFontFamily,
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
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colorPrimary)
                    ) {
                        Text(
                            stringResource(R.string.text_continue),
                            fontFamily = robotoFontFamily,
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
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.generate_qr), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(200.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                val interactionSource = remember { MutableInteractionSource() }

                BasicTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 6.dp), // External spacing only
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(Color.White),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    interactionSource = interactionSource,
                    decorationBox = { innerTextField ->
                        TextFieldDefaults.DecorationBox(
                            value = password,
                            innerTextField = innerTextField,
                            enabled = true,
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            interactionSource = interactionSource,
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.password),
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 16.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                        tint = Color.White
                                    )
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF176B43),
                                unfocusedContainerColor = Color(0xFF176B43),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(
                                top = 0.dp,
                                bottom = 0.dp
                            )
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {onFinish()},
                        modifier = Modifier.width(100.dp).align(Alignment.CenterVertically),
                        colors = ButtonDefaults.buttonColors(colorPrimary)

                    ) {Text(stringResource(R.string.close)) }

                    if (password.isEmpty() ) {
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier.align(Alignment.CenterVertically),
                            enabled = false
                        ) {
                            Text(stringResource(R.string.approve), color = Color.White)
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
                            modifier = Modifier.align(Alignment.CenterVertically)
                            ,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32)
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
                                    Text(stringResource(R.string.saving), color = Color.White)
                                }
                            } else {
                                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.approve), color = Color.White)
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

    val size = 512

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