package com.pv.transport.presentation

import android.app.Activity
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.pv.transport.R
import com.pv.transport.extension.HandleBackPressWithDialog
import com.pv.transport.ui.theme.NetworkAwarePageTitle
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.green_primary
import com.pv.transport.ui.theme.iconBg
import com.pv.transport.ui.theme.lightGreen
import com.pv.transport.ui.theme.textColor
import com.pv.transport.ui.theme.textPrimary
import com.pv.transport.ui.theme.textSecondary
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.NetworkStatusViewModel
import com.pv.transport.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    navToLogin: () -> Unit,
    navToLanguage: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    networkViewModel: NetworkStatusViewModel = hiltViewModel()
){
    val context = LocalContext.current
    val navController = rememberNavController()
    val showExitDialog = remember { mutableStateOf(false) }
    val activity = LocalContext.current as Activity
    val networkStatus by networkViewModel.networkStatus.collectAsStateWithLifecycle()

    HandleBackPressWithDialog(
        onBackConfirmed = {
            activity.finish()
        },
        showDialog = showExitDialog
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorSecondary)
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            NetworkAwarePageTitle(
                title = stringResource(R.string.driver_profile),
                subtitle = stringResource(R.string.information_driving_status),
                networkStatus = networkStatus
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
                        tint = textColor
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = stringResource(R.string.language),
                        fontFamily = appFontFamily ,
                        fontWeight = FontWeight.Normal,
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
    val username by viewModel.username.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val driverId by viewModel.driverId.collectAsState()
    val corporate by viewModel.corporate.collectAsState()
    val vehicleName by viewModel.vehicleName.collectAsState()
    val vehicleType by viewModel.vehicleType.collectAsState()
    val licensePlate by viewModel.licensePlate.collectAsState()
    val fuelTypeName by viewModel.fuelTypeName.collectAsState()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(white),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier
                    .size(76.dp)
                    .background(iconBg, CircleShape)
                    .padding(16.dp),
                tint = green_primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!username.isNullOrBlank()) {
                Text(
                    text = username!!,
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (!phone.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = phone!!,
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (!corporate.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = corporate!!,
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = green_primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(lightGreen, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileInfoRow(stringResource(R.string.vehicle_assigned_id), driverId)
            ProfileInfoRow(stringResource(R.string.vehicle_name), vehicleName)
            ProfileInfoRow(stringResource(R.string.vehicle_type), vehicleType)
            ProfileInfoRow(stringResource(R.string.license_number), licensePlate)
            ProfileInfoRow(stringResource(R.string.fuel_type), fuelTypeName)
        }
    }
}

@Composable
fun ProfileInfoRow(title: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title,
            color = textPrimary,
            fontFamily = appFontFamily ,
            fontWeight = FontWeight.Normal
        )
        Text(
            value,
            fontFamily = appFontFamily ,
            fontWeight = FontWeight.Normal
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun InfoRow(title: String, value: String) {
    ProfileInfoRow(title, value)
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
                fontFamily = appFontFamily ,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = stringResource(R.string.manage_your_account_settings),
                fontFamily = appFontFamily ,
                fontWeight = FontWeight.Normal,
                color = textSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onLogout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                )
            ) {
                Text(
                    stringResource(R.string.log_out),
                    fontFamily = appFontFamily ,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}
