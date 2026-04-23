package com.pv.transport.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.pv.transport.extension.FuelLogNavHost
import com.pv.transport.extension.FuelRequestNavHost
import kotlinx.coroutines.launch

@Composable
fun FuelTabScreen() {
    val tabs = listOf("Fuel Request", "Fuel Log", "Wallet")

    val pagerState = rememberPagerState(
        pageCount = { tabs.size }
    )
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        val showTabs = remember { mutableStateOf(true) }
        if (showTabs.value){
            TabRow(
                selectedTabIndex = pagerState.currentPage
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->

            when (page) {
                0 -> FuelRequestNavHost(showTabs)
                1 -> FuelLogNavHost(showTabs)
                2 -> WalletScreen()
            }
        }
    }
}