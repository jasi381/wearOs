package com.example.wearrr.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme


/**
 * Fall Guard Material Theme for Wear OS
 *
 * A complete Material Design 3 theme implementation optimized for Wear OS.
 * This theme provides:
 * - OLED-optimized color scheme for battery efficiency
 * - High contrast ratios meeting WCAG AA standards
 * - Semantic color usage for clear visual communication
 * - Consistent design language across all screens
 *
 * Design Principles:
 * - Dark theme as primary (OLED optimization)
 * - Pure blacks avoided to prevent burn-in
 * - Vibrant accents for visual hierarchy
 * - Accessibility-first color contrasts
 */

/**
 * Fall Guard Color Scheme
 * Implements Material Design 3 color system with custom brand colors
 */
private val FallGuardColorScheme = ColorScheme(
    // Primary brand colors
    primary = FallGuardColors.primary,
    primaryDim = Color(0xFFCC3D57),
    primaryContainer = Color(0xFF8B2A3D),
    onPrimary = Color.White,
    onPrimaryContainer = Color.White,

    // Secondary colors (Teal accent)
    secondary = FallGuardColors.accent1,
    secondaryDim = Color(0xFF3EA49D),
    secondaryContainer = Color(0xFF2A7A75),
    onSecondary = Color.White,
    onSecondaryContainer = Color.White,

    // Tertiary colors (Purple accent)
    tertiary = FallGuardColors.accent3,
    tertiaryDim = Color(0xFF7D4691),
    tertiaryContainer = Color(0xFF5A3368),
    onTertiary = Color.White,
    onTertiaryContainer = Color.White,

    // Surface colors
    surfaceContainer = FallGuardColors.surface,
    surfaceContainerLow = FallGuardColors.background,
    surfaceContainerHigh = Color(0xFF242D45),
    onSurface = FallGuardColors.textPrimary,
    onSurfaceVariant = FallGuardColors.textSecondary,

    // Background
    background = FallGuardColors.background,
    onBackground = FallGuardColors.textPrimary,

    // Error states
    error = FallGuardColors.error,
    onError = Color.White,
    errorContainer = Color(0xFF8B2C24),
    onErrorContainer = Color.White,

    // Outline and borders
    outline = Color(0xFF4A5568),
    outlineVariant = Color(0xFF2D3748)
)

/**
 * Main theme composable for Fall Guard Wear OS app
 *
 * Wraps content with Material Design 3 theme including:
 * - Custom color scheme
 * - Typography system (uses Material defaults optimized for Wear OS)
 * - Shape system (uses Material defaults with appropriate corner radii)
 *
 * Usage:
 * ```
 * FallGuardTheme {
 *     // Your composable content here
 * }
 * ```
 */
@Composable
fun CapabiltiesaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FallGuardColorScheme,
        content = content
    )
}

/**
 * Legacy theme name for backwards compatibility
 */
@Composable
fun WearOsAppTheme(
    content: @Composable () -> Unit
) {
    CapabiltiesaTheme(content)
}