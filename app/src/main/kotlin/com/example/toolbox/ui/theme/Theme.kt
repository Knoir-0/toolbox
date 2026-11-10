/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.toolbox.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import com.example.toolbox.ui.main.DEFAULT_ACCENT_COLOR_AUTO
import com.example.toolbox.ui.main.DEFAULT_THEME_COLOR_SYSTEM
import com.example.toolbox.ui.main.FALLBACK_THEME_COLOR_HEX
import com.example.toolbox.ui.main.ThemeMode
import kotlin.math.abs
import kotlin.math.roundToInt

private val DarkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

private val LightColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

// Material 3 scheme for fallback color #8E66A6 (Toolbox Purple)
val Purple8E66A6LightColorScheme = lightColorScheme(
    primary = Color(0xFF734C8B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF3DAFF),
    onPrimaryContainer = Color(0xFF2C0044),
    secondary = Color(0xFF675A6E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEFDEF5),
    onSecondaryContainer = Color(0xFF221729),
    tertiary = Color(0xFF81525A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD9DF),
    onTertiaryContainer = Color(0xFF331018),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFDF7FF),
    onBackground = Color(0xFF1D1B1E),
    surface = Color(0xFFFDF7FF),
    onSurface = Color(0xFF1D1B1E),
    surfaceVariant = Color(0xFFE9DFEA),
    onSurfaceVariant = Color(0xFF4A454E),
    outline = Color(0xFF7C757F),
    outlineVariant = Color(0xFFCDC4CF),
)

val Purple8E66A6DarkColorScheme = darkColorScheme(
    primary = Color(0xFFDDB9F6),
    onPrimary = Color(0xFF421D5A),
    primaryContainer = Color(0xFF5A3472),
    onPrimaryContainer = Color(0xFFF3DAFF),
    secondary = Color(0xFFD3C2D8),
    onSecondary = Color(0xFF372C3E),
    secondaryContainer = Color(0xFF4E4356),
    onSecondaryContainer = Color(0xFFEFDEF5),
    tertiary = Color(0xFFF4B7BF),
    onTertiary = Color(0xFF4C252D),
    tertiaryContainer = Color(0xFF663B43),
    onTertiaryContainer = Color(0xFFFFD9DF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF151217),
    onBackground = Color(0xFFE7E1E6),
    surface = Color(0xFF151217),
    onSurface = Color(0xFFE7E1E6),
    surfaceVariant = Color(0xFF4A454E),
    onSurfaceVariant = Color(0xFFCDC4CF),
    outline = Color(0xFF968E99),
    outlineVariant = Color(0xFF4A454E),
)

fun parseHexColor(hex: String): Color? {
    val clean = hex.removePrefix("#").trim()
    if (clean.length != 6 && clean.length != 8) return null
    return try {
        val fullHex = if (clean.length == 6) "FF$clean" else clean
        Color(fullHex.toLong(16))
    } catch (_: Exception) {
        null
    }
}

/**
 * Calculates the complementary color (180-degree hue shift in HSV space) for a given [Color].
 * Ensures the resulting color has sufficient saturation and brightness to function effectively
 * as a Material 3 accent/tertiary color.
 */
fun calculateComplementaryColor(baseColor: Color): Color {
    val r = (baseColor.red * 255f).roundToInt().coerceIn(0, 255)
    val g = (baseColor.green * 255f).roundToInt().coerceIn(0, 255)
    val b = (baseColor.blue * 255f).roundToInt().coerceIn(0, 255)

    val rf = r / 255f
    val gf = g / 255f
    val bf = b / 255f
    val maxC = maxOf(rf, maxOf(gf, bf))
    val minC = minOf(rf, minOf(gf, bf))
    val delta = maxC - minC

    val hue = when {
        delta == 0f -> 0f
        maxC == rf -> 60f * (((gf - bf) / delta) % 6f)
        maxC == gf -> 60f * (((bf - rf) / delta) + 2f)
        else -> 60f * (((rf - gf) / delta) + 4f)
    }
    val normalizedHue = ((hue % 360f) + 360f) % 360f
    val complementHue = (normalizedHue + 180f) % 360f

    val sat = if (maxC == 0f) 0f else (delta / maxC)
    val value = maxC

    // If base color is nearly monochromatic (e.g. grayscale/black/white), fallback to warm amber accent
    val targetSat = if (delta < 0.05f) 0.85f else sat.coerceIn(0.50f, 0.95f)
    val targetVal = if (delta < 0.05f) 0.90f else value.coerceIn(0.60f, 0.95f)
    val effectiveHue = if (delta < 0.05f) 30f else complementHue

    val c = targetVal * targetSat
    val x = c * (1f - abs(((effectiveHue / 60f) % 2f) - 1f))
    val m = targetVal - c

    val (rPrime, gPrime, bPrime) = when {
        effectiveHue < 60f -> Triple(c, x, 0f)
        effectiveHue < 120f -> Triple(x, c, 0f)
        effectiveHue < 180f -> Triple(0f, c, x)
        effectiveHue < 240f -> Triple(0f, x, c)
        effectiveHue < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = (rPrime + m).coerceIn(0f, 1f),
        green = (gPrime + m).coerceIn(0f, 1f),
        blue = (bPrime + m).coerceIn(0f, 1f),
        alpha = 1f,
    )
}

data class TertiaryColorTokens(
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
)

fun createTertiaryTokens(accentSeed: Color, darkTheme: Boolean): TertiaryColorTokens {
    return if (darkTheme) {
        val tertiary = lerp(accentSeed, Color.White, 0.45f)
        TertiaryColorTokens(
            tertiary = tertiary,
            onTertiary = if (tertiary.luminance() > 0.5f) Color(0xFF1F1F1F) else Color.White,
            tertiaryContainer = lerp(accentSeed, Color.Black, 0.4f),
            onTertiaryContainer = lerp(accentSeed, Color.White, 0.88f),
        )
    } else {
        val tertiary = accentSeed
        TertiaryColorTokens(
            tertiary = tertiary,
            onTertiary = if (tertiary.luminance() > 0.5f) Color.Black else Color.White,
            tertiaryContainer = lerp(accentSeed, Color.White, 0.82f),
            onTertiaryContainer = lerp(accentSeed, Color.Black, 0.78f),
        )
    }
}

fun resolveEffectiveAccentColor(
    themeColorHex: String,
    accentColorSetting: String,
    fallbackPrimary: Color = Color(0xFF8E66A6),
): Color {
    if (!accentColorSetting.equals(DEFAULT_ACCENT_COLOR_AUTO, ignoreCase = true)) {
        val parsed = parseHexColor(accentColorSetting)
        if (parsed != null) return parsed
    }
    val primary = parseHexColor(themeColorHex) ?: fallbackPrimary
    return calculateComplementaryColor(primary)
}

fun createCustomColorScheme(
    seedColor: Color,
    darkTheme: Boolean,
    accentColor: Color? = null,
): ColorScheme {
    val accentSeed = accentColor ?: calculateComplementaryColor(seedColor)
    val tertiaryTokens = createTertiaryTokens(accentSeed, darkTheme)
    return if (darkTheme) {
        darkColorScheme(
            primary = lerp(seedColor, Color.White, 0.45f),
            onPrimary = lerp(seedColor, Color.Black, 0.85f),
            primaryContainer = lerp(seedColor, Color.Black, 0.4f),
            onPrimaryContainer = lerp(seedColor, Color.White, 0.88f),
            secondary = lerp(seedColor, Color(0xFFB0B0B0), 0.5f),
            onSecondary = Color(0xFF1F1F1F),
            secondaryContainer = lerp(seedColor, Color(0xFF333333), 0.6f),
            onSecondaryContainer = lerp(seedColor, Color.White, 0.85f),
            tertiary = tertiaryTokens.tertiary,
            onTertiary = tertiaryTokens.onTertiary,
            tertiaryContainer = tertiaryTokens.tertiaryContainer,
            onTertiaryContainer = tertiaryTokens.onTertiaryContainer,
            surface = Color(0xFF151217),
            onSurface = Color(0xFFE7E1E6),
            surfaceVariant = lerp(seedColor, Color(0xFF4A454E), 0.75f),
            onSurfaceVariant = Color(0xFFCDC4CF),
            background = Color(0xFF151217),
            onBackground = Color(0xFFE7E1E6),
        )
    } else {
        lightColorScheme(
            primary = seedColor,
            onPrimary = if (seedColor.luminance() > 0.5f) Color.Black else Color.White,
            primaryContainer = lerp(seedColor, Color.White, 0.8f),
            onPrimaryContainer = lerp(seedColor, Color.Black, 0.78f),
            secondary = lerp(seedColor, Color(0xFF555555), 0.45f),
            onSecondary = Color.White,
            secondaryContainer = lerp(seedColor, Color.White, 0.86f),
            onSecondaryContainer = lerp(seedColor, Color.Black, 0.75f),
            tertiary = tertiaryTokens.tertiary,
            onTertiary = tertiaryTokens.onTertiary,
            tertiaryContainer = tertiaryTokens.tertiaryContainer,
            onTertiaryContainer = tertiaryTokens.onTertiaryContainer,
            surface = Color(0xFFFDF8FD),
            onSurface = Color(0xFF1D1B1E),
            surfaceVariant = lerp(seedColor, Color(0xFFE7E0EC), 0.85f),
            onSurfaceVariant = Color(0xFF4A454E),
            background = Color(0xFFFDF8FD),
            onBackground = Color(0xFF1D1B1E),
        )
    }
}

@Composable
fun ToolboxTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    themeColor: String = DEFAULT_THEME_COLOR_SYSTEM,
    accentColor: String = DEFAULT_ACCENT_COLOR_AUTO,
    darkTheme: Boolean = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    },
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = (themeColor == DEFAULT_THEME_COLOR_SYSTEM),
    content: @Composable () -> Unit,
) {
    val baseColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeColor == DEFAULT_THEME_COLOR_SYSTEM -> {
            // Fallback to #8E66A6 for older Android versions
            if (darkTheme) Purple8E66A6DarkColorScheme else Purple8E66A6LightColorScheme
        }
        themeColor.equals(FALLBACK_THEME_COLOR_HEX, ignoreCase = true) -> {
            if (darkTheme) Purple8E66A6DarkColorScheme else Purple8E66A6LightColorScheme
        }
        themeColor.equals("#00668B", ignoreCase = true) -> {
            if (darkTheme) DarkColorScheme else LightColorScheme
        }
        else -> {
            val parsed = parseHexColor(themeColor)
            if (parsed != null) {
                createCustomColorScheme(parsed, darkTheme)
            } else {
                if (darkTheme) Purple8E66A6DarkColorScheme else Purple8E66A6LightColorScheme
            }
        }
    }

    val resolvedAccentColor = if (accentColor.equals(DEFAULT_ACCENT_COLOR_AUTO, ignoreCase = true)) {
        calculateComplementaryColor(baseColorScheme.primary)
    } else {
        parseHexColor(accentColor) ?: calculateComplementaryColor(baseColorScheme.primary)
    }

    val tertiaryTokens = createTertiaryTokens(resolvedAccentColor, darkTheme)
    val colorScheme = baseColorScheme.copy(
        tertiary = tertiaryTokens.tertiary,
        onTertiary = tertiaryTokens.onTertiary,
        tertiaryContainer = tertiaryTokens.tertiaryContainer,
        onTertiaryContainer = tertiaryTokens.onTertiaryContainer,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

