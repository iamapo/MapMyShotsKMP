package com.redred.mapmyshots.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MapMyShotsColorScheme = lightColorScheme(
    primary = MapMyShotsColors.primary,
    secondary = MapMyShotsColors.textMuted,
    background = MapMyShotsColors.background,
    surface = MapMyShotsColors.surface,
    onSurface = MapMyShotsColors.textPrimary
)

@Composable
fun MapMyShotsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MapMyShotsColorScheme,
        content = content
    )
}
