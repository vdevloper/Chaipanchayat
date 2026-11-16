package com.chaipanchayat.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.chaipanchayat.app.ui.theme.LocalChaiStrings
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import com.chaipanchayat.app.utils.ChaiAudioReader
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
    displayTitle: String? = null,
    parallaxOffsetPx: Float = 0f,
    horizontalParallaxPx: Float = 0f,
    applyTestTag: Boolean = true
) {
    val context = LocalContext.current
    val haptics = rememberChaiHaptics()
    val strings = LocalChaiStrings.current
    val coroutineScope = rememberCoroutineScope()
    val bookmarkRepo = remember { BookmarkRepository.getInstance(context) }
    val isBookmarked by bookmarkRepo.isBookmarked(post.id).collectAsState(initial = false)
    val hasVideo = !post.youtubeId.isNullOrBlank() || !post.videoUrl.isNullOrBlank()
    val readingTimeMinutes = remember(post.rawContent) {
        (post.rawContent.split("\\s+".toRegex()).size / 180).coerceAtLeast(1)
    }

    val audioReader = remember { ChaiAudioReader.getInstance(context) }
    val activeAudio by audioReader.activeArticle
    val audioPlayState by audioReader.playState
    val isPlayingThisPost = activeAudio?.postId == post.id &&
        audioPlayState == ChaiAudioReader.PlayState.PLAYING

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = 420f),
        label = "hero_scale"
    )

    val isLiquid = ChaiTheme.extended.isLiquidGlass
    val isLightGlass = ChaiTheme.extended.isLightGlass
    val cardShape = if (isLiquid) RoundedCornerShape(22.dp) else RoundedCornerShape(14.dp)
    val capsuleShape = remember { Capsule() }
    val shimmerBrush = rememberShimmerBrush()
    var isImageLoaded by remember(post.id) { mutableStateOf(false) }

    val imageUrl = post.featuredImageUrl?.takeIf { it.isNotBlank() }
        ?: post.youtubeId?.takeIf { it.isNotBlank() }?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isLiquid) Color.Transparent else ChaiTheme.extended.surfaceSecondary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLiquid) 0.dp else 1.dp),
        modifier = modifier
            .then(if (applyTestTag) Modifier.testTag("hero-card") else Modifier)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
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
            // 1. Sleek 16:8.8 Featured Media Area with Shimmer & Parallax
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 8.8f)
                    .chaiSharedElement("article_image_${post.id}")
                    .then(
                        if (isLiquid) {
                            Modifier
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    0.8.dp,
                                    if (isLightGlass) Color(0x280F172A) else Color.White.copy(alpha = 0.28f),
                                    RoundedCornerShape(16.dp)
                                )
                        } else {
                            Modifier.clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                        }
                    )
                    .background(if (isImageLoaded) ChaiTheme.extended.skeleton else Color.Transparent)
                    .then(if (!isImageLoaded) Modifier.background(shimmerBrush) else Modifier)
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = post.cleanTitle,
                        contentScale = ContentScale.Crop,
                        onSuccess = { isImageLoaded = true },
                        onError = { isImageLoaded = true },
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer {
                                scaleX = 1.05f
                                scaleY = 1.05f
                                translationY = parallaxOffsetPx * 0.16f
                                translationX = horizontalParallaxPx * 0.24f
                            }
                    )
                }

                // Soft bottom transition gradient so bright photos meet card body smoothly
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = if (isLiquid) {
                                    listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.36f)
                                    )
                                } else {
                                    listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        ChaiTheme.extended.surfaceSecondary.copy(alpha = 0.38f)
                                    )
                                }
                            )
                        )
                )

                // Hairline seam between image & body on standard cards
                if (!isLiquid) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(0.8.dp)
                            .background(ChaiTheme.extended.border.copy(alpha = 0.45f))
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
                            .size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Video",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }

            // 2. Editorial Headline & Unified Footer Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = if (isLiquid) 6.dp else 10.dp,
                        bottom = 12.dp
                    )
            ) {
                // Clean Category Tag + Read Time Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val catText = (post.primaryCategory ?: "प्रमुख समाचार").uppercase()
                    if (isLiquid) {
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
                                .padding(horizontal = 10.dp, vertical = 3.5.dp)
                        ) {
                            Text(
                                text = catText,
                                color = if (isLightGlass) Color(0xFFC2410C) else Color.White,
                                fontFamily = InterFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.5.sp,
                                letterSpacing = 0.7.sp
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ChaiSaffron.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = catText,
                                color = ChaiTheme.extended.brandText,
                                fontFamily = InterFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.5.sp,
                                letterSpacing = 0.7.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = "$readingTimeMinutes मिनट पठन",
                        color = ChaiTheme.extended.muted,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(7.dp))

                // Lead Headline with SharedBounds support
                Text(
                    text = displayTitle ?: post.cleanTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = NotoSerifFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.5.sp,
                    lineHeight = 27.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.chaiSharedBounds("article_title_${post.id}")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Grouped Footer: Author + Time on Left, Audio + Bookmark + Share on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (!post.authorName.isNullOrBlank()) post.authorName else strings.appName,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = " • ",
                            color = ChaiTheme.extended.muted,
                            fontSize = 12.sp
                        )
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = ChaiTheme.extended.textSecondary,
                            modifier = Modifier.size(12.5.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = DateUtils.timeAgo(post.date),
                            color = ChaiTheme.extended.textSecondary,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Unified Action Cluster: Listen + Bookmark + Share
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ChaiAudioActionButton(
                            isPlayingThisPost = isPlayingThisPost,
                            onClick = {
                                if (isPlayingThisPost) {
                                    audioReader.pause()
                                } else if (activeAudio?.postId == post.id && audioPlayState == ChaiAudioReader.PlayState.PAUSED) {
                                    audioReader.resume()
                                } else {
                                    audioReader.playArticle(
                                        postId = post.id,
                                        title = displayTitle ?: post.cleanTitle,
                                        contentHtml = post.rawContent,
                                        imageUrl = post.featuredImageUrl,
                                        category = post.primaryCategory ?: "ताज़ा ख़बरें"
                                    )
                                }
                            },
                            size = 32.dp,
                            iconSize = 17.dp,
                            isLiquid = isLiquid,
                            isLightGlass = isLightGlass
                        )

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .then(
                                    if (isLiquid) {
                                        Modifier
                                            .clip(CircleShape)
                                            .background(
                                                if (isLightGlass) Color(0x100F172A) else Color.White.copy(alpha = 0.12f),
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
                            ChaiBookmarkButton(
                                isBookmarked = isBookmarked,
                                onToggle = { coroutineScope.launch { bookmarkRepo.toggleBookmark(post) } },
                                size = 32.dp,
                                iconSize = 18.dp,
                                inactiveColor = if (isLiquid) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f) else ChaiTheme.extended.textSecondary,
                                testTag = null
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .then(
                                    if (isLiquid) {
                                        Modifier
                                            .clip(CircleShape)
                                            .background(
                                                if (isLightGlass) Color(0x100F172A) else Color.White.copy(alpha = 0.12f),
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
}
