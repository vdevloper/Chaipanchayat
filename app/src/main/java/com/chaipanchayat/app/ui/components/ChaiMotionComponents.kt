package com.chaipanchayat.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.utils.rememberChaiHaptics

/**
 * Premium spring-animated Bookmark Toggle Button.
 * Features an energetic tactile pop and color morph on save, accompanied by haptic feedback.
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
    testTag: String = "bookmark-button"
) {
    val haptics = rememberChaiHaptics()
    val popScale = remember { Animatable(1.0f) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label = "press_scale"
    )

    LaunchedEffect(isBookmarked) {
        if (isBookmarked) {
            popScale.snapTo(0.85f)
            popScale.animateTo(
                targetValue = 1.32f,
                animationSpec = spring(dampingRatio = 0.45f, stiffness = 700f)
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
    }

    val animatedColor by animateColorAsState(
        targetValue = if (isBookmarked) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "bookmark_color"
    )

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
        modifier = modifier
            .testTag(testTag)
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
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2Height by transition.animateFloat(
        initialValue = 14f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val bar3Height by transition.animateFloat(
        initialValue = 6f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )
    val bar4Height by transition.animateFloat(
        initialValue = 12f,
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
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(barCount.coerceAtMost(4)) { index ->
            val height = if (isPlaying) heights[index].dp else 4.dp
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height)
                    .background(barColor, RoundedCornerShape(1.5.dp))
            )
        }
    }
}
