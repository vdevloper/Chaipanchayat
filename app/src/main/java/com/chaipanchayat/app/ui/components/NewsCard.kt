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
import com.chaipanchayat.app.ui.theme.ChaiCrimson
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import com.chaipanchayat.app.utils.DateUtils
import com.chaipanchayat.app.utils.DeepLinkManager
import com.chaipanchayat.app.utils.rememberChaiHaptics
import kotlinx.coroutines.launch

@Composable
fun NewsCard(
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

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1.0f,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = 420f),
        label = "card_scale"
    )

    val bookmarkScale by animateFloatAsState(
        targetValue = if (isBookmarked) 1.22f else 1.0f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 500f),
        label = "bookmark_bounce"
    )
    val bookmarkTint by animateColorAsState(
        targetValue = if (isBookmarked) ChaiSaffron else ChaiTheme.extended.muted,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "bookmark_tint"
    )

    val isLiquid = ChaiTheme.extended.isLiquidGlass
    val cardShape = if (isLiquid) RoundedCornerShape(18.dp) else RoundedCornerShape(12.dp)
    val thumbShape = if (isLiquid) RoundedCornerShape(12.dp) else RoundedCornerShape(8.dp)

    val imageUrl = post.featuredImageUrl?.takeIf { it.isNotBlank() }
        ?: post.youtubeId?.takeIf { it.isNotBlank() }?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isLiquid) Color.Transparent else ChaiTheme.extended.surfaceSecondary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLiquid) 0.dp else 1.dp),
        modifier = modifier
            .testTag("news-card-${post.id}")
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .scale(scale)
            .then(
                if (isLiquid) {
                    Modifier.liquidGlassCard(
                        shape = cardShape,
                        blurRadiusDp = 22.dp
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Thumbnail
            if (!imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .size(width = 108.dp, height = 84.dp)
                        .clip(thumbShape)
                        .then(
                            if (isLiquid) {
                                val isLightGlass = ChaiTheme.extended.isLightGlass
                                Modifier.border(
                                    0.8.dp,
                                    if (isLightGlass) Color(0x280F172A) else Color.White.copy(alpha = 0.26f),
                                    thumbShape
                                )
                            } else Modifier
                        )
                        .background(ChaiTheme.extended.skeleton)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = post.cleanTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )

                    if (hasVideo) {
                        Surface(
                            shape = CircleShape,
                            color = ChaiCrimson.copy(alpha = 0.90f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))
            }

            // Right Text Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (!post.primaryCategory.isNullOrBlank()) {
                    if (isLiquid) {
                        val isLightGlass = ChaiTheme.extended.isLightGlass
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ChaiSaffron.copy(alpha = if (isLightGlass) 0.16f else 0.22f),
                            border = BorderStroke(0.75.dp, ChaiSaffron.copy(alpha = if (isLightGlass) 0.58f else 0.50f))
                        ) {
                            Text(
                                text = post.primaryCategory.uppercase(),
                                color = if (isLightGlass) Color(0xFFC2410C) else Color(0xFFFFB370),
                                fontFamily = InterFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.5.sp,
                                letterSpacing = 0.7.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = post.primaryCategory.uppercase(),
                            color = ChaiTheme.extended.brandText,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            letterSpacing = 0.7.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = displayTitle ?: post.cleanTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = NotoSerifFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.5.sp,
                    lineHeight = 22.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateUtils.timeAgo(post.date),
                        color = ChaiTheme.extended.textSecondary,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .then(
                                    if (isLiquid) {
                                        val isLightGlass = ChaiTheme.extended.isLightGlass
                                        Modifier
                                            .clip(CircleShape)
                                            .background(
                                                if (isLightGlass) Color(0x100F172A) else Color.White.copy(alpha = 0.10f),
                                                CircleShape
                                            )
                                            .border(
                                                0.7.dp,
                                                if (isLightGlass) Color(0x280F172A) else Color.White.copy(alpha = 0.22f),
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
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Save",
                                    tint = if (isLiquid && !isBookmarked) ChaiTheme.extended.textSecondary else bookmarkTint,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .scale(bookmarkScale)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .then(
                                    if (isLiquid) {
                                        val isLightGlass = ChaiTheme.extended.isLightGlass
                                        Modifier
                                            .clip(CircleShape)
                                            .background(
                                                if (isLightGlass) Color(0x100F172A) else Color.White.copy(alpha = 0.10f),
                                                CircleShape
                                            )
                                            .border(
                                                0.7.dp,
                                                if (isLightGlass) Color(0x280F172A) else Color.White.copy(alpha = 0.22f),
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
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = "Share",
                                    tint = ChaiTheme.extended.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
