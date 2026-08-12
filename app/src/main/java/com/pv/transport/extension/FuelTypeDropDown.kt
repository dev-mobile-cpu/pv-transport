package com.pv.transport.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pv.transport.data.fuel.FuelType
import com.pv.transport.ui.theme.FormSelect

@Composable
fun FuelTypeDropDown(
    types: List<FuelType>,
    selectedType: String,
    onTypeSelected: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    FormSelect(
        selectedLabel = selectedType,
        options = types.map { it.name },
        onSelected = { index, _ ->
            val type = types.getOrNull(index) ?: return@FormSelect
            onTypeSelected(type.id, type.name)
        },
        modifier = modifier
    )
}
