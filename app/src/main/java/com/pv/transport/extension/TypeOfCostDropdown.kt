package com.pv.transport.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pv.transport.data.CostType
import com.pv.transport.ui.theme.FormSelect

@Composable
fun TypeOfCostDropdown(
    reasons: List<CostType>,
    selectedReason: String,
    onReasonSelected: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    FormSelect(
        selectedLabel = selectedReason,
        options = reasons.map { it.name },
        onSelected = { index, _ ->
            val cost = reasons.getOrNull(index) ?: return@FormSelect
            onReasonSelected(cost.id.toInt(), cost.name)
        },
        modifier = modifier
    )
}
