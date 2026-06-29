package com.pv.transport.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pv.transport.R
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.extension.FuelLogNavHost
import com.pv.transport.extension.FuelRequestNavHost
import kotlinx.coroutines.launch

@Composable
fun FuelTabScreen(
    onRouteChanged: (String) -> Unit
) {
    val authPrefs = AuthPrefs(LocalContext.current)
    val driverType = authPrefs.getDriverType()


    val tabs = when (driverType) {
        "office" -> listOf(
            stringResource(R.string.fuel_log),
            stringResource(R.string.wallet)
        )
        else -> listOf(
            stringResource(R.string.fuel_request),
            stringResource(R.string.fuel_log),
            stringResource(R.string.wallet)
        )
    }


    val pagerState = rememberPagerState(
        pageCount = { tabs.size }
    )
    val scope = rememberCoroutineScope()


    Column(modifier = Modifier.fillMaxSize()) {
        val showTabs = remember { mutableStateOf(true) }
        if (showTabs.value) {
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
            modifier = Modifier.weight(1f),
            userScrollEnabled = showTabs.value

        ) { page ->
            if (driverType == "office") {
                when (page) {
                    0 -> FuelLogNavHost(showTabs, onRouteChanged)
                    1 -> WalletScreen()
                }
            } else{
                when (page) {
                    0 -> FuelRequestNavHost(showTabs,onRouteChanged)
                    1 -> FuelLogNavHost(showTabs,onRouteChanged)
                    2 -> WalletScreen()
                }
            }
        }
    }
}