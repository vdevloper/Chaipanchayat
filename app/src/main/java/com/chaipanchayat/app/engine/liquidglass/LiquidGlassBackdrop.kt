package com.chaipanchayat.app.engine.liquidglass

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.chaipanchayat.app.ui.theme.ChaiTheme

/**
 * True Optical Liquid Glass Backdrop
 * Renders floating, morphing chromatic orbs behind the glass layer
 * so glass elements experience genuine optical refraction and deep illumination.
 */
@Composable
fun LiquidGlassBackdrop(
    settings: LiquidGlassSettings,
    modifier: Modifier = Modifier
) {
    if (!ChaiTheme.extended.isLiquidGlass) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        )
        return
    }

    val transition = rememberInfiniteTransition(label = "liquid_orbs_transition")

    // Dynamic orb 1 translation (Cyan / Sapphire Accent)
    val orb1X by transition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 13000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1_x"
    )
    val orb1Y by transition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.48f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1_y"
    )

    // Dynamic orb 2 translation (Saffron / Amber Brand Glow)
    val orb2X by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 17000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2_x"
    )
    val orb2Y by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2_y"
    )

    // Dynamic orb 3 translation (Bottom Dock Violet/Indigo/Amber Cushion)
    val orb3X by transition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb3_x"
    )

    val tint = settings.tintStyle

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050811))
    ) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f || w.isNaN() || h.isNaN()) return@Canvas

        if (settings.ambientOrbs) {
            // Orb 1: Atmospheric Cool Lens Glow (Cyan / Indigo)
            val center1 = Offset(orb1X * w, orb1Y * h)
            val radius1 = (w * 0.90f).coerceAtLeast(340f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        tint.accentColor.copy(alpha = 0.24f),
                        Color(0x1E6366F1),
                        Color.Transparent
                    ),
                    center = center1,
                    radius = radius1
                ),
                radius = radius1,
                center = center1
            )

            // Orb 2: Editorial Warm Saffron Flare (Chai Panchayat brand presence)
            val center2 = Offset(orb2X * w, orb2Y * h)
            val radius2 = (w * 0.82f).coerceAtLeast(300f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x36FF5400),
                        Color(0x18FF9E00),
                        Color.Transparent
                    ),
                    center = center2,
                    radius = radius2
                ),
                radius = radius2,
                center = center2
            )

            // Orb 3: Rich Bottom Dock Chromatic Cushion (illuminates floating mini-player & bottom tabs)
            val center3 = Offset(orb3X * w, h * 0.90f)
            val radius3 = (w * 0.88f).coerceAtLeast(340f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x307C3AED),
                        Color(0x220EA5E9),
                        Color.Transparent
                    ),
                    center = center3,
                    radius = radius3
                ),
                radius = radius3,
                center = center3
            )
        }
    }
}
