package com.financer.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val Cream = Color(0xFFFFF9E4)
private val DarkNavy = Color(0xFF141852)
private val SoftBlue = Color(0xFFB5D2F5)
private val LightBlue = Color(0xFFD6E6F6)
private val Pink = Color(0xFFE8A0B8)
private val Lavender = Color(0xFFDBC4E8)
private val Peach = Color(0xFFF0C0A0)
private val Terracotta = Color(0xFF8B4A4A)

@Immutable
data class FinancerColors(
    val cream: Color = Cream,
    val darkNavy: Color = DarkNavy,
    val softBlue: Color = SoftBlue,
    val lightBlue: Color = LightBlue,
    val pink: Color = Pink,
    val lavender: Color = Lavender,
    val peach: Color = Peach,
    val terracotta: Color = Terracotta,
)

internal val LocalFinancerColors = staticCompositionLocalOf { FinancerColors() }

private val FinancerColorScheme = lightColorScheme(
    primary = SoftBlue,
    onPrimary = DarkNavy,
    primaryContainer = LightBlue,
    onPrimaryContainer = DarkNavy,
    secondary = Pink,
    onSecondary = DarkNavy,
    secondaryContainer = Pink.copy(alpha = 0.3f),
    onSecondaryContainer = DarkNavy,
    tertiary = Lavender,
    onTertiary = DarkNavy,
    tertiaryContainer = Lavender.copy(alpha = 0.3f),
    onTertiaryContainer = DarkNavy,
    background = Color.White,
    onBackground = DarkNavy,
    surface = Color.White,
    onSurface = DarkNavy,
    surfaceVariant = Cream,
    onSurfaceVariant = DarkNavy,
    error = Terracotta,
    onError = Color.White,
)

object FinancerTheme {
    val colors: FinancerColors
        @Composable get() = LocalFinancerColors.current
}

@Composable
fun FinancerTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalFinancerColors provides FinancerColors(),
    ) {
        MaterialTheme(
            colorScheme = FinancerColorScheme,
            content = content,
        )
    }
}
