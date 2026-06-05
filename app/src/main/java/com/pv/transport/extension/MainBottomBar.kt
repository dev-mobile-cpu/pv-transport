package com.pv.transport.extension

import android.R.attr.bottom
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.red
import com.pv.transport.ui.theme.white

@Composable
fun MainBottomBar(
    currentRoute: String,
    onItemClick: (String) -> Unit
) {

    val authPrefs = AuthPrefs(LocalContext.current)
    val driverType = authPrefs.getDriverType()

    val items = when (driverType) {
        "office" -> listOf(
            BottomNavItem.Logs,
            BottomNavItem.Fuel,
            BottomNavItem.Expense,
            BottomNavItem.Profile
        )
        else -> listOf(
            BottomNavItem.Logs,
            BottomNavItem.Fuel,
            BottomNavItem.Approval,
            BottomNavItem.Expense,
            BottomNavItem.Profile
        )
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        horizontalArrangement = Arrangement.SpaceAround
    ) {

        items.forEach { item ->

            val selected = currentRoute == item.route

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,

                modifier = Modifier
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null
                    ) {
                        onItemClick(item.route)
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {

                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = if (selected)
                        colorPrimary
                    else
                        Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.title,
                    fontSize = 10.sp,
                    color = if (selected)
                        colorPrimary
                    else
                        Color.Gray
                )
            }
        }
    }

}


//@Preview(showBackground = true)
//@Composable
//fun PreviewSplashScreen() {
//    // You can preview the SplashScreen composable in Android Studio.
//    // Replace 'NavController' and 'Context' with mock data for previewing purposes.
//    MainBottomBar(
//        currentRoute = "logs",
//        onItemClick = {}
//    )
//}