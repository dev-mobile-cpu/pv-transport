package com.pv.transport.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pv.transport.R
import com.pv.transport.data.ExpenseData
import com.pv.transport.extension.withComma
import com.pv.transport.ui.theme.DetailItem
import com.pv.transport.ui.theme.DetailPhotoThumbnail
import com.pv.transport.ui.theme.DetailSectionCard
import com.pv.transport.ui.theme.StatusBadge
import com.pv.transport.ui.theme.appFontFamily
import com.pv.transport.ui.theme.colorPrimary
import com.pv.transport.ui.theme.textSecondary
import com.pv.transport.ui.theme.white

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherExpenseDetailScreen(
    data: ExpenseData,
    navController: NavController
) {
    val amount = data.amount.orEmpty()
    val costName = data.typeOfCost.name.orEmpty()
    val docs = data.documents.orEmpty()
    val syncState = data.syncState

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.other_expense_detail),
                        color = Color.Black,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            modifier = Modifier.size(20.dp),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = white),
                windowInsets = WindowInsets(0)
            )
        },
        containerColor = Color(0xFFF4F4F4)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = costName.ifBlank { stringResource(R.string.type_of_cost) },
                    fontSize = 13.sp,
                    fontFamily = appFontFamily,
                    color = textSecondary
                )
                Text(
                    text = "${amount.withComma()} Ks",
                    fontSize = 28.sp,
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = colorPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                syncState?.let { StatusBadge(status = it) }
            }

            DetailSectionCard(title = stringResource(R.string.transaction_info)) {
                DetailItem(label = stringResource(R.string.date), value = data.date.orEmpty())
                DetailItem(label = stringResource(R.string.type_of_cost), value = costName)
                DetailItem(
                    label = stringResource(R.string.expense_amount),
                    value = "${amount.withComma()} Ks"
                )
                DetailItem(
                    label = stringResource(R.string.license_number),
                    value = data.licensePlate.orEmpty()
                )
                if (data.createdAt.isNotBlank()) {
                    DetailItem(label = stringResource(R.string.created_at), value = data.createdAt)
                }
                if (data.updatedAt.isNotBlank()) {
                    DetailItem(label = stringResource(R.string.updated_at), value = data.updatedAt)
                }
            }

            if (docs.isNotEmpty()) {
                DetailSectionCard(title = stringResource(R.string.expense_proof)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        docs.forEachIndexed { index, doc ->
                            val url = doc.documentUrl.takeIf { it.isNotBlank() }
                                ?: doc.fileName.takeIf { it.isNotBlank() }
                            DetailPhotoThumbnail(
                                label = doc.kindOfDoc.ifBlank {
                                    "${stringResource(R.string.expense_proof)} ${index + 1}"
                                },
                                imageUrl = url
                            )
                        }
                    }
                }
            }
        }
    }
}
