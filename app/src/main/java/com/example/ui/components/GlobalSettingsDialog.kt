package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.GymViewModel

val LocalSettingsAction = staticCompositionLocalOf<() -> Unit> { {} }

@Composable
fun GlobalSettingsButton(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val onClick = LocalSettingsAction.current
    IconButton(
        onClick = onClick,
        modifier = modifier.size(44.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Настройки",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
