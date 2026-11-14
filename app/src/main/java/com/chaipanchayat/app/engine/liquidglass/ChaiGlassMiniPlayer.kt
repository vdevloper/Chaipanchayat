package com.chaipanchayat.app.engine.liquidglass

import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.utils.ChaiAudioReader
import com.chaipanchayat.app.utils.rememberChaiHaptics
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.Capsule
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.isRenderEffectSupported
import com.kyant.backdrop.shadow.Shadow

/**
 * Floating Liquid Glass Mini-Player for Chai Panchayat
 * Supports both Dark Glass and Light Glass submodes with crisp 22dp backdrop blur,
 * AGSL chromatic lens refraction, and an inset capsule progress indicator.
 */
@Composable
fun ChaiGlassMiniPlayer(
    article: ChaiAudioReader.ActiveArticleAudio,
    playState: ChaiAudioReader.PlayState,
    progress: Float,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val haptics = rememberChaiHaptics()
    val isLightGlass = ChaiTheme.extended.isLightGlass
    val shape = remember { Capsule() }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 1.03f else 1.0f,
        animationSpec = spring(stiffness = 380f, dampingRatio = 0.60f),
        label = "mini_player_press_scale"
    )

    val isPlaying = playState == ChaiAudioReader.PlayState.PLAYING

    // Animated waveform bars when playing
    val waveTransition = rememberInfiniteTransition(label = "audio_bars")
    val wave1 by waveTransition.animateFloat(
        initialValue = 4f, targetValue = 14f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wave_1"
    )
    val wave2 by waveTransition.animateFloat(
        initialValue = 12f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(380, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wave_2"
    )
    val wave3 by waveTransition.animateFloat(
        initialValue = 6f, targetValue = 16f,
        animationSpec = infiniteRepeatable(tween(520, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wave_3"
    )

    val primaryTextColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = ChaiTheme.extended.textSecondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
            .scale(pressScale)
            .then(
                if (isRenderEffectSupported()) {
                    Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { shape },
                        effects = {
                            blur(22.dp.toPx())
                            colorControls(
                                brightness = 0.0f,
                                contrast = 1.04f,
                                saturation = 1.12f
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                lens(
                                    refractionHeight = 14.dp.toPx(),
                                    refractionAmount = 26.dp.toPx(),
                                    depthEffect = true,
                                    chromaticAberration = true
                                )
                            }
                        },
                        highlight = {
                            glassHighlight(
                                isLightGlass = isLightGlass,
                                width = if (isLightGlass) 1.05.dp else 0.82.dp,
                                alpha = if (isLightGlass) 0.28f else 0.68f
                            )
                        },
                        shadow = {
                            Shadow(
                                radius = 16.dp,
                                offset = DpOffset(0.dp, 5.dp),
                                color = Color.Black.copy(alpha = if (isLightGlass) 0.10f else 0.42f)
                            )
                        },
                        onDrawSurface = {
                            if (size.width > 0f && size.height > 0f) {
                                if (isLightGlass) {
                                    // Sheer crystal-clear transparent glass with top white specular sheen
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
                        }
                    )
                } else {
                    Modifier
                        .background(if (isLightGlass) Color(0x160F172A) else Color(0xD8141416), shape)
                        .border(
                            width = 0.9.dp,
                            brush = Brush.verticalGradient(
                                if (isLightGlass) {
                                    listOf(
                                        Color(0x360F172A),
                                        Color(0x180F172A)
                                    )
                                } else {
                                    listOf(
                                        Color.White.copy(alpha = 0.40f),
                                        Color.White.copy(alpha = 0.14f)
                                    )
                                }
                            ),
                            shape = shape
                        )
                }
            )
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptics.click()
                    onClick()
                }
            )
            .testTag("chai_glass_mini_player"),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cover Art / Thumbnail
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isLightGlass) Color(0x140F172A) else Color(0x33FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!article.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = article.imageUrl,
                            contentDescription = article.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = "☕",
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Title & Subtitle Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = article.title,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        letterSpacing = 0.1.sp,
                        color = primaryTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(1.5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isPlaying) {
                            Row(
                                modifier = Modifier.padding(end = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.width(2.2.dp).height(wave1.dp).background(ChaiSaffron, CircleShape))
                                Box(modifier = Modifier.width(2.2.dp).height(wave2.dp).background(ChaiSaffron, CircleShape))
                                Box(modifier = Modifier.width(2.2.dp).height(wave3.dp).background(ChaiSaffron, CircleShape))
                            }
                        }
                        Text(
                            text = if (isPlaying) "ऑडियो सक्रिय • Tap to Open" else article.subtitle,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.5.sp,
                            color = secondaryTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Transport Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Play / Pause Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isLightGlass) Color(0x140F172A) else Color.White.copy(alpha = 0.14f))
                            .border(
                                width = 0.7.dp,
                                color = if (isLightGlass) Color(0x280F172A) else Color.White.copy(alpha = 0.24f),
                                shape = CircleShape
                            )
                            .clickable {
                                haptics.click()
                                onPlayPause()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = primaryTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Next / Skip sentence
                    IconButton(
                        onClick = {
                            haptics.click()
                            onNext()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FastForward,
                            contentDescription = "Skip Forward",
                            tint = secondaryTextColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Close / Dismiss
                    IconButton(
                        onClick = {
                            haptics.click()
                            onDismiss()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close player",
                            tint = secondaryTextColor.copy(alpha = 0.75f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Inset Bottom Audio Progress Bar (padded horizontally by 24.dp so it stays cleanly inside the capsule curve)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(2.dp)
                    .clip(CircleShape)
                    .background(if (isLightGlass) Color(0x140F172A) else Color.White.copy(alpha = 0.10f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                        .height(2.dp)
                        .background(ChaiSaffron, CircleShape)
                )
            }
        }
    }
}
