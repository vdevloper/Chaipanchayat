package com.chaipanchayat.app.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chaipanchayat.app.data.model.VideoItem
import com.chaipanchayat.app.data.repository.NewsRepository
import com.chaipanchayat.app.ui.components.EmptyState
import com.chaipanchayat.app.ui.components.HeroSkeleton
import com.chaipanchayat.app.ui.theme.ChaiCrimson
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.ChaiTheme
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.ui.theme.NotoSerifFamily
import com.chaipanchayat.app.utils.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(
    onNavigateToArticle: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { NewsRepository.getInstance() }

    // Instant data from memory cache
    val initialCached = remember { repository.getCachedVideos().orEmpty() }
    var videos by remember { mutableStateOf(initialCached) }
    var isLoading by remember { mutableStateOf(initialCached.isEmpty()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isOffline by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    var activePlayingVideo by remember { mutableStateOf<VideoItem?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    suspend fun loadVideos(forceRefresh: Boolean = false) {
        if (videos.isEmpty()) isLoading = true
        try {
            val fetched = repository.getVideoPosts(forceRefresh = forceRefresh)
            videos = fetched
            isOffline = false
        } catch (e: Exception) {
            if (videos.isEmpty()) {
                isOffline = true
            }
        } finally {
            isLoading = false
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        loadVideos(forceRefresh = false)
    }

    val filterOptions = listOf("All", "Ground Reports", "Special Stories", "Police & Law")

    val filteredVideos = remember(videos, selectedFilter) {
        when (selectedFilter) {
            "Ground Reports" -> videos.filter {
                it.title.contains("ग्राउंड", ignoreCase = true) ||
                        it.title.contains("रिपोर्ट", ignoreCase = true) ||
                        it.category?.contains("Report", ignoreCase = true) == true
            }
            "Special Stories" -> videos.filter {
                it.title.contains("विशेष", ignoreCase = true) ||
                        it.title.contains("साक्षात्कार", ignoreCase = true) ||
                        it.title.contains("कहानी", ignoreCase = true)
            }
            "Police & Law" -> videos.filter {
                it.title.contains("पुलिस", ignoreCase = true) ||
                        it.title.contains("दरोगा", ignoreCase = true) ||
                        it.title.contains("कैंट", ignoreCase = true) ||
                        it.title.contains("एसएसपी", ignoreCase = true)
            }
            else -> videos
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.PlayCircle,
                            contentDescription = null,
                            tint = ChaiCrimson,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "वीडियो बुलेटिन",
                                fontFamily = NotoSerifFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ground Reports & Video Stories",
                                fontFamily = InterFamily,
                                fontSize = 11.sp,
                                color = ChaiTheme.extended.muted
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isRefreshing = true
                                loadVideos(forceRefresh = true)
                            }
                        },
                        modifier = Modifier.testTag("videos-refresh-button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh Videos",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = ChaiTheme.extended.border)

            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = when (filter) {
                                    "All" -> "सभी वीडियो (${videos.size})"
                                    "Ground Reports" -> "ग्राउंड रिपोर्ट"
                                    "Special Stories" -> "खास खबरें"
                                    "Police & Law" -> "कानून व पुलिस"
                                    else -> filter
                                },
                                fontFamily = InterFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChaiSaffron,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Main Content Area with PullToRefresh
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    coroutineScope.launch {
                        isRefreshing = true
                        loadVideos(forceRefresh = true)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    isLoading && videos.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            HeroSkeleton()
                            Spacer(modifier = Modifier.height(16.dp))
                            HeroSkeleton()
                        }
                    }

                    isOffline && videos.isEmpty() -> {
                        EmptyState(
                            title = "कोई वीडियो उपलब्ध नहीं है",
                            message = "इंटरनेट कनेक्शन की जांच करें और पुनः प्रयास करें।",
                            actionLabel = "पुनः प्रयास करें",
                            onAction = {
                                coroutineScope.launch { loadVideos(forceRefresh = true) }
                            }
                        )
                    }

                    filteredVideos.isEmpty() -> {
                        EmptyState(
                            title = "इस श्रेणी में कोई वीडियो नहीं मिला",
                            message = "कृपया 'सभी वीडियो' श्रेणी चुनें।",
                            actionLabel = "सभी वीडियो देखें",
                            onAction = { selectedFilter = "All" }
                        )
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            // Featured Top Video
                            item(key = "featured_video") {
                                val topVideo = filteredVideos.first()
                                FeaturedVideoCard(
                                    video = topVideo,
                                    onPlay = { activePlayingVideo = topVideo },
                                    onReadStory = { onNavigateToArticle(topVideo.articleId) },
                                    onShare = {
                                        shareVideo(context, topVideo)
                                    }
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "ताज़ा वीडियो खबरें",
                                    fontFamily = NotoSerifFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }

                            // Remaining Video Cards
                            items(
                                items = filteredVideos.drop(1),
                                key = { it.youtubeId + it.id }
                            ) { video ->
                                VideoFeedCard(
                                    video = video,
                                    onPlay = { activePlayingVideo = video },
                                    onReadStory = { onNavigateToArticle(video.articleId) },
                                    onShare = { shareVideo(context, video) }
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }
                    }
                }
            }
        }

        // In-App Video Player Sheet
        activePlayingVideo?.let { activeVideo ->
            VideoPlayerSheet(
                video = activeVideo,
                onDismiss = { activePlayingVideo = null },
                onReadStory = {
                    val id = activeVideo.articleId
                    activePlayingVideo = null
                    onNavigateToArticle(id)
                }
            )
        }
    }
}

@Composable
private fun FeaturedVideoCard(
    video: VideoItem,
    onPlay: () -> Unit,
    onReadStory: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("featured-video-card")
    ) {
        Column {
            // Thumbnail with Play Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clickable(onClick = onPlay)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(video.thumbnail)
                        .crossfade(true)
                        .build(),
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark Scrim Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.15f),
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                )

                // Prominent Play Button Badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .align(Alignment.Center)
                        .background(ChaiCrimson, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Video",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Category & Live Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = ChaiCrimson,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "विशेष रिपोर्ट",
                            color = Color.White,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = DateUtils.getRelativeTime(video.date),
                            color = Color.White,
                            fontFamily = InterFamily,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Details and Actions
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = video.title,
                    fontFamily = NotoSerifFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onPlay,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = ChaiCrimson,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "वीडियो देखें",
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    Row {
                        IconButton(onClick = onReadStory) {
                            Icon(
                                imageVector = Icons.Outlined.Article,
                                contentDescription = "Read full article",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onShare) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share video",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoFeedCard(
    video: VideoItem,
    onPlay: () -> Unit,
    onReadStory: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Video Thumbnail
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(video.thumbnail)
                        .crossfade(true)
                        .build(),
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                            )
                        )
                )

                // Mini Play badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center)
                        .background(ChaiCrimson, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(82.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = video.title,
                    fontFamily = NotoSerifFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateUtils.getRelativeTime(video.date),
                        fontFamily = InterFamily,
                        fontSize = 11.sp,
                        color = ChaiTheme.extended.muted
                    )

                    Row {
                        IconButton(
                            onClick = onReadStory,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Article,
                                contentDescription = "Read story",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoPlayerSheet(
    video: VideoItem,
    onDismiss: () -> Unit,
    onReadStory: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "चाय पंचायत वीडियो प्लेयर",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close player"
                    )
                }
            }

            // Embedded YouTube Web Player
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            webChromeClient = WebChromeClient()
                            webViewClient = WebViewClient()

                            val embedHtml = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                <style>
                                    body { margin: 0; padding: 0; background-color: #000; overflow: hidden; }
                                    iframe { width: 100vw; height: 56.25vw; max-height: 100vh; border: 0; }
                                </style>
                                </head>
                                <body>
                                <iframe 
                                    src="https://www.youtube.com/embed/${video.youtubeId}?autoplay=1&playsinline=1&rel=0" 
                                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                                    allowfullscreen>
                                </iframe>
                                </body>
                                </html>
                            """.trimIndent()

                            loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "utf-8", null)
                        }
                    }
                )
            }

            // Video Title & Meta
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = video.title,
                    fontFamily = NotoSerifFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "प्रकाशित: ${DateUtils.formatDateTime(video.date)}",
                    fontFamily = InterFamily,
                    fontSize = 12.sp,
                    color = ChaiTheme.extended.muted
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.youtubeUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("YouTube पर देखें", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = onReadStory,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Article,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("पूरी खबर पढ़ें", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

private fun shareVideo(context: android.content.Context, video: VideoItem) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            "${video.title}\n\nवीडियो देखें: ${video.youtubeUrl}\n\nचाय पंचायत - www.chaipanchayat.com"
        )
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "वीडियो शेयर करें"))
}
