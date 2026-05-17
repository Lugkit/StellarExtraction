package com.lugkit.stellarextraction.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SpaceBlack   = Color(0xFF050A18)
val SpaceDark    = Color(0xFF0D1B2A)
val SpacePanel   = Color(0xFF112240)
val SpaceAccent  = Color(0xFF00B4D8)
val SpaceAccent2 = Color(0xFF7B2FBE)
val SpaceText    = Color(0xFFCCD6F6)
val SpaceSubtext = Color(0xFF8892B0)

private val DarkColors = darkColorScheme(
    primary          = SpaceAccent,
    onPrimary        = Color.Black,
    secondary        = SpaceAccent2,
    onSecondary      = Color.White,
    background       = SpaceBlack,
    onBackground     = SpaceText,
    surface          = SpacePanel,
    onSurface        = SpaceText,
    surfaceVariant   = SpaceDark,
    onSurfaceVariant = SpaceSubtext,
    outline          = SpaceAccent.copy(alpha = 0.3f)
)

@Composable
fun StellarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}
