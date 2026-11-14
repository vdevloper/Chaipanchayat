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
 * In both Dark Glass and Light Glass submodes, the dock and the active water-droplet selection pill
 * are genuinely transparent crystal glass — refracting and frosting the underlying feed.
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
    val isLightGlass = ChaiTheme.extended.isLightGlass
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

    // Elevated floating dock container: 16.dp bottom clearance above system nav bar so it truly floats
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 6.dp, bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Unclipped 68.dp Outer Container with CenterStart alignment so the pill is ALWAYS vertically centered (offsetY = 0)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val totalTabs = tabs.size.coerceAtLeast(1)
            val rowHorizontalPadding = 8.dp
            val availableWidth = maxWidth - (rowHorizontalPadding * 2)
            val barHeight = maxHeight
            val singleTabWidth = availableWidth / totalTabs

            val density = LocalDensity.current
            val singleTabWidthPx = with(density) { singleTabWidth.toPx() }
            val barHeightPx = with(density) { barHeight.toPx() }
            val rowPadPx = with(density) { rowHorizontalPadding.toPx() }

            val pillRestExtraW = with(density) { 8.dp.toPx() }
            val pillRestInsetH = with(density) { 10.dp.toPx() }
            val liftExtraW = with(density) { 28.dp.toPx() }
            val liftExtraH = with(density) { 26.dp.toPx() }

            LaunchedEffect(singleTabWidthPx) {
                pillMotion.stepDp = singleTabWidthPx
            }

            // Layer 1: Floating Transparent & Refractive Liquid Glass Capsule Bar Backdrop
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
                                        saturation = 1.12f
                                    )
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        lens(
                                            refractionHeight = 15.dp.toPx(),
                                            refractionAmount = 28.dp.toPx(),
                                            depthEffect = true,
                                            chromaticAberration = true
                                        )
                                    }
                                },
                                highlight = {
                                    glassHighlight(
                                        isLightGlass = isLightGlass,
                                        width = if (isLightGlass) 1.1.dp else 0.85.dp,
                                        alpha = if (isLightGlass) 0.30f else 0.72f
                                    )
                                },
                                shadow = {
                                    Shadow(
                                        radius = 20.dp,
                                        offset = DpOffset(0.dp, 6.dp),
                                        color = Color.Black.copy(alpha = if (isLightGlass) 0.12f else 0.48f)
                                    )
                                },
                                onDrawSurface = {
                                    if (size.isSpecified && size.width > 0f && size.height > 0f) {
                                        if (isLightGlass) {
                                            // Crystal-clear transparent glass dock with top white specular sheen
                                            drawRect(Color(0x0B0F172A))
                                            drawRect(Color(0x2EFFFFFF))
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
                                            // Dark Smoked-Crystal Frosted Glass Dock
                                            drawRect(Color(0x52121318))
                                            drawRect(Color(0x12FFFFFF))
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
                                },
                                exportedBackdrop = barGlassLayer
                            )
                        } else {
                            Modifier
                                .background(if (isLightGlass) Color(0x160F172A) else Color(0xDF141416), capsuleShape)
                                .border(
                                    width = 0.95.dp,
                                    brush = Brush.verticalGradient(
                                        if (isLightGlass) {
                                            listOf(
                                                Color(0x380F172A),
                                                Color(0x180F172A)
                                            )
                                        } else {
                                            listOf(
                                                Color.White.copy(alpha = 0.42f),
                                                Color.White.copy(alpha = 0.14f)
                                            )
                                        }
                                    ),
                                    shape = capsuleShape
                                )
                        }
                    )
            )

            // Sheer transparent crystal water-droplet pill in both Dark and Light Glass modes!
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

            val activeTabColor = if (isLightGlass) Color(0xFF0F172A) else Color.White
            val inactiveTabColor = if (isLightGlass) Color(0xFF475569) else Color.White.copy(alpha = 0.65f)

            // Layer 3: Razor-Sharp Interactive Tab Row (Recorded into tabRowLayer so glass pill refracts it!)
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
                                        .coerceIn(-0.10f, (totalTabs - 1) + 0.10f)
                                    lastHoveredSlot = touchedSlot.roundToInt().coerceIn(0, totalTabs - 1)
                                    haptics.click()
                                    pillMotion.dragTo(touchedSlot)
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                if (singleTabWidthPx > 0f) {
                                    val nextPos = (pillMotion.position + dragAmount / singleTabWidthPx)
                                        .coerceIn(-0.12f, (totalTabs - 1) + 0.12f)
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
                        targetValue = if (isSelected) 1.06f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.58f, stiffness = 480f),
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
                                    modifier = Modifier.size(if (isTabHome) 21.dp else 20.dp),
                                    tint = if (isSelected) activeTabColor else inactiveTabColor
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
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tabTitle,
                                fontFamily = InterFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                letterSpacing = 0.15.sp,
                                maxLines = 1,
                                color = if (isSelected) activeTabColor else inactiveTabColor
                            )
                        }
                    }
                }
            }

            // Layer 4: Continuous 3D Water-Droplet Refractive Glass Selection Pill (Centered at offsetY = 0)
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
}
