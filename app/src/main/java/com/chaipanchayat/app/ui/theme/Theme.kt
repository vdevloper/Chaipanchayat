package com.chaipanchayat.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ExtendedColors(
    val surfaceSecondary: Color,
    val surfaceTertiary: Color,
    val brandTertiary: Color,
    val border: Color,
    val muted: Color,
    val skeleton: Color,
    val errorBreaking: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        surfaceSecondary = LightSurfaceSecondary,
        surfaceTertiary = LightSurfaceTertiary,
        brandTertiary = ChaiSaffronTintLight,
        border = LightBorder,
        muted = LightMuted,
        skeleton = LightSkeleton,
        errorBreaking = ErrorBreakingNews
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

@Composable
fun ChaiPanchayatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkScheme else LightScheme
    val extendedColors = if (darkTheme) {
        ExtendedColors(
            surfaceSecondary = DarkSurfaceSecondary,
            surfaceTertiary = DarkSurfaceTertiary,
            brandTertiary = ChaiSaffronTintDark,
            border = DarkBorder,
            muted = DarkMuted,
            skeleton = DarkSkeleton,
            errorBreaking = ErrorBreakingNews
        )
    } else {
        ExtendedColors(
            surfaceSecondary = LightSurfaceSecondary,
            surfaceTertiary = LightSurfaceTertiary,
            brandTertiary = ChaiSaffronTintLight,
            border = LightBorder,
            muted = LightMuted,
            skeleton = LightSkeleton,
            errorBreaking = ErrorBreakingNews
        )
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ChaiTypography,
            content = content
        )
    }
}

object ChaiTheme {
    val extended: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
