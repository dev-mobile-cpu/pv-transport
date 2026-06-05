package com.pv.transport.extension


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Logs : BottomNavItem("logs", "Log", Icons.Default.MenuBook)
    object Fuel : BottomNavItem("fuel", "Fuel", Icons.Default.LocalGasStation)
    object Approval : BottomNavItem("approval", "Approval", Icons.Default.CheckCircle)
    object Expense : BottomNavItem("expense", "Expense", Icons.Default.Money)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
}

