package com.chaipanchayat.app.engine.liquidglass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.kyant.backdrop.Backdrop

val LocalLiquidGlassSettings = staticCompositionLocalOf {
    LiquidGlassSettings()
}

val LocalBackdrop = staticCompositionLocalOf<Backdrop?> {
    null
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
    backdrop: Backdrop? = null,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLiquidGlassSettings provides settings,
        LocalBackdrop provides backdrop,
        content = content
    )
}
