package com.chaipanchayat.app.engine.liquidglass

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chaipanchayat.app.ui.theme.ChaiTheme

/**
 * World-Class High Performance Liquid Glass Card / Panel
 * Recreates the authentic iOS & Android 15/16 liquid glass aesthetic:
 * - Refractive chromatic rim border
 * - Top-edge specular bevel reflection
 * - Dynamic light shimmer sweep
 * - Tactile spring physics compression on touch
 * - Deep frosted acrylic tint
 */
@Composable
fun LiquidGlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable BoxScope.() -> Unit
) {
    val extended = ChaiTheme.extended
    val isGlass = extended.isLiquidGlass
    val settings = LiquidGlassEngine.current

    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && settings.touchPhysics) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f),
        label = "glass_press_scale"
    )

    // Dynamic light shimmer sweep across the glass surface
    val infiniteTransition = rememberInfiniteTransition(label = "glass_shimmer_transition")
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glass_shimmer_progress"
    )

    val surfaceColor = if (isGlass) {
        settings.tintStyle.surfaceColor.copy(alpha = settings.frostOpacity)
    } else {
        extended.surfaceSecondary
    }

    val rimBrush = if (isGlass) settings.createRimBrush() else Brush.linearGradient(listOf(extended.border, extended.border))
    val specularBrush = if (isGlass) settings.createSpecularBevelBrush() else null

    Box(
        modifier = modifier
            .scale(pressScale)
            .shadow(
                elevation = if (isGlass) elevation else 1.dp,
                shape = shape,
                ambientColor = if (isGlass) settings.tintStyle.glowColor else Color.Transparent,
                spotColor = if (isGlass) settings.tintStyle.glowColor else Color.Transparent
            )
            .clip(shape)
            .background(surfaceColor)
            .border(
                width = borderWidth,
                brush = rimBrush,
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        // Layer 1: Top Specular Bevel Highlight (iOS / VisionOS physical glass bevel)
        if (isGlass && specularBrush != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(specularBrush)
            )
        }

        // Layer 2: Dynamic Light Sweep Shimmer (Traveling sunlight reflection)
        if (isGlass && settings.specularShimmer) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.03f * settings.specularIntensity),
                                Color.White.copy(alpha = 0.12f * settings.specularIntensity),
                                Color.White.copy(alpha = 0.03f * settings.specularIntensity),
                                Color.Transparent
                            ),
                            start = Offset(shimmerProgress * 600f, 0f),
                            end = Offset((shimmerProgress + 0.6f) * 600f, 600f)
                        )
                    )
            )
        }

        // Layer 3: Inner Glass Content
        content()
    }
}

/**
 * Liquid Glass Pill Badge
 */
@Composable
fun LiquidGlassPill(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val settings = LiquidGlassEngine.current
    val isGlass = ChaiTheme.extended.isLiquidGlass

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (isGlass) settings.tintStyle.surfaceSecondaryColor.copy(alpha = 0.85f)
                else ChaiTheme.extended.surfaceTertiary
            )
            .border(
                width = 1.dp,
                brush = if (isGlass) settings.createRimBrush() else Brush.linearGradient(listOf(ChaiTheme.extended.border, ChaiTheme.extended.border)),
                shape = shape
            )
    ) {
        content()
    }
}
