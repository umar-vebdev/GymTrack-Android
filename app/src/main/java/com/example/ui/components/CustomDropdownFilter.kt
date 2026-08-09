package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CustomDropdownFilter(
    options: Map<String, String>,
    selectedKey: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    customSelectedLabel: String? = null
) {
    val currentLabel = customSelectedLabel ?: options[selectedKey] ?: options.values.firstOrNull() ?: ""
    FilterDropdownChip(
        label = currentLabel,
        options = options,
        onItemSelected = onItemSelected,
        modifier = modifier
    )
}
