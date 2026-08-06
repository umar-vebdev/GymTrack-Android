package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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

private val DarkMinimalColorScheme = darkColorScheme(
    primary = Color(0xFF8B9DF0),
    onPrimary = Color(0xFF141A38),
    primaryContainer = Color(0xFF2E3B70),
    onPrimaryContainer = Color(0xFFE0E5FF),
    secondary = Color(0xFF2E3B70),
    onSecondary = Color(0xFFE0E5FF),
    secondaryContainer = Color(0xFF262A38),
    onSecondaryContainer = Color(0xFFE1E2EC),
    tertiary = GymAmberAlert,
    onTertiary = Color.White,
    background = Color(0xFF11131A),
    onBackground = Color(0xFFE3E5ED),
    surface = Color(0xFF1A1D27),
    onSurface = Color(0xFFE3E5ED),
    surfaceVariant = Color(0xFF242836),
    onSurfaceVariant = Color(0xFFA0A5B5),
    outline = Color(0xFF2E3346),
    error = GymRoseAlert
)

@Composable
fun GymTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkMinimalColorScheme else CleanMinimalColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
