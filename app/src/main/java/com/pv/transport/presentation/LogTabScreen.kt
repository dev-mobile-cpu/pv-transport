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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.os.SystemClock
import com.pv.transport.R
import com.pv.transport.extension.LogNavHost
import com.pv.transport.extension.LogSheetNavHost
import com.pv.transport.ui.theme.AddActionButton
import com.pv.transport.ui.theme.CollapsibleTitleSlot
import com.pv.transport.ui.theme.LocalCollapsibleChrome
import com.pv.transport.ui.theme.NetworkAwarePageTitle
import com.pv.transport.ui.theme.SegmentedTabs
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.viewmodels.NetworkStatusViewModel
import kotlinx.coroutines.launch

/** Set true to show Logsheet tab again. */
private const val ENABLE_LOG_SHEET_TAB = false

@Composable
fun LogTabScreen(
    onRouteChanged: (String) -> Unit,
    resetTab: Boolean = false,
    networkViewModel: NetworkStatusViewModel = hiltViewModel()
) {

    val tabs = buildList {
        add(stringResource(R.string.log))
        if (ENABLE_LOG_SHEET_TAB) {
            add(stringResource(R.string.log_sheet))
        }
    }
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val showTabs = remember { mutableStateOf(true) }
    val networkStatus by networkViewModel.networkStatus.collectAsStateWithLifecycle()
    val chromeState = LocalCollapsibleChrome.current

    var createRequestId by remember { mutableIntStateOf(0) }
    var createRequestTab by remember { mutableIntStateOf(0) }
    var lastCreateClickAt by remember { mutableStateOf(0L) }

    LaunchedEffect(resetTab) {
        if (resetTab) {
            pagerState.animateScrollToPage(0)
        }
    }

    LaunchedEffect(showTabs.value) {
        if (showTabs.value) chromeState?.show()
    }

    val createButtonLabel = if (ENABLE_LOG_SHEET_TAB && pagerState.currentPage == 1) {
        stringResource(R.string.add_log_sheet)
    } else {
        stringResource(R.string.add_log)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorSecondary)
    ) {
        if (showTabs.value) {
            CollapsibleTitleSlot(visible = chromeState?.titleVisible != false) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NetworkAwarePageTitle(
                        title = stringResource(R.string.daily_logs),
                        subtitle = stringResource(R.string.track_your_daily_trips),
                        networkStatus = networkStatus,
                        modifier = Modifier.weight(1f)
                    )

                    AddActionButton(
                        text = createButtonLabel,
                        onClick = {
                            val now = SystemClock.elapsedRealtime()
                            if (now - lastCreateClickAt < 1000L) return@AddActionButton
                            lastCreateClickAt = now
                            createRequestTab = pagerState.currentPage
                            createRequestId += 1
                        }
                    )
                }
            }

            if (ENABLE_LOG_SHEET_TAB && tabs.size > 1) {
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
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = showTabs.value && ENABLE_LOG_SHEET_TAB
        ) { page ->
            when (page) {
                0 -> LogNavHost(
                    showTabs = showTabs,
                    onRouteChanged = onRouteChanged,
                    createRequestId = if (createRequestTab == 0) createRequestId else 0
                )
                1 -> LogSheetNavHost(
                    showTabs = showTabs,
                    onRouteChanged = onRouteChanged,
                    createRequestId = if (createRequestTab == 1) createRequestId else 0
                )
            }
        }
    }
}
