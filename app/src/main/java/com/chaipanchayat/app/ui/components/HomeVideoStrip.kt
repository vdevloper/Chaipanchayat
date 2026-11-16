package com.chaipanchayat.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chaipanchayat.app.data.model.VideoItem
import com.chaipanchayat.app.engine.liquidglass.liquidGlassCard
import com.chaipanchayat.app.ui.theme.ChaiCrimson
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import com.chaipanchayat.app.utils.DateUtils
import com.chaipanchayat.app.utils.rememberChaiHaptics

/**
 * Mid-feed Horizontal Video Bulletin Strip for the Home Screen.
 * Breaks up the vertical reading flow with directly playable video story cards.
 */
@Composable
fun HomeVideoStrip(
    videos: List<VideoItem>,
    onVideoClick: (Long) -> Unit,
    isHindiUi: Boolean,
    modifier: Modifier = Modifier
) {
    if (videos.isEmpty()) return

    val infiniteTransition = rememberInfiniteTransition(label = "video_strip_pulse")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "play_ring_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(18.dp)
                        .background(ChaiCrimson, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.Videocam,
                    contentDescription = null,
                    tint = ChaiCrimson,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isHindiUi) "वीडियो बुलेटिन" else "VIDEO BULLETINS",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.5.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Surface(
                shape = CircleShape,
                color = ChaiCrimson.copy(alpha = 0.14f),
                border = BorderStroke(0.8.dp, ChaiCrimson.copy(alpha = 0.42f))
            ) {
                Text(
                    text = if (isHindiUi) "${videos.size} वीडियो" else "${videos.size} REELS",
                    color = ChaiCrimson,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(videos.take(8), key = { it.id }) { video ->
                HomeVideoCard(
                    video = video,
                    ringScale = ringScale,
                    onClick = {
                        val targetId = if (video.articleId > 0L) video.articleId else video.id
                        onVideoClick(targetId)
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeVideoCard(
    video: VideoItem,
    ringScale: Float,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val haptics = rememberChaiHaptics()
    val isLiquid = ChaiTheme.extended.isLiquidGlass
    val isLightGlass = ChaiTheme.extended.isLightGlass
    val cardShape = if (isLiquid) RoundedCornerShape(18.dp) else RoundedCornerShape(13.dp)
    val shimmerBrush = rememberShimmerBrush()
    var isImageLoaded by remember(video.id) { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 460f),
        label = "video_card_scale"
    )

    val thumbUrl = video.thumbnail.takeIf { it.isNotBlank() }
        ?: video.youtubeId.takeIf { it.isNotBlank() }?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }
        ?: ""

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isLiquid) Color.Transparent else ChaiTheme.extended.surfaceSecondary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLiquid) 0.dp else 1.dp),
        modifier = Modifier
            .width(214.dp)
            .scale(pressScale)
            .then(
                if (isLiquid) {
                    Modifier.liquidGlassCard(
                        shape = cardShape,
                        accentColor = ChaiCrimson,
                        blurRadiusDp = 22.dp
                    )
                } else {
                    Modifier.border(
                        0.8.dp,
                        ChaiTheme.extended.border.copy(alpha = 0.7f),
                        cardShape
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptics.click()
                    onClick()
                }
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9.2f)
                    .then(
                        if (isLiquid) {
                            Modifier
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    0.7.dp,
                                    if (isLightGlass) Color(0x280F172A) else Color.White.copy(alpha = 0.25f),
                                    RoundedCornerShape(12.dp)
                                )
                        } else {
                            Modifier.clip(RoundedCornerShape(topStart = 13.dp, topEnd = 13.dp))
                        }
                    )
                    .background(if (isImageLoaded) ChaiTheme.extended.skeleton else Color.Transparent)
                    .then(if (!isImageLoaded) Modifier.background(shimmerBrush) else Modifier)
            ) {
                if (thumbUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(thumbUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        onSuccess = { isImageLoaded = true },
                        onError = { isImageLoaded = true },
                        modifier = Modifier.matchParentSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))
                            )
                        )
                )

                // Pulsing halo + Play Button
                Box(
                    modifier = Modifier.align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .scale(ringScale)
                            .background(ChaiCrimson.copy(alpha = 0.28f), CircleShape)
                    )
                    Surface(
                        shape = CircleShape,
                        color = ChaiCrimson.copy(alpha = 0.94f),
                        border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.85f)),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Watch video",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (!video.category.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.62f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = video.category.uppercase(),
                            color = Color.White,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.6.sp,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = video.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = NotoSerifFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = DateUtils.timeAgo(video.date),
                    color = ChaiTheme.extended.textSecondary,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                )
            }
        }
    }
}
