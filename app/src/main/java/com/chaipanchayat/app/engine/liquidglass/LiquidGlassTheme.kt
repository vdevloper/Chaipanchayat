package com.chaipanchayat.app.engine.liquidglass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalLiquidGlassSettings = staticCompositionLocalOf {
    LiquidGlassSettings()
}

object LiquidGlassEngine {
    val current: LiquidGlassSettings
        @Composable
        @ReadOnlyComposable
        get() = LocalLiquidGlassSettings.current
}

@Composable
fun ProvideLiquidGlassEngine(
    settings: LiquidGlassSettings,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLiquidGlassSettings provides settings,
        content = content
    )
}
