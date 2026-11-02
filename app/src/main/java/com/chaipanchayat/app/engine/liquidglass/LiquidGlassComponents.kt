package com.chaipanchayat.app.engine.liquidglass

import android.os.Build
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
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
 * Floating Refractive Liquid Glass Top Header Container
 * Samples the scrolling feed backdrop underneath so headlines, photos, and category cards
 * visibly bend, refract, and flow through the top bar as the user scrolls.
 */
@Composable
fun ChaiGlassHeader(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val isGlass = ChaiTheme.extended.isLiquidGlass
    val isLayerRecording = (backdrop as? com.kyant.backdrop.backdrops.LayerBackdrop)?.let {
        it.isRecording || !it.isRecorded
    } ?: false

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isGlass && isRenderEffectSupported() && !isLayerRecording) {
                    Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { shape },
                        effects = {
                            vibrancy()
                            colorControls(saturation = 1.45f)
                            blur(9.dp.toPx())
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                lens(
                                    refractionHeight = 12.dp.toPx(),
                                    refractionAmount = 24.dp.toPx(),
                                    depthEffect = true,
                                    chromaticAberration = true
                                )
                            }
                        },
                        highlight = { Highlight.Default },
                        shadow = { Shadow.Default },
                        onDrawSurface = {
                            drawRect(Color(0x360B101B))
                        }
                    )
                } else if (isGlass) {
                    Modifier
                        .background(Color(0xD80B101B), shape)
                        .border(
                            width = 0.8.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.35f),
                                    Color.White.copy(alpha = 0.12f)
                                )
                            ),
                            shape = shape
                        )
                } else {
                    Modifier
                }
            )
            .clip(shape)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
        if (isGlass) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.8.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.10f),
                                Color.White.copy(alpha = 0.45f),
                                Color.White.copy(alpha = 0.10f)
                            )
                        )
                    )
            )
        }
    }
}

/**
 * Reusable Liquid Glass modifier for surfaces that sample the app backdrop.
 * Incorporates genuine optical backdrop blur, vibrancy, AGSL lens refraction, surface tint, and specular edge highlight.
 */
@Composable
fun Modifier.liquidGlass(
    shape: CornerBasedShape,
    surfaceTint: Color? = null,
    blurRadiusDp: Dp = 8.dp
): Modifier {
    val isGlass = ChaiTheme.extended.isLiquidGlass
    if (!isGlass || !isRenderEffectSupported()) {
        return this
            .clip(shape)
            .background(surfaceTint ?: ChaiTheme.extended.surfaceSecondary, shape)
            .border(1.dp, ChaiTheme.extended.border.copy(alpha = 0.6f), shape)
    }

    val backdrop = LocalBackdrop.current
    val tint = surfaceTint ?: Color(0x330D111A)

    val isLayerRecording = (backdrop as? com.kyant.backdrop.backdrops.LayerBackdrop)?.let {
        it.isRecording || !it.isRecorded
    } ?: false
    return if (backdrop != null && !isLayerRecording) {
        this
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    colorControls(saturation = 1.45f)
                    blur(blurRadiusDp.value.dp.toPx())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        lens(
                            refractionHeight = 10.dp.toPx(),
                            refractionAmount = 20.dp.toPx(),
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
    } else {
        this
            .clip(shape)
            .background(tint, shape)
            .border(
                width = LIQUID_GLASS_EDGE_WIDTH,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.44f),
                        Color.White.copy(alpha = 0.12f)
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
 * - Genuine optical backdrop blur, vibrancy, and AGSL lens refraction when backdrop is available
 * - Refractive specular rim border
 * - Top-edge specular bevel reflection
 * - Dynamic light shimmer sweep
 * - Tactile spring physics compression on touch
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
        settings.tintStyle.surfaceColor.copy(alpha = (settings.frostOpacity * 0.55f).coerceIn(0.18f, 0.42f))
    } else {
        extended.surfaceSecondary
    }

    val rimBrush = if (isGlass) settings.createRimBrush() else Brush.linearGradient(listOf(extended.border, extended.border))
    val specularBrush = if (isGlass) settings.createSpecularBevelBrush() else null

    val isLayerRecording = (backdrop as? com.kyant.backdrop.backdrops.LayerBackdrop)?.let {
        it.isRecording || !it.isRecorded
    } ?: false

    Box(
        modifier = modifier
            .scale(pressScale)
            .then(
                if (isGlass && isRenderEffectSupported() && backdrop != null && !isLayerRecording) {
                    Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { shape },
                        effects = {
                            vibrancy()
                            colorControls(saturation = 1.38f)
                            blur(10.dp.toPx())
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                lens(
                                    refractionHeight = 8.dp.toPx(),
                                    refractionAmount = 16.dp.toPx(),
                                    depthEffect = true,
                                    chromaticAberration = true
                                )
                            }
                        },
                        highlight = { Highlight.Default },
                        shadow = null,
                        onDrawSurface = {
                            drawRect(surfaceColor)
                        }
                    )
                } else {
                    Modifier
                        .shadow(
                            elevation = if (isGlass) elevation else 1.dp,
                            shape = shape,
                            ambientColor = if (isGlass) Color(0x30000000) else Color.Transparent,
                            spotColor = if (isGlass) Color(0x3C000000) else Color.Transparent
                        )
                        .background(surfaceColor, shape)
                        .border(
                            width = borderWidth,
                            brush = rimBrush,
                            shape = shape
                        )
                }
            )
            .clip(shape)
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
                if (isGlass) settings.tintStyle.surfaceSecondaryColor.copy(alpha = 0.65f)
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
