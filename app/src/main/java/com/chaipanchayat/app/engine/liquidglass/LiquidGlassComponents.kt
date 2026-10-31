package com.chaipanchayat.app.engine.liquidglass

import android.os.Build
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
import androidx.compose.foundation.shape.CornerBasedShape
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.drawPlainBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightElement
import com.kyant.backdrop.internal.ShapeProvider
import com.kyant.backdrop.isRenderEffectSupported
import com.kyant.backdrop.shadow.Shadow

/**
 * Hairline rim along glass edges matching Apple / iOS Liquid Glass
 */
val LIQUID_GLASS_EDGE_WIDTH = 0.8.dp
val LIQUID_GLASS_EDGE_COLOR = Color.White.copy(alpha = 0.22f)

/**
 * Reusable Liquid Glass modifier for surfaces that sample the app backdrop.
 * Incorporates genuine optical backdrop blur, vibrancy, AGSL lens refraction, surface tint, and specular edge highlight.
 */
@Composable
fun Modifier.liquidGlass(
    shape: CornerBasedShape,
    surfaceTint: Color? = null,
    blurRadiusDp: Dp = 14.dp
): Modifier {
    val isGlass = ChaiTheme.extended.isLiquidGlass
    if (!isGlass || !isRenderEffectSupported()) {
        return this
            .clip(shape)
            .background(surfaceTint ?: ChaiTheme.extended.surfaceSecondary, shape)
            .border(1.dp, ChaiTheme.extended.border.copy(alpha = 0.6f), shape)
    }

    val backdrop = LocalBackdrop.current
    val tint = surfaceTint ?: Color(0x3D0B1220)

    val isLayerRecording = (backdrop as? com.kyant.backdrop.backdrops.LayerBackdrop)?.let { it.isRecording || !it.isRecorded } ?: false
    return if (backdrop != null && !isLayerRecording) {
        this
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    colorControls(saturation = 1.35f)
                    blur(blurRadiusDp.value.dp.toPx())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        lens(
                            refractionHeight = 7.dp.toPx(),
                            refractionAmount = 9.dp.toPx(),
                            depthEffect = true,
                            chromaticAberration = true
                        )
                    }
                },
                highlight = { Highlight.Default },
                shadow = null,
                onDrawSurface = {
                    drawRect(color = tint)
                }
            )
            .clip(shape)
            .border(
                width = LIQUID_GLASS_EDGE_WIDTH,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.48f),
                        Color.White.copy(alpha = 0.14f),
                        Color.White.copy(alpha = 0.04f)
                    )
                ),
                shape = shape
            )
    } else {
        this
            .clip(shape)
            .background(tint, shape)
            .border(
                width = LIQUID_GLASS_EDGE_WIDTH,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.40f),
                        Color.White.copy(alpha = 0.10f)
                    )
                ),
                shape = shape
            )
    }
}

/**
 * Directional specular rim highlight along the shape boundary
 */
@Composable
fun Modifier.liquidGlassRim(shape: CornerBasedShape): Modifier {
    if (!ChaiTheme.extended.isLiquidGlass || !isRenderEffectSupported()) return this
    val shapeProvider = remember(shape) { ShapeProvider { shape } }
    return then(
        HighlightElement(
            shapeProvider = shapeProvider,
            highlight = { Highlight.Default }
        )
    )
}

/**
 * World-Class High Performance Liquid Glass Card / Panel
 * Recreates the authentic iOS & Android liquid glass aesthetic:
 * - Genuine optical backdrop blur and vibrancy when backdrop is available
 * - Refractive specular rim border
 * - Top-edge specular bevel reflection
 * - Dynamic light shimmer sweep
 * - Tactile spring physics compression on touch
 * - Deep frosted acrylic tint
 */
@Composable
fun LiquidGlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable BoxScope.() -> Unit
) {
    val extended = ChaiTheme.extended
    val isGlass = extended.isLiquidGlass
    val settings = LiquidGlassEngine.current
    val backdrop = LocalBackdrop.current

    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && settings.touchPhysics) 0.975f else 1.0f,
        animationSpec = spring(dampingRatio = 0.60f, stiffness = 420f),
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
                ambientColor = if (isGlass) Color(0x30000000) else Color.Transparent,
                spotColor = if (isGlass) Color(0x3C000000) else Color.Transparent
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
        // Layer 1: Top Specular Bevel Highlight (physical light entering upper glass rim)
        if (isGlass && specularBrush != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(specularBrush)
            )
        }

        // Layer 2: Subtle Ambient Specular Reflection (Directional soft light)
        if (isGlass && settings.specularShimmer) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.02f * settings.specularIntensity),
                                Color.White.copy(alpha = 0.06f * settings.specularIntensity),
                                Color.White.copy(alpha = 0.02f * settings.specularIntensity),
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

