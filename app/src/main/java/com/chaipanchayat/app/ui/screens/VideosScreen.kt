package com.chaipanchayat.app.ui.screens

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.chaipanchayat.app.data.model.VideoItem
import com.chaipanchayat.app.data.repository.NewsRepository
import com.chaipanchayat.app.ui.components.ChaiVideoPlayer
import com.chaipanchayat.app.ui.theme.ChaiCrimson
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.utils.DateUtils
import com.chaipanchayat.app.utils.DeepLinkManager
import com.chaipanchayat.app.utils.rememberChaiHaptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Filter tabs for Video feed:
 * 1. ALL: Normal videos list
 * 2. TAJA_CHAI: Latest videos from today and the last 5 days ordered serially by post timings
 */
enum class VideoFeedTab(val label: String, val testTag: String) {
    ALL("वीडियो", "tab_all_videos"),
    TAJA_CHAI("ताज़ा चाय", "tab_taja_chai")
}

/**
 * Full-screen vertical video feed.
 * Features:
 * - Two top options: "वीडियो" (normal) and "ताज़ा चाय" (today & last 5 days serial-wise)
 * - Pure, uncluttered full-screen video layout
 * - Vertical swipe (VerticalPager) to seamlessly transition between videos
 * - Plays full videos completely to the end (no short loops, repeatMode = OFF)
 * - Sleek playback progress indicator with time elapsed and total duration
 * - Native Media3 ExoPlayer for direct WordPress uploaded MP4 videos
 * - Tap anywhere to toggle play/pause with animated indicator
 * - Replay action when video finishes
 * - Robust deduplication preventing duplicate items on scroll up
 * - Global mute/unmute sound toggle
 * - Clean bottom overlay with headline, category, and direct share action
 */
@Composable
fun VideosScreen(
    onNavigateToArticle: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberChaiHaptics()
    val repository = remember { NewsRepository.getInstance() }
    val initialCached = remember { repository.getCachedVideos().orEmpty() }

    var videos by remember { mutableStateOf(initialCached) }
    var isLoading by remember { mutableStateOf(initialCached.isEmpty()) }
    var isMuted by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(VideoFeedTab.ALL) }

    val coroutineScope = rememberCoroutineScope()

    fun loadVideos(forceNetwork: Boolean = false) {
        coroutineScope.launch {
            if (videos.isEmpty()) isLoading = true
            try {
                val fetched = repository.getVideoPosts(forceRefresh = forceNetwork)
                if (fetched.isNotEmpty()) {
                    videos = fetched
                }
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadVideos(forceNetwork = initialCached.isEmpty())
    }

    // Filter displayed videos based on selected tab
    val displayedVideos = remember(videos, selectedTab) {
        when (selectedTab) {
            VideoFeedTab.ALL -> videos
            VideoFeedTab.TAJA_CHAI -> {
                // Today and last 5 days only, strictly ordered serially by post timing (newest first)
                videos.filter { DateUtils.isTodayOrPastDays(it.date, pastDays = 5) }
                    .sortedByDescending { DateUtils.parseDate(it.date)?.time ?: 0L }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            isLoading && videos.isEmpty() -> {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = ChaiSaffron,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "चाय पंचायत वीडियो लोड हो रहे हैं...",
                            color = Color.White.copy(alpha = 0.8f),
                            fontFamily = InterFamily,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            videos.isEmpty() -> {
                // Empty state with retry
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "कोई वीडियो उपलब्ध नहीं है",
                            color = Color.White,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "कृपया इंटरनेट कनेक्शन जांचें और पुनः प्रयास करें।",
                            color = Color.White.copy(alpha = 0.7f),
                            fontFamily = InterFamily,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { loadVideos(forceNetwork = true) },
                            colors = ButtonDefaults.buttonColors(containerColor = ChaiCrimson),
                            modifier = Modifier.testTag("btn_video_refresh")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("पुनः लोड करें")
                        }
                    }
                }
            }

            displayedVideos.isEmpty() -> {
                // Empty state specific to Taja Chai tab (no videos in last 5 days)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ChaiSaffron.copy(alpha = 0.15f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "☕",
                                    fontSize = 30.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "ताज़ा चाय - आज और पिछले 5 दिन",
                            color = Color.White,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "पिछले 5 दिनों में कोई नया वीडियो नहीं आया है। आप सभी वीडियो देख सकते हैं।",
                            color = Color.White.copy(alpha = 0.75f),
                            fontFamily = InterFamily,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                haptics.click()
                                selectedTab = VideoFeedTab.ALL
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ChaiCrimson),
                            modifier = Modifier.testTag("btn_switch_all_videos")
                        ) {
                            Text("सभी वीडियो देखें")
                        }
                    }
                }
            }

            else -> {
                val pagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = { displayedVideos.size }
                )

                // When switching tabs, reset to top page
                LaunchedEffect(selectedTab) {
                    pagerState.scrollToPage(0)
                }

                VerticalPager(
                    state = pagerState,
                    key = { index ->
                        val item = displayedVideos.getOrNull(index)
                        item?.videoUrl ?: item?.youtubeUrl ?: item?.id ?: index
                    },
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 2
                ) { page ->
                    val video = displayedVideos[page]
                    val isCurrentPage = pagerState.currentPage == page

                    VideoFeedItem(
                        video = video,
                        isActive = isCurrentPage,
                        isMuted = isMuted,
                        onNavigateToArticle = onNavigateToArticle
                    )
                }
            }
        }

        // Top Navigation Bar: Segmented Tab Options ("वीडियो" | "ताज़ा चाय") & Sound Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Segmented Tabs Pill: Option 1: वीडियो, Option 2: ताज़ा चाय
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
            ) {
                Row(
                    modifier = Modifier.padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VideoFeedTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) ChaiCrimson else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    if (selectedTab != tab) {
                                        haptics.click()
                                        selectedTab = tab
                                    }
                                }
                                .testTag(tab.testTag)
                        ) {
                            Text(
                                text = tab.label,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                fontFamily = InterFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Global Sound Toggle Button
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        haptics.click()
                        isMuted = !isMuted
                    }
                    .testTag("btn_video_sound_toggle")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isMuted) "Unmute" else "Mute",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoFeedItem(
    video: VideoItem,
    isActive: Boolean,
    isMuted: Boolean,
    onNavigateToArticle: (Long) -> Unit
) {
    val context = LocalContext.current
    val haptics = rememberChaiHaptics()
    val scope = rememberCoroutineScope()

    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(false) }
    var isVideoEnded by remember { mutableStateOf(false) }
    var showPlayPauseIndicator by remember { mutableStateOf(false) }

    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }

    // Direct MP4 URL if available (from native WordPress media or post-embedded MP4)
    val mp4Url = video.videoUrl?.takeIf { it.isNotBlank() }
        ?: video.youtubeUrl.takeIf { it.contains(".mp4", ignoreCase = true) }

    val isNativeMp4 = mp4Url != null

    // ExoPlayer for native MP4 playback: Full video without short loops (REPEAT_MODE_OFF)
    val exoPlayer = remember(mp4Url) {
        if (isNativeMp4 && mp4Url != null) {
            ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_OFF
                val mediaItem = MediaItem.fromUri(mp4Url)
                setMediaItem(mediaItem)
                prepare()
            }
        } else {
            null
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = (state == Player.STATE_BUFFERING)
                isVideoEnded = (state == Player.STATE_ENDED)
                if (state == Player.STATE_READY) {
                    val d = exoPlayer?.duration ?: 0L
                    if (d > 0L) durationMs = d
                }
            }
        }
        exoPlayer?.addListener(listener)

        onDispose {
            exoPlayer?.removeListener(listener)
            exoPlayer?.release()
        }
    }

    // Sync play/pause with active page state
    LaunchedEffect(isActive, isPlaying, exoPlayer) {
        exoPlayer?.let { player ->
            if (isActive && isPlaying && !isVideoEnded) {
                player.play()
            } else {
                player.pause()
            }
        }
    }

    // Sync volume with global mute state
    LaunchedEffect(isMuted, exoPlayer) {
        exoPlayer?.volume = if (isMuted) 0f else 1f
    }

    // Real-time playback progress tracker for displaying accurate full-video scrubber & duration
    LaunchedEffect(isActive, isPlaying, isVideoEnded, exoPlayer) {
        if (isActive && isPlaying && !isVideoEnded && exoPlayer != null) {
            while (true) {
                currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                val d = exoPlayer.duration
                if (d > 0L) durationMs = d
                delay(250)
            }
        }
    }

    // Auto-hide play/pause indicator after brief animation
    LaunchedEffect(showPlayPauseIndicator) {
        if (showPlayPauseIndicator) {
            delay(650)
            showPlayPauseIndicator = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptics.click()
                if (isVideoEnded) {
                    // Replay full video from start
                    exoPlayer?.seekTo(0)
                    exoPlayer?.play()
                    isVideoEnded = false
                    isPlaying = true
                } else {
                    isPlaying = !isPlaying
                    showPlayPauseIndicator = true
                }
            }
    ) {
        if (isNativeMp4 && exoPlayer != null) {
            // Native Full-Screen MP4 Video Player View
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        player = exoPlayer
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { view ->
                    view.player = exoPlayer
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // YouTube or Web fallback player
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ChaiVideoPlayer(
                    youtubeId = video.youtubeId,
                    videoUrl = video.videoUrl,
                    title = video.title,
                    thumbnailUrl = video.thumbnail,
                    autoPlay = isActive,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Buffering Indicator
        if (isBuffering && isPlaying) {
            CircularProgressIndicator(
                color = ChaiSaffron,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
            )
        }

        // Replay Button when Full Video Finishes Playing
        if (isVideoEnded) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.75f),
                border = BorderStroke(1.dp, ChaiSaffron),
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {
                        haptics.click()
                        exoPlayer?.seekTo(0)
                        exoPlayer?.play()
                        isVideoEnded = false
                        isPlaying = true
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Replay",
                        tint = ChaiSaffron,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "फिर से देखें",
                        color = Color.White,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Animated Play/Pause Center Indicator
        AnimatedVisibility(
            visible = showPlayPauseIndicator && !isVideoEnded,
            enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.6f),
            exit = fadeOut(tween(250)) + scaleOut(targetScale = 1.2f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier.size(68.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = if (isPlaying) "Play" else "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }

        // Bottom Gradient & Content (Headline, Category, Authentic Time, Progress Scrubber & Share)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.70f),
                            Color.Black.copy(alpha = 0.96f)
                        )
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Category pill and real authentic post time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ChaiCrimson.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = video.category ?: "वीडियो",
                            color = Color.White,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    val formattedTime = DateUtils.timeAgo(video.date)
                    if (formattedTime.isNotBlank()) {
                        Text(
                            text = formattedTime,
                            color = Color.White.copy(alpha = 0.85f),
                            fontFamily = InterFamily,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Video Title
                Text(
                    text = video.title,
                    color = Color.White,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Full Video Progress Scrubber Bar and Duration Display
                if (isNativeMp4 && durationMs > 0L) {
                    val progressFraction = (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            color = ChaiSaffron,
                            trackColor = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatDurationMs(currentPositionMs),
                                color = Color.White.copy(alpha = 0.7f),
                                fontFamily = InterFamily,
                                fontSize = 10.sp
                            )
                            Text(
                                text = formatDurationMs(durationMs),
                                color = Color.White.copy(alpha = 0.7f),
                                fontFamily = InterFamily,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Action Bar: "पूरी खबर पढ़ें" (if attached to post) + Clean Share Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (video.articleId > 0) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    haptics.medium()
                                    onNavigateToArticle(video.articleId)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "पूरी खबर पढ़ें",
                                    color = Color.White,
                                    fontFamily = InterFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = null,
                                    tint = ChaiSaffron,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Share Button (No copyright mention)
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier
                            .size(38.dp)
                            .clickable {
                                haptics.medium()
                                scope.launch {
                                    val shareTargetId = if (video.articleId > 0) video.articleId else video.id
                                    DeepLinkManager.shareArticle(
                                        context = context,
                                        postId = shareTargetId,
                                        cleanTitle = video.title,
                                        imageUrl = video.thumbnail,
                                        permalink = "https://chaipanchayat.com/?p=$shareTargetId"
                                    )
                                }
                            }
                            .testTag("btn_share_video")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "शेयर करें",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDurationMs(ms: Long): String {
    if (ms <= 0L) return "00:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
