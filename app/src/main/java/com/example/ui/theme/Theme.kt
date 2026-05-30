package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CosmicColorScheme = darkColorScheme(
    primary = CosmicAccentPurple,
    onPrimary = Color(0xFF381E72), // Deep purple text/icon on light purple buttons
    secondary = CosmicAccentGreen,
    onSecondary = Color(0xFF003820), // Deep green on minty details
    tertiary = CosmicAccentRed,
    onTertiary = Color(0xFF601410), // Deep red/maroon on salmon/pink details
    background = CosmicDarkBg,
    onBackground = CosmicTextLight,
    surface = CosmicCardBg,
    onSurface = CosmicTextLight,
    surfaceVariant = CosmicBorder,
    onSurfaceVariant = CosmicTextMuted
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CosmicColorScheme,
        typography = Typography,
        content = content
    )
}
