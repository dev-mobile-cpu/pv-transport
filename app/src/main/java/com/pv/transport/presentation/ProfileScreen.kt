package com.pv.transport.presentation

import android.content.Context
import android.provider.ContactsContract
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(navToLogin: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()){


    Column(
        modifier = Modifier.fillMaxSize()
            .background(colorSecondary)
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Driver Profile",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Your information and driving stats",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileCard(viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            SettingsCard(viewModel, onForgot = {
                // navigate to forgot password (or implement as needed)
                navToLogin()
            }, onLogout = {
                viewModel.logout()
                navToLogin()
            })

            Spacer(modifier = Modifier.height(16.dp))

            SafetyScoreCard()
        }


    }

}

@Composable
fun ProfileCard(viewModel: ProfileViewModel) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(white),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFFE8F5E9), CircleShape)
                        .padding(12.dp),
                    tint = Color(0xFF2E7D32)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(viewModel.username.value.toString(), fontWeight = FontWeight.Bold)
                    Text(viewModel.phone.value.toString(), color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow("License Number", viewModel.licensePlate.value.toString())
            InfoRow("Vehicle Assigned", viewModel.driverId.value.toString())
            InfoRow("Member Since", viewModel.createdAt.value.toString())
        }
    }
}

@Composable
fun InfoRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Medium)
    }
    Spacer(modifier = Modifier.height(8.dp))
}


@Composable
fun SettingsCard(viewModel: ProfileViewModel, onForgot: () -> Unit = {}, onLogout: () -> Unit = {}) {

    var darkMode by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(white),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "Settings",
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Manage your account settings",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("Dark Mode", fontWeight = FontWeight.Medium)
                    Text("Toggle dark/light theme", color = Color.Gray)
                }

                Switch(
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { onForgot() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Forget Password")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onLogout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                )
            ) {
                Text("Logout")
            }
        }
    }
}


@Composable
fun SafetyScoreCard() {

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(white),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text("Safety Score", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "92",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(" /100", color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
            progress = { 0.92f },
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF2E7D32),
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
        }
    }
}



//@Preview(showBackground = true)
//@Composable
//fun PreviewProfile() {
//    // You can preview the ProfileScreen composable in Android Studio.
//    // Replace 'NavController' and 'Context' with mock data for previewing purposes.
//    ProfileScreen(navToLogin = {})
//}