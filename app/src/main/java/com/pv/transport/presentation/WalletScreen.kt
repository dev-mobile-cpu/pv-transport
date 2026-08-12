package com.pv.transport.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.automirrored.filled.HelpOutline
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
import com.pv.transport.extension.withComma
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.iconBg
import com.pv.transport.ui.theme.iconColor
import com.pv.transport.ui.theme.red
import com.pv.transport.ui.theme.textPrimary
import com.pv.transport.ui.theme.textSecondary
import com.pv.transport.ui.theme.white
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
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ){
                    Text(text = state.message, color = Color.Red)
                }

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
fun WalletBalanceItem(title: String, balance: Balance) {

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(white),
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = title,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = textSecondary
                    )
                    Text(
                        text = balance.total.withComma(),
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (title == "Cash") {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Due From Office",
                            fontFamily = appFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = textSecondary
                        )
                        Text(
                            text = balance.due!!.withComma() ?: "-",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = appFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

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
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = textSecondary
                    )
                    Text(
                        text = balance.earmarked.withComma(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,

                    ) {
                    Text(
                        text = "Available",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = textSecondary
                    )
                    Text(
                        text = balance.available.withComma(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

        }

    }

}

@Composable
fun TransactionCard(transactionId: String, amount: String, type: String, date: String) {
    val isMoneyIn = type == "fuel_request_approved" || type == "due_settled"

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
                val (icon, iconTint) = when (type) {
                    "fuel_request_approved", "due_settled" -> Icons.Default.AddCircle to iconColor
                    "fuel_log_cash" -> Icons.Default.Payments to red
                    "fuel_log_credit" -> Icons.Default.LocalGasStation to red
                    else -> Icons.AutoMirrored.Filled.HelpOutline to Color.Gray
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {

            Text(
                text =
                    when (type) {
                        "fuel_request_approved" -> {
                            stringResource(R.string.approved_top_up)

                        }
                        "fuel_log_cash" -> {
                            stringResource(R.string.fuel_log_cash)

                        }
                        "fuel_log_credit" -> {
                            stringResource(R.string.fuel_log_credit)

                        }
                        "due_settled" -> {
                            stringResource(R.string.due_settled)

                        }
                        else -> {
                            stringResource(R.string.unknown)
                        }
                    },
                fontSize = 14.sp,
                fontFamily = appFontFamily ,
                fontWeight = FontWeight.SemiBold,
                color = textSecondary
            )

            val dateFormatted = date.replace("T", " ").split("+").first()

            Text(
                text = dateFormatted,
                fontSize = 12.sp,
                fontFamily = appFontFamily ,
                fontWeight = FontWeight.Normal,
                color = textPrimary
            )
        }

        Text(
            text = if (isMoneyIn) "+ ${amount.withComma()} Ks" else "- ${amount.withComma()} Ks",
            fontSize = 14.sp,
            fontFamily = appFontFamily ,
            fontWeight = FontWeight.SemiBold,
            color = if (isMoneyIn) iconColor else red
        )
    }
}
