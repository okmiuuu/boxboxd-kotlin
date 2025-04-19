package com.example.boxboxd.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Red,
    secondary = AccentRed,
    tertiary = White,
    background = DarkGrey,
    surface = LightGrey,
    onSurface = MediumGrey,
    error = BrightRed
)

private val LightColorScheme = lightColorScheme(
    primary = Red,
    secondary = AccentRed,
    tertiary = DarkGrey,
    background = White,
    surface = LightGrey,
    onSurface = MediumGrey
)

val textColor = { isDarkTheme: Boolean -> if (isDarkTheme) DarkColorScheme.tertiary else LightColorScheme.tertiary }

@Composable
fun BoxboxdTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
        typography =  createTypography(isDarkTheme = darkTheme),
        content = content
    )
}