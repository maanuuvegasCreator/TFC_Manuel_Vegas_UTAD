package com.example.imdbapplogin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = PurplePrimary,
    secondary = PurpleSecondary,
    background = BackgroundLight,
    surface = BackgroundLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark,
    error = ErrorRed,
)

private val DarkColors = darkColorScheme(
    primary = PurpleLight,
    secondary = PurpleSecondary,
    background = TextDark,
    surface = TextDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = BackgroundLight,
    onSurface = BackgroundLight,
    error = ErrorRed,
)

@Composable
fun IMDbAppLoginTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
