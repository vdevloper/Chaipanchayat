package com.chaipanchayat.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Verified
import com.chaipanchayat.app.ui.components.ContactUsDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chaipanchayat.app.data.model.WPPost
import com.chaipanchayat.app.data.repository.BookmarkRepository
import com.chaipanchayat.app.data.repository.NewsRepository
import com.chaipanchayat.app.data.repository.SettingsRepository
import com.chaipanchayat.app.ui.components.ArticleHtmlView
import com.chaipanchayat.app.ui.components.ArticleSkeleton
import com.chaipanchayat.app.ui.components.ChaiVideoPlayer
import com.chaipanchayat.app.ui.components.EmptyState
import com.chaipanchayat.app.ui.components.TopBar
import com.chaipanchayat.app.ui.theme.ChaiBrandGradient
import com.chaipanchayat.app.ui.theme.ChaiCrimson
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import com.chaipanchayat.app.utils.ChaiAudioReader
import com.chaipanchayat.app.utils.ChaiHaptics
import com.chaipanchayat.app.utils.DateUtils
import com.chaipanchayat.app.utils.rememberChaiHaptics
import kotlinx.coroutines.launch

@Composable
fun ArticleScreen(
    postId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = rememberChaiHaptics()
    val bookmarkRepo = BookmarkRepository.getInstance(context)
    val settingsRepo = SettingsRepository.getInstance(context)

    val textSize by settingsRepo.textSize.collectAsState()
    val isBookmarked by bookmarkRepo.isBookmarked(postId).collectAsState(initial = false)

    var post by remember { mutableStateOf<WPPost?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    val audioReader = remember { ChaiAudioReader(context) }
    DisposableEffect(Unit) {
        onDispose {
            audioReader.release()
        }
    }
    val audioPlayState by audioReader.playState
    val audioProgress by audioReader.progress

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var showContactDialog by remember { mutableStateOf(false) }

    // Scroll progress calculation
    val scrollProgress by remember {
        derivedStateOf {
            if (scrollState.maxValue > 0) {
                (scrollState.value.toFloat() / scrollState.maxValue.toFloat()).coerceIn(0f, 1f)
            } else 0f
        }
    }
    val animatedProgress by animateFloatAsState(
        targetValue = scrollProgress,
        animationSpec = spring(stiffness = 500f),
        label = "reading_progress"
    )

    val showBackToTop by remember {
        derivedStateOf { scrollState.value > 650 }
    }

    LaunchedEffect(postId) {
        isLoading = true
        try {
            val fetched = NewsRepository.getInstance().getPost(postId)
            post = fetched
            isError = false
        } catch (e: Exception) {
            isError = true
        } finally {
            isLoading = false
        }
    }

    // Audio waveform animation
    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave")
    val waveHeight1 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w1"
    )
    val waveHeight2 by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w2"
    )
    val waveHeight3 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w3"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Top Bar
        TopBar(
            onBack = {
                haptics.click()
                onBack()
            },
            title = post?.primaryCategory,
            rightActions = {
                post?.let { p ->
                    IconButton(
                        onClick = {
                            haptics.click()
                            if (p.link.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(p.link))
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.testTag("article-open-web")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = "Open in browser",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        )

        // Reading Progress Indicator
        if (post != null) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp),
                color = ChaiSaffron,
                trackColor = Color.Transparent
            )
        }

        // Content Area
        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                ArticleSkeleton()
            } else if (isError || post == null) {
                EmptyState(
                    title = "Story unavailable",
                    message = "Could not load this story. Please check your network connection.",
                    actionLabel = "Retry",
                    onAction = {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                post = NewsRepository.getInstance().getPost(postId, forceRefresh = true)
                                isError = false
                            } catch (_: Exception) {
                                isError = true
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )
            } else {
                val currentPost = post!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Pill
                    if (!currentPost.primaryCategory.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(ChaiBrandGradient)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = currentPost.primaryCategory.uppercase(),
                                color = Color.White,
                                fontFamily = InterFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Bold Editorial Headline
                    Text(
                        text = currentPost.cleanTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = NotoSerifFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 25.sp,
                        lineHeight = 33.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date, Author & Read Time Byline
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ChaiTheme.extended.brandTertiary,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = (currentPost.authorName?.take(1) ?: "च").uppercase(),
                                    color = ChaiSaffron,
                                    fontFamily = InterFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Column {
                            Text(
                                text = if (!currentPost.authorName.isNullOrBlank()) "ब्यूरो / ${currentPost.authorName}" else "चाय पंचायत टीम",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = InterFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = DateUtils.formatDateTime(currentPost.date),
                                    color = ChaiTheme.extended.textSecondary,
                                    fontFamily = InterFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = " • 3 मिनट पठन",
                                    color = ChaiTheme.extended.brandText,
                                    fontFamily = InterFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Interactive Audio Listen Widget with real Text-to-Speech
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ChaiTheme.extended.surfaceSecondary,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ChaiTheme.extended.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable {
                                haptics.medium()
                                audioReader.togglePlayPause(currentPost.cleanTitle, currentPost.rawContent)
                            }
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = ChaiSaffron,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (audioPlayState == ChaiAudioReader.PlayState.PLAYING) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                contentDescription = "Play audio",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = when (audioPlayState) {
                                                ChaiAudioReader.PlayState.PLAYING -> "ऑडियो बज रहा है • Playing Audio"
                                                ChaiAudioReader.PlayState.PAUSED -> "ऑडियो रुका हुआ है • Tap to Resume"
                                                ChaiAudioReader.PlayState.PREPARING -> "ऑडियो तैयार हो रहा है..."
                                                ChaiAudioReader.PlayState.COMPLETED -> "ऑडियो पूरा हुआ • पुनः सुनें"
                                                ChaiAudioReader.PlayState.ERROR -> "ऑडियो सेवा तैयार हो रही है • टैप करें"
                                                else -> "ऑडियो सुनें • Listen in Hindi"
                                            },
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontFamily = InterFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = when (audioPlayState) {
                                                ChaiAudioReader.PlayState.PLAYING -> "हिंदी स्वरवाचन सक्रिय • Hindi Voice"
                                                ChaiAudioReader.PlayState.PAUSED -> "रोका गया • Tap to resume"
                                                else -> "चाय पंचायत ऑडियो वाचक • Hindi TTS"
                                            },
                                            color = ChaiTheme.extended.textSecondary,
                                            fontFamily = InterFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.5.sp
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (audioPlayState == ChaiAudioReader.PlayState.PLAYING) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Box(modifier = Modifier.width(3.dp).height(waveHeight1.dp).background(ChaiSaffron, CircleShape))
                                            Box(modifier = Modifier.width(3.dp).height(waveHeight2.dp).background(ChaiSaffron, CircleShape))
                                            Box(modifier = Modifier.width(3.dp).height(waveHeight3.dp).background(ChaiSaffron, CircleShape))
                                        }
                                    }

                                    if (audioPlayState == ChaiAudioReader.PlayState.PLAYING || audioPlayState == ChaiAudioReader.PlayState.PAUSED) {
                                        IconButton(
                                            onClick = {
                                                haptics.click()
                                                audioReader.stop()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Stop,
                                                contentDescription = "Stop audio",
                                                tint = ChaiTheme.extended.muted,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.Headphones,
                                            contentDescription = null,
                                            tint = ChaiSaffron,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            if (audioPlayState == ChaiAudioReader.PlayState.PLAYING || audioPlayState == ChaiAudioReader.PlayState.PAUSED) {
                                LinearProgressIndicator(
                                    progress = { audioProgress },
                                    modifier = Modifier.fillMaxWidth().height(2.dp),
                                    color = ChaiSaffron,
                                    trackColor = ChaiTheme.extended.border
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // In-Post Video Report Card (if video attached to the news post)
                    if (!currentPost.youtubeId.isNullOrBlank() || !currentPost.videoUrl.isNullOrBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ChaiCrimson)
                                )
                                Text(
                                    text = "वीडियो रिपोर्ट • VIDEO REPORT",
                                    fontFamily = InterFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ChaiCrimson,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            ChaiVideoPlayer(
                                youtubeId = currentPost.youtubeId,
                                videoUrl = currentPost.videoUrl,
                                title = currentPost.cleanTitle,
                                thumbnailUrl = currentPost.featuredImageUrl,
                                autoPlay = false
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Featured Image with curved modern borders
                    if (!currentPost.featuredImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(currentPost.featuredImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = currentPost.cleanTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .aspectRatio(16f / 9.5f)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, ChaiTheme.extended.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .background(ChaiTheme.extended.skeleton)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Native Parsed Article Body HTML
                    ArticleHtmlView(
                        html = currentPost.rawContent,
                        multiplier = textSize.multiplier
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Google Play News Policy Compliance: Source & Publisher Attribution Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ChaiTheme.extended.surfaceSecondary,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ChaiTheme.extended.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Verified,
                                    contentDescription = null,
                                    tint = ChaiSaffron,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "स्रोत एवं प्रकाशक विवरण • Source & Publisher",
                                    fontFamily = InterFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ChaiSaffron
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "प्रकाशन: चाय पंचायत (Chai Panchayat Digital Media)",
                                fontFamily = NotoSerifFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "ब्यूरो/लेखक: ${if (!currentPost.authorName.isNullOrBlank()) currentPost.authorName else "चाय पंचायत संपादकीय डेस्क"}\nआधिकारिक वेबसाइट: chaipanchayat.com\nसंपादकीय संपर्क: chaipanchayat@gmail.com",
                                fontFamily = InterFamily,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = ChaiTheme.extended.textSecondary
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = ChaiTheme.extended.border)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = ChaiSaffron.copy(alpha = 0.12f),
                                    modifier = Modifier
                                        .clickable {
                                            if (currentPost.link.isNotBlank()) {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentPost.link))
                                                context.startActivity(intent)
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "वेबसाइट पर देखें",
                                            fontFamily = InterFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.5.sp,
                                            color = ChaiSaffron
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                            contentDescription = null,
                                            tint = ChaiSaffron,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = ChaiTheme.extended.surfaceSecondary,
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, ChaiTheme.extended.border),
                                    modifier = Modifier
                                        .clickable { showContactDialog = true }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "संपर्क एवं प्रकाशक",
                                            fontFamily = InterFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))
                }
            }

            // Back to Top Floating Button
            androidx.compose.animation.AnimatedVisibility(
                visible = showBackToTop,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        haptics.click()
                        coroutineScope.launch {
                            scrollState.animateScrollTo(0)
                        }
                    },
                    containerColor = ChaiSaffron,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Back to top",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Sticky Bottom Action Bar with elevated styling
        post?.let { currentPost ->
            HorizontalDivider(thickness = 0.8.dp, color = ChaiTheme.extended.border.copy(alpha = 0.7f))

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bookmark Toggle with spring bounce and smooth color morphing
                    val bookmarkScale by animateFloatAsState(
                        targetValue = if (isBookmarked) 1.25f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.45f, stiffness = 500f),
                        label = "bm_scale"
                    )
                    val bookmarkBgColor by animateColorAsState(
                        targetValue = if (isBookmarked) ChaiTheme.extended.brandTertiary else Color.Transparent,
                        animationSpec = tween(220, easing = FastOutSlowInEasing),
                        label = "bm_bg_color"
                    )
                    val bookmarkTextColor by animateColorAsState(
                        targetValue = if (isBookmarked) ChaiSaffron else MaterialTheme.colorScheme.onSurface,
                        animationSpec = tween(220, easing = FastOutSlowInEasing),
                        label = "bm_text_color"
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = bookmarkBgColor,
                        modifier = Modifier
                            .clickable {
                                haptics.success()
                                coroutineScope.launch {
                                    bookmarkRepo.toggleBookmark(currentPost)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("article-bookmark-button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = if (isBookmarked) "Saved" else "Save",
                                tint = bookmarkTextColor,
                                modifier = Modifier
                                    .size(20.dp)
                                    .scale(bookmarkScale)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBookmarked) "सहेजा गया" else "सहेजें",
                                fontFamily = InterFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = bookmarkTextColor
                            )
                        }
                    }

                    // Text Size Cycle
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .clickable {
                                haptics.click()
                                settingsRepo.cycleTextSize()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("article-textsize-button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.FormatSize,
                                contentDescription = "Text Size",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = textSize.displayName,
                                fontFamily = InterFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Share Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .clickable {
                                haptics.click()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "${currentPost.cleanTitle}\n\nपूरी खबर: ${currentPost.link}\n\nचाय पंचायत")
                                    putExtra(Intent.EXTRA_SUBJECT, currentPost.cleanTitle)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "खबर शेयर करें")
                                context.startActivity(shareIntent)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("article-share-button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "शेयर",
                                fontFamily = InterFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    if (showContactDialog) {
        ContactUsDialog(
            onDismiss = { showContactDialog = false }
        )
    }
}
