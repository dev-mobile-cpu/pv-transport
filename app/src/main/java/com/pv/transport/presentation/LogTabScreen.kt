package com.pv.transport.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pv.transport.R
import com.pv.transport.extension.LogNavHost
import com.pv.transport.extension.LogSheetNavHost
import com.pv.transport.ui.theme.AddActionButton
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.textPrimary
import com.pv.transport.ui.theme.textSecondary
import com.pv.transport.ui.theme.white
import kotlinx.coroutines.launch

/** Set true to show Logsheet tab again. */
private const val ENABLE_LOG_SHEET_TAB = false

@Composable
fun LogTabScreen(
    onRouteChanged: (String) -> Unit,
    resetTab: Boolean = false
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

    // Increment to request create; tabIndex chooses Log vs Logsheet destination.
    var createRequestId by remember { mutableIntStateOf(0) }
    var createRequestTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(resetTab) {
        if (resetTab) {
            pagerState.animateScrollToPage(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorSecondary)
    ) {
        // Title + Create button hidden on form pages to avoid double-header
        if (showTabs.value) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.daily_logs),
                        color = textPrimary,
                        fontSize = 20.sp,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.track_your_daily_trips),
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }

                AddActionButton(
                    text = stringResource(R.string.add_log),
                    onClick = {
                        createRequestTab = pagerState.currentPage
                        createRequestId += 1
                    }
                )
            }

            // Segmented tabs only when Logsheet is enabled
            if (ENABLE_LOG_SHEET_TAB && tabs.size > 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE2E3EC))
                        .padding(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        tabs.forEachIndexed { index, title ->
                            val selected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(if (selected) white else Color.Transparent)
                                    .clickable {
                                        scope.launch { pagerState.animateScrollToPage(index) }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (selected) colorPrimary else textSecondary,
                                    fontFamily = appFontFamily
                                )
                            }
                        }
                    }
                }
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
