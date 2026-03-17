package com.financer.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
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
private val LightGray = Color(0xFFF4F4F4)

private val Green = Color(0xFF199201)
private val Gray = Color(0xFFCCD0CC)
private val Night = Color(0xFF0F1226)
private val NightSurface = Color(0xFF161A32)
private val NightVariant = Color(0xFF222844)
private val NightText = Color(0xFFF3F5FF)

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
    val green: Color = Green,
    val gray: Color = Gray,
    val lightGray: Color = LightGray
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

private val FinancerDarkColorScheme = darkColorScheme(
    primary = SoftBlue,
    onPrimary = DarkNavy,
    primaryContainer = Color(0xFF2C385F),
    onPrimaryContainer = NightText,
    secondary = Pink,
    onSecondary = DarkNavy,
    secondaryContainer = Color(0xFF5D3A52),
    onSecondaryContainer = NightText,
    tertiary = Lavender,
    onTertiary = DarkNavy,
    tertiaryContainer = Color(0xFF4A4365),
    onTertiaryContainer = NightText,
    background = Night,
    onBackground = NightText,
    surface = NightSurface,
    onSurface = NightText,
    surfaceVariant = NightVariant,
    onSurfaceVariant = Color(0xFFD5D9F0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

object FinancerTheme {
    val colors: FinancerColors
        @Composable get() = LocalFinancerColors.current
}

@Composable
fun FinancerTheme(content: @Composable () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()

    CompositionLocalProvider(
        LocalFinancerColors provides FinancerColors(),
    ) {
        MaterialTheme(
            colorScheme = if (isDarkTheme) FinancerDarkColorScheme else FinancerColorScheme,
            content = content,
        )
    }
}
