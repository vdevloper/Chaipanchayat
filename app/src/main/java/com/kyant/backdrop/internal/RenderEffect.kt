package com.kyant.backdrop.internal

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asAndroidColorFilter
import androidx.compose.ui.graphics.asComposeRenderEffect
import com.kyant.backdrop.RuntimeShader
import com.kyant.backdrop.asAndroidRuntimeShader

@RequiresApi(Build.VERSION_CODES.S)
internal fun RenderEffect?.chain(other: RenderEffect?): RenderEffect? {
    if (this == null) return other
    if (other == null) return this
    return try {
        android.graphics.RenderEffect.createChainEffect(
            other.asAndroidRenderEffect(),
            this.asAndroidRenderEffect()
        ).asComposeRenderEffect()
    } catch (_: Throwable) {
        this
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal fun RuntimeShaderEffect(
    runtimeShader: RuntimeShader,
    uniformShaderName: String
): RenderEffect? {
    val androidShader = runtimeShader.asAndroidRuntimeShader() ?: return null
    return try {
        android.graphics.RenderEffect.createRuntimeShaderEffect(
            androidShader,
            uniformShaderName
        ).asComposeRenderEffect()
    } catch (_: Throwable) {
        null
    }
}

@RequiresApi(Build.VERSION_CODES.S)
internal fun ColorFilterEffect(
    renderEffect: RenderEffect?,
    colorFilter: ColorFilter
): RenderEffect? {
    return try {
        if (renderEffect != null) {
            android.graphics.RenderEffect.createColorFilterEffect(
                colorFilter.asAndroidColorFilter(),
                renderEffect.asAndroidRenderEffect()
            ).asComposeRenderEffect()
        } else {
            android.graphics.RenderEffect.createColorFilterEffect(
                colorFilter.asAndroidColorFilter()
            ).asComposeRenderEffect()
        }
    } catch (_: Throwable) {
        renderEffect
    }
}
