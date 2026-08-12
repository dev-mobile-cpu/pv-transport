package com.pv.transport.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pv.transport.data.log.ReasonListResponse
import com.pv.transport.ui.theme.FormSelect

@Composable
fun ReasonDropdown(
    reasons: List<ReasonListResponse>,
    selectedReason: String,
    onReasonSelected: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    FormSelect(
        selectedLabel = selectedReason,
        options = reasons.map { it.value },
        onSelected = { index, _ ->
            val reason = reasons.getOrNull(index) ?: return@FormSelect
            onReasonSelected(reason.id.toInt(), reason.value)
        },
        modifier = modifier
    )
}
