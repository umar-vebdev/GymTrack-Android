package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CleanMinimalColorScheme = lightColorScheme(
    primary = GymPrimaryIndigo,
    onPrimary = Color.White,
    primaryContainer = GymIndigoContainer,
    onPrimaryContainer = GymIndigoOnContainer,
    secondary = GymIndigoContainer,
    onSecondary = GymIndigoOnContainer,
    secondaryContainer = GymSurfaceVariant,
    onSecondaryContainer = GymTextPrimary,
    tertiary = GymAmberAlert,
    onTertiary = Color.White,
    background = GymBgLight,
    onBackground = GymTextPrimary,
    surface = GymSurfaceWhite,
    onSurface = GymTextPrimary,
    surfaceVariant = GymSurfaceVariant,
    onSurfaceVariant = GymTextSecondary,
    outline = GymCardBorder,
    error = GymRoseAlert
)

@Composable
fun GymTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CleanMinimalColorScheme,
        typography = Typography,
        content = content
    )
}

