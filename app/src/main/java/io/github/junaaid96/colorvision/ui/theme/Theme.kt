package io.github.junaaid96.colorvision.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.junaaid96.colorvision.data.Settings
import io.github.junaaid96.colorvision.data.ThemeMode

/**
 * The app's own palette is built on the Okabe-Ito set, which stays distinguishable for all
 * common color vision deficiencies. Status is never conveyed by color alone.
 */
object Safe {
    val blue = Color(0xFF0072B2)
    val sky = Color(0xFF56B4E9)
    val orange = Color(0xFFE69F00)
    val green = Color(0xFF009E73)
    val vermillion = Color(0xFFD55E00)
    val yellow = Color(0xFFF0E442)
    val purple = Color(0xFFCC79A7)
}

private val Light = lightColorScheme(
    primary = Safe.blue, onPrimary = Color.White, primaryContainer = Color(0xFFD3E6F5), onPrimaryContainer = Color(0xFF00243A),
    secondary = Color(0xFF8A5A00), secondaryContainer = Color(0xFFFFE3B0), onSecondaryContainer = Color(0xFF2B1A00),
    tertiary = Color(0xFF006B4F), tertiaryContainer = Color(0xFFB8F0D9), onTertiaryContainer = Color(0xFF002116),
)
private val Dark = darkColorScheme(
    primary = Safe.sky, onPrimary = Color(0xFF00324F), primaryContainer = Color(0xFF004B73), onPrimaryContainer = Color(0xFFD3E6F5),
    secondary = Safe.orange, secondaryContainer = Color(0xFF5C3D00), onSecondaryContainer = Color(0xFFFFE3B0),
    tertiary = Color(0xFF5FD6AE), tertiaryContainer = Color(0xFF00513B), onTertiaryContainer = Color(0xFFB8F0D9),
)
private val LightHigh = lightColorScheme(
    primary = Color(0xFF003A5C), onPrimary = Color.White, background = Color.White, surface = Color.White,
    onBackground = Color.Black, onSurface = Color.Black, onSurfaceVariant = Color(0xFF1A1A1A), outline = Color.Black,
)
private val DarkHigh = darkColorScheme(
    primary = Color(0xFFBFE3FF), onPrimary = Color.Black, background = Color.Black, surface = Color.Black,
    onBackground = Color.White, onSurface = Color.White, onSurfaceVariant = Color(0xFFEEEEEE), outline = Color.White,
)

private val base = Typography()
private val AppTypography = base.copy(
    displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, letterSpacing = 0.2.sp),
)

/** True when the person asked for reduced motion; animations should snap. */
val LocalReduceMotion = staticCompositionLocalOf { false }

@Composable
fun ColorVisionTheme(settings: Settings, content: @Composable () -> Unit) {
    val dark = when (settings.theme) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val scheme = when {
        settings.highContrast -> if (dark) DarkHigh else LightHigh
        settings.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> Dark
        else -> Light
    }
    CompositionLocalProvider(LocalReduceMotion provides settings.reduceMotion) {
        MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
    }
}
