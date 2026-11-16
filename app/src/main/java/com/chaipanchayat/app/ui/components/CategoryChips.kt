package com.chaipanchayat.app.ui.components

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.engine.liquidglass.LocalBackdrop
import com.chaipanchayat.app.engine.liquidglass.glassHighlight
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.utils.rememberChaiHaptics
import com.kyant.backdrop.Capsule
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.isRenderEffectSupported

data class CategoryChipItem(
    val id: Long,
    val name: String
)

/**
 * Horizontal Category Selector Bar
 * In Liquid Glass mode, renders 3D refractive capsule droplets supporting both Dark Glass and Light Glass submodes.
 */
@Composable
fun CategoryChips(
    chips: List<CategoryChipItem>,
    selectedId: Long,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: com.kyant.backdrop.Backdrop? = LocalBackdrop.current,
    isLoading: Boolean = false
) {
    val haptics = rememberChaiHaptics()
    val isLiquid = ChaiTheme.extended.isLiquidGlass
    val isLightGlass = ChaiTheme.extended.isLightGlass
    val capsuleShape = remember { Capsule() }

    Surface(
        color = if (isLiquid) Color.Transparent else MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chips, key = { it.id }) { cat ->
                val isSelected = cat.id == selectedId
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                val chipScale by animateFloatAsState(
                    targetValue = when {
                        isPressed -> 0.92f
                        isSelected -> 1.04f
                        else -> 1.0f
                    },
                    animationSpec = spring(dampingRatio = 0.50f, stiffness = 460f),
                    label = "chip_scale_${cat.id}"
                )

                if (isLiquid) {
                    val activeFraction by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        animationSpec = spring(dampingRatio = 0.62f, stiffness = 420f),
                        label = "chip_active_fraction_${cat.id}"
                    )

                    val chipTextColor = when {
                        isLightGlass && isSelected -> Color(0xFF0F172A)
                        isLightGlass -> Color(0xFF334155)
                        isSelected -> Color.White
                        else -> Color.White.copy(alpha = 0.78f)
                    }

                    Box(
                        modifier = Modifier
                            .testTag("category-chip-${cat.id}")
                            .scale(chipScale)
                            .then(
                                if (isRenderEffectSupported() && backdrop != null) {
                                    Modifier.drawBackdrop(
                                        backdrop = backdrop,
                                        shape = { capsuleShape },
                                        effects = {
                                            blur((18f + 4f * activeFraction).dp.toPx())
                                            colorControls(
                                                brightness = 0.0f,
                                                contrast = 1.04f,
                                                saturation = 1.08f
                                            )
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                lens(
                                                    refractionHeight = (10f + 4f * activeFraction).dp.toPx(),
                                                    refractionAmount = (18f + 8f * activeFraction).dp.toPx(),
                                                    depthEffect = true,
                                                    chromaticAberration = true
                                                )
                                            }
                                        },
                                        highlight = {
                                            glassHighlight(
                                                isLightGlass = isLightGlass,
                                                width = (0.90f + 0.25f * activeFraction).dp,
                                                alpha = if (isLightGlass) {
                                                    (0.28f + 0.14f * activeFraction).coerceIn(0f, 1f)
                                                } else {
                                                    (0.62f + 0.22f * activeFraction).coerceIn(0f, 1f)
                                                }
                                            )
                                        },
                                        shadow = null,
                                        onDrawSurface = {
                                            if (size.isSpecified && size.width > 0f && size.height > 0f) {
                                                if (isLightGlass) {
                                                    if (isSelected) {
                                                        drawRect(Color(0x160F172A))
                                                        drawRect(Color(0x33FFFFFF))
                                                        drawRect(
                                                            brush = Brush.verticalGradient(
                                                                listOf(
                                                                    Color.White.copy(alpha = 0.62f),
                                                                    Color.Transparent,
                                                                    Color(0x0C0F172A)
                                                                )
                                                            )
                                                        )
                                                    } else {
                                                        drawRect(Color(0x0A0F172A))
                                                        drawRect(Color(0x22FFFFFF))
                                                        drawRect(
                                                            brush = Brush.verticalGradient(
                                                                listOf(
                                                                    Color.White.copy(alpha = 0.50f),
                                                                    Color.Transparent,
                                                                    Color(0x080F172A)
                                                                )
                                                            )
                                                        )
                                                    }
                                                } else {
                                                    if (isSelected) {
                                                        drawRect(Color(0x36FFFFFF))
                                                        drawRect(
                                                            brush = Brush.verticalGradient(
                                                                listOf(
                                                                    Color.White.copy(alpha = 0.16f),
                                                                    Color.Transparent,
                                                                    Color.White.copy(alpha = 0.05f)
                                                                )
                                                            )
                                                        )
                                                    } else {
                                                        drawRect(Color(0x4E16171C))
                                                        drawRect(Color(0x10FFFFFF))
                                                        drawRect(
                                                            brush = Brush.verticalGradient(
                                                                listOf(
                                                                    Color.White.copy(alpha = 0.08f),
                                                                    Color.Transparent,
                                                                    Color.White.copy(alpha = 0.02f)
                                                                )
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    )
                                } else {
                                    Modifier
                                        .background(
                                            when {
                                                isLightGlass && isSelected -> Color(0x180F172A)
                                                isLightGlass -> Color(0x0C0F172A)
                                                isSelected -> Color(0x3BFFFFFF)
                                                else -> Color(0x5218181B)
                                            },
                                            capsuleShape
                                        )
                                        .border(
                                            width = 0.85.dp,
                                            color = when {
                                                isLightGlass && isSelected -> Color(0x420F172A)
                                                isLightGlass -> Color(0x280F172A)
                                                isSelected -> Color.White.copy(alpha = 0.75f)
                                                else -> Color.White.copy(alpha = 0.28f)
                                            },
                                            shape = capsuleShape
                                        )
                                }
                            )
                            .clip(capsuleShape)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                haptics.click()
                                onSelect(cat.id)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 15.dp, vertical = 7.5.dp)
                        ) {
                            AnimatedVisibility(
                                visible = isSelected,
                                enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.3f, animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f)),
                                exit = fadeOut(tween(120)) + scaleOut(targetScale = 0.3f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(ChaiSaffron, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                            }
                            Text(
                                text = cat.name,
                                color = chipTextColor,
                                fontFamily = InterFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 13.sp,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }
                } else {
                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) ChaiSaffron else ChaiTheme.extended.surfaceSecondary,
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                        label = "chip_bg_${cat.id}"
                    )

                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                        label = "chip_text_${cat.id}"
                    )

                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) ChaiSaffron else ChaiTheme.extended.border,
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                        label = "chip_border_${cat.id}"
                    )

                    val chipShape = RoundedCornerShape(20.dp)

                    Surface(
                        shape = chipShape,
                        color = containerColor,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .testTag("category-chip-${cat.id}")
                            .scale(chipScale)
                            .clip(chipShape)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                haptics.click()
                                onSelect(cat.id)
                            }
                    ) {
                        Text(
                            text = cat.name,
                            color = textColor,
                            fontFamily = InterFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 13.sp,
                            letterSpacing = 0.2.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }
    }
}
