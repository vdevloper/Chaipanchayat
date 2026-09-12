package com.chaipanchayat.app.engine.liquidglass

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
    if (!settings.ambientOrbs || !ChaiTheme.extended.isLiquidGlass) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        )
        return
    }

    val transition = rememberInfiniteTransition(label = "liquid_orbs_transition")

    // Dynamic orb 1 translation (Cyan / Accent)
    val orb1X by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1_x"
    )
    val orb1Y by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1_y"
    )

    // Dynamic orb 2 translation (Saffron / Secondary)
    val orb2X by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2_x"
    )
    val orb2Y by transition.animateFloat(
        initialValue = 0.70f,
        targetValue = 0.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2_y"
    )

    val tint = settings.tintStyle

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        val w = size.width
        val h = size.height

        // Orb 1: Primary Glass Tint Orb (Cyan/Teal/Ice)
        val center1 = Offset(orb1X * w, orb1Y * h)
        val radius1 = (w * 0.75f).coerceAtLeast(300f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    tint.glowColor.copy(alpha = 0.38f),
                    tint.accentColor.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = center1,
                radius = radius1
            ),
            radius = radius1,
            center = center1
        )

        // Orb 2: Editorial Warm Saffron Flare
        val center2 = Offset(orb2X * w, orb2Y * h)
        val radius2 = (w * 0.65f).coerceAtLeast(260f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x35FF5400),
                    Color(0x12FF9E00),
                    Color.Transparent
                ),
                center = center2,
                radius = radius2
            ),
            radius = radius2,
            center = center2
        )

        // Orb 3: Deep Bottom Ambient Glow
        val center3 = Offset(w * 0.5f, h * 0.92f)
        val radius3 = (w * 0.90f).coerceAtLeast(350f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    tint.surfaceSecondaryColor.copy(alpha = 0.40f),
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
