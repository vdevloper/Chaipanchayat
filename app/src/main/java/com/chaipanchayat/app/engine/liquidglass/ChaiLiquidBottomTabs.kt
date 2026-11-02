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
 * Synthesizes the best of BitChord & iOS 26 Liquid Glass:
 * - Elevated floating capsule dock (16dp above system navigation bar, 16dp horizontal margin)
 *   with deep ambient drop shadow so content visibly flows underneath and around the floating bar.
 * - Deep frosted backdrop blur (26dp) + vibrancy + color saturation (1.65x) so underlying text
 *   melts into smooth, velvety color clouds and never bleeds through unblurred.
 * - Perfectly centered 3D selection pill (58dp tall inside a 68dp dock, offsetY = 0 relative to
 *   Alignment.CenterStart) that generously frames 100% of the tab icon + Hindi label at rest
 *   and swells symmetrically (+28dp width, +24dp height) past both top and bottom bar edges when lifted.
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

            // At rest: 58.dp tall (5.dp top & bottom margin inside 68.dp bar) and singleTabWidth + 8.dp wide
            // so the 20.dp icon + 11.sp label (~36.dp total height) has 11.dp of cushion on top and bottom!
            val pillRestExtraW = with(density) { 8.dp.toPx() }
            val pillRestInsetH = with(density) { 10.dp.toPx() }
            // When lifted: swells to 84.dp tall (+8.dp above top of bar and +8.dp below bottom of bar) and +28.dp wider
            val liftExtraW = with(density) { 28.dp.toPx() }
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
                                        brightness = -0.04f,
                                        contrast = 1.14f,
                                        saturation = 1.68f
                                    )
                                    blur(26.dp.toPx())
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        lens(
                                            refractionHeight = 18.dp.toPx(),
                                            refractionAmount = 36.dp.toPx(),
                                            depthEffect = true,
                                            chromaticAberration = true
                                        )
                                    }
                                },
                                highlight = {
                                    Highlight.Default.copy(
                                        width = 1.2.dp,
                                        alpha = 0.92f
                                    )
                                },
                                shadow = {
                                    Shadow(
                                        radius = 22.dp,
                                        offset = DpOffset(0.dp, 8.dp),
                                        color = Color.Black.copy(alpha = 0.54f)
                                    )
                                },
                                onDrawSurface = {
                                    if (size.isSpecified && size.width > 0f && size.height > 0f) {
                                        // 1. Deep Frosted Obsidian-Smoky Glass Base
                                        drawRect(Color(0x90080C18))
                                        // 2. Iridescent Chromatic Glass Cushion (Indigo-Violet -> Chai Saffron -> Warm Amber)
                                        drawRect(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0x344C1D95),
                                                    Color(0x261E1B4B),
                                                    Color(0x2EFF5400),
                                                    Color(0x30D97706)
                                                )
                                            )
                                        )
                                        // 3. Milky Frosted Diffusion Layer
                                        drawRect(Color(0x1AFFFFFF))
                                        // 4. Upper Specular Glass Bevel & Lower Depth Vignette
                                        drawRect(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.25f),
                                                    Color.White.copy(alpha = 0.06f),
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.28f)
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
                                            Color.White.copy(alpha = 0.55f),
                                            Color.White.copy(alpha = 0.20f),
                                            Color.White.copy(alpha = 0.06f)
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
                ChaiSaffron.copy(alpha = 0.38f)
            } else {
                Color(0x48E0E7FF) // Luminous milky-crystal glass pill tint
            }

            // Compute live physical capsule size (58.dp tall at rest, 84.dp tall water-droplet when lifted)
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

            // Horizontal center along the tab bar; vertical alignment is handled by BoxWithConstraints(CenterStart) so offsetY = 0!
            val centerX = rowPadPx + (pillMotion.position + 0.5f) * singleTabWidthPx
            val offsetX = (centerX - currentPillSize.width * 0.5f).roundToInt()

            // Layer 2: Pre-Android 12 Fallback Pill
            if (!isRenderEffectSupported()) {
                FlatSelectionPill(
                    color = basePillFill,
                    shape = capsuleShape,
                    modifier = Modifier
                        .offset { IntOffset(offsetX, 0) }
                        .size(width = pillWidthDp, height = pillHeightDp)
                )
            }

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
                        targetValue = if (isSelected) 1.08f else 1.0f,
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
                                    tint = if (isSelected) {
                                        Color.White
                                    } else {
                                        Color.White.copy(alpha = 0.68f)
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
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tabTitle,
                                fontFamily = InterFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                letterSpacing = 0.15.sp,
                                maxLines = 1,
                                color = if (isSelected) {
                                    Color.White
                                } else {
                                    Color.White.copy(alpha = 0.65f)
                                }
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
