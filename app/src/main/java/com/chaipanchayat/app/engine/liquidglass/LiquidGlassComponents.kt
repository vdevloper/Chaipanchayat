package com.chaipanchayat.app.engine.liquidglass

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.utils.rememberChaiHaptics
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.Capsule
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightElement
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.backdrop.internal.ShapeProvider
import com.kyant.backdrop.isRenderEffectSupported
import com.kyant.backdrop.shadow.Shadow
import kotlin.math.roundToInt

/**
 * Hairline rim along glass edges matching Apple / iOS 26 Liquid Glass
 */
val LIQUID_GLASS_EDGE_WIDTH = 0.85.dp
val LIQUID_GLASS_EDGE_COLOR = Color.White.copy(alpha = 0.32f)

/**
 * Directional 3D glass rim highlight that adapts to Dark Glass and Light Glass:
 * - Dark Glass: Luminous silver-white additive rim (BlendMode.Plus).
 * - Light Glass: Crisp smoked-slate directional refractive rim (BlendMode.SrcOver) so transparent
 *   glass edges never wash out into white-on-white on a light studio background.
 */
fun glassHighlight(
    isLightGlass: Boolean,
    width: Dp = if (isLightGlass) 1.0.dp else 0.80.dp,
    alpha: Float = if (isLightGlass) 0.28f else 0.68f
): Highlight = if (isLightGlass) {
    Highlight(
        width = width,
        alpha = alpha,
        style = HighlightStyle.Default(
            color = Color(0xFF0F172A),
            blendMode = BlendMode.SrcOver,
            angle = 45f,
            falloff = 0.85f
        )
    )
} else {
    Highlight.Default.copy(
        width = width,
        alpha = alpha
    )
}

/**
 * Floating Refractive Liquid Glass Top Header Container
 * Both Dark Glass and Light Glass are genuinely transparent crystal glass — defined by optical blur,
 * AGSL lens refraction, and crisp directional rim highlights rather than opaque paint.
 */
@Composable
fun ChaiGlassHeader(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val isGlass = ChaiTheme.extended.isLiquidGlass
    val isLightGlass = ChaiTheme.extended.isLightGlass

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isGlass && isRenderEffectSupported()) {
                    Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { shape },
                        effects = {
                            blur(22.dp.toPx())
                            colorControls(
                                brightness = 0.0f,
                                contrast = 1.04f,
                                saturation = 1.10f
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                lens(
                                    refractionHeight = 14.dp.toPx(),
                                    refractionAmount = 24.dp.toPx(),
                                    depthEffect = true,
                                    chromaticAberration = true
                                )
                            }
                        },
                        highlight = {
                            glassHighlight(
                                isLightGlass = isLightGlass,
                                width = if (isLightGlass) 1.05.dp else 0.82.dp,
                                alpha = if (isLightGlass) 0.26f else 0.64f
                            )
                        },
                        shadow = {
                            Shadow(
                                radius = 14.dp,
                                offset = DpOffset(0.dp, 4.dp),
                                color = Color.Black.copy(alpha = if (isLightGlass) 0.08f else 0.36f)
                            )
                        },
                        onDrawSurface = {
                            if (size.isSpecified && size.width > 0f && size.height > 0f) {
                                if (isLightGlass) {
                                    // Sheer crystal-clear transparent glass in Light Mode (4% cool crystal + top specular catchlight)
                                    drawRect(Color(0x0A0F172A))
                                    drawRect(Color(0x28FFFFFF))
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.55f),
                                                Color.Transparent,
                                                Color(0x0A0F172A)
                                            )
                                        )
                                    )
                                } else {
                                    // Translucent Smoked Crystal Glass Header in Dark Mode
                                    drawRect(Color(0x54121318))
                                    drawRect(Color(0x10FFFFFF))
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.11f),
                                                Color.Transparent,
                                                Color.White.copy(alpha = 0.03f)
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    )
                } else if (isGlass) {
                    Modifier
                        .background(if (isLightGlass) Color(0x38FFFFFF) else Color(0xCC121318), shape)
                        .border(
                            width = 0.9.dp,
                            brush = Brush.verticalGradient(
                                if (isLightGlass) {
                                    listOf(
                                        Color(0x330F172A),
                                        Color(0x180F172A)
                                    )
                                } else {
                                    listOf(
                                        Color.White.copy(alpha = 0.32f),
                                        Color.White.copy(alpha = 0.10f)
                                    )
                                }
                            ),
                            shape = shape
                        )
                } else {
                    Modifier.background(MaterialTheme.colorScheme.surface)
                }
            )
            .clip(shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            content = content
        )
    }
}

/**
 * Reusable Liquid Glass modifier for buttons, pills, capsules, and interactive controls.
 * Uses sheer glassy transparency in both Dark and Light modes so the optical blur and lens refraction shine through.
 */
@Composable
fun Modifier.liquidGlass(
    shape: CornerBasedShape,
    surfaceTint: Color? = null,
    blurRadiusDp: Dp = 20.dp,
    backdrop: Backdrop? = LocalBackdrop.current,
    refractionHeightDp: Dp = 12.dp,
    refractionAmountDp: Dp = 22.dp,
    withShadow: Boolean = false
): Modifier {
    val isGlass = ChaiTheme.extended.isLiquidGlass
    val isLightGlass = ChaiTheme.extended.isLightGlass
    if (!isGlass) {
        return this
            .clip(shape)
            .background(surfaceTint ?: ChaiTheme.extended.surfaceSecondary, shape)
            .border(1.dp, ChaiTheme.extended.border.copy(alpha = 0.6f), shape)
    }

    val tint = when {
        isLightGlass -> Color(0x0B0F172A) // Sheer cool-crystal glass density (never white paint!)
        surfaceTint != null -> surfaceTint
        else -> Color(0x3816181D)
    }

    return if (backdrop != null && isRenderEffectSupported()) {
        this
            .then(
                if (withShadow) {
                    Modifier.shadow(
                        elevation = 5.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = if (isLightGlass) 0.08f else 0.28f),
                        spotColor = Color.Black.copy(alpha = if (isLightGlass) 0.12f else 0.36f)
                    )
                } else Modifier
            )
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    blur(blurRadiusDp.toPx())
                    colorControls(
                        brightness = 0.0f,
                        contrast = 1.04f,
                        saturation = 1.08f
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        lens(
                            refractionHeight = refractionHeightDp.toPx(),
                            refractionAmount = refractionAmountDp.toPx(),
                            depthEffect = true,
                            chromaticAberration = true
                        )
                    }
                },
                highlight = {
                    glassHighlight(
                        isLightGlass = isLightGlass,
                        width = if (isLightGlass) 1.0.dp else 0.80.dp,
                        alpha = if (isLightGlass) 0.28f else 0.64f
                    )
                },
                shadow = null,
                onDrawSurface = {
                    if (size.isSpecified && size.width > 0f && size.height > 0f) {
                        drawRect(color = tint)
                        if (isLightGlass) {
                            drawRect(color = Color(0x28FFFFFF))
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.58f),
                                        Color.Transparent,
                                        Color(0x0A0F172A)
                                    )
                                )
                            )
                        } else {
                            drawRect(color = Color(0x10FFFFFF))
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.10f),
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.03f)
                                    )
                                )
                            )
                        }
                    }
                }
            )
            .clip(shape)
    } else {
        this
            .then(
                if (withShadow) {
                    Modifier.shadow(
                        elevation = 5.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = if (isLightGlass) 0.08f else 0.28f),
                        spotColor = Color.Black.copy(alpha = if (isLightGlass) 0.12f else 0.36f)
                    )
                } else Modifier
            )
            .clip(shape)
            .background(tint, shape)
            .border(
                width = LIQUID_GLASS_EDGE_WIDTH,
                brush = Brush.verticalGradient(
                    if (isLightGlass) {
                        listOf(
                            Color(0x360F172A),
                            Color(0x180F172A),
                            Color(0x280F172A)
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.36f),
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.20f)
                        )
                    }
                ),
                shape = shape
            )
    }
}

/**
 * Flagship Refractive Liquid Glass Card Modifier for News Cards, Hero Cards, Category Cards, and Settings Cards.
 * In Light Glass mode, uses sheer transparent crystal glass with a crisp directional slate-crystal rim
 * and top white specular catchlight so cards are clearly defined while remaining genuinely transparent.
 */
@Composable
fun Modifier.liquidGlassCard(
    shape: CornerBasedShape = RoundedCornerShape(20.dp),
    accentColor: Color? = null,
    blurRadiusDp: Dp = 20.dp,
    backdrop: Backdrop? = LocalBackdrop.current,
    withShadow: Boolean = true
): Modifier {
    val isGlass = ChaiTheme.extended.isLiquidGlass
    val isLightGlass = ChaiTheme.extended.isLightGlass
    if (!isGlass) {
        return this
    }
    val baseSurface = if (isLightGlass) Color(0x090F172A) else Color(0x3816181D)

    if (!isRenderEffectSupported() || backdrop == null) {
        return this
            .then(
                if (withShadow) {
                    Modifier.shadow(
                        elevation = 5.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = if (isLightGlass) 0.08f else 0.30f),
                        spotColor = Color.Black.copy(alpha = if (isLightGlass) 0.12f else 0.40f)
                    )
                } else Modifier
            )
            .clip(shape)
            .background(baseSurface, shape)
            .border(
                width = if (isLightGlass) 1.0.dp else 0.78.dp,
                brush = Brush.verticalGradient(
                    if (isLightGlass) {
                        listOf(
                            Color(0x360F172A),
                            Color(0x180F172A),
                            Color(0x280F172A)
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.36f),
                            Color.White.copy(alpha = 0.10f),
                            Color.White.copy(alpha = 0.18f)
                        )
                    }
                ),
                shape = shape
            )
    }

    return this
        .then(
            if (withShadow) {
                Modifier.shadow(
                    elevation = 5.dp,
                    shape = shape,
                    ambientColor = Color.Black.copy(alpha = if (isLightGlass) 0.08f else 0.32f),
                    spotColor = Color.Black.copy(alpha = if (isLightGlass) 0.12f else 0.42f)
                )
            } else Modifier
        )
        .drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                blur(blurRadiusDp.toPx())
                colorControls(
                    brightness = 0.0f,
                    contrast = 1.04f,
                    saturation = 1.08f
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    lens(
                        refractionHeight = 12.dp.toPx(),
                        refractionAmount = 22.dp.toPx(),
                        depthEffect = true,
                        chromaticAberration = true
                    )
                }
            },
            highlight = {
                glassHighlight(
                    isLightGlass = isLightGlass,
                    width = if (isLightGlass) 1.05.dp else 0.80.dp,
                    alpha = if (isLightGlass) 0.28f else 0.64f
                )
            },
            shadow = null,
            onDrawSurface = {
                if (size.isSpecified && size.width > 0f && size.height > 0f) {
                    drawRect(baseSurface)
                    if (isLightGlass) {
                        drawRect(Color(0x28FFFFFF))
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.56f),
                                    Color.Transparent,
                                    Color(0x0A0F172A)
                                )
                            )
                        )
                    } else {
                        drawRect(Color(0x10FFFFFF))
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.10f),
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.03f)
                                )
                            )
                        )
                    }
                }
            }
        )
        .clip(shape)
}

/**
 * Directional specular rim highlight along the shape boundary
 */
@Composable
fun Modifier.liquidGlassRim(shape: CornerBasedShape): Modifier {
    val isGlass = ChaiTheme.extended.isLiquidGlass
    val isLightGlass = ChaiTheme.extended.isLightGlass
    if (!isGlass || !isRenderEffectSupported()) return this
    val shapeProvider = remember(shape) { ShapeProvider { shape } }
    return then(
        HighlightElement(
            shapeProvider = shapeProvider,
            highlight = { glassHighlight(isLightGlass = isLightGlass) }
        )
    )
}

data class LiquidSegmentItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

/**
 * 3D Refractive Liquid Glass Segmented Switcher with Water-Droplet Pill Physics
 * Uses sheer transparent crystal glass in both Dark and Light submodes.
 */
@Composable
fun LiquidGlassSegmentedTabs(
    items: List<LiquidSegmentItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val haptics = rememberChaiHaptics()
    val isLightGlass = ChaiTheme.extended.isLightGlass
    val capsuleShape = remember { Capsule() }
    val scope = rememberCoroutineScope()

    val pillMotion = remember { GlassPillMotion(scope, selectedIndex.coerceAtLeast(0)) }
    var lastHoveredSlot by remember { mutableIntStateOf(selectedIndex.coerceAtLeast(0)) }

    LaunchedEffect(selectedIndex) {
        val safeIdx = selectedIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
        lastHoveredSlot = safeIdx
        if (safeIdx != pillMotion.index) {
            pillMotion.animateTo(safeIdx)
        }
    }

    val barGlassLayer = rememberLayerBackdrop()
    val tabRowLayer = rememberLayerBackdrop()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val totalTabs = items.size.coerceAtLeast(1)
        val rowHorizontalPadding = 5.dp
        val availableWidth = maxWidth - (rowHorizontalPadding * 2)
        val barHeight = maxHeight
        val singleTabWidth = availableWidth / totalTabs

        val density = LocalDensity.current
        val singleTabWidthPx = with(density) { singleTabWidth.toPx() }
        val barHeightPx = with(density) { barHeight.toPx() }
        val rowPadPx = with(density) { rowHorizontalPadding.toPx() }

        val pillRestExtraW = with(density) { 4.dp.toPx() }
        val pillRestInsetH = with(density) { 8.dp.toPx() }
        val liftExtraW = with(density) { 20.dp.toPx() }
        val liftExtraH = with(density) { 18.dp.toPx() }

        LaunchedEffect(singleTabWidthPx) {
            pillMotion.stepDp = singleTabWidthPx
        }

        // Layer 1: Transparent Frosted Crystal Capsule Container
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(
                    if (isRenderEffectSupported()) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { capsuleShape },
                            effects = {
                                blur(22.dp.toPx())
                                colorControls(
                                    brightness = 0.0f,
                                    contrast = 1.04f,
                                    saturation = 1.08f
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    lens(
                                        refractionHeight = 13.dp.toPx(),
                                        refractionAmount = 24.dp.toPx(),
                                        depthEffect = true,
                                        chromaticAberration = true
                                    )
                                }
                            },
                            highlight = {
                                glassHighlight(
                                    isLightGlass = isLightGlass,
                                    width = if (isLightGlass) 1.05.dp else 0.80.dp,
                                    alpha = if (isLightGlass) 0.28f else 0.68f
                                )
                            },
                            shadow = {
                                Shadow(
                                    radius = 12.dp,
                                    offset = DpOffset(0.dp, 3.dp),
                                    color = Color.Black.copy(alpha = if (isLightGlass) 0.08f else 0.34f)
                                )
                            },
                            onDrawSurface = {
                                if (size.isSpecified && size.width > 0f && size.height > 0f) {
                                    if (isLightGlass) {
                                        drawRect(Color(0x0B0F172A))
                                        drawRect(Color(0x28FFFFFF))
                                        drawRect(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.56f),
                                                    Color.Transparent,
                                                    Color(0x0A0F172A)
                                                )
                                            )
                                        )
                                    } else {
                                        drawRect(Color(0x4214151A))
                                        drawRect(Color(0x10FFFFFF))
                                        drawRect(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.10f),
                                                    Color.Transparent,
                                                    Color.White.copy(alpha = 0.03f)
                                                )
                                            )
                                        )
                                    }
                                }
                            },
                            exportedBackdrop = barGlassLayer
                        )
                    } else {
                        Modifier
                            .background(if (isLightGlass) Color(0x120F172A) else Color(0xD8141416), capsuleShape)
                            .border(
                                0.9.dp,
                                if (isLightGlass) Color(0x360F172A) else Color.White.copy(alpha = 0.32f),
                                capsuleShape
                            )
                    }
                )
        )

        // Transparent water-droplet selection pill
        val basePillFill = if (isLightGlass) Color(0x140F172A) else Color(0x32FFFFFF)

        val restSize = Size(
            width = (singleTabWidthPx + pillRestExtraW).coerceAtLeast(1f),
            height = (barHeightPx - pillRestInsetH).coerceAtLeast(1f)
        )
        val liftedSize = Size(
            width = (restSize.width + liftExtraW).coerceAtLeast(1f),
            height = (restSize.height + liftExtraH).coerceAtLeast(1f)
        )
        val currentPillSize = pillMotion.liveSize(restSize, liftedSize)
        val pillWidthDp = with(density) { currentPillSize.width.toDp() }
        val pillHeightDp = with(density) { currentPillSize.height.toDp() }

        val centerX = rowPadPx + (pillMotion.position + 0.5f) * singleTabWidthPx
        val offsetX = (centerX - currentPillSize.width * 0.5f).roundToInt()

        if (!isRenderEffectSupported()) {
            FlatSelectionPill(
                color = basePillFill,
                shape = capsuleShape,
                modifier = Modifier
                    .offset { IntOffset(offsetX, 0) }
                    .size(width = pillWidthDp, height = pillHeightDp)
            )
        }

        val activeTextColor = if (isLightGlass) Color(0xFF0F172A) else Color.White
        val inactiveTextColor = if (isLightGlass) Color(0xFF475569) else Color.White.copy(alpha = 0.65f)

        // Layer 3: Segment Row recorded into tabRowLayer
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = rowHorizontalPadding, vertical = 4.dp)
                .layerBackdrop(tabRowLayer)
                .pointerInput(totalTabs, singleTabWidthPx) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            pillMotion.startDrag()
                            if (singleTabWidthPx > 0f) {
                                val touchedSlot = ((offset.x / singleTabWidthPx) - 0.5f)
                                    .coerceIn(-0.08f, (totalTabs - 1) + 0.08f)
                                lastHoveredSlot = touchedSlot.roundToInt().coerceIn(0, totalTabs - 1)
                                haptics.click()
                                pillMotion.dragTo(touchedSlot)
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (singleTabWidthPx > 0f) {
                                val nextPos = (pillMotion.position + dragAmount / singleTabWidthPx)
                                    .coerceIn(-0.10f, (totalTabs - 1) + 0.10f)
                                val hoveredSlot = nextPos.roundToInt().coerceIn(0, totalTabs - 1)
                                if (hoveredSlot != lastHoveredSlot) {
                                    lastHoveredSlot = hoveredSlot
                                    haptics.click()
                                }
                                pillMotion.dragTo(nextPos)
                            }
                        },
                        onDragEnd = {
                            val target = pillMotion.position.roundToInt().coerceIn(0, totalTabs - 1)
                            pillMotion.release(target)
                            if (target != selectedIndex) {
                                haptics.click()
                                onSelect(target)
                            }
                        },
                        onDragCancel = {
                            pillMotion.release(selectedIndex)
                        }
                    )
                },
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index
                val interactionSource = remember { MutableInteractionSource() }

                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect { interaction ->
                        if (interaction is PressInteraction.Press) {
                            pillMotion.onPress(index)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(capsuleShape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            haptics.click()
                            pillMotion.animateTo(index)
                            if (selectedIndex != index) {
                                onSelect(index)
                            }
                        }
                        .testTag(item.testTag),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (isSelected) activeTextColor else inactiveTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.title,
                            fontFamily = InterFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 12.5.sp,
                            color = if (isSelected) activeTextColor else inactiveTextColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Layer 4: 3D Refractive Water-Droplet Selection Pill
        if (isRenderEffectSupported()) {
            GlassSelectionPill(
                motion = pillMotion,
                fillColor = basePillFill,
                pageBackdrop = backdrop,
                barGlass = barGlassLayer,
                tabRow = tabRowLayer,
                shape = capsuleShape,
                modifier = Modifier
                    .offset { IntOffset(offsetX, 0) }
                    .size(width = pillWidthDp, height = pillHeightDp)
            )
        }
    }
}

/**
 * World-Class High Performance Liquid Glass Card / Panel
 */
@Composable
fun LiquidGlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    borderWidth: Dp = 0.85.dp,
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable BoxScope.() -> Unit
) {
    val extended = ChaiTheme.extended
    val isGlass = extended.isLiquidGlass
    val isLightGlass = extended.isLightGlass
    val settings = LiquidGlassEngine.current
    val backdrop = LocalBackdrop.current

    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && settings.touchPhysics) 0.975f else 1.0f,
        animationSpec = spring(dampingRatio = 0.60f, stiffness = 420f),
        label = "glass_press_scale"
    )

    val surfaceColor = when {
        isGlass && isLightGlass -> Color(0x090F172A)
        isGlass -> Color(0x3816181D)
        else -> extended.surfaceSecondary
    }

    val rimBrush = if (isGlass) settings.createRimBrush() else Brush.linearGradient(listOf(extended.border, extended.border))

    Box(
        modifier = modifier
            .scale(pressScale)
            .shadow(
                elevation = if (isGlass) elevation else 1.dp,
                shape = shape,
                ambientColor = when {
                    isGlass && isLightGlass -> Color(0x14000000)
                    isGlass -> Color(0x30000000)
                    else -> Color.Transparent
                },
                spotColor = when {
                    isGlass && isLightGlass -> Color(0x1E000000)
                    isGlass -> Color(0x3C000000)
                    else -> Color.Transparent
                }
            )
            .then(
                if (isGlass && isRenderEffectSupported() && backdrop != null) {
                    Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { shape },
                        effects = {
                            blur(20.dp.toPx())
                            colorControls(
                                brightness = 0.0f,
                                contrast = 1.04f,
                                saturation = 1.08f
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                lens(
                                    refractionHeight = 12.dp.toPx(),
                                    refractionAmount = 22.dp.toPx(),
                                    depthEffect = true,
                                    chromaticAberration = true
                                )
                            }
                        },
                        highlight = {
                            glassHighlight(
                                isLightGlass = isLightGlass,
                                width = if (isLightGlass) 1.05.dp else 0.80.dp,
                                alpha = if (isLightGlass) 0.28f else 0.64f
                            )
                        },
                        shadow = null,
                        onDrawSurface = {
                            if (size.isSpecified && size.width > 0f && size.height > 0f) {
                                drawRect(surfaceColor)
                                if (isLightGlass) {
                                    drawRect(Color(0x28FFFFFF))
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.56f),
                                                Color.Transparent,
                                                Color(0x0A0F172A)
                                            )
                                        )
                                    )
                                } else {
                                    drawRect(Color(0x10FFFFFF))
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.10f),
                                                Color.Transparent,
                                                Color.White.copy(alpha = 0.03f)
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    )
                } else {
                    Modifier
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
        content()
    }
}

/**
 * Liquid Glass Pill Badge
 */
@Composable
fun LiquidGlassPill(
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = RoundedCornerShape(20.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val isGlass = ChaiTheme.extended.isLiquidGlass

    Box(
        modifier = modifier.then(
            if (isGlass) {
                Modifier.liquidGlass(shape = shape, blurRadiusDp = 18.dp)
            } else {
                Modifier
                    .clip(shape)
                    .background(ChaiTheme.extended.surfaceTertiary)
                    .border(1.dp, ChaiTheme.extended.border, shape)
            }
        )
    ) {
        content()
    }
}
