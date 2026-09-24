package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PhishGuardColorScheme = darkColorScheme(
    primary = CyberPrimary,
    onPrimary = CyberOnPrimary,
    primaryContainer = CyberSurfaceVariant,
    onPrimaryContainer = CyberPrimary,
    secondary = CyberSecondary,
    onSecondary = CyberOnSecondary,
    tertiary = CyberWarning,
    error = CyberAlert,
    onError = Color.White,
    background = CyberBg,
    onBackground = CyberTextPrimary,
    surface = CyberSurface,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberOutline
)

@Composable
fun PhishGuardTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PhishGuardColorScheme,
        typography = Typography,
        content = content
    )
}
