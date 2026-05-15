package com.seniorsafe.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary          = Primary500,
    onPrimary        = Neutral000,
    surface          = Neutral000,
    onSurface        = Neutral900,
    background       = Neutral050,
    onBackground     = Neutral900,
    error            = Danger500,
    onError          = Neutral000,
)

@Composable
fun SeniorSafeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = Typography,
        content     = content
    )
}
