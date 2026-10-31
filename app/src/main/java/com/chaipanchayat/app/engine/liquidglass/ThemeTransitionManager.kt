package com.chaipanchayat.app.engine.liquidglass

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaipanchayat.app.data.repository.ThemeMode
import com.chaipanchayat.app.ui.components.ChaiBrewLoadingIndicator
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.InterFamily
import com.kyant.backdrop.Capsule
import com.kyant.backdrop.RuntimeShaderCacheImpl
import com.kyant.backdrop.internal.DefaultHighlightShaderString
import com.kyant.backdrop.isRenderEffectSupported
import com.kyant.backdrop.isRuntimeShaderSupported
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Prepares and pre-caches Liquid Glass & Theme rendering resources
 * during app startup so theme transitions execute smoothly and immediately.
 */
object ThemePrewarmer {
    @Volatile
    private var isPrewarmed = false

    private val runtimeShaderCache = RuntimeShaderCacheImpl()

    fun warmUp(scope: CoroutineScope? = null) {
        if (isPrewarmed) return
        isPrewarmed = true

        val runner = {
            try {
                // 1. Pre-warm hardware feature checks
                val renderEffectOk = isRenderEffectSupported()
                val runtimeShaderOk = isRuntimeShaderSupported()

                // 2. Pre-cache common geometry shapes
                val capsule = Capsule()

                // 3. Pre-cache brushes and settings
                val defaultSettings = LiquidGlassSettings()
                defaultSettings.createRimBrush()
                defaultSettings.createSpecularBevelBrush()

                // 4. Pre-compile AGSL runtime shaders on Android 13+ (API 33+)
                if (runtimeShaderOk && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    try {
                        runtimeShaderCache.obtainRuntimeShader("Default", DefaultHighlightShaderString)
                    } catch (_: Throwable) {
                        // Shader compilation fallback
                    }
                }
            } catch (_: Throwable) {
                // Ignore any prewarm exception
            }
        }

        if (scope != null) {
            scope.launch(Dispatchers.Default) { runner() }
        } else {
            runner()
        }
    }
}

/**
 * Manages smooth, tea-cup loading transitions when switching themes.
 * Coordinates prewarming, destination theme application, and readiness reveal
 * while strictly preserving the navigation backstack and scroll states.
 */
object ThemeTransitionManager {
    private val _isTransitioning = MutableStateFlow(false)
    val isTransitioning: StateFlow<Boolean> = _isTransitioning.asStateFlow()

    private val _destinationTheme = MutableStateFlow<ThemeMode?>(null)
    val destinationTheme: StateFlow<ThemeMode?> = _destinationTheme.asStateFlow()

    fun switchTheme(
        targetTheme: ThemeMode,
        coroutineScope: CoroutineScope,
        onApplyTheme: (ThemeMode) -> Unit
    ) {
        if (_isTransitioning.value) return

        _destinationTheme.value = targetTheme
        _isTransitioning.value = true

        coroutineScope.launch {
            // 1. Ensure theme rendering resources are prewarmed
            withContext(Dispatchers.Default) {
                ThemePrewarmer.warmUp()
            }

            // 2. Give the UI a brief moment to smoothly present the AMOLED tea-cup indicator
            delay(100)

            // 3. Activate the destination theme
            onApplyTheme(targetTheme)

            // 4. Give Compose 2-3 frames to recompose and record graphics layers
            delay(220)

            // 5. Reveal the destination theme smoothly
            _isTransitioning.value = false
            _destinationTheme.value = null
        }
    }
}

/**
 * Minimal, high-end AMOLED-black theme transition overlay.
 * Uses Chai Panchayat's hand-crafted Kulhad tea cup with undulating chai
 * and rising animated steam ribbons.
 */
@Composable
fun ThemeTransitionOverlay(
    modifier: Modifier = Modifier
) {
    val isTransitioning by ThemeTransitionManager.isTransitioning.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible = isTransitioning,
        enter = fadeIn(animationSpec = tween(140)),
        exit = fadeOut(animationSpec = tween(240)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF04060A)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ChaiBrewLoadingIndicator(
                    size = 72.dp,
                    tintColor = ChaiSaffron,
                    showAura = true
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "थीम लागू की जा रही है...",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}
