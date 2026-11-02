package com.chaipanchayat.app.engine.liquidglass

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
 * - Unclipped outer container so the lifted GlassSelectionPill swells (+20dp width, +16dp height)
 *   past the top and bottom edges of the capsule dock during travel and drag
 * - Genuine AGSL lens refraction (refractionHeight = 12dp, refractionAmount = 24dp) with depthEffect
 *   and RGB chromatic aberration
 * - Vibrancy + Color saturation boost (1.45x) and crystal blur (8dp) so underlying content bends visibly
 * - Dual layered backdrops (barGlassLayer + tabRowLayer) so the lifted selection pill magnifies and
 *   chromatically refracts the tab icons and Hindi labels underneath it
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

    LaunchedEffect(selectedIndex) {
        pillMotion.animateTo(selectedIndex)
    }

    // Layer backdrops for multi-surface glass refraction
    val barGlassLayer = rememberLayerBackdrop()
    val tabRowLayer = rememberLayerBackdrop()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Unclipped Outer Container so the lifted selection pill can swell outside the 64.dp bar
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
            val liftExtraW = with(density) { 22.dp.toPx() }
            val liftExtraH = with(density) { 16.dp.toPx() }

            LaunchedEffect(singleTabWidthPx) {
                pillMotion.stepDp = singleTabWidthPx
            }

            // Layer 1: Floating Liquid Glass Capsule Bar Backdrop
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
                                    colorControls(saturation = 1.45f)
                                    blur(8.dp.toPx())
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
                                    drawRect(Color(0x330D111A))
                                },
                                exportedBackdrop = barGlassLayer
                            )
                        } else {
                            Modifier
                                .background(Color(0xEE0B1220), capsuleShape)
                                .border(
                                    width = 0.9.dp,
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

            val isCurrentTabHome = tabs.getOrNull(selectedIndex)?.tab == MainTab.HOME
            val basePillFill = if (isCurrentTabHome) {
                ChaiSaffron.copy(alpha = 0.24f)
            } else {
                Color.White.copy(alpha = 0.16f)
            }

            // Compute live physical size (rest vs lifted with acceleration squash)
            val restSize = Size(
                width = (singleTabWidthPx - padPx).coerceAtLeast(1f),
                height = (barHeightPx - padPx * 2f).coerceAtLeast(1f)
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

            // Layer 2: Flat Settled Pill (sits behind tab glyphs when settled or handing over)
            if (!isRenderEffectSupported() || pillMotion.glassPresence < 1f) {
                val flatAlpha = if (isRenderEffectSupported()) (1f - pillMotion.glassPresence).coerceIn(0f, 1f) else 1f
                FlatSelectionPill(
                    color = basePillFill.copy(alpha = basePillFill.alpha * flatAlpha),
                    shape = capsuleShape,
                    modifier = Modifier
                        .offset { IntOffset(offsetX, offsetY) }
                        .size(width = pillWidthDp, height = pillHeightDp)
                )
            }

            // Layer 3: Razor-Sharp Interactive Tab Row (Recorded into tabRowLayer so lifted glass pill refracts it!)
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
                                        .coerceIn(-0.15f, (totalTabs - 1) + 0.15f)
                                    pillMotion.dragTo(touchedSlot)
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                if (singleTabWidthPx > 0f) {
                                    val nextPos = (pillMotion.position + dragAmount / singleTabWidthPx)
                                        .coerceIn(-0.18f, (totalTabs - 1) + 0.18f)
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

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) (if (isTabHome) 1.16f else 1.10f) else 1.0f,
                        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
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
                                interactionSource = remember { MutableInteractionSource() },
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
                                        Color.White.copy(alpha = 0.65f)
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
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.5.sp,
                                letterSpacing = 0.15.sp,
                                color = if (isSelected) {
                                    if (isTabHome) ChaiSaffron else Color.White
                                } else {
                                    Color.White.copy(alpha = 0.60f)
                                }
                            )
                        }
                    }
                }
            }

            // Layer 4: Lifted Water-Droplet Refractive Glass Selection Pill (sits ABOVE tab row when moving/lifted!)
            if (isRenderEffectSupported() && !pillMotion.isFlat) {
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
