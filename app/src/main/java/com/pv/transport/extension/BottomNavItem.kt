package com.pv.transport.extension


import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.pv.transport.R


sealed class BottomNavItem(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector
) {
    object Logs : BottomNavItem("logs", R.string.log, Icons.Default.MenuBook)
    object Fuel : BottomNavItem("fuel", R.string.fuel, Icons.Default.LocalGasStation)
    object Approval : BottomNavItem("approval", R.string.approval, Icons.Default.CheckCircle)
    object Expense : BottomNavItem("expense", R.string.expense, Icons.Default.Money)
    object Profile : BottomNavItem("profile", R.string.profile, Icons.Default.Person)
}

