package com.mnowo.transportationalarmclock.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = darkPrimary,
    primaryVariant = darkPrimaryVariant,
    secondary = darkSecondary,
    secondaryVariant = darkSecondaryVariant,
    background = darkBackground,
    surface = darkSurface,
    error = darkError,
    onPrimary = darkOnPrimary,
    onSecondary = darkOnSecondary,
    onBackground = darkOnBackground,
    onSurface = darkOnSurface,
    onError = darkOnError
)

private val LightColorPalette = lightColors(
    primary = lightPrimary,
    primaryVariant = lightPrimaryVariant,
    secondary = lightSecondary,
    secondaryVariant = lightSecondaryVariant,
    background = lightBackground,
    surface = lightSurface,
    error = lightError,
    onPrimary = lightOnPrimary,
    onSecondary = lightOnSecondary,
    onBackground = lightOnBackground,
    onSurface = lightOnSurface,
    onError = lightOnError
)

@Composable
fun TransportationAlarmClockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}