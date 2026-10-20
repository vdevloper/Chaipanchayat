package com.chaipanchayat.app.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.OndemandVideo
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chaipanchayat.app.ui.theme.ChaiCrimson
import com.chaipanchayat.app.ui.theme.ChaiSaffron
import com.chaipanchayat.app.ui.theme.InterFamily
import com.chaipanchayat.app.utils.rememberChaiHaptics

/**
 * Modern, professional in-post video player.
 * - Uses Media3 ExoPlayer for native WordPress video uploads (.mp4, .webm) with hardware acceleration.
 * - Uses hardware-accelerated WebView with DOM storage for YouTube embeds with zero text distortion.
 * - Clean, sleek M3 editorial controls without outdated, cluttered button bars.
 */
@Composable
fun ChaiVideoPlayer(
    youtubeId: String?,
    videoUrl: String? = null,
    title: String? = null,
    thumbnailUrl: String? = null,
    autoPlay: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = rememberChaiHaptics()
    val configuration = LocalConfiguration.current

    var isPlaying by rememberSaveable { mutableStateOf(autoPlay) }
    var isFullscreen by rememberSaveable { mutableStateOf(false) }

    val cleanYtId = remember(youtubeId) {
        youtubeId?.trim()?.takeIf { it.isNotBlank() && !it.startsWith("mp4_") }
    }

    val displayThumb = remember(cleanYtId, thumbnailUrl) {
        when {
            !thumbnailUrl.isNullOrBlank() -> thumbnailUrl
            !cleanYtId.isNullOrBlank() -> "https://img.youtube.com/vi/$cleanYtId/hqdefault.jpg"
            else -> null
        }
    }

    val activity = remember(context) {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return@remember ctx
            ctx = ctx.baseContext
        }
        null
    }

    fun toggleOrientation() {
        haptics.heavy()
        val act = activity ?: return
        val current = act.resources.configuration.orientation
        if (current == Configuration.ORIENTATION_LANDSCAPE) {
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    fun launchExternalVideo() {
        haptics.click()
        if (!cleanYtId.isNullOrBlank()) {
            try {
                val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$cleanYtId")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(appIntent)
            } catch (_: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$cleanYtId")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
            }
        } else if (!videoUrl.isNullOrBlank()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            } catch (_: Exception) { }
        }
    }

    // Fullscreen Dialog View
    if (isFullscreen) {
        Dialog(
            onDismissRequest = {
                isFullscreen = false
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            BackHandler {
                isFullscreen = false
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Video Surface in Fullscreen
                VideoPlayerSurface(
                    isPlaying = true,
                    cleanYtId = cleanYtId,
                    videoUrl = videoUrl,
                    displayThumb = displayThumb,
                    title = title,
                    onPlayClick = { },
                    modifier = Modifier.fillMaxSize()
                )

                // Top Controls Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                isFullscreen = false
                                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title ?: "चाय पंचायत वीडियो",
                            color = Color.White,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { toggleOrientation() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ScreenRotation,
                                contentDescription = "Rotate",
                                tint = ChaiSaffron
                            )
                        }
                        IconButton(
                            onClick = { launchExternalVideo() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                contentDescription = "Open in app",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                isFullscreen = false
                                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FullscreenExit,
                                contentDescription = "Exit Fullscreen",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Standard In-Post Video Box
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF101010),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF282828), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                VideoPlayerSurface(
                    isPlaying = isPlaying,
                    cleanYtId = cleanYtId,
                    videoUrl = videoUrl,
                    displayThumb = displayThumb,
                    title = title,
                    onPlayClick = {
                        haptics.click()
                        isPlaying = true
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Top-right Action Buttons Overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!cleanYtId.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable { launchExternalVideo() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "YouTube",
                                    color = Color.White,
                                    fontFamily = InterFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = "Open YouTube",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    // Fullscreen Button
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.65f),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .size(32.dp)
                            .clickable {
                                haptics.click()
                                isPlaying = true
                                isFullscreen = true
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen",
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

@OptIn(UnstableApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun VideoPlayerSurface(
    isPlaying: Boolean,
    cleanYtId: String?,
    videoUrl: String?,
    displayThumb: String?,
    title: String?,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(modifier = modifier) {
        if (isPlaying) {
            if (!videoUrl.isNullOrBlank()) {
                // High-performance Native Media3 ExoPlayer for WordPress videos
                val exoPlayer = remember(context, videoUrl) {
                    ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
                        repeatMode = Player.REPEAT_MODE_OFF
                        prepare()
                        playWhenReady = true
                    }
                }

                DisposableEffect(exoPlayer) {
                    onDispose {
                        exoPlayer.release()
                    }
                }

                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                            setShowNextButton(false)
                            setShowPreviousButton(false)
                            setShowFastForwardButton(true)
                            setShowRewindButton(true)
                            setBackgroundColor(android.graphics.Color.BLACK)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (!cleanYtId.isNullOrBlank()) {
                // Hardware-accelerated WebView with DOM storage for YouTube
                var isWebLoading by remember { mutableStateOf(true) }

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            // Ensure hardware acceleration is enabled to prevent text distortion
                            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                cacheMode = WebSettings.LOAD_DEFAULT
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                allowFileAccess = false
                                useWideViewPort = true
                                loadWithOverviewMode = true
                            }

                            webChromeClient = object : WebChromeClient() {}
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isWebLoading = false
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                    if (request?.isForMainFrame == true) {
                                        isWebLoading = false
                                    }
                                }

                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    val url = request?.url?.toString().orEmpty()
                                    if (url.contains("youtube.com") || url.contains("youtube-nocookie.com") || url.contains("googlevideo.com")) {
                                        return false
                                    }
                                    return try {
                                        val intent = Intent(Intent.ACTION_VIEW, request?.url).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        ctx.startActivity(intent)
                                        true
                                    } catch (_: Exception) {
                                        false
                                    }
                                }
                            }

                            val embedHtml = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                <style>
                                    * { margin:0; padding:0; box-sizing:border-box; }
                                    html, body { background:#000; overflow:hidden; width:100%; height:100%; }
                                    iframe { width:100%; height:100%; border:0; display:block; }
                                </style>
                                </head>
                                <body>
                                <iframe 
                                    src="https://www.youtube-nocookie.com/embed/$cleanYtId?autoplay=1&playsinline=1&enablejsapi=1&rel=0&modestbranding=1&controls=1" 
                                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                                    allowfullscreen>
                                </iframe>
                                </body>
                                </html>
                            """.trimIndent()
                            loadDataWithBaseURL("https://www.youtube-nocookie.com", embedHtml, "text/html", "UTF-8", null)
                        }
                    }
                )

                if (isWebLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = ChaiSaffron,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                    }
                }
            }
        } else {
            // Modern Preview Surface with High-Resolution Thumbnail & Glass Play Button
            if (!displayThumb.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(displayThumb)
                        .crossfade(true)
                        .build(),
                    contentDescription = title ?: "Video thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Cinematic Dark Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.25f),
                                Color.Black.copy(alpha = 0.65f)
                            )
                        )
                    )
            )

            // Centered Glass Play Button
            Surface(
                shape = CircleShape,
                color = ChaiCrimson.copy(alpha = 0.92f),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.35f)),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(58.dp)
                    .align(Alignment.Center)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onPlayClick() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Video",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            // Bottom Info Pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.OndemandVideo,
                        contentDescription = null,
                        tint = ChaiSaffron,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "वीडियो देखें • Tap to Play",
                        color = Color.White,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
