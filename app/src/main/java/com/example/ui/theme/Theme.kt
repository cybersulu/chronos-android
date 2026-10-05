package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ElectricIndigoDark,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF2E335A),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = NeonCyanDark,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF133E48),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = AmberGoldLight,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = Color(0xFF5A3008),
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = ChronosDarkBg,
    onBackground = Color(0xFFF1F5F9),
    surface = ChronosDarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = ChronosDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = NeonCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF083344),
    tertiary = AmberGold,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF451A03),
    background = ChronosLightBg,
    onBackground = Color(0xFF0F172A),
    surface = ChronosLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = ChronosLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep Chronos' distinctive cosmic styling consistent
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
