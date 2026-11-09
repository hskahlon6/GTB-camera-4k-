package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CinemaColorScheme = darkColorScheme(
    primary = CinemaGreenActive,
    onPrimary = CinemaPureBlack,
    primaryContainer = CinemaSurfaceLight,
    onPrimaryContainer = CinemaGreenActive,
    secondary = CinemaCyan4K,
    onSecondary = CinemaPureBlack,
    secondaryContainer = CinemaSurface,
    onSecondaryContainer = CinemaCyan4K,
    tertiary = CinemaRedRecord,
    onTertiary = CinemaWhite,
    background = CinemaPureBlack,
    onBackground = CinemaWhite,
    surface = CinemaSurface,
    onSurface = CinemaWhite,
    surfaceVariant = CinemaSurfaceLight,
    onSurfaceVariant = CinemaTextSecondary,
    outline = CinemaPanelBorder
)

@Composable
fun GTBCameraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CinemaColorScheme,
        typography = Typography,
        content = content
    )
}
