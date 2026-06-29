package com.pv.transport.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pv.transport.R
import com.pv.transport.data.fuel.Balance
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.iconBg
import com.pv.transport.ui.theme.iconColor
import com.pv.transport.ui.theme.red
import com.pv.transport.ui.theme.textPrimary
import com.pv.transport.ui.theme.textSecondary
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.DriverLogViewModel
import com.pv.transport.viewmodels.FuelViewModel

@Composable
fun WalletScreen(fuelViewModel: FuelViewModel = hiltViewModel()){
    val walletState by fuelViewModel.walletState.collectAsState()

    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem != null &&
                    lastVisibleItem.index >= totalItems - 5
        }
    }

    LaunchedEffect(Unit) {
        fuelViewModel.getWalletBalance()
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            fuelViewModel.loadMoreTransactions()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorSecondary)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        // Wallet Card
        when (val state = walletState) {
            is FuelViewModel.WalletState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is FuelViewModel.WalletState.Error -> {
                Text(text = state.message, color = Color.Red)
            }
            is FuelViewModel.WalletState.Success -> {
                val walletData = state.response.data
                val transactions = walletData.transactions.data
                        WalletBalanceItem(
                            title = "Cash",
                            balance = walletData.cash
                        )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.recent_transactions),
                    fontFamily = appFontFamily,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(24.dp))
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    if (transactions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(R.string.no_transactions_found), color = Color.Gray)
                            }
                        }
                    }else{

                     item {
                         Card(
                             modifier = Modifier.fillMaxWidth(),
                             shape = RoundedCornerShape(12.dp),
                             colors = CardDefaults.cardColors(containerColor = Color.White)
                         ) {
                             Column {
                                 if (transactions.isEmpty()) {
                                     Text(
                                         text = stringResource(R.string.no_transactions_found),
                                         modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
                                         color = Color.Gray
                                     )
                                 } else {
                                     transactions.forEachIndexed { index, transaction ->
                                         TransactionCard(
                                             transactionId = transaction.id,
                                             amount = transaction.amount,
                                             type = transaction.type,
                                             date = transaction.createdAt
                                         )

                                         if (index < transactions.size - 1) {
                                             HorizontalDivider(
                                                 modifier = Modifier.padding(horizontal = 16.dp),
                                                 thickness = 0.5.dp,
                                                 color = Color.LightGray.copy(alpha = 0.5f)
                                             )
                                         }
                                     }
                                 }
                             }
                         }
                     }

                    }
                    item {
                        if (walletData.transactions.meta.currentPage < walletData.transactions.meta.lastPage) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
            else -> {}
        }
    }

}

@Composable
fun WalletBalanceItem(title: String,balance: Balance) {

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(white),
        modifier = Modifier.fillMaxWidth()
    ){

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontFamily = appFontFamily ,
                fontWeight = FontWeight.Normal,
                color = textSecondary
            )
            Text(
                text = balance.total,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = appFontFamily  ,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Earmarked",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = appFontFamily ,
                        fontWeight = FontWeight.Normal,
                        color = textSecondary
                    )
                    Text(
                        text = balance.earmarked,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = appFontFamily ,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,

                    ) {
                    Text(
                        text = "Available",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = appFontFamily ,
                        fontWeight = FontWeight.Normal,
                        color = textSecondary
                    )
                    Text(
                        text = balance.available,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = appFontFamily ,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

        }

    }

}

@Composable
fun TransactionCard(transactionId: String, amount: String, type: String, date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rounded Icon Background
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = iconBg
        ) {
            Box(contentAlignment = Alignment.Center) {

                Icon(
                    imageVector = if (type == "fuel_request_approved")Icons.Default.CheckCircle else Icons.Default.LocalGasStation,
                    contentDescription = null,
                    tint = if (type == "fuel_request_approved")iconColor else red,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {

            Text(
                text =
                    if (type == "fuel_request_approved") {
                        stringResource(R.string.approved_top_up)

                    } else if (type == "fuel_log_cash") {
                        stringResource(R.string.fuel_log_cash)

                    } else {
                        stringResource(R.string.fuel_log_credit)
                    },
                fontSize = 14.sp,
                fontFamily = appFontFamily ,
                fontWeight = FontWeight.SemiBold,
                color = textSecondary
            )

            val date = date.replace("T", " ").split("+").first()

            Text(
                text = date,
                fontSize = 12.sp,
                fontFamily = appFontFamily ,
                fontWeight = FontWeight.Normal,
                color = textPrimary
            )
        }

        Text(
            text = if (type == "fuel_request_approved") amount else "- $amount",
            fontSize = 14.sp,
            fontFamily = appFontFamily ,
            fontWeight = FontWeight.SemiBold,
            color = textPrimary
        )
    }
}

