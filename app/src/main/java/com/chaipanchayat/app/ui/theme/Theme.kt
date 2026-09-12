package com.chaipanchayat.app.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chaipanchayat.app.data.repository.ThemeMode
import com.chaipanchayat.app.engine.liquidglass.LiquidGlassPanel
import com.chaipanchayat.app.engine.liquidglass.LiquidGlassSettings
import com.chaipanchayat.app.engine.liquidglass.ProvideLiquidGlassEngine

@Immutable
data class ExtendedColors(
    val isLiquidGlass: Boolean,
    val surfaceSecondary: Color,
    val surfaceTertiary: Color,
    val brandTertiary: Color,
    val border: Color,
    val muted: Color,
    val skeleton: Color,
    val errorBreaking: Color,
    val cardGlow: Color,
    val amber: Color = ChaiAmber,
    val gold: Color = ChaiGold,
    val emerald: Color = ChaiEmerald,
    val cyan: Color = LiquidGlassCyan,
    val glassBorderBrush: Brush = LiquidGlassBorderBrush,
    val glassSpecularBrush: Brush = LiquidGlassCardSpecularBrush
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        isLiquidGlass = false,
        surfaceSecondary = DarkSurfaceSecondary,
        surfaceTertiary = DarkSurfaceTertiary,
        brandTertiary = ChaiSaffronTintDark,
        border = DarkBorder,
        muted = DarkMuted,
        skeleton = DarkSkeleton,
        errorBreaking = ErrorBreakingNews,
        cardGlow = DarkCardGlow,
        glassBorderBrush = Brush.linearGradient(listOf(DarkBorder, DarkBorder))
    )
}

private val LightScheme = lightColorScheme(
    primary = ChaiSaffron,
    onPrimary = Color.White,
    primaryContainer = ChaiSaffronTintLight,
    onPrimaryContainer = ChaiSaffronDark,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceTertiary,
    onSurfaceVariant = LightMuted,
    outline = LightBorder,
    error = ErrorBreakingNews,
    onError = Color.White
)

private val DarkScheme = darkColorScheme(
    primary = ChaiSaffron,
    onPrimary = Color.White,
    primaryContainer = ChaiSaffronTintDark,
    onPrimaryContainer = ChaiSaffron,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceTertiary,
    onSurfaceVariant = DarkMuted,
    outline = DarkBorder,
    error = ErrorBreakingNews,
    onError = Color.White
)

private val LiquidGlassScheme = darkColorScheme(
    primary = ChaiSaffron,
    onPrimary = Color.White,
    primaryContainer = Color(0x2E00E5FF),
    onPrimaryContainer = Color(0xFF00E5FF),
    surface = LiquidGlassBackground,
    onSurface = LiquidGlassOnSurface,
    surfaceVariant = LiquidGlassSurfaceSecondary,
    onSurfaceVariant = LiquidGlassMuted,
    outline = LiquidGlassBorderColor,
    error = ErrorBreakingNews,
    onError = Color.White
)

@Composable
fun ChaiPanchayatTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    liquidGlassSettings: LiquidGlassSettings = LiquidGlassSettings(),
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isLiquid = themeMode == ThemeMode.LIQUID_GLASS
    val isDark = when (themeMode) {
        ThemeMode.LIQUID_GLASS -> false
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemDark
    }

    val colorScheme = when {
        isLiquid -> LiquidGlassScheme.copy(
            primaryContainer = liquidGlassSettings.tintStyle.glowColor.copy(alpha = 0.2f),
            onPrimaryContainer = liquidGlassSettings.tintStyle.accentColor,
            surface = LiquidGlassBackground,
            surfaceVariant = liquidGlassSettings.tintStyle.surfaceSecondaryColor
        )
        isDark -> DarkScheme
        else -> LightScheme
    }

    val extendedColors = when {
        isLiquid -> ExtendedColors(
            isLiquidGlass = true,
            surfaceSecondary = liquidGlassSettings.tintStyle.surfaceSecondaryColor.copy(alpha = liquidGlassSettings.frostOpacity),
            surfaceTertiary = liquidGlassSettings.tintStyle.surfaceSecondaryColor.copy(alpha = 0.55f),
            brandTertiary = liquidGlassSettings.tintStyle.accentColor.copy(alpha = 0.18f),
            border = liquidGlassSettings.tintStyle.rimHighlight,
            muted = LiquidGlassMuted,
            skeleton = LiquidGlassSkeleton,
            errorBreaking = ErrorBreakingNews,
            cardGlow = liquidGlassSettings.tintStyle.glowColor,
            cyan = liquidGlassSettings.tintStyle.accentColor,
            glassBorderBrush = liquidGlassSettings.createRimBrush(),
            glassSpecularBrush = liquidGlassSettings.createSpecularBevelBrush()
        )
        isDark -> ExtendedColors(
            isLiquidGlass = false,
            surfaceSecondary = DarkSurfaceSecondary,
            surfaceTertiary = DarkSurfaceTertiary,
            brandTertiary = ChaiSaffronTintDark,
            border = DarkBorder,
            muted = DarkMuted,
            skeleton = DarkSkeleton,
            errorBreaking = ErrorBreakingNews,
            cardGlow = DarkCardGlow,
            glassBorderBrush = Brush.linearGradient(listOf(DarkBorder, DarkBorder))
        )
        else -> ExtendedColors(
            isLiquidGlass = false,
            surfaceSecondary = LightSurfaceSecondary,
            surfaceTertiary = LightSurfaceTertiary,
            brandTertiary = ChaiSaffronTintLight,
            border = LightBorder,
            muted = LightMuted,
            skeleton = LightSkeleton,
            errorBreaking = ErrorBreakingNews,
            cardGlow = LightCardGlow,
            glassBorderBrush = Brush.linearGradient(listOf(LightBorder, LightBorder))
        )
    }

    ProvideLiquidGlassEngine(settings = liquidGlassSettings) {
        CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = ChaiTypography,
                content = content
            )
        }
    }
}

object ChaiTheme {
    val extended: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}

/**
 * High-Performance Liquid Glass Card
 * Seamlessly backed by LiquidGlassPanel with optical refraction and tactile physics.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable BoxScope.() -> Unit
) {
    LiquidGlassPanel(
        modifier = modifier,
        shape = shape,
        borderWidth = borderWidth,
        elevation = elevation,
        onClick = onClick,
        interactionSource = interactionSource,
        content = content
    )
}
