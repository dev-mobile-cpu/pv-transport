package com.pv.transport.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pv.transport.R
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(navToLogin: () -> Unit, navToLanguage: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()){
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorSecondary)
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.driver_profile),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.information_driving_status),
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileCard(viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            SettingsCard(viewModel, onForgot = {
                navToLogin()
            }, onLogout = {
                viewModel.logout()
                navToLogin()
            })
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(white)
                        .clickable { navToLanguage() }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = Color(0xFF6A5AE0) // purple like design
                    )

                   Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = stringResource(R.string.language),
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null
                    )
                }
            }
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
                    Text(viewModel.username.collectAsState().value.toString(), fontWeight = FontWeight.Bold)
                    Text(viewModel.phone.collectAsState().value.toString(), color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(stringResource(R.string.license_number), viewModel.licensePlate.collectAsState().value.toString())
            InfoRow(stringResource(R.string.vehicle_assigned), viewModel.driverId.collectAsState().value.toString())
            InfoRow(stringResource(R.string.member_since), viewModel.createdAt.collectAsState().value.toString())
        }
    }
}

@Composable
fun InfoRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = Color.Black)
        Text(value, fontWeight = FontWeight.Medium)
    }
    Spacer(modifier = Modifier.height(8.dp))
}


@Composable
fun SettingsCard(viewModel: ProfileViewModel, onForgot: () -> Unit = {}, onLogout: () -> Unit = {}) {

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(white),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = stringResource(R.string.settings),
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.manage_your_account_settings),
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))


            OutlinedButton(
                onClick = { onForgot() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.forgot_password))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onLogout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                )
            ) {
                Text(stringResource(R.string.log_out))
            }
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