package com.chaipanchayat.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.FlashOn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaipanchayat.app.data.model.WPPost
import com.chaipanchayat.app.ui.theme.ChaiBrandGradient
import com.chaipanchayat.app.ui.theme.ChaiCrimson
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BreakingNewsTicker(
    posts: List<WPPost>,
    onPostClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (posts.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }

    // Auto-cycle ticker every 6.5 seconds (calm reading pace, avoids sensory overload)
    LaunchedEffect(posts) {
        while (true) {
            delay(6500)
            if (posts.isNotEmpty()) {
                currentIndex = (currentIndex + 1) % posts.size
            }
        }
    }

    val currentPost = posts.getOrNull(currentIndex) ?: return

    // Gentle, non-distracting breath animation for LIVE badge (no harsh scaling)
    val infiniteTransition = rememberInfiniteTransition(label = "beacon_breathe")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        color = ChaiTheme.extended.surfaceSecondary.copy(alpha = 0.65f),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPostClick(currentPost.id) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Calm Live Indicator Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(ChaiCrimson)
                    .padding(horizontal = 5.5.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(Color.White.copy(alpha = pulseAlpha), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(3.5.dp))
                    Text(
                        text = "LIVE",
                        color = Color.White,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Smooth crossfade headline animation (smooth, no jarring slides)
            AnimatedContent(
                targetState = currentPost,
                transitionSpec = {
                    (slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(420, easing = FastOutSlowInEasing)
                    ) + fadeIn(tween(380))) togetherWith
                    (slideOutVertically(
                        targetOffsetY = { -it / 2 },
                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                    ) + fadeOut(tween(280)))
                },
                modifier = Modifier.weight(1f),
                label = "ticker_headline"
            ) { targetPost ->
                Text(
                    text = targetPost.cleanTitle,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = ChaiTheme.extended.muted.copy(alpha = 0.7f),
                modifier = Modifier.size(10.dp)
            )
        }
    }
}
