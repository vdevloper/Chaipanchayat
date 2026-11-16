package com.chaipanchayat.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chaipanchayat.app.ui.theme.ChaiGold
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.utils.rememberChaiHaptics
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Safely applies sharedElement transition when both SharedTransitionScope and AnimatedVisibilityScope
 * are active (e.g., navigating from Home/Search cards to ArticleScreen).
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.chaiSharedElement(key: String): Modifier = composed {
    val sharedScope = LocalSharedTransitionScope.current
    val animScope = LocalNavAnimatedVisibilityScope.current
    if (sharedScope != null && animScope != null) {
        with(sharedScope) {
            this@composed.sharedElement(
                state = rememberSharedContentState(key = key),
                animatedVisibilityScope = animScope
            )
        }
    } else {
        this
    }
}

/**
 * Safely applies sharedBounds transition for text / containers across navigation routes.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.chaiSharedBounds(key: String): Modifier = composed {
    val sharedScope = LocalSharedTransitionScope.current
    val animScope = LocalNavAnimatedVisibilityScope.current
    if (sharedScope != null && animScope != null) {
        with(sharedScope) {
            this@composed.sharedBounds(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = animScope
            )
        }
    } else {
        this
    }
}

/**
 * Smooth entrance glide + fade for feed cards as they enter the viewport.
 */
fun Modifier.chaiFeedItemReveal(staggerIndex: Int = 0): Modifier = composed {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        val delayMs = (staggerIndex.coerceIn(0, 5) * 32).toLong()
        if (delayMs > 0L) delay(delayMs)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
        )
    }
    this.graphicsLayer {
        val p = progress.value
        alpha = p
        translationY = (1f - p) * 26f
    }
}

/**
 * Premium spring-animated Bookmark Toggle Button with radial Saffron Spark Burst.
 */
@Composable
fun ChaiBookmarkButton(
    isBookmarked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    iconSize: Dp = 19.dp,
    activeColor: Color = ChaiSaffron,
    inactiveColor: Color = ChaiTheme.extended.muted,
    testTag: String? = "bookmark-button"
) {
    val haptics = rememberChaiHaptics()
    val popScale = remember { Animatable(1.0f) }
    val burstProgress = remember { Animatable(0f) }
    var previousBookmarked by remember { mutableStateOf(isBookmarked) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label = "press_scale"
    )

    LaunchedEffect(isBookmarked) {
        if (isBookmarked && !previousBookmarked) {
            burstProgress.snapTo(0.01f)
            popScale.snapTo(0.82f)
            popScale.animateTo(
                targetValue = 1.32f,
                animationSpec = spring(dampingRatio = 0.42f, stiffness = 700f)
            )
            popScale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f)
            )
        } else {
            popScale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f)
            )
        }
        previousBookmarked = isBookmarked
    }

    LaunchedEffect(isBookmarked) {
        if (isBookmarked && burstProgress.value > 0f) {
            burstProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 440, easing = FastOutSlowInEasing)
            )
            burstProgress.snapTo(0f)
        }
    }

    val animatedColor by animateColorAsState(
        targetValue = if (isBookmarked) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "bookmark_color"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (burstProgress.value > 0f && burstProgress.value < 1f) {
            val p = burstProgress.value
            Canvas(modifier = Modifier.size(size * 1.35f)) {
                val cx = this.size.width / 2f
                val cy = this.size.height / 2f
                val maxR = this.size.minDimension * 0.48f
                val sparkCount = 6
                for (i in 0 until sparkCount) {
                    val angle = (2.0 * PI * i / sparkCount) - PI / 2.0
                    val dist = maxR * p
                    val sx = cx + (dist * cos(angle)).toFloat()
                    val sy = cy + (dist * sin(angle)).toFloat()
                    val radius = (1f - p * 0.65f) * 2.4.dp.toPx()
                    val sparkColor = if (i % 2 == 0) ChaiSaffron else ChaiGold
                    drawCircle(
                        color = sparkColor.copy(alpha = (1f - p).coerceIn(0f, 1f)),
                        radius = radius,
                        center = Offset(sx, sy)
                    )
                }
            }
        }

        IconButton(
            onClick = {
                if (!isBookmarked) {
                    haptics.success()
                } else {
                    haptics.click()
                }
                onToggle()
            },
            interactionSource = interactionSource,
            modifier = Modifier
                .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
                .size(size)
                .scale(popScale.value * pressScale)
        ) {
            Icon(
                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = if (isBookmarked) "Saved story" else "Save story",
                tint = animatedColor,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/**
 * Compact 1-tap Audio Listen button for feed cards.
 * Morphs cleanly into live animated equalizer bars while that story is playing.
 */
@Composable
fun ChaiAudioActionButton(
    isPlayingThisPost: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    iconSize: Dp = 15.dp,
    isLiquid: Boolean = ChaiTheme.extended.isLiquidGlass,
    isLightGlass: Boolean = ChaiTheme.extended.isLightGlass
) {
    val haptics = rememberChaiHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 520f),
        label = "audio_btn_scale"
    )

    val containerBg by animateColorAsState(
        targetValue = when {
            isPlayingThisPost -> ChaiSaffron.copy(alpha = if (isLightGlass) 0.18f else 0.24f)
            isLiquid && isLightGlass -> Color(0x100F172A)
            isLiquid -> Color.White.copy(alpha = 0.10f)
            else -> Color.Transparent
        },
        animationSpec = tween(220),
        label = "audio_btn_bg"
    )

    val containerBorder by animateColorAsState(
        targetValue = when {
            isPlayingThisPost -> ChaiSaffron.copy(alpha = 0.65f)
            isLiquid && isLightGlass -> Color(0x280F172A)
            isLiquid -> Color.White.copy(alpha = 0.22f)
            else -> Color.Transparent
        },
        animationSpec = tween(220),
        label = "audio_btn_border"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(pressScale)
            .then(
                if (isLiquid || isPlayingThisPost) {
                    Modifier
                        .clip(CircleShape)
                        .background(containerBg, CircleShape)
                        .border(0.75.dp, containerBorder, CircleShape)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = {
                haptics.click()
                onClick()
            },
            interactionSource = interactionSource,
            modifier = Modifier.size(size)
        ) {
            AnimatedContent(
                targetState = isPlayingThisPost,
                transitionSpec = {
                    (fadeIn(tween(180)) + scaleIn(initialScale = 0.75f)) togetherWith
                        (fadeOut(tween(150)) + scaleOut(targetScale = 0.75f))
                },
                label = "audio_icon_morph"
            ) { playing ->
                if (playing) {
                    ChaiAudioWaveform(
                        isPlaying = true,
                        barColor = ChaiSaffron,
                        barCount = 3
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Headphones,
                        contentDescription = "Listen to article",
                        tint = ChaiTheme.extended.textSecondary,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
    }
}

/**
 * Animated dynamic equalizer wave representing active news voice narration.
 */
@Composable
fun ChaiAudioWaveform(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = ChaiSaffron,
    barCount: Int = 4
) {
    val transition = rememberInfiniteTransition(label = "audio_waveform")

    val bar1Height by transition.animateFloat(
        initialValue = 4f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2Height by transition.animateFloat(
        initialValue = 13f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val bar3Height by transition.animateFloat(
        initialValue = 5f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )
    val bar4Height by transition.animateFloat(
        initialValue = 11f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar4"
    )

    val heights = listOf(bar1Height, bar2Height, bar3Height, bar4Height)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(barCount.coerceAtMost(4)) { index ->
            val height = if (isPlaying) heights[index].dp else 4.dp
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(height)
                    .background(barColor, RoundedCornerShape(1.5.dp))
            )
        }
    }
}
