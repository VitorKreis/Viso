package com.viso.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VisoColorScheme = darkColorScheme(
    primary = AccentBlue,
    secondary = AccentTeal,
    tertiary = AccentGreen,
    background = BgApp,
    surface = BgCard,
    surfaceVariant = BgCard2,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = AccentRed,
    outline = Border
)

@Composable
fun VisoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VisoColorScheme,
        typography = VisoTypography,
        shapes = VisoShapes,
        content = content
    )
}

object Spacing {
    val xs = androidx.compose.ui.unit.Dp(4f)
    val sm = androidx.compose.ui.unit.Dp(8f)
    val md = androidx.compose.ui.unit.Dp(12f)
    val lg = androidx.compose.ui.unit.Dp(16f)
    val xl = androidx.compose.ui.unit.Dp(20f)
    val xxl = androidx.compose.ui.unit.Dp(24f)
    val xxxl = androidx.compose.ui.unit.Dp(32f)
}
