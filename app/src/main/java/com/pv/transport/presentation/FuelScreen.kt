package com.pv.transport.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.colorSecondary

@Composable
fun FuelScreen(){
    Column(modifier = Modifier.fillMaxSize().background(colorSecondary)) {
        Text(text = "FuelScreen")
    }
}