package com.chaipanchayat.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.data.model.WPPost
import com.chaipanchayat.app.engine.liquidglass.liquidGlassRim
import com.chaipanchayat.app.ui.theme.ChaiCrimson
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import com.chaipanchayat.app.utils.rememberChaiHaptics
import com.kyant.backdrop.Capsule
import kotlinx.coroutines.delay

/**
 * Breaking News Live Ticker Strip
 * Cycles through top headlines with vertical slide transition.
 * In Liquid Glass mode, features a 3D convex Crimson-Saffron Glass Flash News capsule with specular shader rim.
 */
@Composable
fun BreakingNewsTicker(
    posts: List<WPPost>,
    onPostClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    translatedTitles: Map<Long, String> = emptyMap()
) {
    if (posts.isEmpty()) return

    val haptics = rememberChaiHaptics()
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(posts.size) {
        if (posts.size > 1) {
            while (true) {
                delay(4200)
                currentIndex = (currentIndex + 1) % posts.size
            }
        }
    }

    val safeIndex = currentIndex.coerceIn(0, posts.lastIndex)
    val currentPost = posts[safeIndex]

    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(750),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val isLiquid = ChaiTheme.extended.isLiquidGlass
    val capsuleShape = remember { Capsule() }

    Surface(
        color = if (isLiquid) Color.Transparent else ChaiTheme.extended.surfaceSecondary,
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                haptics.click()
                onPostClick(currentPost.id)
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Flash News Badge
            if (isLiquid) {
                Box(
                    modifier = Modifier
                        .clip(capsuleShape)
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    ChaiCrimson.copy(alpha = 0.88f),
                                    ChaiSaffron.copy(alpha = 0.85f)
                                )
                            ),
                            shape = capsuleShape
                        )
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.34f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.18f)
                                )
                            ),
                            shape = capsuleShape
                        )
                        .liquidGlassRim(capsuleShape)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .alpha(dotAlpha)
                                .background(Color.White, CircleShape)
                        )
                        Text(
                            text = "Flash News",
                            color = Color.White,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.5.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = ChaiCrimson
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .alpha(dotAlpha)
                                .background(Color.White, CircleShape)
                        )
                        Text(
                            text = "Flash News",
                            color = Color.White,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Animated Headline
            AnimatedContent(
                targetState = currentPost,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn(tween(260))) togetherWith
                            (slideOutVertically { height -> -height } + fadeOut(tween(220)))
                },
                modifier = Modifier.weight(1f),
                label = "ticker_headline"
            ) { post ->
                Text(
                    text = translatedTitles[post.id] ?: post.cleanTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = NotoSerifFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = ChaiTheme.extended.textSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
