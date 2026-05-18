package com.lugkit.stellarextraction.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SpaceBlack = Color(0xFF0A0F1E)
private val SpaceSurface = Color(0xFF111827)
private val SpaceAccent = Color(0xFF00D4FF)
private val SpaceText = Color(0xFFE0E0E0)
private val SpaceDim = Color(0xFF6B7280)

private val ColorScheme = darkColorScheme(
    primary = SpaceAccent,
    onPrimary = Color(0xFF000000),
    background = SpaceBlack,
    onBackground = SpaceText,
    surface = SpaceSurface,
    onSurface = SpaceText,
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = SpaceDim
)

@Composable
fun StellarExtractionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        content = content
    )
}
