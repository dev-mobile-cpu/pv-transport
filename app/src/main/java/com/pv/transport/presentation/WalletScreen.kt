package com.pv.transport.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pv.transport.data.fuel.Balance
import com.pv.transport.viewmodels.FuelViewModel

@Composable
fun WalletScreen(fuelViewModel: FuelViewModel = hiltViewModel()){
    val context = LocalContext.current
    val walletState = fuelViewModel.walletState.collectAsState()
    LaunchedEffect(Unit) {
        fuelViewModel.getWalletBalance()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        // Wallet Card
        when (val state = walletState.value) {
            is FuelViewModel.WalletState.Loading -> {
                CircularProgressIndicator()
            }

            is FuelViewModel.WalletState.Error -> {
                Text(text = state.message, color = Color.Red)
            }

            is FuelViewModel.WalletState.Success -> {

                val walletData = state.response.data

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f).height(120.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        WalletBalanceItem(
                            title = "Credit",
                            balance = walletData.credit)
                    }
                    Card(
                        modifier = Modifier.weight(1f).height(120.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        WalletBalanceItem(
                            title = "Cash",
                            balance = walletData.cash
                        )
                    }
                }
            }
            else -> {}
        }
    }

}

@Composable
fun WalletBalanceItem(title: String,balance: Balance) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Text(
            text = balance.total,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
