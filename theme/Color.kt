package com.aktarjabed.androphoshop.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val IndigoGradientLight = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
val NeonBlueVioletDark = listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4))

val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E0FF),
    secondary = Color(0xFF6D28D9),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F5F9)
)

val DarkColors = darkColorScheme(
    primary = Color(0xFF8B5CF6),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF3F3B6B),
    secondary = Color(0xFF06B6D4),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF334155)
)