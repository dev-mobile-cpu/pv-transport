package com.pv.transport.extension

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pv.transport.ui.theme.white

@Composable
fun MainBottomBar(
    currentRoute: String,
    onItemClick: (String) -> Unit
) {

    val items = listOf(
        BottomNavItem.Logs,
        BottomNavItem.Fuel,
        BottomNavItem.Approval,
        BottomNavItem.Expense,
        BottomNavItem.Profile
    )

    NavigationBar(modifier = Modifier.fillMaxWidth(), containerColor = white) {
        items.forEach { item ->

            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item.route) },
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {

                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (selected) Color(0xFF1B8E5F) else Color.Gray
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.title,
                            maxLines = 1,
                            color = if (selected) Color(0xFF1B8E5F) else Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                },
                label = null,
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
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