package com.codebyramiz.ambient.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Typography
import androidx.tv.material3.darkColorScheme

/** Safe-area padding recommended for TV so content survives overscan. */
val ScreenPaddingHorizontal = 58.dp
val ScreenPaddingVertical = 36.dp

private val AmbientColors = darkColorScheme(
    primary = Color(0xFFFFB68A),
    onPrimary = Color(0xFF3B1A05),
    secondary = Color(0xFF9FC4FF),
    background = Color(0xFF0B1030),
    onBackground = Color(0xFFF4F1FA),
    surface = Color(0xFF161B45),
    onSurface = Color(0xFFF4F1FA),
    surfaceVariant = Color(0xFF232A63),
    onSurfaceVariant = Color(0xFFC9CDEB),
    border = Color(0xFFFFE2AA),
)

private val AmbientTypography = Typography(
    displayLarge = TextStyle(fontSize = 120.sp, fontWeight = FontWeight.Thin, letterSpacing = (-2).sp),
    displayMedium = TextStyle(fontSize = 72.sp, fontWeight = FontWeight.Light),
    headlineMedium = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Medium),
    titleLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Medium),
    titleMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 22.sp),
    bodyMedium = TextStyle(fontSize = 18.sp),
    labelLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun AmbientTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AmbientColors, typography = AmbientTypography) {
        // tv-material's Text falls back to LocalContentColor, which is black outside a Surface.
        CompositionLocalProvider(LocalContentColor provides AmbientColors.onSurface, content = content)
    }
}
