package com.chaipanchayat.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chaipanchayat.app.data.model.WPPost
import com.chaipanchayat.app.data.repository.BookmarkRepository
import com.chaipanchayat.app.engine.liquidglass.liquidGlassCard
import com.chaipanchayat.app.engine.liquidglass.liquidGlassRim
import com.chaipanchayat.app.ui.theme.ChaiCrimson
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import com.chaipanchayat.app.utils.DateUtils
import com.chaipanchayat.app.utils.DeepLinkManager
import com.chaipanchayat.app.utils.rememberChaiHaptics
import com.kyant.backdrop.Capsule
import kotlinx.coroutines.launch

@Composable
fun HeroCard(
    post: WPPost,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    displayTitle: String? = null
) {
    val context = LocalContext.current
    val haptics = rememberChaiHaptics()
    val coroutineScope = rememberCoroutineScope()
    val bookmarkRepo = remember { BookmarkRepository.getInstance(context) }
    val isBookmarked by bookmarkRepo.isBookmarked(post.id).collectAsState(initial = false)
    val hasVideo = !post.youtubeId.isNullOrBlank() || !post.videoUrl.isNullOrBlank()
    val readingTimeMinutes = remember(post.rawContent) {
        (post.rawContent.split("\\s+".toRegex()).size / 180).coerceAtLeast(1)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = 420f),
        label = "hero_scale"
    )

    val bookmarkScale by animateFloatAsState(
        targetValue = if (isBookmarked) 1.22f else 1.0f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 500f),
        label = "hero_bookmark_scale"
    )
    val bookmarkTint by animateColorAsState(
        targetValue = if (isBookmarked) ChaiSaffron else ChaiTheme.extended.textSecondary,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "hero_bookmark_tint"
    )

    val isLiquid = ChaiTheme.extended.isLiquidGlass
    val cardShape = if (isLiquid) RoundedCornerShape(22.dp) else RoundedCornerShape(14.dp)
    val capsuleShape = remember { Capsule() }

    val imageUrl = post.featuredImageUrl?.takeIf { it.isNotBlank() }
        ?: post.youtubeId?.takeIf { it.isNotBlank() }?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isLiquid) Color.Transparent else ChaiTheme.extended.surfaceSecondary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLiquid) 0.dp else 1.dp),
        modifier = modifier
            .testTag("hero-card")
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .scale(scale)
            .then(
                if (isLiquid) {
                    Modifier.liquidGlassCard(
                        shape = cardShape,
                        accentColor = ChaiSaffron,
                        blurRadiusDp = 24.dp
                    )
                } else {
                    Modifier.border(
                        width = 0.8.dp,
                        color = ChaiTheme.extended.border.copy(alpha = 0.7f),
                        shape = cardShape
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
            // 1. Clean 16:9 Featured Media Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9.2f)
                    .then(
                        if (isLiquid) {
                            val isLightGlass = ChaiTheme.extended.isLightGlass
                            Modifier
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    0.8.dp,
                                    if (isLightGlass) Color(0x280F172A) else Color.White.copy(alpha = 0.28f),
                                    RoundedCornerShape(16.dp)
                                )
                        } else {
                            Modifier
                        }
                    )
                    .background(ChaiTheme.extended.skeleton)
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = post.cleanTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }

                // Subtle bottom vignette on Liquid Glass lead photo
                if (isLiquid) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.35f)
                                    )
                                )
                            )
                    )
                }

                // Video Play Badge if video story
                if (hasVideo) {
                    Surface(
                        shape = CircleShape,
                        color = ChaiCrimson.copy(alpha = 0.92f),
                        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.85f)),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Video",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // 2. Editorial Headline & Meta Section Below Image
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = if (isLiquid) 8.dp else 14.dp,
                        bottom = 14.dp
                    )
            ) {
                // Category & Bookmark Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLiquid) {
                        val isLightGlass = ChaiTheme.extended.isLightGlass
                        Box(
                            modifier = Modifier
                                .clip(capsuleShape)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        if (isLightGlass) {
                                            listOf(
                                                ChaiSaffron.copy(alpha = 0.18f),
                                                ChaiCrimson.copy(alpha = 0.14f)
                                            )
                                        } else {
                                            listOf(
                                                ChaiSaffron.copy(alpha = 0.36f),
                                                ChaiCrimson.copy(alpha = 0.24f)
                                            )
                                        }
                                    ),
                                    shape = capsuleShape
                                )
                                .border(0.8.dp, ChaiSaffron.copy(alpha = 0.58f), capsuleShape)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = (post.primaryCategory ?: "प्रमुख समाचार").uppercase(),
                                color = if (isLightGlass) Color(0xFFC2410C) else Color.White,
                                fontFamily = InterFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp
                            )
                        }
                    } else {
                        Text(
                            text = (post.primaryCategory ?: "प्रमुख समाचार").uppercase(),
                            color = ChaiTheme.extended.brandText,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.5.sp,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .then(
                                if (isLiquid) {
                                    val isLightGlass = ChaiTheme.extended.isLightGlass
                                    Modifier
                                        .clip(CircleShape)
                                        .background(
                                            if (isLightGlass) Color(0x120F172A) else Color.White.copy(alpha = 0.12f),
                                            CircleShape
                                        )
                                        .border(
                                            0.8.dp,
                                            if (isLightGlass) Color(0x280F172A) else Color.White.copy(alpha = 0.26f),
                                            CircleShape
                                        )
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                haptics.success()
                                coroutineScope.launch { bookmarkRepo.toggleBookmark(post) }
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isLiquid && !isBookmarked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f) else bookmarkTint,
                                modifier = Modifier
                                    .size(19.dp)
                                    .scale(bookmarkScale)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Lead Headline
                Text(
                    text = displayTitle ?: post.cleanTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = NotoSerifFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    lineHeight = 28.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Author, Relative Time, Read Time & Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (!post.authorName.isNullOrBlank()) post.authorName else "चाय पंचायत ब्यूरो",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.90f),
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "  •  ",
                            color = ChaiTheme.extended.muted,
                            fontSize = 12.sp
                        )
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = ChaiTheme.extended.textSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = DateUtils.timeAgo(post.date),
                            color = ChaiTheme.extended.textSecondary,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "  •  $readingTimeMinutes मिनट पठन",
                            color = ChaiTheme.extended.muted,
                            fontFamily = InterFamily,
                            fontSize = 11.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .then(
                                if (isLiquid) {
                                    val isLightGlass = ChaiTheme.extended.isLightGlass
                                    Modifier
                                        .clip(CircleShape)
                                        .background(
                                            if (isLightGlass) Color(0x120F172A) else Color.White.copy(alpha = 0.12f),
                                            CircleShape
                                        )
                                        .border(
                                            0.8.dp,
                                            if (isLightGlass) Color(0x280F172A) else Color.White.copy(alpha = 0.25f),
                                            CircleShape
                                        )
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                haptics.click()
                                coroutineScope.launch {
                                    DeepLinkManager.shareArticle(
                                        context = context,
                                        postId = post.id,
                                        cleanTitle = displayTitle ?: post.cleanTitle,
                                        imageUrl = post.featuredImageUrl,
                                        permalink = post.link.takeIf { it.isNotBlank() }
                                    )
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = ChaiTheme.extended.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
