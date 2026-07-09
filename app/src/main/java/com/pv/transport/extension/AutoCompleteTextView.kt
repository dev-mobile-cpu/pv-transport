package com.pv.transport.extension


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.PopupProperties
import com.pv.transport.data.log.CorporateUsersResponse


@Composable
fun AutoCompleteTextView(
    modifier: Modifier = Modifier,
    users: List<CorporateUsersResponse>,

) {

    var userName by remember { mutableStateOf("") }

    var textFieldSize by remember {
        mutableStateOf(Size.Zero)
    }

    val density = LocalDensity.current

    var searchText by remember {
        mutableStateOf(TextFieldValue(""))
    }

    // Filtered List
    val filteredUsers = remember(searchText.text) {

        if (searchText.text.isEmpty()) {
            emptyList()
        } else {

            users.filter {

                it.name.contains(
                    searchText.text,
                    ignoreCase = true
                )
            }
        }
    }

    // Dropdown visible or not
    val expanded = filteredUsers.isNotEmpty()

    var expanded2 by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = ""
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Box {

            // Search TextField
            BasicTextField(
                value = searchText,

                onValueChange = {
                    searchText = it
                },

                singleLine = true,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                }

                    .background(
                        Color.White,
                        RoundedCornerShape(6.dp)
                    ),

                decorationBox = { innerTextField ->

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(45.dp)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchText.text.isEmpty()) {
                                Text("Search...", color = Color.Gray)
                            }
                            innerTextField()
                            IconButton(
                                onClick = { expanded2 = !expanded2 },
                                modifier = Modifier.align(Alignment.CenterEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.rotate(rotation)
                                )
                            }
                        }


                }
            )

            // Dropdown Menu
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                },

                // IMPORTANT FIX
                properties = PopupProperties(
                    focusable = false
                ),

                modifier = Modifier
                    .width( with(density) {
                        textFieldSize.width.toDp()
                    })
                    .background(Color.White)

            ) {

                filteredUsers.forEach { user ->

                    DropdownMenuItem(

                        text = {
                            Text(user.name)
                        },

                        onClick = {

                            searchText = TextFieldValue(user.name)
                            userName = user.name
                            expanded2 = false

                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Other TextField
        BasicTextField(

            value = userName,

            onValueChange = {
                userName = it
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .background(
                    Color.White,
                    RoundedCornerShape(6.dp)
                ),

            decorationBox = { innerTextField ->

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (userName.isEmpty()) {
                        Text(
                            text = "Other field",
                            color = Color.Gray
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}