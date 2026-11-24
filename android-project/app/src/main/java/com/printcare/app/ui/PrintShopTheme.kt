package com.printcare.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PrintShopColors = lightColorScheme(
    primary = Color(0xFF155EEF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEAF0FF),
    onPrimaryContainer = Color(0xFF123A80),
    secondary = Color(0xFF475467),
    background = Color(0xFFF6F8FC),
    surface = Color.White,
    surfaceVariant = Color(0xFFF0F3F8),
    error = Color(0xFFB42318),
    onBackground = Color(0xFF182230),
    onSurface = Color(0xFF182230)
)

@Composable
fun PrintShopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PrintShopColors,
        content = content
    )
}
