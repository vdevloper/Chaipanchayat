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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.ui.navigation.MainTab
import com.chaipanchayat.app.ui.theme.ChaiCrimson
import com.chaipanchayat.app.ui.theme.ChaiSaffron
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
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.isRenderEffectSupported
import com.kyant.backdrop.shadow.Shadow
import kotlin.math.roundToInt

data class ChaiTabItem(
    val tab: MainTab,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

/**
 * Flagship Refractive Liquid Glass Floating Navigation Dock for Chai Panchayat
 * Modeled 1:1 after BitChord & Kyant0/AndroidLiquidGlass:
 * - Deep frosted backdrop blur (24dp) + vibrancy + color saturation (1.65x) so underlying text
 *   is diffused into rich, velvety color clouds and never bleeds through unblurred.
 * - Unclipped outer container so the lifted GlassSelectionPill swells (+26dp width, +18dp height)
 *   past the top and bottom edges of the capsule dock during travel and drag.
 * - Genuine AGSL lens refraction (refractionHeight = 16dp, refractionAmount = 34dp) with depthEffect
 *   and RGB chromatic aberration on both the bar and the continuous 3D selection droplet.
 */
@Composable
fun ChaiLiquidBottomTabs(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    backdrop: Backdrop,
    tabs: List<ChaiTabItem>,
    videoBadgeAlpha: Float,
    modifier: Modifier = Modifier
) {
    val haptics = rememberChaiHaptics()
    val strings = com.chaipanchayat.app.ui.theme.LocalChaiStrings.current
    val capsuleShape = remember { Capsule() }
    val scope = rememberCoroutineScope()

    val selectedIndex = tabs.indexOfFirst { it.tab == selectedTab }.coerceAtLeast(0)

    val pillMotion = remember { GlassPillMotion(scope, selectedIndex) }
    var lastHoveredSlot by remember { mutableIntStateOf(selectedIndex) }

    LaunchedEffect(selectedIndex) {
        lastHoveredSlot = selectedIndex
        if (selectedIndex != pillMotion.index) {
            pillMotion.animateTo(selectedIndex)
        }
    }

    // Layer backdrops for multi-surface glass refraction
    val barGlassLayer = rememberLayerBackdrop()
    val tabRowLayer = rememberLayerBackdrop()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Unclipped Outer Container so the lifted selection pill swells outside the 64.dp bar
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val totalTabs = tabs.size.coerceAtLeast(1)
            val availableWidth = maxWidth - 8.dp
            val barHeight = maxHeight
            val singleTabWidth = availableWidth / totalTabs

            val density = LocalDensity.current
            val singleTabWidthPx = with(density) { singleTabWidth.toPx() }
            val barHeightPx = with(density) { barHeight.toPx() }
            val padPx = with(density) { 4.dp.toPx() }
            val pillRestExtraW = with(density) { 10.dp.toPx() }
            val pillRestInsetH = with(density) { 10.dp.toPx() }
            val liftExtraW = with(density) { 26.dp.toPx() }
            val liftExtraH = with(density) { 26.dp.toPx() }

            LaunchedEffect(singleTabWidthPx) {
                pillMotion.stepDp = singleTabWidthPx
            }

            // Layer 1: Floating Frosted & Refractive Liquid Glass Capsule Bar Backdrop
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .then(
                        if (isRenderEffectSupported()) {
                            Modifier.drawBackdrop(
                                backdrop = backdrop,
                                shape = { capsuleShape },
                                effects = {
                                    vibrancy()
                                    colorControls(
                                        brightness = -0.03f,
                                        contrast = 1.12f,
                                        saturation = 1.65f
                                    )
                                    blur(24.dp.toPx())
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        lens(
                                            refractionHeight = 16.dp.toPx(),
                                            refractionAmount = 34.dp.toPx(),
                                            depthEffect = true,
                                            chromaticAberration = true
                                        )
                                    }
                                },
                                highlight = {
                                    Highlight.Default.copy(
                                        width = 1.1.dp,
                                        alpha = 0.88f
                                    )
                                },
                                shadow = {
                                    Shadow(
                                        radius = 18.dp,
                                        offset = DpOffset(0.dp, 6.dp),
                                        color = Color.Black.copy(alpha = 0.48f)
                                    )
                                },
                                onDrawSurface = {
                                    if (size.isSpecified && size.width > 0f && size.height > 0f) {
                                        // 1. Deep Frosted Obsidian-Sapphire Base (blocks raw text bleed-through)
                                        drawRect(Color(0x88080D1A))
                                        // 2. BitChord Chromatic Glass Cushion (Indigo-Violet -> Chai Saffron -> Warm Amber)
                                        drawRect(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0x305B21B6),
                                                    Color(0x221E1B4B),
                                                    Color(0x2AFF5400),
                                                    Color(0x2ED97706)
                                                )
                                            )
                                        )
                                        // 3. Milky Frosted Diffusion Layer
                                        drawRect(Color(0x16FFFFFF))
                                        // 4. Upper Specular Glass Bevel & Lower Depth Vignette
                                        drawRect(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.22f),
                                                    Color.White.copy(alpha = 0.05f),
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.25f)
                                                )
                                            )
                                        )
                                    }
                                },
                                exportedBackdrop = barGlassLayer
                            )
                        } else {
                            Modifier
                                .background(Color(0xF00B1220), capsuleShape)
                                .border(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.52f),
                                            Color.White.copy(alpha = 0.18f),
                                            Color.White.copy(alpha = 0.05f)
                                        )
                                    ),
                                    shape = capsuleShape
                                )
                        }
                    )
            )

            val activeSlotIndex = pillMotion.position.roundToInt().coerceIn(0, totalTabs - 1)
            val isCurrentTabHome = tabs.getOrNull(activeSlotIndex)?.tab == MainTab.HOME
            val basePillFill = if (isCurrentTabHome) {
                ChaiSaffron.copy(alpha = 0.30f)
            } else {
                Color(0x42A5B4FC) // Luminous crystal indigo-ice tint
            }

            // Compute live physical capsule size (wide capsule at rest, oversized water-droplet when lifted)
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

            // Center coordinates of the pill along the tab bar
            val centerX = padPx + (pillMotion.position + 0.5f) * singleTabWidthPx
            val centerY = barHeightPx * 0.5f
            val offsetX = (centerX - currentPillSize.width * 0.5f).roundToInt()
            val offsetY = (centerY - currentPillSize.height * 0.5f).roundToInt()

            // Layer 2: Pre-Android 12 Fallback Pill
            if (!isRenderEffectSupported()) {
                FlatSelectionPill(
                    color = basePillFill,
                    shape = capsuleShape,
                    modifier = Modifier
                        .offset { IntOffset(offsetX, offsetY) }
                        .size(width = pillWidthDp, height = pillHeightDp)
                )
            }

            // Layer 3: Razor-Sharp Interactive Tab Row (Recorded into tabRowLayer so glass pill refracts it!)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .layerBackdrop(tabRowLayer)
                    .pointerInput(totalTabs, singleTabWidthPx) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                pillMotion.startDrag()
                                if (singleTabWidthPx > 0f) {
                                    val touchedSlot = ((offset.x / singleTabWidthPx) - 0.5f)
                                        .coerceIn(-0.12f, (totalTabs - 1) + 0.12f)
                                    lastHoveredSlot = touchedSlot.roundToInt().coerceIn(0, totalTabs - 1)
                                    haptics.click()
                                    pillMotion.dragTo(touchedSlot)
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                if (singleTabWidthPx > 0f) {
                                    val nextPos = (pillMotion.position + dragAmount / singleTabWidthPx)
                                        .coerceIn(-0.15f, (totalTabs - 1) + 0.15f)
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
                                    onTabSelected(tabs[target].tab)
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
                tabs.forEachIndexed { index, item ->
                    val isSelected = selectedTab == item.tab
                    val isTabHome = item.tab == MainTab.HOME
                    val tabInteractionSource = remember { MutableInteractionSource() }

                    LaunchedEffect(tabInteractionSource) {
                        tabInteractionSource.interactions.collect { interaction ->
                            if (interaction is PressInteraction.Press) {
                                pillMotion.onPress(index)
                            }
                        }
                    }

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) (if (isTabHome) 1.18f else 1.12f) else 1.0f,
                        animationSpec = spring(dampingRatio = 0.55f, stiffness = 480f),
                        label = "tab_scale_${item.tab.name}"
                    )

                    val tabTitle = when (item.tab) {
                        MainTab.HOME -> strings.home
                        MainTab.VIDEOS -> strings.videos
                        MainTab.CATEGORIES -> strings.categories
                        MainTab.SAVED -> strings.saved
                        MainTab.SETTINGS -> strings.settings
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(capsuleShape)
                            .clickable(
                                interactionSource = tabInteractionSource,
                                indication = null
                            ) {
                                haptics.click()
                                pillMotion.animateTo(index)
                                if (selectedTab != item.tab) {
                                    onTabSelected(item.tab)
                                }
                            }
                            .testTag(item.testTag),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier.scale(iconScale),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = tabTitle,
                                    modifier = Modifier.size(if (isTabHome) 23.dp else 21.dp),
                                    tint = if (isSelected) {
                                        if (isTabHome) ChaiSaffron else Color.White
                                    } else {
                                        Color.White.copy(alpha = 0.72f)
                                    }
                                )

                                if (item.tab == MainTab.VIDEOS) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.5.dp)
                                            .background(ChaiCrimson.copy(alpha = videoBadgeAlpha), CircleShape)
                                            .align(Alignment.TopEnd)
                                            .offset(x = 3.dp, y = (-2).dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.5.dp))
                            Text(
                                text = tabTitle,
                                fontFamily = InterFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 10.5.sp,
                                letterSpacing = 0.15.sp,
                                color = if (isSelected) {
                                    if (isTabHome) ChaiSaffron else Color.White
                                } else {
                                    Color.White.copy(alpha = 0.68f)
                                }
                            )
                        }
                    }
                }
            }

            // Layer 4: Continuous 3D Water-Droplet Refractive Glass Selection Pill
            if (isRenderEffectSupported()) {
                GlassSelectionPill(
                    motion = pillMotion,
                    fillColor = basePillFill,
                    pageBackdrop = backdrop,
                    barGlass = barGlassLayer,
                    tabRow = tabRowLayer,
                    shape = capsuleShape,
                    modifier = Modifier
                        .offset { IntOffset(offsetX, offsetY) }
                        .size(width = pillWidthDp, height = pillHeightDp)
                )
            }
        }
    }
}
