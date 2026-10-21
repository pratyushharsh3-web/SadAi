package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SadbhavnaDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF04101E),
    primaryContainer = Color(0xFF0E3A5A),
    onPrimaryContainer = ElectricCyan,
    secondary = NeonSky,
    onSecondary = Color(0xFF04101E),
    secondaryContainer = Color(0xFF132A4A),
    onSecondaryContainer = NeonSky,
    tertiary = AccentPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF2E1A47),
    onTertiaryContainer = Color(0xFFD8B4FE),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF1E355B),
    outlineVariant = Color(0xFF12233F),
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SadbhavnaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
