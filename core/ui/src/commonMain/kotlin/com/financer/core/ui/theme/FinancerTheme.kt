package com.financer.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Cream = Color(0xFFF0E4D4)
private val DarkNavy = Color(0xFF141852)
private val SoftBlue = Color(0xFFB5D2F5)
private val Pink = Color(0xFFE8A0B8)
private val Lavender = Color(0xFFDBC4E8)
private val Peach = Color(0xFFF0C0A0)
private val Terracotta = Color(0xFF8B4A4A)

private val FinancerColorScheme = lightColorScheme(
    primary = SoftBlue,
    onPrimary = DarkNavy,
    primaryContainer = SoftBlue.copy(alpha = 0.3f),
    onPrimaryContainer = DarkNavy,
    secondary = Pink,
    onSecondary = DarkNavy,
    secondaryContainer = Pink.copy(alpha = 0.3f),
    onSecondaryContainer = DarkNavy,
    tertiary = Lavender,
    onTertiary = DarkNavy,
    tertiaryContainer = Lavender.copy(alpha = 0.3f),
    onTertiaryContainer = DarkNavy,
    background = Cream,
    onBackground = DarkNavy,
    surface = Cream,
    onSurface = DarkNavy,
    surfaceVariant = Peach.copy(alpha = 0.3f),
    onSurfaceVariant = DarkNavy,
    error = Terracotta,
    onError = Color.White,
)

@Composable
fun FinancerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FinancerColorScheme,
        content = content,
    )
}
