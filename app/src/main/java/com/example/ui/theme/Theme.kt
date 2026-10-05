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
    primary = NewsRed,
    onPrimary = Color.White,
    primaryContainer = NewsRedDark,
    onPrimaryContainer = Color.White,
    secondary = AccentBlue,
    onSecondary = Color.White,
    secondaryContainer = SurfaceNavy,
    onSecondaryContainer = Color.White,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = BorderSlate
)

private val LightColorScheme = lightColorScheme(
    primary = NewsRed,
    onPrimary = Color.White,
    primaryContainer = NewsRedLight,
    onPrimaryContainer = NewsRedDark,
    secondary = AccentBlue,
    onSecondary = Color.White,
    secondaryContainer = SlateLightGray,
    onSecondaryContainer = DeepNavy,
    background = SlateLight,
    surface = Color.White,
    onBackground = DeepNavy,
    onSurface = DeepNavy,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = SlateGray,
    outline = SlateLightGray
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded news identity
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
