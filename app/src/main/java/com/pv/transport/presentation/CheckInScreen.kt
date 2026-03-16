package com.pv.transport.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pv.transport.viewmodels.ReasonViewModel
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.DriverLogViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    navController: NavController,
    reasonViewModel: ReasonViewModel = hiltViewModel(),
    driverLogViewModel: DriverLogViewModel = hiltViewModel()
) {
    val options = listOf("daily", "trip")
    var selectedOption by remember { mutableStateOf(options[0]) }
    var expandedType by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Add Daily Log", color = Color.Black)
                        Text(
                            text = "Record your trip details",
                            color = Color.Black,
                            fontSize = 12.sp
                        )
                    }
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = white
                )
            )
        },
        containerColor = Color(0xFFF4F4F4)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
           Column(modifier = Modifier.padding(16.dp)) {
               Text("Trip Type")
               Spacer(modifier = Modifier.height(4.dp))
               Box{
                   Row(
                       modifier = Modifier
                           .fillMaxWidth()
                           .clip(RoundedCornerShape(5.dp))
                           .background(white)
                           .clickable { expandedType = true }
                           .padding(horizontal = 8.dp, vertical = 6.dp),
                       horizontalArrangement = Arrangement.SpaceBetween,
                       verticalAlignment = Alignment.CenterVertically
                   ) {
                       Text(selectedOption)
                       Icon(
                           imageVector = Icons.Default.KeyboardArrowDown,
                           contentDescription = null
                       )
                   }

                   DropdownMenu(
                       expanded = expandedType,
                       onDismissRequest = { expandedType = false },
                       modifier = Modifier.fillMaxWidth()
                   ) {
                       options.forEach { status ->
                           DropdownMenuItem(
                               text = {
                                   Row(
                                       modifier = Modifier.fillMaxWidth(),
                                       horizontalArrangement = Arrangement.SpaceBetween
                                   ) {
                                       Text(status)

                                       if (status == selectedOption) {
                                           Icon(
                                               imageVector = Icons.Default.Check,
                                               contentDescription = null
                                           )
                                       }
                                   }
                               },
                               onClick = {
                                   selectedOption = status
                                   expandedType = false
                               }
                           )
                       }
                   }
               }
           }
            Spacer(modifier = Modifier.height(5.dp))
            when (selectedOption) {
                "daily" -> DailyCheckInScreen(navController,"daily",reasonViewModel,driverLogViewModel)
                "trip" -> TripCheckInScreen(navController,"trip",reasonViewModel,driverLogViewModel)
            }
        }
    }
}


//@RequiresApi(Build.VERSION_CODES.O)
//@Preview(showBackground = true)
//@Composable
//fun PreviewProfile() {
//    // You can preview the ProfileScreen composable in Android Studio.
//    // Replace 'NavController' and 'Context' with mock data for previewing purposes.
//    CheckInScreen(navController = rememberNavController())
//}
