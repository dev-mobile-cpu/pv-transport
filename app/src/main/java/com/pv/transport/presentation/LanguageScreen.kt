package com.pv.transport.presentation

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pv.transport.R
import com.pv.transport.ui.theme.colorSecondary
import com.pv.transport.ui.theme.robotoFontFamily
import com.pv.transport.ui.theme.textPrimary
import com.pv.transport.ui.theme.white
import com.pv.transport.viewmodels.ProfileViewModel

@Composable
fun LanguageScreen(onBack: () -> Unit, onLanguageChanged: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()) {

    val context = LocalContext.current
    val languages = listOf("English", "မြန်မာ")
    var selectedLanguage by remember { mutableStateOf(viewModel.getLanguage() ?: "en") }
    println("Current Language: $selectedLanguage")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorSecondary)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(white)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier.clickable { onBack() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.language),
                fontFamily = robotoFontFamily,
                fontWeight = FontWeight.Normal,
                color = textPrimary,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language List Card
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {

                languages.forEachIndexed { index, lang ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clickable {
                                println("Selected Language: $lang")
                                selectedLanguage = if (lang == "English") "en" else "my"
                                viewModel.saveLanguage(selectedLanguage)
                                (context as Activity).recreate()
                                onLanguageChanged()

                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = lang,
                            modifier = Modifier.weight(1f),
                            fontFamily = robotoFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp
                        )

                        if ((selectedLanguage == "en" && lang == "English") || (selectedLanguage == "my" && lang == "မြန်မာ")) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFF1565C0)
                            )
                        }
                    }

                    if (index != languages.lastIndex) {
                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )
                    }
                }
            }
        }
    }
}