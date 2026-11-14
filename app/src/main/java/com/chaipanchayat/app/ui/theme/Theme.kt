package com.chaipanchayat.app.ui.theme

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
    val isLightGlass: Boolean = false,
    val surfaceSecondary: Color,
    val surfaceTertiary: Color,
    val brandTertiary: Color,
    val border: Color,
    val muted: Color,
    val textSecondary: Color,
    val brandText: Color,
    val goldText: Color,
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
        isLightGlass = false,
        surfaceSecondary = DarkSurfaceSecondary,
        surfaceTertiary = DarkSurfaceTertiary,
        brandTertiary = ChaiSaffronTintDark,
        border = DarkBorder,
        muted = DarkMuted,
        textSecondary = DarkSecondaryText,
        brandText = DarkBrandText,
        goldText = DarkGoldText,
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
    onSurfaceVariant = LightSecondaryText,
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
    onSurfaceVariant = DarkSecondaryText,
    outline = DarkBorder,
    error = ErrorBreakingNews,
    onError = Color.White
)

private val LiquidGlassDarkScheme = darkColorScheme(
    primary = ChaiSaffron,
    onPrimary = Color.White,
    primaryContainer = Color(0x24FFFFFF),
    onPrimaryContainer = Color.White,
    surface = Color(0xFF0A0B0E),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0x40181A20),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0x38FFFFFF),
    error = ErrorBreakingNews,
    onError = Color.White
)

private val LiquidGlassLightScheme = lightColorScheme(
    primary = ChaiSaffron,
    onPrimary = Color.White,
    primaryContainer = Color(0x140F172A),
    onPrimaryContainer = Color(0xFF0F172A),
    surface = Color(0xFFF0F2F5),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0x0D0F172A),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0x280F172A),
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
    val isLightGlass = isLiquid && liquidGlassSettings.isLightGlass
    val isDark = when (themeMode) {
        ThemeMode.LIQUID_GLASS -> !liquidGlassSettings.isLightGlass
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemDark
    }

    val colorScheme = when {
        isLiquid && isLightGlass -> LiquidGlassLightScheme
        isLiquid -> LiquidGlassDarkScheme
        isDark -> DarkScheme
        else -> LightScheme
    }

    val extendedColors = when {
        isLiquid && isLightGlass -> ExtendedColors(
            isLiquidGlass = true,
            isLightGlass = true,
            surfaceSecondary = Color(0x0D0F172A), // Pure sheer transparent crystal glass
            surfaceTertiary = Color(0x140F172A),
            brandTertiary = ChaiSaffron.copy(alpha = 0.14f),
            border = Color(0x280F172A),
            muted = Color(0xFF475569),
            textSecondary = Color(0xFF334155),
            brandText = Color(0xFFC2410C),
            goldText = Color(0xFFB45309),
            skeleton = Color(0x3394A3B8),
            errorBreaking = ErrorBreakingNews,
            cardGlow = Color(0x28FFFFFF),
            cyan = ChaiSaffron,
            glassBorderBrush = liquidGlassSettings.createRimBrush(),
            glassSpecularBrush = liquidGlassSettings.createSpecularBevelBrush()
        )
        isLiquid -> ExtendedColors(
            isLiquidGlass = true,
            isLightGlass = false,
            surfaceSecondary = Color(0x3816181D),
            surfaceTertiary = Color(0x481E2028),
            brandTertiary = ChaiSaffron.copy(alpha = 0.18f),
            border = Color(0x38FFFFFF),
            muted = Color(0xFF94A3B8),
            textSecondary = Color(0xFFCBD5E1),
            brandText = Color(0xFFFF7A28),
            goldText = DarkGoldText,
            skeleton = LiquidGlassSkeleton,
            errorBreaking = ErrorBreakingNews,
            cardGlow = Color(0x18FFFFFF),
            cyan = Color(0xFFFF7A28),
            glassBorderBrush = liquidGlassSettings.createRimBrush(),
            glassSpecularBrush = liquidGlassSettings.createSpecularBevelBrush()
        )
        isDark -> ExtendedColors(
            isLiquidGlass = false,
            isLightGlass = false,
            surfaceSecondary = DarkSurfaceSecondary,
            surfaceTertiary = DarkSurfaceTertiary,
            brandTertiary = ChaiSaffronTintDark,
            border = DarkBorder,
            muted = DarkMuted,
            textSecondary = DarkSecondaryText,
            brandText = DarkBrandText,
            goldText = DarkGoldText,
            skeleton = DarkSkeleton,
            errorBreaking = ErrorBreakingNews,
            cardGlow = DarkCardGlow,
            glassBorderBrush = Brush.linearGradient(listOf(DarkBorder, DarkBorder))
        )
        else -> ExtendedColors(
            isLiquidGlass = false,
            isLightGlass = false,
            surfaceSecondary = LightSurfaceSecondary,
            surfaceTertiary = LightSurfaceTertiary,
            brandTertiary = ChaiSaffronTintLight,
            border = LightBorder,
            muted = LightMuted,
            textSecondary = LightSecondaryText,
            brandText = LightBrandText,
            goldText = LightGoldText,
            skeleton = LightSkeleton,
            errorBreaking = ErrorBreakingNews,
            cardGlow = LightCardGlow,
            glassBorderBrush = Brush.linearGradient(
                listOf(LightBorder, ChaiSaffron.copy(alpha = 0.24f), LightBorder)
            )
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
    borderWidth: Dp = 0.85.dp,
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
