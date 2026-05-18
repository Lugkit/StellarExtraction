package com.lugkit.stellarextraction.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AsteroidsGreen = Color(0xFF00FF41)

private val ColorScheme = darkColorScheme(
    primary = AsteroidsGreen,
    onPrimary = Color(0xFF000000),
    background = Color(0xFF000000),
    onBackground = AsteroidsGreen,
    surface = Color(0xFF050505),
    onSurface = AsteroidsGreen,
    surfaceVariant = Color(0xFF0A0A0A),
    onSurfaceVariant = Color(0xFF006620)
)

@Composable
fun StellarExtractionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        content = content
    )
}
