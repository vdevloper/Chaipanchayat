package com.chaipanchayat.app.engine.liquidglass

import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
 * Modeled after iOS 26 and BitChord/Kyant0 backdrop rendering:
 * - Genuine AGSL lens refraction with depth and chromatic dispersion
 * - Color saturation boost (1.35x) so underlying content colors flow through the glass
 * - Traveling glass selection pill with Euler physics, under-damped spring lift & squash deformation
 * - Dual layered backdrops (records bar glass and foreground tab glyphs for realistic refraction)
 * - Directional specular rim highlight and upper bevel reflection
 * - High-contrast razor-sharp icons and Hindi typography
 * - Full accessibility compliance (>= 48dp touch targets)
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

    // Layer backdrops for layered glass refraction
    val barGlassLayer = rememberLayerBackdrop()
    val tabRowLayer = rememberLayerBackdrop()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer Floating Liquid Glass Capsule
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = capsuleShape,
                    ambientColor = Color(0x35000000),
                    spotColor = Color(0x45000000)
                )
                .then(
                    if (isRenderEffectSupported()) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { capsuleShape },
                            effects = {
                                colorControls(saturation = 1.35f)
                                blur(16.dp.toPx())
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    lens(
                                        refractionHeight = 8.dp.toPx(),
                                        refractionAmount = 10.dp.toPx(),
                                        depthEffect = true,
                                        chromaticAberration = true
                                    )
                                }
                            },
                            highlight = { Highlight.Default },
                            shadow = { Shadow.Default },
                            onDrawSurface = {
                                // Translucent optical glass tint
                                drawRect(Color(0x380B1220))
                            },
                            exportedBackdrop = barGlassLayer
                        )
                    } else {
                        Modifier.background(Color(0xEE0B1220), capsuleShape)
                    }
                )
                .clip(capsuleShape)
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
                .padding(horizontal = 4.dp, vertical = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val totalTabs = tabs.size.coerceAtLeast(1)
            val availableWidth = maxWidth
            val singleTabWidth = availableWidth / totalTabs

            val density = LocalDensity.current
            LaunchedEffect(singleTabWidth) {
                pillMotion.stepDp = with(density) { singleTabWidth.toPx() }
            }

            // Layer 1: Traveling Physical Liquid Glass Selection Pill
            val isCurrentTabHome = tabs.getOrNull(selectedIndex)?.tab == MainTab.HOME
            val pillAccentColor = if (isCurrentTabHome) ChaiSaffron else Color.White

            if (isRenderEffectSupported() && !pillMotion.isFlat) {
                // Lifted Refractive Glass Pill with AGSL Lens
                GlassSelectionPill(
                    motion = pillMotion,
                    fillColor = if (isCurrentTabHome) ChaiSaffron.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.14f),
                    pageBackdrop = backdrop,
                    barGlass = barGlassLayer,
                    tabRow = tabRowLayer,
                    shape = capsuleShape,
                    modifier = Modifier
                        .width(singleTabWidth)
                        .fillMaxHeight()
                        .offset(x = singleTabWidth * pillMotion.position)
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                )
            } else {
                // Flat Settled Pill
                FlatSelectionPill(
                    color = if (isCurrentTabHome) ChaiSaffron.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.14f),
                    shape = capsuleShape,
                    modifier = Modifier
                        .width(singleTabWidth)
                        .fillMaxHeight()
                        .offset(x = singleTabWidth * pillMotion.position)
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                )
            }

            // Layer 2: Top Specular Bevel Reflection (physical light entry along upper glass edge)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.2.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.65f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Layer 3: Razor-Sharp Interactive Tab Row (Recorded for lens refraction, rendered crisp foreground)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(tabRowLayer)
                    .pointerInput(totalTabs) {
                        detectHorizontalDragGestures(
                            onDragStart = { pillMotion.startDrag() },
                            onHorizontalDrag = { _, dragAmount ->
                                val stepPx = singleTabWidth.toPx()
                                if (stepPx > 0f) {
                                    pillMotion.dragTo(pillMotion.position + dragAmount / stepPx)
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
                                if (selectedTab != item.tab) {
                                    haptics.click()
                                    pillMotion.animateTo(index)
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
                                        Color.White.copy(alpha = 0.58f)
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
                                    Color.White.copy(alpha = 0.52f)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
