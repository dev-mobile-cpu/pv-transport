package com.pv.transport.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pv.transport.R
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.data.fuel.FuelLogData
import com.pv.transport.data.fuel.FuelRequestData
import com.pv.transport.extension.activityHiltViewModel
import com.pv.transport.extension.navEnterSlide
import com.pv.transport.extension.navExitSlide
import com.pv.transport.extension.navPopEnterSlide
import com.pv.transport.extension.navPopExitSlide
import com.pv.transport.extension.rememberNavPayload
import com.pv.transport.network.ConnectivityObserver
import com.pv.transport.ui.theme.AddActionButton
import com.pv.transport.ui.theme.NetworkAwarePageTitle
import com.pv.transport.ui.theme.SegmentedTabs
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.viewmodels.NetworkStatusViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun FuelTabScreen(
    onRouteChanged: (String) -> Unit,
    networkViewModel: NetworkStatusViewModel = activityHiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        val route = navBackStackEntry?.destination?.route ?: "fuel_tabs"
        onRouteChanged(route)
    }

    NavHost(
        navController = navController,
        startDestination = "fuel_tabs",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("fuel_tabs") {
            FuelTabsContent(
                navController = navController,
                networkViewModel = networkViewModel
            )
        }

        composable(
            "add_fuel_request",
            enterTransition = { navEnterSlide() },
            exitTransition = { navExitSlide() },
            popEnterTransition = { navPopEnterSlide() },
            popExitTransition = { navPopExitSlide() }
        ) {
            AddFuelRequestScreen(navController)
        }

        composable(
            "fuel_request_detail",
            enterTransition = { navEnterSlide() },
            exitTransition = { navExitSlide() },
            popEnterTransition = { navPopEnterSlide() },
            popExitTransition = { navPopExitSlide() }
        ) {
            val request = navController.rememberNavPayload<FuelRequestData>("fuel_request_detail")
            if (request != null) {
                FuelRequestDetailScreen(request, navController)
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack("fuel_request_detail", inclusive = true)
                }
            }
        }

        composable(
            "add_fuel_log",
            enterTransition = { navEnterSlide() },
            exitTransition = { navExitSlide() },
            popEnterTransition = { navPopEnterSlide() },
            popExitTransition = { navPopExitSlide() }
        ) {
            AddFuelLogScreen(navController)
        }

        composable(
            "fuel_log_detail",
            enterTransition = { navEnterSlide() },
            exitTransition = { navExitSlide() },
            popEnterTransition = { navPopEnterSlide() },
            popExitTransition = { navPopExitSlide() }
        ) {
            val log = navController.rememberNavPayload<FuelLogData>("fuel_log_detail")
            if (log != null) {
                FuelLogDetailScreen(log, navController)
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack("fuel_log_detail", inclusive = true)
                }
            }
        }
    }
}

@Composable
private fun FuelTabsContent(
    navController: NavHostController,
    networkViewModel: NetworkStatusViewModel
) {
    val context = LocalContext.current
    val authPrefs = AuthPrefs(context)
    val driverType = authPrefs.getDriverType()
    val isOffice = driverType == "office"
    val networkStatus by networkViewModel.networkStatus.collectAsStateWithLifecycle()
    val isOffline = networkStatus != ConnectivityObserver.Status.Available

    val tabs = when {
        isOffice -> listOf(
            stringResource(R.string.fuel_log),
            stringResource(R.string.wallet)
        )
        else -> listOf(
            stringResource(R.string.fuel_request),
            stringResource(R.string.fuel_log),
            stringResource(R.string.wallet)
        )
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(
        initialPage = selectedTab.coerceIn(0, (tabs.size - 1).coerceAtLeast(0)),
        pageCount = { tabs.size }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page -> selectedTab = page }
    }

    val currentPage = pagerState.currentPage
    val isWalletTab = if (isOffice) currentPage == 1 else currentPage == 2
    val isFuelRequestTab = !isOffice && currentPage == 0
    val isFuelLogTab = if (isOffice) currentPage == 0 else currentPage == 1

    val addButtonLabel = when {
        isFuelRequestTab -> stringResource(R.string.add_request)
        isFuelLogTab -> stringResource(R.string.add_log)
        else -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorSecondary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NetworkAwarePageTitle(
                title = stringResource(R.string.fuel),
                subtitle = stringResource(R.string.track_your_fuel),
                networkStatus = networkStatus,
                modifier = Modifier.weight(1f)
            )

            if (!isWalletTab && !(isFuelRequestTab && isOffline)) {
                AddActionButton(
                    text = addButtonLabel,
                    onClick = {
                        when {
                            isFuelRequestTab -> navController.navigate("add_fuel_request")
                            isFuelLogTab -> navController.navigate("add_fuel_log")
                        }
                    }
                )
            }
        }

        SegmentedTabs(
            tabs = tabs,
            selectedIndex = pagerState.currentPage,
            onTabSelected = { index ->
                scope.launch { pagerState.animateScrollToPage(index) }
            },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp)
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            beyondViewportPageCount = tabs.size
        ) { page ->
            if (isOffice) {
                when (page) {
                    0 -> FuelLogScreen(navController)
                    1 -> WalletScreen()
                }
            } else {
                when (page) {
                    0 -> FuelRequestScreen(navController)
                    1 -> FuelLogScreen(navController)
                    2 -> WalletScreen()
                }
            }
        }
    }
}
