package com.yorklunchclub.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val YorkLunchClubColorScheme = lightColorScheme(
    primary = Amber,
    onPrimary = Color.White,
    primaryContainer = AmberLight,
    onPrimaryContainer = TextPrimary,
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = TealLight,
    onSecondaryContainer = TextPrimary,
    background = Background,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = AmberLight,
    onSurfaceVariant = TextMuted,
    outline = TextMuted
)

@Composable
fun YorkLunchClubTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = YorkLunchClubColorScheme,
        typography = Typography,
        content = content
    )
}
